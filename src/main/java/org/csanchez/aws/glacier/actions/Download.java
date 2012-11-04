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

public class Download implements Callable<File> {

    private static final Log LOG = LogFactory.getLog( Download.class );
    
    private final AmazonGlacierClient client;
    private final AWSCredentials credentials;
    private final String vault;
    private final String archiveId;

    public Download( AmazonGlacierClient client, AWSCredentials credentials, String vault, String archiveId ) {
        this.client = notNull( client );
        this.credentials = notNull( credentials );
        this.vault = notBlank( vault );
        this.archiveId = notBlank( archiveId );
    }

    public File call() {
        try {
            LOG.info( "Downloading archiveId \"" + archiveId + "\" from vault \"" + vault + "\"" );
            File temp = File.createTempFile( "glacier-" + vault + '-' + archiveId, null );
            ArchiveTransferManager atm = new ArchiveTransferManager( client, credentials );
            atm.download( vault, archiveId, temp );
            
            LOG.info( "Successfully downloaded archiveId \"" + archiveId + "\" from vault \"" + vault + "\"" );
            return temp;
        } catch ( Exception e ) {
            String errorMessage = "Failed to download archiveId \"" + archiveId + "\" from vault \"" + vault + "\"";
            LOG.error( errorMessage, e );
            throw new RuntimeException( errorMessage, e );
        }
    }

}
