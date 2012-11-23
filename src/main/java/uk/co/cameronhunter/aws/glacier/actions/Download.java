package uk.co.cameronhunter.aws.glacier.actions;

import static uk.co.cameronhunter.aws.glacier.utils.Check.notBlank;
import static uk.co.cameronhunter.aws.glacier.utils.Check.notNull;

import java.io.File;
import java.io.IOException;
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
        try {
            return downloadTo( File.createTempFile( "glacier-" + vault + '-' + archiveId, null ) );
        } catch ( IOException e ) {
            LOG.error( "Couldn't create temp file", e );
            throw new RuntimeException( e );
        }
    }

    File downloadTo( File file ) {
        try {
            LOG.info( "Downloading archiveId \"" + archiveId + "\" from vault \"" + vault + "\"" );

            transferManager.download( vault, archiveId, file );

            LOG.info( "Successfully downloaded archiveId \"" + archiveId + "\" from vault \"" + vault + "\"" );
            return file;
        } catch ( Exception e ) {
            if ( file != null ) file.delete();

            LOG.error( "Failed to download archiveId \"" + archiveId + "\" from vault \"" + vault + "\"", e );
            throw new RuntimeException( e );
        }
    }

}
