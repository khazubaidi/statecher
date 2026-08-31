package io.github.khazubaidi.contracts;

import io.github.khazubaidi.Statechable;
import io.github.khazubaidi.models.State;

public interface Rollbackable {

    void onRollback(Statechable entity, State currentState, State nextState);
}
