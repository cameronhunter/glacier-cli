package org.csanchez.aws.glacier.actions;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.union;
import static java.util.Collections.unmodifiableSet;
import static org.csanchez.aws.glacier.utils.Check.notNull;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;

import org.csanchez.aws.glacier.domain.Vault;
import org.csanchez.aws.glacier.utils.Check;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.model.DescribeVaultOutput;
import com.amazonaws.services.glacier.model.ListVaultsRequest;
import com.amazonaws.services.glacier.model.ListVaultsResult;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

public class Vaults implements Callable<Collection<Vault>> {

    private final AmazonGlacierClient client;

    public Vaults( AmazonGlacierClient client ) {
        this.client = notNull( client );
    }

    @Override
    public Collection<Vault> call() {
        try {
            return listVaults( client, new ListVaultsRequest( "-" ) );
        } catch ( Exception e ) {
            throw new RuntimeException( "Couldn't retrieve vault list", e );
        }
    }

    private static Set<Vault> listVaults( AmazonGlacierClient client, ListVaultsRequest request ) {
        ListVaultsResult result = client.listVaults( request );

        Set<Vault> vaults = ImmutableSet.copyOf( transform( result.getVaultList(), TO_VAULT ) );

        String marker = result.getMarker();
        if ( marker != null ) {
            ListVaultsRequest nextRequest = new ListVaultsRequest( "-" ).withMarker( marker );
            return union( vaults, listVaults( client, nextRequest ) );
        }

        return unmodifiableSet( vaults );
    }

    private static final Function<DescribeVaultOutput, Vault> TO_VAULT = new Function<DescribeVaultOutput, Vault>() {
        @Override
        public Vault apply( DescribeVaultOutput response ) {
            Check.notNull( response );

            String arn = response.getVaultARN();
            String name = response.getVaultName();
            Long numberOfArchives = response.getNumberOfArchives();
            Long sizeInBytes = response.getSizeInBytes();
            DateTime creationDate = ISODateTimeFormat.dateTimeParser().parseDateTime( response.getCreationDate() );

            return new Vault( arn, name, numberOfArchives, sizeInBytes, creationDate );
        }
    };

}
