package uk.co.cameronhunter.aws.glacier.cli;

import com.amazonaws.services.s3.model.Tier;
import uk.co.cameronhunter.aws.glacier.Glacier;

import java.io.File;
import java.util.List;

import static org.apache.commons.lang.Validate.isTrue;
import static uk.co.cameronhunter.aws.glacier.utils.Check.notBlank;

public class GlacierDownload extends AbstractGlacierCli {

    public GlacierDownload(String... parameters) {
        super(parameters);
    }

    public GlacierDownload(Glacier glacier, List<String> parameters) {
        super(glacier, parameters);
    }

    public static void main(String... parameters) throws Exception {
        new GlacierDownload(parameters).run();
    }

    @Override
    protected void validate(List<String> parameters) {
        isTrue(parameters.size() == 3);
        notBlank(parameters.get(0), "No vault parameter given");
        notBlank(parameters.get(1), "No archiveId parameter given");
        notBlank(parameters.get(2), "No target parameter given");
    }

    @Override
    protected void execute(Glacier glacier, List<String> parameters) throws Exception {
        String vault = parameters.get(0);
        String archiveId = parameters.get(1);
        String target = checkPath(parameters.get(2));

        File archive = glacier.download(vault, archiveId, Tier.Expedited).get();
        boolean success = archive.renameTo(new File(target));

        if (success) {
            LOG.info("Successfully saved archiveId \"" + archiveId + "\" as \"" + target + "\"");
        } else {
            throw new RuntimeException("Failed to rename downloaded archive \"" + archive.getAbsolutePath() + "\" to \"" + target + "\"");
        }
    }

    private static String checkPath(String path) {
        if (path.startsWith("~" + File.separator)) {
            return System.getProperty("user.home") + path.substring(1);
        } else {
            return path;
        }
    }

}
