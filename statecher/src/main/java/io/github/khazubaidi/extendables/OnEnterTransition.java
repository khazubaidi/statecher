package io.github.khazubaidi.extendables;

import io.github.khazubaidi.markers.StatecherTransition;
import io.github.khazubaidi.models.State;

public interface OnEnterTransition<T> extends StatecherTransition<T> {

    void onEnter(T entity, State currentState, State previousState);
}
