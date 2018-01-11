package uk.co.cameronhunter.aws.glacier.domain;

@FunctionalInterface
public interface Callback<T> {
    void run(T item);
}
