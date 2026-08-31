package io.github.khazubaidi.contracts;

import io.github.khazubaidi.markers.StatecherState;
import io.github.khazubaidi.models.State;

public interface StateExit<T> extends StatecherState<T> {

    void onExit(T entity, State state);
}
