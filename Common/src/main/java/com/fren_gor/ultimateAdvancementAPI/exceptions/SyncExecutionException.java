package com.fren_gor.ultimateAdvancementAPI.exceptions;

/**
 * This exception is thrown when some code which should be executed async is executed on the main thread instead.
 *
 * @since 3.0.0
 */
public class SyncExecutionException extends RuntimeException {

    public SyncExecutionException() {
        super();
    }

    public SyncExecutionException(String message) {
        super(message);
    }

    public SyncExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public SyncExecutionException(Throwable cause) {
        super(cause);
    }

    protected SyncExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
