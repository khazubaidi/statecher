package io.github.khazubaidi.contracts;

import io.github.khazubaidi.models.State;

public interface StatecherValidator<T> {

    boolean isValid(T entity, State state);
}
