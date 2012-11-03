package org.csanchez.aws.glacier.actions;

import static org.csanchez.aws.glacier.utils.Check.notBlank;
import static org.csanchez.aws.glacier.utils.Check.notNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;

public class Inventory implements Callable<File> {

    private static final Log LOG = LogFactory.getLog( Inventory.class );
    
    private final AmazonGlacierClient client;
    private final String vault;

    public Inventory( AmazonGlacierClient client, String vault ) {
        this.client = notNull( client );
        this.vault = notBlank( vault );
    }
    
    public File call() {
        InputStream in = null;
        OutputStream out = null;
        try {
            LOG.info( "Requesting inventory of vault \"" + vault + "\"" );
            File temp = File.createTempFile( "glacier-" + vault + "-inventory", ".json" );
            
            InitiateJobResult jobRequest = client.initiateJob( new InitiateJobRequest( vault, new JobParameters( "JSON", "inventory-retrieval", null, null ) ) );
            GetJobOutputResult result = client.getJobOutput( new GetJobOutputRequest( vault, jobRequest.getJobId(), null ) );

            in = result.getBody();
            out = new FileOutputStream( temp );
            
            IOUtils.copy( in, out );
            
            out.flush();
            
            LOG.info( "Successfully retrieved inventory of vault \"" + vault + "\"" );
            return temp;
        } catch ( Exception e ) {
            throw new RuntimeException( "Failed to retrieve inventory of vault \"" + vault + "\"", e );
        } finally {
            IOUtils.closeQuietly( out );
            IOUtils.closeQuietly( in );
        }
    }

}
