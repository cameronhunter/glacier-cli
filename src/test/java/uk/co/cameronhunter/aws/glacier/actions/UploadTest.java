package uk.co.cameronhunter.aws.glacier.actions;

import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;
import com.amazonaws.services.glacier.transfer.UploadResult;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import uk.co.cameronhunter.aws.glacier.domain.Archive;
import uk.co.cameronhunter.aws.glacier.test.utils.MockTestHelper;

import java.io.File;
import java.io.FileNotFoundException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class UploadTest extends MockTestHelper {

    private File archive;
    private ArchiveTransferManager transferManager;

    @Before
    public void setUp() {
        archive = mock(File.class);
        transferManager = mock(ArchiveTransferManager.class);

        expectThat(new Expectations() {
            {
                allowing(archive).length();
                allowing(archive).getAbsolutePath();
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void upload_requiresFileToExist() throws Exception {
        expectThat(archiveExists(false));

        new Upload(transferManager, "vault", archive);
    }

    @Test(expected = IllegalArgumentException.class)
    public void upload_requiresArchiveToBeFile() throws Exception {
        expectThat(archiveExists(true));
        expectThat(archiveIsFile(false));

        new Upload(transferManager, "vault", archive);
    }

    @Test
    public void upload() throws Exception {
        String vault = "vault";
        String archiveName = "archive.zip";

        expectThat(archiveExists(true));
        expectThat(archiveIsFile(true));
        expectThat(archiveIsCalled(archiveName));
        expectThat(archiveIsUploadedTo(vault));

        Archive uploadedArchive = new Upload(transferManager, vault, archive).call();

        assertThat(uploadedArchive.archiveId, is(notNullValue()));
        assertThat(uploadedArchive.name, is(equalTo(archiveName)));
    }

    private Expectations archiveExists(final boolean exists) {
        return new Expectations() {
            {
                one(archive).exists();
                will(returnValue(exists));
            }
        };
    }

    private Expectations archiveIsFile(final boolean isFile) {
        return new Expectations() {
            {
                one(archive).isFile();
                will(returnValue(isFile));
            }
        };
    }

    private Expectations archiveIsCalled(final String filename) {
        return new Expectations() {
            {
                allowing(archive).getName();
                will(returnValue(filename));
            }
        };
    }

    private Expectations archiveIsUploadedTo(final String vault) throws FileNotFoundException {
        return new Expectations() {
            {
                one(transferManager).upload(vault, archive.getName(), archive);
                will(returnValue(new UploadResult("archive-id")));
            }
        };
    }

}
