package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.exceptions.UnhandledException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ObjectResult<T> extends Result {

    protected final T result;

    public ObjectResult() {
        this((T) null);
    }

    public ObjectResult(T result) {
        super();
        this.result = result;
    }

    public ObjectResult(@NotNull Exception occurredException) {
        super(occurredException);
        this.result = null;
    }

    public boolean hasResult() {
        return result != null;
    }

    @Contract(pure = true)
    public T getResult() throws UnhandledException {
        rethrowExceptionIfOccurred();
        return result;
    }

    @Override
    public String toString() {
        return "ObjectResult{" + (isExceptionOccurred() ? "occurredException=" + occurredException + ", succeeded=false" : "result=" + result + ", succeeded=true") + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ObjectResult<?> that = (ObjectResult<?>) o;

        return Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        int result1 = super.hashCode();
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        return result1;
    }
}
