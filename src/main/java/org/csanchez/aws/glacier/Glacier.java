package org.csanchez.aws.glacier;

import static org.csanchez.aws.glacier.utils.Check.notBlank;
import static org.csanchez.aws.glacier.utils.Check.notNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.csanchez.aws.glacier.actions.Delete;
import org.csanchez.aws.glacier.actions.Download;
import org.csanchez.aws.glacier.actions.Inventory;
import org.csanchez.aws.glacier.actions.Upload;
import org.csanchez.aws.glacier.actions.Vaults;
import org.csanchez.aws.glacier.domain.Vault;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;

/**
 * Uses Glacier high level API for uploading, downloading, deleting files, and
 * the low-level one for retrieving vault inventory.
 * 
 * @see http://docs.amazonwebservices.com/amazonglacier/latest/dev/
 */
public class Glacier implements Closeable {

    private static final Log LOG = LogFactory.getLog( Glacier.class );
    
    private final ExecutorService workers;
    private final AmazonGlacierClient client;
    private final AWSCredentials credentials;

    public Glacier( AWSCredentials credentials, String region ) {
        this( Executors.newSingleThreadExecutor(), credentials, region );
    }

    public Glacier( ExecutorService workers, AWSCredentials credentials, String region ) {
        this.workers = notNull( workers );
        this.credentials = notNull( credentials );
        this.client = new AmazonGlacierClient( credentials );
        this.client.setEndpoint( "https://glacier." + notBlank( region ) + ".amazonaws.com/" );
        
        LOG.info( "Using \"" + region + "\" region" );
    }

    public Future<Set<Vault>> vaults() {
        return workers.submit( new Vaults( client ) );
    }

    public Future<File> inventory( String vault ) {
        return workers.submit( new Inventory( client, vault ) );
    }

    public Future<String> upload( String vault, String archiveName ) {
        return workers.submit( new Upload( client, credentials, vault, archiveName ) );
    }

    public Future<File> download( String vault, String archiveId ) {
        return workers.submit( new Download( client, credentials, vault, archiveId ) );
    }

    public Future<Boolean> delete( String vault, String archiveId ) {
        return workers.submit( new Delete( client, vault, archiveId ) );
    }

    public void close() throws IOException {
        workers.shutdown();
    }

}
