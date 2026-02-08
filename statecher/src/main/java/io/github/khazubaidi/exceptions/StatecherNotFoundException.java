package io.github.khazubaidi.exceptions;

public class StatecherNotFoundException extends RuntimeException {

    private final String statecherName;

    public StatecherNotFoundException(String statecherName) {
        super("Statecher not found: " + statecherName);
        this.statecherName = statecherName;
    }

    public StatecherNotFoundException(String statecherName, Throwable cause) {
        super("Statecher not found: " + statecherName, cause);
        this.statecherName = statecherName;
    }

    public String getStatecherName() {
        return statecherName;
    }
}
