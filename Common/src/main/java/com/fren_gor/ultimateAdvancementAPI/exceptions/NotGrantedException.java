package com.fren_gor.ultimateAdvancementAPI.exceptions;

/**
 * This exception is thrown when an operation couldn't be done since an advancement is not granted for a team or player.
 */
public class NotGrantedException extends RuntimeException {

    public NotGrantedException() {
        super();
    }

    public NotGrantedException(String message) {
        super(message);
    }

    public NotGrantedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotGrantedException(Throwable cause) {
        super(cause);
    }

    protected NotGrantedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
