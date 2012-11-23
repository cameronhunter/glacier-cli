package uk.co.cameronhunter.aws.glacier.domain;

public interface Callback<T> {

    public void run( T item );
    
}
