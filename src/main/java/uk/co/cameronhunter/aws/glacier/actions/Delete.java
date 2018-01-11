package uk.co.cameronhunter.aws.glacier.actions;

import com.amazonaws.services.glacier.AmazonGlacier;
import com.amazonaws.services.glacier.model.DeleteArchiveRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.Callable;

import static uk.co.cameronhunter.aws.glacier.utils.Check.notBlank;
import static uk.co.cameronhunter.aws.glacier.utils.Check.notNull;

public class Delete implements Callable<Boolean> {

    private static final Log LOG = LogFactory.getLog(Delete.class);

    private final AmazonGlacier client;
    private final String vault;
    private final String archiveId;

    public Delete(AmazonGlacier client, String vault, String archiveId) {
        this.client = notNull(client);
        this.vault = notBlank(vault);
        this.archiveId = notBlank(archiveId);
    }

    @Override
    public Boolean call() {
        try {
            LOG.info("Deleting archiveId \"" + archiveId + "\" from vault \"" + vault + "\"");
            client.deleteArchive(new DeleteArchiveRequest(vault, archiveId));

            LOG.info("Successfully deleted archiveId \"" + archiveId + "\" from vault \"" + vault + "\"");
            return true;
        } catch (Exception e) {
            LOG.error("Failed to delete archiveId \"" + archiveId + "\" from vault \"" + vault + "\"", e);
            return false;
        }
    }

}
