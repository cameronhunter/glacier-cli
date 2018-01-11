package uk.co.cameronhunter.aws.glacier.cli;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.co.cameronhunter.aws.glacier.Glacier;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static uk.co.cameronhunter.aws.glacier.utils.Check.notNull;

abstract class AbstractGlacierCli implements Runnable {

    protected static final Log LOG = LogFactory.getLog(AbstractGlacierCli.class);

    private final Glacier glacier;
    private final List<String> parameters;

    AbstractGlacierCli(String... parameters) {
        Glacier glacier = null;
        try {
            CommandLine cmd = new PosixParser().parse(commonOptions(), parameters);

            if (cmd.hasOption("help")) {
                printHelp(commonOptions());
                System.exit(0);
            }

            AWSCredentialsProvider credentialsProvider =
                    cmd.hasOption("accessKey") && cmd.hasOption("secretKey")
                            ? new AWSStaticCredentialsProvider(new BasicAWSCredentials(cmd.getOptionValue("accessKey"), cmd.getOptionValue("secretKey")))
                            : DefaultAWSCredentialsProviderChain.getInstance();

            Regions region =
                    cmd.hasOption("region")
                            ? Regions.fromName(cmd.getOptionValue("region"))
                            : Regions.DEFAULT_REGION;

            glacier = new Glacier(credentialsProvider, region);

            this.parameters = newArrayList(notNull(cmd.getArgs()));
            this.glacier = glacier;
        } catch (Exception e) {
            IOUtils.closeQuietly(glacier);
            throw new RuntimeException(e);
        }
    }

    AbstractGlacierCli(Glacier glacier, List<String> parameters) {
        this.glacier = notNull(glacier);
        this.parameters = notNull(parameters);
    }

    abstract protected void validate(List<String> parameters) throws IllegalArgumentException;

    abstract protected void execute(Glacier glacier, List<String> parameters) throws Exception;

    @Override
    public void run() {
        try {
            if (hasValidParameters(parameters)) {
                execute(glacier, parameters);
            } else {
                printHelp(commonOptions());
            }
        } catch (Throwable t) {
            LOG.error(t.getMessage(), t);
        } finally {
            IOUtils.closeQuietly(glacier);
        }
    }

    private boolean hasValidParameters(List<String> parameters) {
        try {
            validate(parameters);
            return true;
        } catch (IllegalArgumentException e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    private static void printHelp(Options options) {
        System.out.println("Usage: glacier [command]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println();
        System.out.println("  upload <vault> <file...>");
        System.out.println("  download <vault> <archive_id> <output_file>");
        System.out.println("  delete <vault> <archive_id>");
        System.out.println("  inventory <vault>");
        System.out.println("  vaults");
        System.out.println();
        System.out.println("Options:");
        System.out.println();
        System.out.println("  -h, --help\t\tShow this message");
        System.out.println("  -a, --accessKey <AWS_ACCESS_KEY>\tAWS Access Key ID");
        System.out.println("  -s, --secretKey <AWS_SECRET_ACCESS_KEY>\tAWS Secret Access Key ID");
        System.out.println("  -r, --region <AWS_REGION>\tAWS Region");
    }

    private static Options commonOptions() {
        Options options = new Options();
        options.addOption(new Option("h", "help", false, "Show help"));
        options.addOption(new Option("a", "accessKey", true, "AWS Access Key ID"));
        options.addOption(new Option("s", "secretKey", true, "AWS Secret Access Key ID"));
        options.addOption(new Option("r", "region", true, "AWS Region"));
        return options;
    }
}
