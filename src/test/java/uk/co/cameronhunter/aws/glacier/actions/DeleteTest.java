package uk.co.cameronhunter.aws.glacier.actions;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.glacier.AmazonGlacier;
import com.amazonaws.services.glacier.model.DeleteArchiveRequest;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import uk.co.cameronhunter.aws.glacier.test.utils.MockTestHelper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DeleteTest extends MockTestHelper {

    private static final String VAULT = "vault";
    private static final String ARCHIVE_ID = "archive-id";

    private AmazonGlacier client;

    @Before
    public void setUp() {
        client = mock(AmazonGlacier.class);
    }

    @Test
    public void delete_returnsTrueWhenSuccessful() {
        expectThat(deleteRequestIsSuccessful(true, VAULT, ARCHIVE_ID));
        assertThat(new Delete(client, "vault", "archive-id").call(), is(equalTo(true)));
    }

    @Test
    public void delete_returnsFalseWhenNotSuccessful() {
        expectThat(deleteRequestIsSuccessful(false, VAULT, ARCHIVE_ID));
        assertThat(new Delete(client, VAULT, ARCHIVE_ID).call(), is(equalTo(false)));
    }

    private Expectations deleteRequestIsSuccessful(final boolean success, final String vault, final String archiveId) {
        return new Expectations() {
            {
                one(client).deleteArchive(with(new DeleteArchiveRequest(vault, archiveId)));
                if (!success) {
                    will(throwException(new AmazonServiceException("Service exception")));
                }
            }
        };
    }

}
