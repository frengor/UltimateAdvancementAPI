package com.fren_gor.ultimateAdvancementAPI.exceptions;

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
