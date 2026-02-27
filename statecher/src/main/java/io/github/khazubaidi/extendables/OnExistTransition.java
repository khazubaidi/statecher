package io.github.khazubaidi.extendables;

import io.github.khazubaidi.markers.StatecherTransition;
import io.github.khazubaidi.models.State;

public interface OnExistTransition<T> extends StatecherTransition<T> {

    void onExist(T entity, State currentState, State nextState);
}
