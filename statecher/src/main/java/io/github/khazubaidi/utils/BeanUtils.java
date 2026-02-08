package io.github.khazubaidi.utils;

import java.util.List;

public interface BeanUtils {

    <T> T findByName(String name, Class<T> type);
    <T> List<T> findByName(List<String> name, Class<T> type);
}
