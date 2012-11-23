package uk.co.cameronhunter.aws.glacier.cli;

import static org.apache.commons.lang.Validate.isTrue;
import static uk.co.cameronhunter.aws.glacier.utils.Check.notBlank;

import java.io.File;
import java.util.List;

import uk.co.cameronhunter.aws.glacier.Glacier;

public class GlacierDownload extends AbstractGlacierCli {

    public GlacierDownload( String... parameters ) {
        super( parameters );
    }

    public GlacierDownload( Glacier glacier, List<String> parameters ) {
        super( glacier, parameters );
    }

    public static void main( String... parameters ) throws Exception {
        new GlacierDownload( parameters ).run();
    }

    @Override
    protected void validate( List<String> parameters ) {
        isTrue( parameters.size() == 3 );
        notBlank( parameters.get( 0 ), "No vault parameter given" );
        notBlank( parameters.get( 1 ), "No archiveId parameter given" );
        notBlank( parameters.get( 2 ), "No target parameter given" );
    }

    @Override
    protected void execute( Glacier glacier, List<String> parameters ) throws Exception {
        String vault = parameters.get( 0 );
        String archiveId = parameters.get( 1 );
        String target = parameters.get( 2 );

        File archive = glacier.download( vault, archiveId ).get();
        archive.renameTo( new File( target ) );
    }

}
