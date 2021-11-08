package com.fren_gor.ultimateAdvancementAPI.exceptions;

import java.util.UUID;

/**
 * This exception is thrown when the requested user is not loaded into the caching system.
 */
public class UserNotLoadedException extends RuntimeException {

    public UserNotLoadedException() {
        super();
    }

    public UserNotLoadedException(UUID user) {
        super(user == null ? "User is not currently loaded. May it be offline?" : "User" + user + " is not currently loaded. May it be offline?");
    }

    public UserNotLoadedException(String message) {
        super(message);
    }

    public UserNotLoadedException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotLoadedException(Throwable cause) {
        super(cause);
    }

    protected UserNotLoadedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
