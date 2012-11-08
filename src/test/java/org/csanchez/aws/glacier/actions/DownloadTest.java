package org.csanchez.aws.glacier.actions;

import java.io.File;

import org.csanchez.aws.glacier.test.utils.MockTestHelper;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.glacier.transfer.ArchiveTransferManager;

public class DownloadTest extends MockTestHelper {

    private static final String VAULT = "vault";
    private static final String ARCHIVE_ID = "archive-id";

    private ArchiveTransferManager atm;
    private File file;

    @Before
    public void setUp() {
        atm = mock( ArchiveTransferManager.class );
        file = mock( File.class );
    }

    @Test( expected = RuntimeException.class )
    public void download_shouldCleanUpAfterAnException() throws Exception {
        expectThat( anExceptionIsThrown() );
        expectThat( theFileIsDeleted() );

        new Download( atm, VAULT, ARCHIVE_ID ).downloadTo( file );
    }

    @Test
    public void download_shouldNotDeleteTheFileIfSuccessful() throws Exception {
        expectThat( fileDownloadServiceCall() );
        expectThat( fileIsNotDeleted() );

        new Download( atm, VAULT, ARCHIVE_ID ).downloadTo( file );
    }

    private Expectations fileDownloadServiceCall() {
        return new Expectations() {
            {
                one( atm ).download( VAULT, ARCHIVE_ID, file );
            }
        };
    }

    private Expectations fileIsNotDeleted() {
        return new Expectations() {
            {
                never( file ).delete();
            }
        };
    }

    private Expectations anExceptionIsThrown() {
        return new Expectations() {
            {
                one( atm ).download( VAULT, ARCHIVE_ID, file );
                will( throwException( new AmazonServiceException( "Some AWS exception" ) ) );
            }
        };
    }

    private Expectations theFileIsDeleted() {
        return new Expectations() {
            {
                one( file ).delete();
            }
        };
    }

}
