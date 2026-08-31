package io.github.khazubaidi.contracts;

import java.util.HashMap;
import java.util.Map;

public interface FormProcessor<T> {

    void process(T entity, Map<String, Object> data);
}
