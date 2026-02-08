package io.github.khazubaidi.utils;

public interface BeanUtils {

    <T> T findByName(String name, Class<T> type);
}
