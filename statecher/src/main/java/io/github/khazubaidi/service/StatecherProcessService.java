package io.github.khazubaidi.service;

public interface StatecherProcessService<T> {

    void process(String name, T id, String transitionId);
}
