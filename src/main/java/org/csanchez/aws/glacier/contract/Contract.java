package org.csanchez.aws.glacier.contract;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public final class Contract {

    public static final DateTimeFormatter GLACIER_DATETIME_FORMAT = ISODateTimeFormat.dateTimeNoMillis();
    
    private Contract() {}
}
