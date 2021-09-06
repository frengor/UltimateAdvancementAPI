package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.exceptions.IllegalOperationException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UnhandledException;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The {@code Result} class represents the result of an operation. It can either succeed or fail.
 * If it failed, then an {@link Exception} occurred.
 * Example usage:
 * <blockquote><pre>
 *     Result res = doOperation();
 *     if (res.isSucceeded()) { // the condition here is equivalent to !res.isExceptionOccurred()
 *         // success
 *     } else {
 *         // exception occurred!
 *         Exception exception = res.getOccurredException();
 *     }
 * </pre></blockquote>
 */
public class Result {

    /**
     * A successful {@code Result}.
     */
    public static final Result SUCCESSFUL = new Result();

    /**
     * The occurred exception. {@code null} if the operation succeed.
     */
    protected final Exception occurredException;

    /**
     * Creates a new successful {@code Result}.
     */
    public Result() {
        this.occurredException = null;
    }

    /**
     * Creates a failed {@code Result}.
     *
     * @param occurredException The exception occurred during the operation.
     * @throws IllegalArgumentException If {@code occurredException} is {@code null}.
     */
    public Result(@NotNull Exception occurredException) {
        Validate.notNull(occurredException, "Exception is null.");
        this.occurredException = occurredException;
    }

    /**
     * Whether an {@link Exception} occurred executing the operation.
     * If no {@link Exception} occurred, then the operation succeeded.
     *
     * @return {@code true} if no exception occurred, {@code false} otherwise.
     */
    public boolean isExceptionOccurred() {
        return occurredException != null;
    }

    /**
     * Whether the operation succeeded.
     * If the operation succeeded, then no {@link Exception} occurred.
     *
     * @return {@code true} if the operation succeeded, {@code false} otherwise.
     */
    public boolean isSucceeded() {
        return occurredException == null;
    }

    /**
     * Gets the occurred {@link Exception}.
     * An {@link IllegalOperationException} is thrown if no exception occurred.
     *
     * @return The occurred exception.
     * @throws IllegalOperationException If no exception occurred.
     */
    public Exception getOccurredException() throws IllegalOperationException {
        if (!isExceptionOccurred()) {
            throw new IllegalOperationException("No exception occurred.");
        }
        return occurredException;
    }

    /**
     * Rethrow the occurred exception as an {@link UnhandledException}.
     * An {@link IllegalOperationException} is thrown if no exception occurred.
     *
     * @throws UnhandledException If an exception occurred.
     * @throws IllegalOperationException If no exception occurred.
     */
    @Contract("-> fail")
    public void rethrowException() throws UnhandledException, IllegalOperationException {
        rethrowExceptionIfOccurred();
        throw new IllegalOperationException("No exception occurred.");
    }

    /**
     * Rethrow the occurred exception as an {@link UnhandledException} only if that occurred.
     *
     * @throws UnhandledException If an exception occurred.
     */
    public void rethrowExceptionIfOccurred() throws UnhandledException {
        if (isExceptionOccurred())
            throw new UnhandledException(occurredException);
    }

    /**
     * Prints the stack trace of the occurred exception.
     * An {@link IllegalOperationException} is thrown if no exception occurred.
     *
     * @throws IllegalOperationException If no exception occurred.
     */
    public void printStackTrace() throws IllegalOperationException {
        if (!isExceptionOccurred()) {
            throw new IllegalOperationException("No exception occurred.");
        }
        occurredException.printStackTrace();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Result{" + (isExceptionOccurred() ? "occurredException=" + occurredException + ", succeeded=false" : "succeeded=true") + '}';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Result result = (Result) o;

        return Objects.equals(occurredException, result.occurredException);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return occurredException != null ? occurredException.hashCode() : 0;
    }
}
