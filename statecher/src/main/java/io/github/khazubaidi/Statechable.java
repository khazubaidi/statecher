package io.github.khazubaidi;

public interface Statechable<T> {

    void setState(T t);
    String getState();
}
