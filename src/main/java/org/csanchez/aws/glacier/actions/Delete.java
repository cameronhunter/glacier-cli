package org.csanchez.aws.glacier.actions;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DeleteArchiveRequest;

public class Delete implements Callable<Boolean> {

    private static final Log LOG = LogFactory.getLog( Delete.class );
    
    private final AmazonGlacierClient client;
    private final String vault;
    private final String archiveId;

    public Delete( AmazonGlacierClient client, String vault, String archiveId ) {
        this.client = client;
        this.vault = vault;
        this.archiveId = archiveId;
    }

    public Boolean call() {
        try {
            LOG.info( "Deleting archiveId \"" + archiveId + "\" from vault \"" + vault + "\"" );
            client.deleteArchive( new DeleteArchiveRequest( vault, archiveId ) );
            
            LOG.info( "Successfully deleted archiveId \"" + archiveId + "\" from vault \"" + vault + "\"" );
            return true;
        } catch ( Exception e ) {
            LOG.info( "Failed to delete archiveId \"" + archiveId + "\" from vault \"" + vault + "\"" );
            return false;
        }
    }

}
