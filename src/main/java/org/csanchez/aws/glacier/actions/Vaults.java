package org.csanchez.aws.glacier.actions;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.union;
import static java.util.Collections.unmodifiableSet;
import static org.csanchez.aws.glacier.utils.Check.notNull;

import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.csanchez.aws.glacier.domain.Vault;
import org.csanchez.aws.glacier.utils.Check;

import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

public class Vaults implements Callable<Set<Vault>> {

    private static final Log LOG = LogFactory.getLog( Vaults.class );
    
    private final AmazonGlacierClient client;

    public Vaults( AmazonGlacierClient client ) {
        this.client = notNull( client );
    }

    public Set<Vault> call() {
        try {
            return listVaults( client, new ListVaultsRequest( "-" ) );
        } catch( Exception e ) {
            String errorMessage = "Couldn't retrieve vaults";
            LOG.error( errorMessage, e );
            throw new RuntimeException( errorMessage, e );
        }
    }

    private static Set<Vault> listVaults( AmazonGlacierClient client, ListVaultsRequest request ) {
        ListVaultsResult result = client.listVaults( request );

        Set<Vault> vaults = ImmutableSet.copyOf( transform( result.getVaultList(), TO_VAULT ) );

        if ( result.getMarker() != null ) {
            ListVaultsRequest nextRequest = new ListVaultsRequest( "-" ).withMarker( result.getMarker() );
            return union( vaults, listVaults( client, nextRequest ) );
        }

        return unmodifiableSet( vaults );
    }
    
    private static final Function<DescribeVaultOutput, Vault> TO_VAULT = new Function<DescribeVaultOutput, Vault>() {
        public Vault apply( DescribeVaultOutput response ) {
            Check.notNull( response );
            return new Vault( response.getVaultARN(), response.getVaultName(), response.getNumberOfArchives(), response.getSizeInBytes() );
        }
    };

}
