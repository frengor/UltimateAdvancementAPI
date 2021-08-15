package com.fren_gor.ultimateAdvancementAPI.exceptions;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public class InvalidVersionException extends RuntimeException {

    @Getter
    @Nullable
    private String expected, found;

    public InvalidVersionException() {
        super();
    }

    public InvalidVersionException(String message) {
        super(message);
    }

    public InvalidVersionException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidVersionException(Throwable cause) {
        super(cause);
    }

    protected InvalidVersionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public InvalidVersionException(String expected, String found) {
        super();
        this.expected = expected;
        this.found = found;
    }

    public InvalidVersionException(String expected, String found, String message) {
        super(message);
        this.expected = expected;
        this.found = found;
    }

    public InvalidVersionException(String expected, String found, String message, Throwable cause) {
        super(message, cause);
        this.expected = expected;
        this.found = found;
    }

    public InvalidVersionException(String expected, String found, Throwable cause) {
        super(cause);
        this.expected = expected;
        this.found = found;
    }

    protected InvalidVersionException(String expected, String found, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.expected = expected;
        this.found = found;
    }
}
