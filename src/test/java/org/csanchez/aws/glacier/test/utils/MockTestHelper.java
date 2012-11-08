package org.csanchez.aws.glacier.test.utils;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;

public class MockTestHelper {

    private final Mockery context = new Mockery() {
        {
            setImposteriser( ClassImposteriser.INSTANCE );
        }
    };

    public final <T> T mock( Class<T> clazz ) {
        return context.mock( clazz );
    }

    public final <T> T mock( Class<T> clazz, String name ) {
        return context.mock( clazz, name );
    }

    public final void expectThat( Expectations expectations ) {
        context.checking( expectations );
    }

    @After
    public void verify() {
        context.assertIsSatisfied();
    }

}
