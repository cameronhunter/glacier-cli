package org.csanchez.aws.glacier.domain;

import static org.csanchez.aws.glacier.utils.Check.notNull;

import java.util.concurrent.Callable;

public class After<T> implements Callable<T> {

    private final Callable<T> callable;
    private final Callback<T> callback;

    private After( Callable<T> callable, Callback<T> callback ) {
        this.callable = notNull( callable );
        this.callback = callback;
    }

    public static <T> After<T> create( Callable<T> callable, Callback<T> callback ) {
        return new After<T>( callable, callback );
    }

    @Override
    public T call() throws Exception {
        T value = callable.call();
        if ( callback != null ) callback.run( value );
        return value;
    }

}
