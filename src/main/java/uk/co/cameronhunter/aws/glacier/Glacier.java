package uk.co.cameronhunter.aws.glacier;

import static com.google.common.collect.Iterables.transform;
import static uk.co.cameronhunter.aws.glacier.utils.Check.notBlank;
import static uk.co.cameronhunter.aws.glacier.utils.Check.notNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.amazonaws.regions.Region;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.cameronhunter.aws.glacier.actions.Delete;
import uk.co.cameronhunter.aws.glacier.actions.Download;
import uk.co.cameronhunter.aws.glacier.actions.Inventory;
import uk.co.cameronhunter.aws.glacier.actions.Upload;
import uk.co.cameronhunter.aws.glacier.actions.Vaults;
import uk.co.cameronhunter.aws.glacier.domain.After;
import uk.co.cameronhunter.aws.glacier.domain.Archive;
import uk.co.cameronhunter.aws.glacier.domain.Callback;
import uk.co.cameronhunter.aws.glacier.domain.Vault;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

/**
 * Uses Glacier high level API for uploading, downloading, deleting files, and
 * the low-level one for retrieving vault inventory.
 *
 * @see http://docs.amazonwebservices.com/amazonglacier/latest/dev/
 */
public class Glacier implements Closeable {

    private static final Log LOG = LogFactory.getLog(Glacier.class);

    public final String region;

    private final ExecutorService workers;
    private final AmazonGlacierClient glacier;
    private final ArchiveTransferManager transferManager;
    private final Set<String> vaults;
    private final AmazonSQSClient sqs;
    private final AmazonSNSClient sns;

    public Glacier(AWSCredentials credentials, String region) {
        this(Executors.newSingleThreadExecutor(), credentials, region);
    }

    public Glacier(ExecutorService workers, AWSCredentials credentials, String region) {
        notNull(credentials);

        this.workers = notNull(workers);
        this.region = notBlank(region);

        LOG.info("Using \"" + region + "\" region");

        this.glacier = new AmazonGlacierClient(credentials);
        this.glacier.setEndpoint("https://glacier." + region + ".amazonaws.com/");

        this.sqs = new AmazonSQSClient(credentials);
        this.sqs.setEndpoint("https://sqs." + region + ".amazonaws.com/");

        this.sns = new AmazonSNSClient(credentials);
        this.sns.setEndpoint("https://sns." + region + ".amazonaws.com/");

        this.transferManager = new ArchiveTransferManager(glacier, sqs, sns);

        LOG.info("Retrieving vault names in \"" + region + "\" region");

        this.vaults = ImmutableSet.copyOf(transform(new Vaults(glacier).call(), VAULT_NAME));
    }

    public Future<Collection<Vault>> vaults() {
        return workers.submit(new Vaults(glacier));
    }

    public Future<Collection<Archive>> inventory(String vault) {
        checkVaultExists(vault);
        return workers.submit(new Inventory(glacier, vault));
    }

    public Future<Archive> upload(String vault, String archiveName) {
        return upload(vault, archiveName, null);
    }

    public Future<Archive> upload(String vault, String archiveName, Callback<Archive> callback) {
        checkVaultExists(vault);
        File archive = new File(archiveName);
        return workers.submit(After.create(new Upload(transferManager, vault, archive), callback));
    }

    public Future<File> download(String vault, String archiveId) {
        checkVaultExists(vault);
        return workers.submit(new Download(transferManager, vault, archiveId));
    }

    public Future<Boolean> delete(String vault, String archiveId) {
        checkVaultExists(vault);
        return workers.submit(new Delete(glacier, vault, archiveId));
    }

    @Override
    public void close() throws IOException {
        workers.shutdown();
    }

    private void checkVaultExists(String vault) {
        Validate.isTrue(vaults.contains(vault), "Vault \"" + vault + "\" doesn't exist in \"" + region + "\" region. Available vaults: " + vaults);
    }

    private static final Function<Vault, String> VAULT_NAME = new Function<Vault, String>() {
        @Override
        public String apply(Vault vault) {
            return vault.name;
        }
    };

}
