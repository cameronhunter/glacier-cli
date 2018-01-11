package uk.co.cameronhunter.aws.glacier.actions;

import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import uk.co.cameronhunter.aws.glacier.domain.Archive;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;
import static uk.co.cameronhunter.aws.glacier.utils.Check.notBlank;
import static uk.co.cameronhunter.aws.glacier.utils.Check.notNull;

public class Upload implements Callable<Archive> {

    private static final Log LOG = LogFactory.getLog(Upload.class);

    private final ArchiveTransferManager transferManager;
    private final String vault;
    private final File archive;

    public Upload(ArchiveTransferManager transferManager, String vault, File archive) {
        this.transferManager = notNull(transferManager);
        this.vault = notBlank(vault);
        this.archive = validate(archive);
    }

    @Override
    public Archive call() {
        try {
            return upload(archive, vault, transferManager);
        } catch (Exception e) {
            LOG.error("Failed to upload archive \"" + archive + "\" to vault \"" + vault + "\"", e);
            throw new RuntimeException(e);
        }
    }

    private static File validate(File upload) {
        Validate.notNull(upload);
        Validate.isTrue(upload.exists(), "File \"" + upload.getAbsolutePath() + "\" doesn't exist");
        Validate.isTrue(upload.isFile(), "Cannot directly upload a directory to Glacier. Create an archive from it first.");

        return upload;
    }

    private static Archive upload(File upload, String vault, ArchiveTransferManager transferManager) throws IOException {
        String filename = upload.getName();

        LOG.info("Uploading \"" + filename + "\" (" + byteCountToDisplaySize(upload.length()) + ") to vault \"" + vault + "\"");

        UploadResult result = transferManager.upload(vault, filename, upload);

        LOG.info("Archive \"" + filename + "\" successfully uploaded to vault \"" + vault + "\"");

        return new Archive(result.getArchiveId(), filename, new DateTime(DateTimeZone.UTC), upload.length());
    }

}
