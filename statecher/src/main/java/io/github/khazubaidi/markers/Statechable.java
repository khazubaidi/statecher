package io.github.khazubaidi.markers;

public interface Statechable<T> {

    void setState(T t);
    String getState();
}
