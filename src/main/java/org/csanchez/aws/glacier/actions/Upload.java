package org.csanchez.aws.glacier.actions;

import static org.csanchez.aws.glacier.utils.Check.notBlank;
import static org.csanchez.aws.glacier.utils.Check.notNull;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;

public class Upload implements Callable<String> {

    private static final Log LOG = LogFactory.getLog( Upload.class );

    private final AmazonGlacierClient client;
    private final AWSCredentials credentials;
    private final String vault;
    private final String archive;

    public Upload( AmazonGlacierClient client, AWSCredentials credentials, String vault, String archive ) {
        this.client = notNull( client );
        this.credentials = notNull( credentials );
        this.vault = notBlank( vault );
        this.archive = notBlank( archive );
    }

    public String call() throws Exception {
        try {
            LOG.info( "Starting upload of archive \"" + archive + "\" to vault \"" + vault + "\"" );
            ArchiveTransferManager atm = new ArchiveTransferManager( client, credentials );
            UploadResult result = atm.upload( vault, archive, new File( archive ) );
            String archiveId = result.getArchiveId();

            LOG.info( "Archive \"" + archive + "\" (" + archiveId + ") successfully uploaded to vault \"" + vault + "\"" );
            return archiveId;
        } catch ( Exception e ) {
            throw new RuntimeException( "Failed to upload archive \"" + archive + "\" to vault \"" + vault + "\"", e );
        }
    }

}
