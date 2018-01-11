package uk.co.cameronhunter.aws.glacier.utils;

import org.junit.Test;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.cameronhunter.aws.glacier.utils.CharDelimitedString.tsv;


public class CharDelimitedStringTest {

    @Test
    public void tsv_empty() throws Exception {
        assertThat(tsv(), is(equalTo(EMPTY)));
    }

    @Test
    public void tsv_single() throws Exception {
        String identity = "foo";
        assertThat(tsv("foo"), is(equalTo(identity)));
    }

    @Test
    public void tsv_tabDelimited() throws Exception {
        assertThat(tsv("foo", "bar"), is(equalTo("foo\tbar")));
    }

    @Test
    public void tsv_withNull() throws Exception {
        assertThat(tsv("foo", null, "bar"), is(equalTo("foo\t\tbar")));
    }

}
