package uk.co.cameronhunter.aws.glacier.domain;

import org.joda.time.DateTime;

import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;
import static org.joda.time.format.ISODateTimeFormat.dateTimeNoMillis;
import static uk.co.cameronhunter.aws.glacier.utils.CharDelimitedString.tsv;

public final class Archive {

    public final String archiveId;
    public final String name;
    public final DateTime creationDate;
    public final Long sizeInBytes;

    public Archive(String archiveId, String description, DateTime creationDate, Long sizeInBytes) {
        this.archiveId = archiveId;
        this.name = description;
        this.creationDate = creationDate;
        this.sizeInBytes = sizeInBytes;
    }

    @Override
    public String toString() {
        return tsv(name, dateTimeNoMillis().print(creationDate), byteCountToDisplaySize(sizeInBytes), sizeInBytes, archiveId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((archiveId == null) ? 0 : archiveId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Archive other = (Archive) obj;
        if (archiveId == null) {
            if (other.archiveId != null) return false;
        } else if (!archiveId.equals(other.archiveId)) return false;
        return true;
    }

}
