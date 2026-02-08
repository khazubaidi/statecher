package io.github.khazubaidi.service;

import io.github.khazubaidi.objects.StatecherObject;

public interface StatecherInitiateService<T> {

    <T> StatecherObject initiate(String name, T id, String initiator);
}
