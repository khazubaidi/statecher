package io.github.khazubaidi.extendables;

import io.github.khazubaidi.markers.StatecherTransition;
import io.github.khazubaidi.models.State;

public interface StatecherBeforeTransition<T> extends StatecherTransition<T> {

    void execute(T entity, State state);
}
