package com.fren_gor.ultimateAdvancementAPI.exceptions;

/**
 * This exception is thrown when an arbitrary progression update is made while it is not admitted.
 */
public class ArbitraryMultiTaskProgressionUpdateException extends RuntimeException {

    public ArbitraryMultiTaskProgressionUpdateException() {
        super();
    }

    public ArbitraryMultiTaskProgressionUpdateException(String message) {
        super(message);
    }

    public ArbitraryMultiTaskProgressionUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArbitraryMultiTaskProgressionUpdateException(Throwable cause) {
        super(cause);
    }

    protected ArbitraryMultiTaskProgressionUpdateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
