package io.github.khazubaidi.service;

import java.time.Duration;

public interface OneTimeTokenService {

    <T> String create(String key, T data, Duration ttl);
    <T> T consume(String key, String token);
}
