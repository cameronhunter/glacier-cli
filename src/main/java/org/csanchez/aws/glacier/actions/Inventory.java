package org.csanchez.aws.glacier.actions;

import static com.google.common.collect.Iterables.transform;
import static org.csanchez.aws.glacier.utils.Check.notBlank;
import static org.csanchez.aws.glacier.utils.Check.notNull;

import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.csanchez.aws.glacier.domain.Archive;

import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DescribeJobRequest;
import com.amazonaws.services.glacier.model.DescribeJobResult;
import com.amazonaws.services.glacier.model.GetJobOutputRequest;
import com.amazonaws.services.glacier.model.GetJobOutputResult;
import com.amazonaws.services.glacier.model.InitiateJobRequest;
import com.amazonaws.services.glacier.model.InitiateJobResult;
import com.amazonaws.services.glacier.model.JobParameters;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

public class Inventory implements Callable<Collection<Archive>> {

    private static final Log LOG = LogFactory.getLog( Inventory.class );
    private static final int INTERVAL = 30 * 60 * 1000;

    private final AmazonGlacierClient client;
    private final ObjectMapper mapper;
    private final String vault;

    public Inventory( AmazonGlacierClient client, String vault ) {
        this.client = notNull( client );
        this.vault = notBlank( vault );
        this.mapper = new ObjectMapper();
    }

    public Collection<Archive> call() {
        InputStream in = null;
        try {
            LOG.info( "Requesting inventory of vault \"" + vault + "\". This usually takes around 4 hours." );

            InitiateJobResult jobRequest = client.initiateJob( new InitiateJobRequest( vault, new JobParameters( "JSON", "inventory-retrieval", null, null ) ) );

            GetJobOutputResult result = pollJobForResult( jobRequest.getJobId() );

            LOG.info( "Successfully retrieved inventory of vault \"" + vault + "\"" );

            in = result.getBody();

            JsonFactory factory = mapper.getJsonFactory();
            JsonNode node = factory.createJsonParser( in ).readValueAsTree();

            Validate.isTrue( node.isObject() );

            return ImmutableSet.copyOf( transform( node.path( "ArchiveList" ), TO_ARCHIVE ) );
        } catch ( Exception e ) {
            String errorMessage = "Failed to retrieve inventory of vault \"" + vault + "\"";
            LOG.error( errorMessage, e );
            throw new RuntimeException( errorMessage, e );
        } finally {
            IOUtils.closeQuietly( in );
        }
    }

    private GetJobOutputResult pollJobForResult( String jobId ) throws InterruptedException {

        DescribeJobResult describeJob = client.describeJob( new DescribeJobRequest( vault, jobId ) );

        if ( describeJob.isCompleted() ) {
            return client.getJobOutput( new GetJobOutputRequest( vault, jobId, null ) );
        }

        Thread.sleep( INTERVAL );
        return pollJobForResult( jobId );
    }

    private static final Function<JsonNode, Archive> TO_ARCHIVE = new Function<JsonNode, Archive>() {
        public Archive apply( JsonNode node ) {
            String archiveId = node.path( "ArchiveId" ).getTextValue();
            String description = node.path( "ArchiveDescription" ).getTextValue();
            String creationDate = node.path( "CreationDate" ).getTextValue();
            long sizeInBytes = node.path( "Size" ).getLongValue();

            return new Archive( archiveId, description, creationDate, sizeInBytes );
        }
    };

}
