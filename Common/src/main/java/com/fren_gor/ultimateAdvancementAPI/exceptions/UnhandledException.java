package com.fren_gor.ultimateAdvancementAPI.exceptions;

/**
 * An unchecked exception which wraps a checked exception.
 */
public class UnhandledException extends RuntimeException {

    public UnhandledException(Throwable cause) {
        super(cause);
    }

    public UnhandledException(String message, Throwable cause) {
        super(message, cause);
    }

    protected UnhandledException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
