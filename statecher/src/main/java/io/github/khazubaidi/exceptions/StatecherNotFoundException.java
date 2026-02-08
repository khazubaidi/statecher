package io.github.khazubaidi.exceptions;

public class StatecherNotFoundException extends RuntimeException {

    public StatecherNotFoundException(String statecherName) {

        super("Statecher not found: " + statecherName);
    }
}
