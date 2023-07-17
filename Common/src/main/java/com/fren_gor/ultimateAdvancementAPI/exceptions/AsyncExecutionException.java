package com.fren_gor.ultimateAdvancementAPI.exceptions;

/**
 * This exception is thrown when some code which should be executed on the main thread is executed async instead.
 */
public class AsyncExecutionException extends RuntimeException {

    public AsyncExecutionException() {
        super();
    }

    public AsyncExecutionException(String message) {
        super(message);
    }

    public AsyncExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsyncExecutionException(Throwable cause) {
        super(cause);
    }

    protected AsyncExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
