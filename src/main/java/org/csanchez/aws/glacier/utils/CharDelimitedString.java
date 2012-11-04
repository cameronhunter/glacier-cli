package org.csanchez.aws.glacier.utils;

import com.google.common.base.Joiner;

public final class CharDelimitedString {

    public static String tsv( Object... fields ) {
        return Joiner.on( '\t' ).join( fields );
    }
    
    private CharDelimitedString() {}
}
