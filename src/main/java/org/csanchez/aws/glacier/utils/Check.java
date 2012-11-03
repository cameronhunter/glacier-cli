package org.csanchez.aws.glacier.utils;

import org.apache.commons.lang.StringUtils;

public final class Check {

    public static <T> T notNull( T input ) {
        if ( input == null ) throw new IllegalArgumentException( "Input is null" );
        return input;
    }

    public static String notBlank( String input ) {
        if ( StringUtils.isBlank( input ) ) throw new IllegalArgumentException( "Input is blank" );
        return input;
    }

    private Check() {}
}
