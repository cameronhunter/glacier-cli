package org.csanchez.aws.glacier.actions;

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
        this.client = client;
        this.credentials = credentials;
        this.vault = vault;
        this.archiveId = archiveId;
    }

    public File call() {
        try {
            LOG.info( "Downloading archiveId \"" + archiveId + "\" from vault \"" + vault + "\"" );
            File temp = File.createTempFile( "glacier-" + vault + '-' + archiveId, null );
            ArchiveTransferManager atm = new ArchiveTransferManager( client, credentials );
            atm.download( vault, archiveId, temp );
            
            LOG.info( "Sucessfully downloaded archiveId \"" + archiveId + "\" from vault \"" + vault + "\"" );
            return temp;
        } catch ( Exception e ) {
            throw new RuntimeException( "Failed to download archiveId \"" + archiveId + "\" from vault \"" + vault + "\"", e );
        }
    }

}
