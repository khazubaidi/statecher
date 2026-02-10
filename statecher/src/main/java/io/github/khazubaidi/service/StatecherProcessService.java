package io.github.khazubaidi.service;

public interface StatecherProcessService<T> {

    void process(String token, String initiator, String transitionId);
}
