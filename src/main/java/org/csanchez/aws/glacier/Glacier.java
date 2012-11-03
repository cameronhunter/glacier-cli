package org.csanchez.aws.glacier;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.csanchez.aws.glacier.actions.Delete;
import org.csanchez.aws.glacier.actions.Download;
import org.csanchez.aws.glacier.actions.Inventory;
import org.csanchez.aws.glacier.actions.Upload;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;

public class Glacier {

    private final ExecutorService workers;
    private final AmazonGlacierClient client;
    private final AWSCredentials credentials;

    public Glacier( AWSCredentials credentials, String region ) {
        this( Executors.newSingleThreadExecutor(), credentials, region );
    }
    
    public Glacier( ExecutorService workers, AWSCredentials credentials, String region ) {
        this.workers = workers;
        this.credentials = credentials;
        this.client = new AmazonGlacierClient( credentials );
        this.client.setEndpoint( "https://glacier." + region + ".amazonaws.com/" );
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
}
