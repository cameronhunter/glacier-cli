package uk.co.cameronhunter.aws.glacier.domain;

import org.joda.time.DateTime;
import uk.co.cameronhunter.aws.glacier.utils.Check;

import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;
import static org.joda.time.format.ISODateTimeFormat.dateTimeNoMillis;
import static uk.co.cameronhunter.aws.glacier.utils.CharDelimitedString.tsv;

public final class Vault {

    public final String arn;
    public final String name;
    public final Long numberOfArchives;
    public final Long sizeInBytes;
    public final DateTime creationDate;

    public Vault(String arn, String name, Long numberOfArchives, Long sizeInBytes, DateTime creationDate) {
        this.arn = Check.notBlank(arn);
        this.name = Check.notBlank(name);
        this.numberOfArchives = numberOfArchives;
        this.sizeInBytes = sizeInBytes;
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return tsv(name, dateTimeNoMillis().print(creationDate), numberOfArchives, byteCountToDisplaySize(sizeInBytes), sizeInBytes, arn);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((arn == null) ? 0 : arn.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Vault other = (Vault) obj;
        if (arn == null) {
            if (other.arn != null) return false;
        } else if (!arn.equals(other.arn)) return false;
        return true;
    }

}
