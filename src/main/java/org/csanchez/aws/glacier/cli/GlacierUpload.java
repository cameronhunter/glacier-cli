package org.csanchez.aws.glacier.cli;

import static org.apache.commons.lang.Validate.isTrue;
import static org.csanchez.aws.glacier.utils.Check.notBlank;

import java.util.List;

import org.csanchez.aws.glacier.Glacier;
import org.csanchez.aws.glacier.domain.Archive;
import org.csanchez.aws.glacier.domain.Callback;

public class GlacierUpload extends AbstractGlacierCli {

    public GlacierUpload( String... parameters ) {
        super( parameters );
    }

    public GlacierUpload( Glacier glacier, List<String> parameters ) {
        super( glacier, parameters );
    }

    public static void main( String... parameters ) throws Exception {
        new GlacierUpload( parameters ).run();
    }

    @Override
    protected void validate( List<String> parameters ) {
        isTrue( parameters.size() >= 2 );
        notBlank( parameters.get( 0 ), "No vault parameter given" );
    }

    @Override
    protected void execute( Glacier glacier, List<String> parameters ) {
        String vault = parameters.get( 0 );
        List<String> uploads = parameters.subList( 1, parameters.size() );

        LOG.info( uploads.size() + " archive(s) requested for upload." );

        for ( String archive : uploads ) {
            glacier.upload( vault, archive, OUTPUT_ARCHIVE );
        }
    }

    private static final Callback<Archive> OUTPUT_ARCHIVE = new Callback<Archive>() {
        @Override
        public void run( Archive archive ) {
            System.out.println( archive );
        }
    };

}
