package com.fren_gor.ultimateAdvancementAPI.exceptions;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;

/**
 * This exception is thrown when an {@link AdvancementKey} couldn't be made from a {@link String} since it is not a valid key.
 *
 * @see AdvancementKey#VALID_ADVANCEMENT_KEY
 */
public class IllegalKeyException extends RuntimeException {

    public IllegalKeyException() {
        super();
    }

    public IllegalKeyException(String message) {
        super(message);
    }

    public IllegalKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalKeyException(Throwable cause) {
        super(cause);
    }

    protected IllegalKeyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
