package com.fren_gor.ultimateAdvancementAPI.exceptions;

import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;

/**
 * This exception is thrown when a method is called on {@link DatabaseManager} while it's already closed.
 */
public class DatabaseManagerClosedException extends RuntimeException {

    public DatabaseManagerClosedException() {
        super();
    }

    public DatabaseManagerClosedException(String message) {
        super(message);
    }

    public DatabaseManagerClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseManagerClosedException(Throwable cause) {
        super(cause);
    }

    protected DatabaseManagerClosedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
