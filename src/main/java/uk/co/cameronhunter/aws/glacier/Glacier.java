package uk.co.cameronhunter.aws.glacier;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.glacier.AmazonGlacier;
import com.amazonaws.services.glacier.AmazonGlacierClientBuilder;
import com.amazonaws.services.glacier.model.*;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManagerBuilder;
import com.amazonaws.services.s3.model.Tier;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.co.cameronhunter.aws.glacier.actions.*;
import uk.co.cameronhunter.aws.glacier.domain.After;
import uk.co.cameronhunter.aws.glacier.domain.Archive;
import uk.co.cameronhunter.aws.glacier.domain.Callback;
import uk.co.cameronhunter.aws.glacier.domain.Vault;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static uk.co.cameronhunter.aws.glacier.utils.Check.notNull;

/**
 * Uses Glacier high level API for uploading, downloading, deleting files, and
 * the low-level one for retrieving vault inventory.
 */
public class Glacier implements Closeable {

    private static final Log LOG = LogFactory.getLog(Glacier.class);
    private static final Function<Vault, String> VAULT_NAME = vault -> vault.name;

    private final ExecutorService workers;
    private final AmazonGlacier glacier;
    private final ArchiveTransferManager transferManager;
    private final AmazonSQS sqs;
    private final AmazonSNS sns;
    private final Set<String> vaults;

    public Glacier(AWSCredentialsProvider credentialsProvider, Regions region) {
        this(Executors.newSingleThreadExecutor(), credentialsProvider, region);
    }

    public Glacier(ExecutorService workers, AWSCredentialsProvider credentialsProvider, Regions region) {
        this.workers = notNull(workers);
        this.glacier = AmazonGlacierClientBuilder.standard().withCredentials(credentialsProvider).withRegion(region).build();
        this.sqs = AmazonSQSClientBuilder.standard().withCredentials(credentialsProvider).withRegion(region).build();
        this.sns = AmazonSNSClientBuilder.standard().withCredentials(credentialsProvider).withRegion(region).build();
        this.transferManager = new ArchiveTransferManagerBuilder().withGlacierClient(glacier).withSqsClient(sqs).withSnsClient(sns).build();
        this.vaults = ImmutableSet.copyOf(new Vaults(glacier).call().stream().map(VAULT_NAME).collect(toList()));
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
        return download(vault, archiveId, Tier.Standard);
    }

    public Future<File> download(String vault, String archiveId, Tier tier) {
        checkVaultExists(vault);
//        checkDataRetrievalPolicy();

        if (tier == null || tier == Tier.Standard) {
            return workers.submit(new Download(transferManager, vault, archiveId));
        } else {
            return workers.submit(new DownloadWithTier(transferManager, glacier, sqs, sns, vault, archiveId, tier));
        }
    }

    public Future<Boolean> delete(String vault, String archiveId) {
        checkVaultExists(vault);
        return workers.submit(new Delete(glacier, vault, archiveId));
    }

    @Override
    public void close() throws IOException {
        workers.shutdown();
        glacier.shutdown();
        sqs.shutdown();
        sns.shutdown();
    }

    private void checkVaultExists(String vault) {
        Validate.isTrue(vaults.contains(vault), "Vault \"" + vault + "\" doesn't exist. Available vaults: " + vaults);
    }

    private void checkDataRetrievalPolicy() {
        GetDataRetrievalPolicyResult result = glacier.getDataRetrievalPolicy(new GetDataRetrievalPolicyRequest().withAccountId("-"));

        DataRetrievalPolicy policy = result.getPolicy();

        LOG.info("Using data retrieval policy: " + policy);

        boolean noDataRetrievalPolicy = policy.getRules().stream().map(DataRetrievalRule::getStrategy).anyMatch("None"::equals);

        Validate.isTrue(!noDataRetrievalPolicy, "No data retrieval policy has been set. See: https://docs.aws.amazon.com/amazonglacier/latest/dev/data-retrieval-policy.html");
    }
}
