package org.csanchez.aws.glacier;

import static org.csanchez.aws.glacier.utils.Check.notBlank;
import static org.csanchez.aws.glacier.utils.Check.notNull;

import java.io.File;
import java.util.concurrent.Callable;

import org.csanchez.aws.glacier.actions.Delete;
import org.csanchez.aws.glacier.actions.Download;
import org.csanchez.aws.glacier.actions.Inventory;
import org.csanchez.aws.glacier.actions.Upload;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;

public class Glacier {

    private final AmazonGlacierClient client;
    private final AWSCredentials credentials;

    public Glacier( AWSCredentials credentials, String region ) {
        this.credentials = notNull( credentials );
        this.client = new AmazonGlacierClient( credentials );
        this.client.setEndpoint( "https://glacier." + notBlank( region ) + ".amazonaws.com/" );
    }

    public Callable<File> inventory( String vault ) {
        return new Inventory( client, vault );
    }

    public Callable<String> upload( String vault, String archiveName ) {
        return new Upload( client, credentials, vault, archiveName );
    }

    public Callable<File> download( String vault, String archiveId ) {
        return new Download( client, credentials, vault, archiveId );
    }

    public Callable<Boolean> delete( String vault, String archiveId ) {
        return new Delete( client, vault, archiveId );
    }
}
