package uk.co.cameronhunter.aws.glacier.actions;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressEventType;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.glacier.AmazonGlacier;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.JobParameters;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.JobStatusMonitor;
import com.amazonaws.services.s3.model.Tier;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sqs.AmazonSQS;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.amazonaws.event.SDKProgressPublisher.publishProgress;

public class DownloadWithTier implements Callable<File> {

    private static final Log LOG = LogFactory.getLog(DownloadWithTier.class);
    private static final ProgressListener DEFAULT_PROGRESS_LISTENER = new ProgressListener() {
        @Override
        public void progressChanged(ProgressEvent progressEvent) {
            LOG.debug("Progress: " + progressEvent);
        }
    };

    private final ArchiveTransferManager transferManager;
    private final AmazonGlacier glacier;
    private final AmazonSQS sqs;
    private final AmazonSNS sns;
    private final String vault;
    private final String archiveId;
    private final Tier tier;
    private final ProgressListener progressListener;

    public DownloadWithTier(ArchiveTransferManager transferManager, AmazonGlacier glacier, AmazonSQS sqs, AmazonSNS sns, String vault, String archiveId) {
        this(transferManager, glacier, sqs, sns, vault, archiveId, Tier.Standard);
    }

    public DownloadWithTier(ArchiveTransferManager transferManager, AmazonGlacier glacier, AmazonSQS sqs, AmazonSNS sns, String vault, String archiveId, Tier tier) {
        this(transferManager, glacier, sqs, sns, vault, archiveId, tier, DEFAULT_PROGRESS_LISTENER);
    }

    public DownloadWithTier(ArchiveTransferManager transferManager, AmazonGlacier glacier, AmazonSQS sqs, AmazonSNS sns, String vault, String archiveId, Tier tier, ProgressListener progressListener) {
        this.transferManager = transferManager;
        this.glacier = glacier;
        this.sqs = sqs;
        this.sns = sns;
        this.vault = vault;
        this.archiveId = archiveId;
        this.tier = tier;
        this.progressListener = progressListener;
    }

    @Override
    public File call() throws Exception {
        try {
            return downloadTo(File.createTempFile("glacier-" + vault + '-' + archiveId, null));
        } catch (IOException e) {
            LOG.error("Couldn't create temp file", e);
            throw new RuntimeException(e);
        }
    }

    private File downloadTo(File file) {
        try {
            LOG.info("Downloading archiveId \"" + archiveId + "\" from vault \"" + vault + "\" using the " + tier + " tier");

            transferManager.downloadJobOutput("-", vault, createJob(), file, progressListener);

            LOG.info("Successfully downloaded archiveId \"" + archiveId + "\" from vault \"" + vault + "\"");
            return file;
        } catch (Exception e) {
            if (file != null) file.delete();

            LOG.error("Failed to download archiveId \"" + archiveId + "\" from vault \"" + vault + "\"", e);
            throw new RuntimeException(e);
        }
    }

    private String createJob() {
        publishProgress(progressListener, ProgressEventType.TRANSFER_PREPARING_EVENT);

        JobStatusMonitor jobStatusMonitor = new JobStatusMonitor(sqs, sns);

        String jobId = null;
        try {
            JobParameters jobParameters = new JobParameters()
                    .withArchiveId(archiveId)
                    .withType("archive-retrieval")
                    .withTier(tier.toString())
                    .withSNSTopic(jobStatusMonitor.getTopicArn());

            InitiateJobRequest jobRequest = new InitiateJobRequest()
                    .withAccountId("-")
                    .withVaultName(vault)
                    .withJobParameters(jobParameters);

            jobId = glacier.initiateJob(jobRequest).getJobId();

            jobStatusMonitor.waitForJobToComplete(jobId);
        } catch (Throwable t) {
            publishProgress(progressListener, ProgressEventType.TRANSFER_FAILED_EVENT);
            throw t;
        } finally {
            jobStatusMonitor.shutdown();
        }

        return jobId;
    }

}
