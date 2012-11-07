package org.csanchez.aws.glacier.cli;

import static org.apache.commons.lang.Validate.isTrue;

import java.util.List;

import org.csanchez.aws.glacier.Glacier;

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
        output( glacier.vaults(), "There are no vaults in \"" + glacier.region + "\" region" );
    }

}
