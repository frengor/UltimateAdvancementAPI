package com.fren_gor.ultimateAdvancementAPI.exceptions;

/**
 * This exception is thrown when an illegal operation occurs.
 */
public class IllegalOperationException extends UnsupportedOperationException {

    public IllegalOperationException() {
        super();
    }

    public IllegalOperationException(String message) {
        super(message);
    }

    public IllegalOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalOperationException(Throwable cause) {
        super(cause);
    }
}
