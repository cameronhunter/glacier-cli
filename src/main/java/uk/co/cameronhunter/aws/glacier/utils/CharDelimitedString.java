package uk.co.cameronhunter.aws.glacier.utils;

import com.google.common.base.Joiner;

import java.util.function.Function;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.EMPTY;

public final class CharDelimitedString {

    private static final Function<Object, Object> NULL_TO_EMPTY_STRING = input -> input == null ? EMPTY : input;

    public static String tsv(Object... fields) {
        return Joiner.on('\t').join(asList(fields).stream().map(NULL_TO_EMPTY_STRING).collect(toList()));
    }

    private CharDelimitedString() {}
}
