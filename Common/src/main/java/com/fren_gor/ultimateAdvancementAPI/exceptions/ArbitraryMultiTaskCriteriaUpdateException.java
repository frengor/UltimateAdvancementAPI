package com.fren_gor.ultimateAdvancementAPI.exceptions;

/**
 * This exception is thrown when an arbitrary criteria update is made while it is not admitted.
 */
public class ArbitraryMultiTaskCriteriaUpdateException extends RuntimeException {

    public ArbitraryMultiTaskCriteriaUpdateException() {
        super();
    }

    public ArbitraryMultiTaskCriteriaUpdateException(String message) {
        super(message);
    }

    public ArbitraryMultiTaskCriteriaUpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArbitraryMultiTaskCriteriaUpdateException(Throwable cause) {
        super(cause);
    }

    protected ArbitraryMultiTaskCriteriaUpdateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
