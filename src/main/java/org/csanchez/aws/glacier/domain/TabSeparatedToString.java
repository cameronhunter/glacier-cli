package org.csanchez.aws.glacier.domain;

import com.google.common.base.Joiner;

abstract class TabSeparatedToString {

    @Override
    public final String toString() {
        return Joiner.on( '\t' ).join( getStringFields() );
    }

    abstract Object[] getStringFields();

}
