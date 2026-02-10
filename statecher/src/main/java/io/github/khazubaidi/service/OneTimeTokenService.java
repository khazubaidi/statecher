package io.github.khazubaidi.service;

import io.github.khazubaidi.objects.OneTimeTokeMetadata;

import java.time.Duration;

public interface OneTimeTokenService {

    String create(String key, OneTimeTokeMetadata data, Duration ttl);
    OneTimeTokeMetadata consume(String key, String token);
}
