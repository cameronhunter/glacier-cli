package uk.co.cameronhunter.aws.glacier.cli;

import uk.co.cameronhunter.aws.glacier.Glacier;

import java.util.List;

import static org.apache.commons.lang.Validate.isTrue;
import static uk.co.cameronhunter.aws.glacier.utils.Check.notBlank;

public class GlacierDelete extends AbstractGlacierCli {

    public GlacierDelete(String... parameters) {
        super(parameters);
    }

    public GlacierDelete(Glacier glacier, List<String> parameters) {
        super(glacier, parameters);
    }

    public static void main(String... parameters) throws Exception {
        new GlacierDelete(parameters).run();
    }

    @Override
    protected void validate(List<String> parameters) {
        isTrue(parameters.size() >= 2);
        notBlank(parameters.get(0), "No vault parameter given");
    }

    @Override
    protected void execute(Glacier glacier, List<String> parameters) {
        String vault = parameters.get(0);
        List<String> deletes = parameters.subList(1, parameters.size());

        LOG.info(deletes.size() + " archive(s) requested for deletion.");

        for (String archive : deletes) {
            glacier.delete(vault, archive);
        }
    }

}
