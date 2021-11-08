package com.fren_gor.ultimateAdvancementAPI.exceptions;

import com.fren_gor.ultimateAdvancementAPI.database.Result;

/**
 * This exception is thrown when an illegal operation occurs.
 * <p>For instance, calling {@link Result#getOccurredException()} when no exception occurred results in an {@code IllegalOperationException}.
 */
public class IllegalOperationException extends UnsupportedOperationException {

    public IllegalOperationException() {
        super();
    }

    public IllegalOperationException(String message) {
        super(message);
    }

    public IllegalOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalOperationException(Throwable cause) {
        super(cause);
    }
}
