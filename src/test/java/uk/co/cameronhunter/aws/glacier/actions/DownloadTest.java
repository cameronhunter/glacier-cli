package uk.co.cameronhunter.aws.glacier.actions;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import uk.co.cameronhunter.aws.glacier.test.utils.MockTestHelper;

import java.io.File;

public class DownloadTest extends MockTestHelper {

    private static final String VAULT = "vault";
    private static final String ARCHIVE_ID = "archive-id";

    private ArchiveTransferManager atm;
    private File file;

    @Before
    public void setUp() {
        atm = mock(ArchiveTransferManager.class);
        file = mock(File.class);
    }

    @Test(expected = RuntimeException.class)
    public void download_shouldCleanUpAfterAnException() throws Exception {
        expectThat(anExceptionIsThrown());
        expectThat(theFileIsDeleted());

        new Download(atm, VAULT, ARCHIVE_ID, ProgressListener.NOOP).downloadTo(file);
    }

    @Test
    public void download_shouldNotDeleteTheFileIfSuccessful() throws Exception {
        expectThat(fileDownloadServiceCall());
        expectThat(fileIsNotDeleted());

        new Download(atm, VAULT, ARCHIVE_ID, ProgressListener.NOOP).downloadTo(file);
    }

    private Expectations fileDownloadServiceCall() {
        return new Expectations() {
            {
                one(atm).download(null, VAULT, ARCHIVE_ID, file, ProgressListener.NOOP);
            }
        };
    }

    private Expectations fileIsNotDeleted() {
        return new Expectations() {
            {
                never(file).delete();
            }
        };
    }

    private Expectations anExceptionIsThrown() {
        return new Expectations() {
            {
                one(atm).download(null, VAULT, ARCHIVE_ID, file, ProgressListener.NOOP);
                will(throwException(new AmazonServiceException("Some AWS exception")));
            }
        };
    }

    private Expectations theFileIsDeleted() {
        return new Expectations() {
            {
                one(file).delete();
            }
        };
    }

}
