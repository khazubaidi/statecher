package io.github.khazubaidi.extendables;

import io.github.khazubaidi.markers.StatecherTransition;
import io.github.khazubaidi.models.State;

public interface StatecherAfterTransition<T> extends StatecherTransition<T> {

    void execute(T entity, State state, String username);
}
