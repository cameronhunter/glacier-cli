package uk.co.cameronhunter.aws.glacier.cli;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static uk.co.cameronhunter.aws.glacier.utils.Check.notNull;

import java.io.File;
import java.io.IOException;
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

import uk.co.cameronhunter.aws.glacier.Glacier;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;

public abstract class AbstractGlacierCli implements Runnable {

    private static final String DEFAULT_REGION = "us-east-1";
    private static final File DEFAULT_CREDENTIAL_FILE = new File( System.getProperty( "user.home" ), "AwsCredentials.properties" );

    protected static final Log LOG = LogFactory.getLog( AbstractGlacierCli.class );

    private final Glacier glacier;
    private final List<String> parameters;

    public AbstractGlacierCli( String... parameters ) {
        Glacier glacier = null;
        try {
            CommandLine cmd = new PosixParser().parse( commonOptions(), parameters );

            String region = cmd.getOptionValue( "region", DEFAULT_REGION );
            String credentials = cmd.getOptionValue( "credentials", DEFAULT_CREDENTIAL_FILE.getAbsolutePath() );

            glacier = new Glacier( getCredentials( credentials ), region );

            this.parameters = newArrayList( notNull( cmd.getArgs() ) );
            this.glacier = glacier;
        } catch ( Exception e ) {
            IOUtils.closeQuietly( glacier );
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
            if ( hasValidParameters( parameters ) ) {
                execute( glacier, parameters );
            } else {
                printHelp( commonOptions() );
            }
        } catch ( Throwable t ) {
            LOG.error( t.getMessage(), t );
        } finally {
            IOUtils.closeQuietly( glacier );
        }
    }

    private boolean hasValidParameters( List<String> parameters ) {
        try {
            validate( parameters );
            return true;
        } catch ( IllegalArgumentException e ) {
            LOG.error( e.getMessage(), e );
            return false;
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

        Option credentials = OptionBuilder.withArgName( "credentials" ).hasArg().withDescription( "Defaults to '$HOME/AwsCredentials.properties'" ).create( "credentials" );
        options.addOption( credentials );

        return options;
    }

    private static AWSCredentials getCredentials( String properties ) throws IOException {
        String accessKey = System.getProperty( "AWS_ACCESS_KEY_ID", null );
        String secretKey = System.getProperty( "AWS_SECRET_ACCESS_KEY", null );

        if ( isNotBlank( accessKey ) && isNotBlank( secretKey ) ) {
            LOG.info( "Using AWS credentials from environment variables" );
            return new BasicAWSCredentials( accessKey, secretKey );
        }

        File credentials = new File( properties );
        Validate.isTrue( credentials.exists(), "AWS credentials not found. See https://github.com/cameronhunter/glacier-cli#configuration for how to get setup." );

        LOG.info( "Using AWS credentials from \"" + credentials.getAbsolutePath() + "\"" );

        return new PropertiesCredentials( credentials );
    }
}
