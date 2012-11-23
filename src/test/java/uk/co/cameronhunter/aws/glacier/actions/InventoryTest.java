package uk.co.cameronhunter.aws.glacier.actions;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;

import uk.co.cameronhunter.aws.glacier.actions.Inventory;
import uk.co.cameronhunter.aws.glacier.domain.Archive;
import uk.co.cameronhunter.aws.glacier.test.utils.MockTestHelper;


public class InventoryTest extends MockTestHelper {

    private static final Archive ARCHIVE_ONE = new Archive( "DMTmICA2n5Tdqq5BV2z7og-A20xnpAPKt3UXwWxdWsn_D6auTUrW6kwy5Qyj9xd1MCE1mBYvMQ63LWaT8yTMzMaCxB_9VBWrW4Jw4zsvg5kehAPDVKcppUD1X7b24JukOr4mMAq-oA", "archive-one.zip", ISODateTimeFormat.dateTimeParser().parseDateTime( "2012-05-15T17:19:46.700Z" ), 2140123L );
    private static final Archive ARCHIVE_TWO = new Archive( "2lHzwhKhgF2JHyvCS-ZRuF08IQLuyB4265Hs3AXj9MoAIhz7tbXAvcFeHusgU_hViO1WeCBe0N5lsYYHRyZ7rrmRkNRuYrXUs_sjl2K8ume_7mKO_0i7C-uHE1oHqaW9d37pabXrSA", "archive-two.zip", ISODateTimeFormat.dateTimeParser().parseDateTime( "2012-05-15T17:21:39.339Z" ), 2140123L );
    private static final String PRECANNED_JSON = "{" + //
                                                 "  \"VaultARN\": \"arn:aws:glacier:us-east-1:012345678901:vaults/examplevault\"," + //
                                                 "  \"InventoryDate\": \"2011-12-12T14:19:01Z\"," + //
                                                 "  \"ArchiveList\": [" + //
                                                 "    {" + //
                                                 "      \"ArchiveId\": \"" + ARCHIVE_ONE.archiveId + "\"," + //
                                                 "      \"ArchiveDescription\": \"" + ARCHIVE_ONE.name + "\"," + //
                                                 "      \"CreationDate\": \"2012-05-15T17:19:46.700Z\"," + //
                                                 "      \"Size\": " + ARCHIVE_ONE.sizeInBytes + "," + //
                                                 "      \"SHA256TreeHash\": \"archive-one-hash\"" + //
                                                 "    }," + //
                                                 "    {" + //
                                                 "     \"ArchiveId\": \"" + ARCHIVE_TWO.archiveId + "\"," + //
                                                 "     \"ArchiveDescription\": \"" + ARCHIVE_TWO.name + "\"," + //
                                                 "     \"CreationDate\": \"2012-05-15T17:21:39.339Z\"," + //
                                                 "     \"Size\": " + ARCHIVE_TWO.sizeInBytes + "," + //
                                                 "     \"SHA256TreeHash\": \"archive-two-hash\"" + //
                                                 "    }" + //
                                                 "  ]" + //
                                                 "}";


    @Test
    public void parseJsonResult() throws Exception {
        InputStream input = IOUtils.toInputStream( PRECANNED_JSON );

        Set<Archive> archives = Inventory.parseJsonResponse( input );

        assertThat( archives.size(), is( equalTo( 2 ) ) );
        assertThat( archives.contains( ARCHIVE_ONE ), is( true ) );
        assertThat( archives.contains( ARCHIVE_TWO ), is( true ) );
    }
}
