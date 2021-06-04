package com.fren_gor.ultimateAdvancementAPI.exceptions;

public class NotGrantedException extends RuntimeException {

    public NotGrantedException() {
        super();
    }

    public NotGrantedException(String message) {
        super(message);
    }

    public NotGrantedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotGrantedException(Throwable cause) {
        super(cause);
    }

    protected NotGrantedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
