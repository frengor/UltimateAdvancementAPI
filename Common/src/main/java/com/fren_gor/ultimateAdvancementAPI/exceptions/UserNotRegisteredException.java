package com.fren_gor.ultimateAdvancementAPI.exceptions;

/**
 * This exception is thrown when the requested user is not present on the database.
 */
public class UserNotRegisteredException extends RuntimeException {

    public UserNotRegisteredException() {
        super();
    }

    public UserNotRegisteredException(String message) {
        super(message);
    }

    public UserNotRegisteredException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotRegisteredException(Throwable cause) {
        super(cause);
    }

    protected UserNotRegisteredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
