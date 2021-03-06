package uk.co.cameronhunter.aws.glacier.actions;

import com.amazonaws.services.glacier.AmazonGlacier;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import uk.co.cameronhunter.aws.glacier.test.utils.MockTestHelper;

public class VaultsTest extends MockTestHelper {

    private AmazonGlacier client;

    @Before
    public void setUp() {
        client = mock(AmazonGlacier.class);
    }

    @Test
    public void vaults_singleResultsList() {
        ListVaultsResult result = mock(ListVaultsResult.class);

        expectThat(vaultServiceCallWith(result));
        expectThat(hasMoreResults(result, false));

        new Vaults(client).call();
    }

    @Test
    public void vaults_multipleResultsList() {
        ListVaultsResult firstResult = mock(ListVaultsResult.class, "Result with marker");
        ListVaultsResult secondResult = mock(ListVaultsResult.class, "Result with no marker");

        expectThat(vaultServiceCallWith(firstResult));
        expectThat(hasMoreResults(firstResult, true));
        expectThat(vaultServiceCallWith(secondResult));
        expectThat(hasMoreResults(secondResult, false));

        new Vaults(client).call();
    }

    private Expectations vaultServiceCallWith(final ListVaultsResult result) {
        return new Expectations() {
            {
                one(client).listVaults(with(any(ListVaultsRequest.class)));
                will(returnValue(result));
            }
        };
    }

    private Expectations hasMoreResults(final ListVaultsResult result, final boolean hasMoreResults) {
        return new Expectations() {
            {
                allowing(result).getVaultList();

                one(result).getMarker();
                if (hasMoreResults) {
                    will(returnValue("marker"));
                } else {
                    will(returnValue(null));
                }
            }
        };
    }
}
