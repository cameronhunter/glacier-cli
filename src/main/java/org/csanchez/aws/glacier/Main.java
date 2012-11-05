package org.csanchez.aws.glacier;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.csanchez.aws.glacier.domain.Action;
import org.csanchez.aws.glacier.domain.Archive;
import org.csanchez.aws.glacier.domain.Vault;
import org.csanchez.aws.glacier.utils.Check;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;

public class Main {

    private static final Log LOG = LogFactory.getLog( Main.class );
    private static final Options COMMON_OPTIONS = commonOptions();

    public static void main( String[] args ) throws Exception {

        File props = new File( System.getProperty( "user.home" ) + "/AwsCredentials.properties" );
        if ( !props.exists() ) {
            LOG.error( "Missing " + props.getAbsolutePath() );
            return;
        }

        AWSCredentials credentials = new PropertiesCredentials( props );

        if ( args.length < 1 ) {
            printHelp( COMMON_OPTIONS );
            return;
        }

        CommandLine cmd = new PosixParser().parse( COMMON_OPTIONS, args );
        List<String> arguments = Arrays.asList( cmd.getArgs() );

        String region = cmd.getOptionValue( "region", "us-east-1" );
        Action action = Check.notNull( Action.fromName( arguments.get( 0 ) ), "No action provided" );

        Glacier glacier = new Glacier( credentials, region );

        try {
            switch ( action ) {
                case VAULTS:
                    Validate.isTrue( arguments.size() == 1 );

                    Collection<Vault> vaults = glacier.vaults().get();
                    for ( Vault vault : vaults ) {
                        System.out.println( vault );
                    }

                    if ( vaults.isEmpty() ) {
                        LOG.info( "There are no vaults in \"" + region + "\" region" );
                    }

                    return;

                case INVENTORY:
                    Validate.isTrue( arguments.size() == 2 );

                    Collection<Archive> inventory = glacier.inventory( arguments.get( 1 ) ).get();
                    for ( Archive archive : inventory ) {
                        System.out.println( archive );
                    }
                    
                    if ( inventory.isEmpty() ) {
                        LOG.info( "There are no archives in vault \"" + arguments.get( 1 ) + "\"" );
                    }
                    
                    return;

                case UPLOAD:
                    Validate.isTrue( arguments.size() >= 3 );

                    // TODO: output filename and archiveId to sysout
                    List<String> uploads = arguments.subList( 2, arguments.size() );

                    LOG.info( uploads.size() + " archive(s) requested for upload." );

                    for ( String archive : uploads ) {
                        glacier.upload( arguments.get( 1 ), archive );
                    }
                    return;

                case DELETE:
                    Validate.isTrue( arguments.size() >= 3 );

                    List<String> deletes = arguments.subList( 2, arguments.size() );

                    LOG.info( deletes.size() + " archive(s) requested for deletion." );

                    for ( String archive : deletes ) {
                        glacier.delete( arguments.get( 1 ), archive );
                    }
                    return;

                case DOWNLOAD:
                    Validate.isTrue( arguments.size() == 4 );

                    File archive = glacier.download( arguments.get( 1 ), arguments.get( 2 ) ).get();
                    archive.renameTo( new File( arguments.get( 3 ) ) );
                    return;
            }
        } catch ( IllegalArgumentException ignore ) {
            // Fall-through for validation
        } finally {
            IOUtils.closeQuietly( glacier );
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
