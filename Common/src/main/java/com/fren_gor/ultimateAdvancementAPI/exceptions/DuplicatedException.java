package com.fren_gor.ultimateAdvancementAPI.exceptions;

/**
 * This exception is thrown when some {@link Object} is registered twice but duplicates are not admitted.
 */
public class DuplicatedException extends RuntimeException {

    public DuplicatedException() {
        super();
    }

    public DuplicatedException(String message) {
        super(message);
    }

    public DuplicatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedException(Throwable cause) {
        super(cause);
    }

    protected DuplicatedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
