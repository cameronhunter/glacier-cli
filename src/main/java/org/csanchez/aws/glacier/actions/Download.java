package org.csanchez.aws.glacier.actions;

import static org.csanchez.aws.glacier.utils.Check.notBlank;
import static org.csanchez.aws.glacier.utils.Check.notNull;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;

public class Download implements Callable<File> {

    private static final Log LOG = LogFactory.getLog( Download.class );

    private final ArchiveTransferManager transferManager;
    private final String vault;
    private final String archiveId;

    public Download( ArchiveTransferManager transferManager, String vault, String archiveId ) {
        this.transferManager = notNull( transferManager );
        this.vault = notBlank( vault );
        this.archiveId = notBlank( archiveId );
    }

    @Override
    public File call() {
        File temp = null;
        try {
            LOG.info( "Downloading archiveId \"" + archiveId + "\" from vault \"" + vault + "\"" );

            temp = File.createTempFile( "glacier-" + vault + '-' + archiveId, null );

            transferManager.download( vault, archiveId, temp );

            LOG.info( "Successfully downloaded archiveId \"" + archiveId + "\" from vault \"" + vault + "\"" );
            return temp;
        } catch ( Exception e ) {
            if ( temp != null ) temp.delete();
            LOG.error( "Failed to download archiveId \"" + archiveId + "\" from vault \"" + vault + "\"", e );
            throw new RuntimeException( e );
        }
    }

}
