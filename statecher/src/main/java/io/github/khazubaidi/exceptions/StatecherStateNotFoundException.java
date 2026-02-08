package io.github.khazubaidi.exceptions;

public class StatecherStateNotFoundException extends RuntimeException {

    public StatecherStateNotFoundException(String state) {

        super("Statecher state not found: " + state);
    }
}
