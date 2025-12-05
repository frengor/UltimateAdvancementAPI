package com.fren_gor.ultimateAdvancementAPI.exceptions;

/**
 * This exception is thrown when the requested resource is not present in the database.
 */
public class NotRegisteredException extends RuntimeException {

    public NotRegisteredException() {
        super();
    }

    public NotRegisteredException(String message) {
        super(message);
    }

    public NotRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotRegisteredException(Throwable cause) {
        super(cause);
    }

    protected NotRegisteredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
