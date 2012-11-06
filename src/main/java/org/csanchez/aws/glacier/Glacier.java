package org.csanchez.aws.glacier;

import static com.google.common.collect.Iterables.transform;
import static org.csanchez.aws.glacier.utils.Check.notBlank;
import static org.csanchez.aws.glacier.utils.Check.notNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.csanchez.aws.glacier.actions.Delete;
import org.csanchez.aws.glacier.actions.Download;
import org.csanchez.aws.glacier.actions.Inventory;
import org.csanchez.aws.glacier.actions.Upload;
import org.csanchez.aws.glacier.actions.Vaults;
import org.csanchez.aws.glacier.domain.After;
import org.csanchez.aws.glacier.domain.Archive;
import org.csanchez.aws.glacier.domain.Callback;
import org.csanchez.aws.glacier.domain.Vault;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

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
    private final String region;
    private final Set<String> vaults;

    public Glacier( AWSCredentials credentials, String region ) {
        this( Executors.newSingleThreadExecutor(), credentials, region );
    }

    public Glacier( ExecutorService workers, AWSCredentials credentials, String region ) {
        this.workers = notNull( workers );
        this.credentials = notNull( credentials );
        this.region = notBlank( region );

        this.client = new AmazonGlacierClient( credentials );
        this.client.setEndpoint( "https://glacier." + region + ".amazonaws.com/" );

        LOG.info( "Using \"" + region + "\" region" );

        this.vaults = ImmutableSet.copyOf( transform( new Vaults( client ).call(), VAULT_NAME ) );

        LOG.info( "Found " + vaults.size() + " vault(s) in \"" + region + "\" region" );
    }

    public Future<Collection<Vault>> vaults() {
        return workers.submit( new Vaults( client ) );
    }

    public Future<Collection<Archive>> inventory( String vault ) {
        checkVaultExists( vault );
        return workers.submit( new Inventory( client, vault ) );
    }

    public Future<Archive> upload( String vault, String archiveName ) {
        return upload( vault, archiveName, null );
    }

    public Future<Archive> upload( String vault, String archiveName, Callback<Archive> callback ) {
        checkVaultExists( vault );
        File archive = new File( archiveName );
        ArchiveTransferManager transferManager = new ArchiveTransferManager( client, credentials );
        return workers.submit( After.create( new Upload( transferManager, vault, archive ), callback ) );
    }

    public Future<File> download( String vault, String archiveId ) {
        checkVaultExists( vault );
        ArchiveTransferManager transferManager = new ArchiveTransferManager( client, credentials );
        return workers.submit( new Download( transferManager, vault, archiveId ) );
    }

    public Future<Boolean> delete( String vault, String archiveId ) {
        checkVaultExists( vault );
        return workers.submit( new Delete( client, vault, archiveId ) );
    }

    @Override
    public void close() throws IOException {
        workers.shutdown();
    }

    private void checkVaultExists( String vault ) {
        Validate.isTrue( vaults.contains( vault ), "Vault \"" + vault + "\" doesn't exist in \"" + region + "\" region. Available vaults: " + vaults );
    }

    private static final Function<Vault, String> VAULT_NAME = new Function<Vault, String>() {
        @Override
        public String apply( Vault vault ) {
            return vault.name;
        }
    };

}
