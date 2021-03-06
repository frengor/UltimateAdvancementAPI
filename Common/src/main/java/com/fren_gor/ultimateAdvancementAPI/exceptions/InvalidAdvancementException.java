package com.fren_gor.ultimateAdvancementAPI.exceptions;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;

/**
 * This exception is thrown when an {@link Advancement} is invalid.
 */
public class InvalidAdvancementException extends Exception {

    public InvalidAdvancementException() {
        super();
    }

    public InvalidAdvancementException(String message) {
        super(message);
    }

    public InvalidAdvancementException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidAdvancementException(Throwable cause) {
        super(cause);
    }

    protected InvalidAdvancementException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
