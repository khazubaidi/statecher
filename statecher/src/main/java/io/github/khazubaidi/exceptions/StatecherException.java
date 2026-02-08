package io.github.khazubaidi.exceptions;

public class StatecherException extends RuntimeException {

    public StatecherException(
            String message,
            Throwable cause) {

        super(message, cause);
    }
}
