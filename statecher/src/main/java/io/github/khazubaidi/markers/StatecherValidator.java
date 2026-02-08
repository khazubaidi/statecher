package io.github.khazubaidi.markers;

import io.github.khazubaidi.models.State;

public interface StatecherValidator<T> {

    boolean isValid(T entity, State state, String username);
}
