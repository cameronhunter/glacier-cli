package uk.co.cameronhunter.aws.glacier.utils;

import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.EMPTY;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

public final class CharDelimitedString {

    private static final Function<Object, Object> NULL_TO_EMPTY_STRING = new Function<Object, Object>() {
        @Override
        public Object apply( Object input ) {
            return input == null ? EMPTY : input;
        }
    };

    public static String tsv( Object... fields ) {
        return Joiner.on( '\t' ).join( transform( asList( fields ), NULL_TO_EMPTY_STRING ) );
    }

    private CharDelimitedString() {}
}
