package org.csanchez.aws.glacier.actions;

import static org.csanchez.aws.glacier.utils.Check.notNull;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.csanchez.aws.glacier.domain.Vault;

import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;

public class Vaults implements Callable<Set<Vault>> {

    private final AmazonGlacierClient client;

    public Vaults( AmazonGlacierClient client ) {
        this.client = notNull( client );
    }

    public Set<Vault> call() throws Exception {
        
        // FIXME: Only returns the first 1000 vaults
        ListVaultsResult result = client.listVaults( new ListVaultsRequest( "-" ) );
        
        Set<Vault> vaults = new HashSet<Vault>();
        for ( DescribeVaultOutput vault : result.getVaultList() ) {
            vaults.add( Vault.from( vault ) );
        }

        return vaults;
    }

}
