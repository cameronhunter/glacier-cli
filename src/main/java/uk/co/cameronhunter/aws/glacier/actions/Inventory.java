package uk.co.cameronhunter.aws.glacier.actions;

import static com.google.common.collect.Iterables.transform;
import static org.joda.time.Duration.standardMinutes;
import static uk.co.cameronhunter.aws.glacier.utils.Check.notBlank;
import static uk.co.cameronhunter.aws.glacier.utils.Check.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.format.ISODateTimeFormat;

import uk.co.cameronhunter.aws.glacier.domain.Archive;

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
    private static final Duration INTERVAL = standardMinutes( 30 );

    private final AmazonGlacierClient client;
    private final String vault;

    public Inventory( AmazonGlacierClient client, String vault ) {
        this.client = notNull( client );
        this.vault = notBlank( vault );
    }

    @Override
    public Collection<Archive> call() {
        InputStream response = null;
        try {
            LOG.info( "Requesting inventory of vault \"" + vault + "\". This usually takes around 4 hours." );

            InitiateJobResult jobRequest = client.initiateJob( new InitiateJobRequest( vault, new JobParameters( "JSON", "inventory-retrieval", null, null ) ) );

            response = pollForJobResponse( jobRequest.getJobId() ).getBody();

            LOG.info( "Successfully retrieved inventory of vault \"" + vault + "\"" );

            return parseJsonResponse( response );
        } catch ( Exception e ) {
            LOG.error( "Failed to retrieve inventory of vault \"" + vault + "\"", e );
            throw new RuntimeException( e );
        } finally {
            IOUtils.closeQuietly( response );
        }
    }

    static Set<Archive> parseJsonResponse( InputStream response ) throws IOException {
        JsonFactory factory = new ObjectMapper().getJsonFactory();
        JsonNode node = factory.createJsonParser( response ).readValueAsTree();

        Validate.isTrue( node.isObject() );
        JsonNode archiveList = node.get( "ArchiveList" );

        return ImmutableSet.copyOf( transform( archiveList, TO_ARCHIVE ) );
    }

    private GetJobOutputResult pollForJobResponse( String jobId ) throws InterruptedException {
        DescribeJobResult describeJob = client.describeJob( new DescribeJobRequest( vault, jobId ) );

        if ( describeJob.isCompleted() ) {
            return client.getJobOutput( new GetJobOutputRequest( vault, jobId, null ) );
        }

        Thread.sleep( INTERVAL.getMillis() );
        return pollForJobResponse( jobId );
    }

    private static final Function<JsonNode, Archive> TO_ARCHIVE = new Function<JsonNode, Archive>() {
        @Override
        public Archive apply( JsonNode node ) {
            String archiveId = node.path( "ArchiveId" ).getTextValue();
            String description = node.path( "ArchiveDescription" ).getTextValue();
            DateTime creationDate = ISODateTimeFormat.dateTimeParser().parseDateTime( node.path( "CreationDate" ).getTextValue() ).withZone( DateTimeZone.UTC );
            long sizeInBytes = node.path( "Size" ).getLongValue();

            return new Archive( archiveId, description, creationDate, sizeInBytes );
        }
    };

}
