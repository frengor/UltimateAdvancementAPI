package com.fren_gor.ultimateAdvancementAPI.exceptions;

import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;

/**
 * This exception is thrown when {@link UltimateAdvancementAPI} is used but the API has not yet been enabled.
 */
public class APINotInstantiatedException extends RuntimeException {

    public APINotInstantiatedException() {
        super();
    }

    public APINotInstantiatedException(String message) {
        super(message);
    }

    public APINotInstantiatedException(String message, Throwable cause) {
        super(message, cause);
    }

    public APINotInstantiatedException(Throwable cause) {
        super(cause);
    }

    protected APINotInstantiatedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
