package io.github.khazubaidi.extendables;

import java.util.Map;

public interface FormProcessor<T> {

    void process(T entity, Map<String, Object> data);
}
