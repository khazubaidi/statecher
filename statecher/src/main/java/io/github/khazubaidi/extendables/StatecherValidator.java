package io.github.khazubaidi.extendables;

import io.github.khazubaidi.models.State;

public interface StatecherValidator<T> {

    boolean isValid(T entity, State state);
}
