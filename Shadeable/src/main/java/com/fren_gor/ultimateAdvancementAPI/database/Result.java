package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.exceptions.UnhandledException;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Result {

    public static final Result SUCCESSFUL = new Result();

    protected final Exception occurredException;

    public Result() {
        this.occurredException = null;
    }

    public Result(@NotNull Exception occurredException) {
        Validate.notNull(occurredException, "Exception is null.");
        this.occurredException = occurredException;
    }

    public boolean isExceptionOccurred() {
        return occurredException != null;
    }

    public boolean isSucceeded() {
        return occurredException == null;
    }

    public Exception getOccurredException() throws UnsupportedOperationException {
        if (!isExceptionOccurred()) {
            throw new UnsupportedOperationException("No exception occurred.");
        }
        return occurredException;
    }

    public void rethrowException() throws UnhandledException, UnsupportedOperationException {
        rethrowExceptionIfOccurred();
        throw new UnsupportedOperationException("No exception occurred.");
    }

    public void rethrowExceptionIfOccurred() throws UnhandledException {
        if (isExceptionOccurred())
            throw new UnhandledException(occurredException);
    }

    public void printStackTrace() throws UnsupportedOperationException {
        if (!isExceptionOccurred()) {
            throw new UnsupportedOperationException("No exception occurred.");
        }
        occurredException.printStackTrace();
    }

    @Override
    public String toString() {
        return "Result{" + (isExceptionOccurred() ? "occurredException=" + occurredException + ", succeeded=false" : "succeeded=true") + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Result result = (Result) o;

        return Objects.equals(occurredException, result.occurredException);
    }

    @Override
    public int hashCode() {
        return occurredException != null ? occurredException.hashCode() : 0;
    }
}
