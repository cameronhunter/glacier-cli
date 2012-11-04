package org.csanchez.aws.glacier.domain;

import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

public final class Archive extends TabSeparatedToString {

    public final String archiveId;
    public final String description;
    public final String creationDate;
    public final Long sizeInBytes;

    public Archive( String archiveId, String description, String creationDate, Long sizeInBytes ) {
        this.archiveId = archiveId;
        this.description = description;
        this.creationDate = creationDate;
        this.sizeInBytes = sizeInBytes;
    }

    @Override
    Object[] getStringFields() {
        return new Object[] { archiveId, description, creationDate, sizeInBytes, byteCountToDisplaySize( sizeInBytes ) };
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((archiveId == null) ? 0 : archiveId.hashCode());
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        Archive other = (Archive) obj;
        if ( archiveId == null ) {
            if ( other.archiveId != null ) return false;
        } else if ( !archiveId.equals( other.archiveId ) ) return false;
        return true;
    }

}
