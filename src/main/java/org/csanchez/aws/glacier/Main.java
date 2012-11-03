package org.csanchez.aws.glacier;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;

/**
 * Uses Glacier high level API for uploading, downloading, deleting files, and
 * the low-level one for retrieving vault inventory.
 * 
 * More info at http://docs.amazonwebservices.com/amazonglacier/latest/dev/
 */
public class Main {

    private static final Log LOG = LogFactory.getLog( Main.class );
    
    private static Options COMMON_OPTIONS = commonOptions();
    private static ExecutorService WORKERS = Executors.newSingleThreadExecutor();

    public static void main( String[] args ) throws Exception {

        File props = new File( System.getProperty( "user.home" ) + "/AwsCredentials.properties" );
        if ( !props.exists() ) {
            LOG.error( "Missing " + props.getAbsolutePath() );
            return;
        }

        AWSCredentials credentials = new PropertiesCredentials( props );

        CommandLine cmd = new PosixParser().parse( COMMON_OPTIONS, args );
        List<String> arguments = Arrays.asList( cmd.getArgs() );

        String region = cmd.getOptionValue( "region" );
        Action action = Action.fromName( arguments.get( 0 ) );
        String vault = arguments.get( 1 );

        try {
            Glacier glacier = new Glacier( WORKERS, credentials, region );

            switch ( action ) {
                case INVENTORY:
                    Validate.isTrue( arguments.size() == 2 );

                    File inventory = glacier.inventory( vault ).get();
                    inventory.renameTo( new File( cmd.getOptionValue( "file", "glacier-" + vault + "-inventory.json" ) ) );
                    return;

                case UPLOAD:
                    Validate.isTrue( arguments.size() >= 3 );

                    for ( String archive : arguments.subList( 2, arguments.size() ) ) {
                        glacier.upload( vault, archive );
                    }
                    return;

                case DELETE:
                    Validate.isTrue( arguments.size() >= 3 );

                    for ( String archive : arguments.subList( 2, arguments.size() ) ) {
                        glacier.delete( vault, archive );
                    }
                    return;

                case DOWNLOAD:
                    Validate.isTrue( arguments.size() == 4 );

                    File archive = glacier.download( vault, arguments.get( 2 ) ).get();
                    archive.renameTo( new File( arguments.get( 3 ) ) );
                    return;
            }
        } catch ( IllegalArgumentException ignore ) {
            // Fall-through for validation
        }

        printHelp( COMMON_OPTIONS );
    }

    private static void printHelp( Options options ) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "glacier " + "upload vault_name file1 file2 ... | " + "download vault_name archiveId output_file | " + "delete vault_name archiveId | " + "inventory vault_name | ", options );
    }

    @SuppressWarnings( "static-access" )
    private static Options commonOptions() {
        Options options = new Options();
        Option region = OptionBuilder.withArgName( "region" ).hasArg().withDescription( "Specify URL as the web service URL to use. Defaults to 'us-east-1'" ).create( "region" );
        options.addOption( region );

        Option topic = OptionBuilder.withArgName( "topic_name" ).hasArg().withDescription( "SNS topic to use for inventory retrieval. Defaults to 'glacier'" ).create( "topic" );
        options.addOption( topic );

        Option queue = OptionBuilder.withArgName( "queue_name" ).hasArg().withDescription( "SQS queue to use for inventory retrieval. Defaults to 'glacier'" ).create( "queue" );
        options.addOption( queue );

        Option output = OptionBuilder.withArgName( "file_name" ).hasArg().withDescription( "File to save the inventory to. Defaults to 'glacier.json'" ).create( "output" );
        options.addOption( output );

        return options;
    }
}
