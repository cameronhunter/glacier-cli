package uk.co.cameronhunter.aws.glacier.cli;

import uk.co.cameronhunter.aws.glacier.Glacier;
import uk.co.cameronhunter.aws.glacier.domain.Action;

import java.util.List;

import static com.google.common.collect.Iterables.skip;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.Validate.isTrue;

public class GlacierAll extends AbstractGlacierCli {

    public GlacierAll(String... parameters) throws Exception {
        super(parameters);
    }

    public static void main(String... parameters) throws Exception {
        new GlacierAll(parameters).run();
    }

    @Override
    protected void validate(List<String> parameters) throws IllegalArgumentException {
        isTrue(parameters.size() >= 1);
    }

    @Override
    protected void execute(Glacier glacier, List<String> parameters) throws Exception {
        Action action = Action.fromName(parameters.get(0));
        List<String> remainingParameters = newArrayList(skip(parameters, 1));

        getActionHandler(action, glacier, remainingParameters).run();
    }

    private static Runnable getActionHandler(Action action, Glacier glacier, List<String> parameters) {
        switch (action) {
            case VAULTS:
                return new GlacierVaults(glacier, parameters);

            case INVENTORY:
                return new GlacierInventory(glacier, parameters);

            case UPLOAD:
                return new GlacierUpload(glacier, parameters);

            case DELETE:
                return new GlacierDelete(glacier, parameters);

            case DOWNLOAD:
                return new GlacierDownload(glacier, parameters);

            default:
                throw new UnsupportedOperationException();
        }
    }

}
