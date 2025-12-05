package com.fren_gor.ultimateAdvancementAPI.exceptions;

/**
 * This exception is thrown when the requested team is not present in the database.
 */
public class TeamNotRegisteredException extends NotRegisteredException {

    public TeamNotRegisteredException() {
        super();
    }

    public TeamNotRegisteredException(String message) {
        super(message);
    }

    public TeamNotRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public TeamNotRegisteredException(Throwable cause) {
        super(cause);
    }

    protected TeamNotRegisteredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
