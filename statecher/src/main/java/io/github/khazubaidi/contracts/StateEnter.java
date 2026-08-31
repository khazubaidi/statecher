package io.github.khazubaidi.contracts;

import io.github.khazubaidi.markers.StatecherState;
import io.github.khazubaidi.models.State;

public interface StateEnter<T> extends StatecherState<T> {

    void onEnter(T entity, State state);
}
