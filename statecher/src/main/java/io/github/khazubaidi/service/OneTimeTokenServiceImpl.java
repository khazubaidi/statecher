package io.github.khazubaidi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.khazubaidi.exceptions.StatecherException;
import io.github.khazubaidi.objects.OneTimeTokeMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class OneTimeTokenServiceImpl implements OneTimeTokenService {

    public static final String ONE_TIME_TOKEN_BUKET = "one-time-token";
    public static final String TOKEN_METADATA_BUCKET = "one-time-metadata";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public String create(String key, OneTimeTokeMetadata data, Duration ttl) {


        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                key(ONE_TIME_TOKEN_BUKET, key, token),
                "ONETIMETOKEN",
                ttl);

        keepMetadata(key, token, ttl.plusMinutes(5), data);
        return token;
    }

    @Override
    public OneTimeTokeMetadata consume(String key, String token) {

        Boolean existed = redisTemplate.delete(key(ONE_TIME_TOKEN_BUKET, key, token));

        if (!existed)
            throw new StatecherException("One time token expired/used.");

        return getAndClearMetadata(key, token);
    }

    public void keepMetadata(String key, String token, Duration ttl, Object data){

        try {

            String value = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(
                    key(TOKEN_METADATA_BUCKET, key, token),
                    value,
                    ttl.plusMinutes(5));
        } catch (JsonProcessingException e) {

            throw new RuntimeException("Couldn't serialize data object.");
        }
    }

    public OneTimeTokeMetadata getAndClearMetadata(String key, String token){

        try {

            String value = redisTemplate.opsForValue().get(key(TOKEN_METADATA_BUCKET, key, token));
            redisTemplate.delete(key(TOKEN_METADATA_BUCKET, key, token));
            return objectMapper.readValue(value, new TypeReference<OneTimeTokeMetadata>() {});
        } catch (JsonProcessingException e) {

            throw new RuntimeException(e);
        }
    }

    public String key(String bucket, String username, String token) {

        return bucket + ":" + username + ":" + token;
    }
}
