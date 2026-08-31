package io.github.khazubaidi.contracts;

import io.github.khazubaidi.markers.StatecherTransition;
import io.github.khazubaidi.models.State;

public interface TransitionEnd<T> extends StatecherTransition<T> {

    void onEnd(T entity, State currentState, State nextState);
}
