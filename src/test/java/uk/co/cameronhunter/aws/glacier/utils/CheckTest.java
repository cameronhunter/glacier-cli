package uk.co.cameronhunter.aws.glacier.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.cameronhunter.aws.glacier.utils.Check.notNull;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import uk.co.cameronhunter.aws.glacier.utils.Check;


public class CheckTest {

    @Test(expected= IllegalArgumentException.class)
    public void notNull_shouldThrowAnExceptionWhenInputIsNull() throws Exception {
        Check.notNull( null );
    }

    @Test
    public void notNull_shouldReturnTheObjectThatWasPassedIn() throws Exception {
        String input = "foo";
        String output = notNull( input );

        assertThat( output, is( equalTo( input ) ) );
    }

    @Test(expected=IllegalArgumentException.class)
    public void notBlank_nullInput() throws Exception {
        Check.notBlank( null );
    }

    @Test(expected=IllegalArgumentException.class)
    public void notBlank_emptyString() throws Exception {
        Check.notBlank( StringUtils.EMPTY );
    }

    @Test(expected=IllegalArgumentException.class)
    public void notBlank_inputIsOnlySpaces() throws Exception {
        Check.notBlank( "     " );
    }

    @Test
    public void notBlank_inputIsNotBlank() throws Exception {
        String input = "bob";
        String output = Check.notBlank( input );

        assertThat( output, is( equalTo( input ) ) );
    }

    @Test
    public void notBlank_inputIsNotBlank_containsSpaces() throws Exception {
        String input = "  bob  ";
        String output = Check.notBlank( input );

        assertThat( output, is( equalTo( input ) ) );
    }

}
