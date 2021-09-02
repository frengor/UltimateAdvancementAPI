package com.fren_gor.ultimateAdvancementAPI.exceptions;

/**
 * This exception is thrown when an illegal operation occurs.
 * <p>For example, calling the TeamProgression constructor from outside the com.fren_gor.ultimateAdvancementAPI.database
 * will throw an IllegalOperationException.
 */
public class IllegalOperationException extends RuntimeException {

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

    protected IllegalOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
