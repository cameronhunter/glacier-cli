package uk.co.cameronhunter.aws.glacier.cli;

import uk.co.cameronhunter.aws.glacier.Glacier;
import uk.co.cameronhunter.aws.glacier.domain.Vault;

import java.util.Collection;
import java.util.List;

import static org.apache.commons.lang.Validate.isTrue;

public class GlacierVaults extends AbstractGlacierCli {

    public GlacierVaults(String... parameters) {
        super(parameters);
    }

    public GlacierVaults(Glacier glacier, List<String> parameters) {
        super(glacier, parameters);
    }

    public static void main(String... parameters) throws Exception {
        new GlacierVaults(parameters).run();
    }

    @Override
    protected void validate(List<String> parameters) throws IllegalArgumentException {
        isTrue(parameters.isEmpty());
    }

    @Override
    protected void execute(Glacier glacier, List<String> parameters) throws Exception {
        Collection<Vault> vaults = glacier.vaults().get();
        for (Vault vault : vaults) {
            System.out.println(vault);
        }

        if (vaults.isEmpty()) {
            LOG.info("No vaults found");
        }
    }

}
