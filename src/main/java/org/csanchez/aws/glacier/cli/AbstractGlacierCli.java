package org.csanchez.aws.glacier.cli;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.csanchez.aws.glacier.utils.Check.notNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

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
import org.csanchez.aws.glacier.Glacier;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;

public abstract class AbstractGlacierCli implements Runnable {

    protected final Log log = LogFactory.getLog( getClass() );

    private final Glacier glacier;
    private final List<String> parameters;

    public AbstractGlacierCli( String... parameters ) {
        try {
            CommandLine cmd = new PosixParser().parse( commonOptions(), parameters );
            String region = cmd.getOptionValue( "region", "us-east-1" );

            this.parameters = newArrayList( notNull( cmd.getArgs() ) );
            this.glacier = new Glacier( getCredentials(), region );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    public AbstractGlacierCli( Glacier glacier, List<String> parameters ) {
        this.glacier = notNull( glacier );
        this.parameters = notNull( parameters );
    }

    abstract protected void validate( List<String> parameters ) throws IllegalArgumentException;

    abstract protected void execute( Glacier glacier, List<String> parameters ) throws Exception;

    @Override
    public void run() {
        try {
            validate( parameters );
        } catch ( IllegalArgumentException ignore ) {
            printHelp( commonOptions() );
            return;
        }

        try {
            execute( glacier, parameters );
        } catch ( Throwable t ) {
            log.error( t.getMessage(), t );
        } finally {
            IOUtils.closeQuietly( glacier );
        }
    }

    private static void printHelp( Options options ) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "glacier " + //
                             "upload vault_name file1 file2 ... | " + //
                             "download vault_name archiveId output_file | " + //
                             "delete vault_name archiveId | " + //
                             "inventory vault_name | " + //
                             "vaults | ", options );
    }

    @SuppressWarnings( "static-access" )
    private static Options commonOptions() {
        Options options = new Options();

        Option region = OptionBuilder.withArgName( "region" ).hasArg().withDescription( "Specify URL as the web service URL to use. Defaults to 'us-east-1'" ).create( "region" );
        options.addOption( region );

        return options;
    }

    private static AWSCredentials getCredentials() throws IOException {
        String accessKey = System.getProperty( "AWS_ACCESS_KEY_ID", null );
        String secretKey = System.getProperty( "AWS_SECRET_ACCESS_KEY", null );

        if ( isNotBlank( accessKey ) && isNotBlank( secretKey ) ) {
            return new BasicAWSCredentials( accessKey, secretKey );
        }

        File properties = new File( System.getProperty( "user.home" ), "AwsCredentials.properties" );
        Validate.isTrue( properties.exists(), "Missing " + properties.getAbsolutePath() );

        return new PropertiesCredentials( properties );
    }

    protected <T> void output( Future<Collection<T>> future, String empty ) throws Exception {
        Collection<T> collection = future.get();
        if ( collection == null || collection.isEmpty() ) {
            log.info( empty );
            return;
        }

        for ( T item : collection ) {
            System.out.println( item );
        }
    }

}
