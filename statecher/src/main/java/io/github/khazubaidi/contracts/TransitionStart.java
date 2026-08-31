package io.github.khazubaidi.contracts;

import io.github.khazubaidi.markers.StatecherTransition;
import io.github.khazubaidi.models.State;

public interface TransitionStart<T> extends StatecherTransition<T> {

    void onStart(T entity, State currentState, State previousState);
}
