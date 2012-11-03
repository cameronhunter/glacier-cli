package org.csanchez.aws.glacier.domain;

import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

import org.csanchez.aws.glacier.utils.Check;

import com.amazonaws.services.glacier.model.DescribeVaultOutput;

public final class Vault {

    public final String name;
    public final Long numberOfArchives;
    public final Long sizeInBytes;

    private final String arn;

    public Vault( String arn, String name, Long numberOfArchives, Long sizeInBytes ) {
        this.arn = Check.notBlank( arn );
        this.name = Check.notBlank( name );
        this.numberOfArchives = numberOfArchives;
        this.sizeInBytes = sizeInBytes;
    }

    public static Vault from( DescribeVaultOutput response ) {
        Check.notNull( response );
        return new Vault( response.getVaultARN(), response.getVaultName(), response.getNumberOfArchives(), response.getSizeInBytes() );
    }
    
    @Override
    public String toString() {
        return "Vault[name=" + name + ", numberOfArchives=" + numberOfArchives + ", size=" + byteCountToDisplaySize( sizeInBytes ) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((arn == null) ? 0 : arn.hashCode());
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        Vault other = (Vault) obj;
        if ( arn == null ) {
            if ( other.arn != null ) return false;
        } else if ( !arn.equals( other.arn ) ) return false;
        return true;
    }

}