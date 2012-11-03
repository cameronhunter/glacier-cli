package org.csanchez.aws.glacier.utils;

import org.apache.commons.lang.StringUtils;

public final class Check {

    public static <T> T notNull( T input ) {
        return notNull( input, "Input is null" );
    }

    public static <T> T notNull( T input, String message ) {
        if ( input == null ) throw new IllegalArgumentException( message );
        return input;
    }

    public static String notBlank( String input ) {
        return notBlank( input, "Input is blank" );
    }

    public static String notBlank( String input, String message ) {
        if ( StringUtils.isBlank( input ) ) throw new IllegalArgumentException( message );
        return input;
    }

    private Check() {}
}
