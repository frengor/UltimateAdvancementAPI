package com.fren_gor.ultimateAdvancementAPI.exceptions;

import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;

/**
 * This exception thrown by {@link DatabaseManager}.
 */
public class DatabaseException extends RuntimeException {

    public DatabaseException() {
        super();
    }

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }

    protected DatabaseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
