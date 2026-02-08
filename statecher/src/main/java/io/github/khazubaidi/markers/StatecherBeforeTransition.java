package io.github.khazubaidi.markers;

import io.github.khazubaidi.models.State;

public interface StatecherBeforeTransition<T> extends StatecherTransition<T> {

    void execute(T entity, State state, String username);
}
