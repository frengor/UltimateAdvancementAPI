package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.exceptions.UnhandledException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The {@code ObjectResult} class represents the result of an operation that returns a value, called result. It can either succeed or fail.
 * If it succeeded, then a (possibly {@code null}) result is provided.
 * Otherwise, an {@link Exception} occurred.
 * Example usage:
 * <blockquote><pre>
 *     ObjectResult&lt;Integer&gt; res = doOperation();
 *     if (res.isSucceeded()) { // the condition here is equivalent to !res.isExceptionOccurred()
 *         // success
 *         Integer value = res.getResult();
 *     } else {
 *         // exception occurred!
 *         Exception exception = res.getOccurredException();
 *     }
 * </pre></blockquote>
 *
 * @param <T> The result type
 */
public class ObjectResult<T> extends Result {

    /**
     * The result.
     */
    protected final T result;

    /**
     * Creates a successful {@code ObjectResult} with {@code null} result.
     */
    public ObjectResult() {
        this((T) null);
    }

    /**
     * Creates a successful {@code ObjectResult} with a result.
     *
     * @param result The result (can be {@code null}).
     */
    public ObjectResult(T result) {
        super();
        this.result = result;
    }

    /**
     * Creates a failed {@code ObjectResult}.
     *
     * @param occurredException The exception occurred during the operation.
     */
    public ObjectResult(@NotNull Exception occurredException) {
        super(occurredException);
        this.result = null;
    }

    /**
     * Whether the operation succeeded and the result is not {@code null}.
     * <p>More formally, this method returns true if and only if {@code (isSucceeded() &amp;&amp; getResult() != null) == true}.
     *
     * @return Whether the operation succeeded and the result is not {@code null}.
     */
    public boolean hasResult() {
        return isSucceeded() && result != null;
    }

    /**
     * Gets the result of the operation if no {@link Exception} occurred.
     * If an exception occurred, then it is rethrown as an {@link UnhandledException}.
     *
     * @return The result of the operation.
     * @throws UnhandledException If an exception occurred.
     */
    @Contract(pure = true)
    public T getResult() throws UnhandledException {
        rethrowExceptionIfOccurred();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ObjectResult{" + (isExceptionOccurred() ? "occurredException=" + occurredException + ", succeeded=false" : "result=" + result + ", succeeded=true") + '}';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ObjectResult<?> that = (ObjectResult<?>) o;

        return Objects.equals(result, that.result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result1 = super.hashCode();
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        return result1;
    }
}
