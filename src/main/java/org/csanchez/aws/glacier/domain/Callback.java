package org.csanchez.aws.glacier.domain;

public interface Callback<T> {

    public void run( T item );
    
}
