package org.csanchez.aws.glacier.actions;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.csanchez.aws.glacier.utils.MockTestHelper;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DeleteArchiveRequest;

public class DeleteTest extends MockTestHelper {

    private AmazonGlacierClient client;

    @Before
    public void setUp() {
        client = mock( AmazonGlacierClient.class );
    }

    @Test
    public void delete_returnsTrueWhenSuccessful() {
        expectThat( deleteRequestIsSuccessful( true ) );
        assertThat( new Delete( client, "vault", "archive-id" ).call(), is( equalTo( true ) ) );
    }

    @Test
    public void delete_returnsFalseWhenNotSuccessful() {
        expectThat( deleteRequestIsSuccessful( false ) );
        assertThat( new Delete( client, "vault", "archive-id" ).call(), is( equalTo( false ) ) );
    }

    private Expectations deleteRequestIsSuccessful( final boolean success ) {
        return new Expectations() {
            {
                one( client ).deleteArchive( with( any( DeleteArchiveRequest.class ) ) );
                if ( !success ) {
                    will( throwException( new AmazonServiceException( "Service exception" ) ) );
                }
            }
        };
    }

}
