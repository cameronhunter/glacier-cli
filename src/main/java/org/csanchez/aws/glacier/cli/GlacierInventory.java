package org.csanchez.aws.glacier.cli;

import static org.apache.commons.lang.Validate.isTrue;
import static org.csanchez.aws.glacier.utils.Check.notBlank;

import java.util.Collection;
import java.util.List;

import org.csanchez.aws.glacier.Glacier;
import org.csanchez.aws.glacier.domain.Archive;

public class GlacierInventory extends AbstractGlacierCli {

    public GlacierInventory( String... parameters ) {
        super( parameters );
    }

    public GlacierInventory( Glacier glacier, List<String> parameters ) {
        super( glacier, parameters );
    }

    public static void main( String... parameters ) throws Exception {
        new GlacierInventory( parameters ).run();
    }

    @Override
    protected void validate( List<String> parameters ) throws IllegalArgumentException {
        isTrue( parameters.size() == 1 );
        notBlank( parameters.get( 0 ), "No vault parameter given" );
    }

    @Override
    protected void execute( Glacier glacier, List<String> parameters ) throws Exception {
        String vault = parameters.get( 0 );

        Collection<Archive> archives = glacier.inventory( vault ).get();
        for ( Archive archive : archives ) {
            System.out.println( archive );
        }

        if ( archives.isEmpty() ) {
            LOG.info( "There are no archives in vault \"" + vault + "\"" );
        }
    }

}
