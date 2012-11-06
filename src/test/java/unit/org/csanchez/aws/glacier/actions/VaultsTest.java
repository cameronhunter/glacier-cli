package org.csanchez.aws.glacier.actions;

import org.csanchez.aws.glacier.utils.MockTestHelper;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;

public class VaultsTest extends MockTestHelper {

    private AmazonGlacierClient client;

    @Before
    public void setUp() {
        client = mock( AmazonGlacierClient.class );
    }

    @Test
    public void vaults_singleResultsList() {
        ListVaultsResult result = mock( ListVaultsResult.class );

        expectThat( vaultServiceCallWith( result ) );
        expectThat( hasMoreResults( result, false ) );

        new Vaults( client ).call();
    }

    @Test
    public void vaults_multipleResultsList() {
        ListVaultsResult firstResult = mock( ListVaultsResult.class, "Result with marker" );
        ListVaultsResult secondResult = mock( ListVaultsResult.class, "Result with no marker" );

        expectThat( vaultServiceCallWith( firstResult ) );
        expectThat( hasMoreResults( firstResult, true ) );
        expectThat( vaultServiceCallWith( secondResult ) );
        expectThat( hasMoreResults( secondResult, false ) );

        new Vaults( client ).call();
    }

    private Expectations vaultServiceCallWith( final ListVaultsResult result ) {
        return new Expectations() {
            {
                one( client ).listVaults( with( any( ListVaultsRequest.class ) ) );
                will( returnValue( result ) );
            }
        };
    }

    private Expectations hasMoreResults( final ListVaultsResult result, final boolean hasMoreResults ) {
        return new Expectations() {
            {
                allowing( result ).getVaultList();

                one( result ).getMarker();
                if ( hasMoreResults ) {
                    will( returnValue( "marker" ) );
                } else {
                    will( returnValue( null ) );
                }
            }
        };
    }
}
