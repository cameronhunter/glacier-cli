package org.csanchez.aws.glacier.cli;

import static org.apache.commons.lang.Validate.isTrue;

import java.util.Collection;
import java.util.List;

import org.csanchez.aws.glacier.Glacier;
import org.csanchez.aws.glacier.domain.Vault;

public class GlacierVaults extends AbstractGlacierCli {

    public GlacierVaults( String... parameters ) {
        super( parameters );
    }

    public GlacierVaults( Glacier glacier, List<String> parameters ) {
        super( glacier, parameters );
    }

    public static void main( String... parameters ) throws Exception {
        new GlacierVaults( parameters ).run();
    }

    @Override
    protected void validate( List<String> parameters ) throws IllegalArgumentException {
        isTrue( parameters.isEmpty() );
    }

    @Override
    protected void execute( Glacier glacier, List<String> parameters ) throws Exception {
        Collection<Vault> vaults = glacier.vaults().get();
        for ( Vault vault : vaults ) {
            System.out.println( vault );
        }

        if ( vaults.isEmpty() ) {
            LOG.info( "There are no vaults in \"" + glacier.region + "\" region" );
        }
    }

}
