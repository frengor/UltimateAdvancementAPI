package com.fren_gor.ultimateAdvancementAPI.util;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * @hidden
 */
@Internal
public final class CompositeSet<T> extends AbstractCompositeCollection<T> implements Set<T> {
    private final int size;
    private final Set<T>[] sets;

    @SafeVarargs
    public static <T> Set<T> of(@NotNull Set<T>... sets) {
        return Collections.unmodifiableSet(new CompositeSet<>(sets));
    }

    CompositeSet(@NotNull Set<T>[] sets) {
        this.sets = Objects.requireNonNull(sets);
        int size = 0;
        for (var set : sets) {
            size += set.size();
        }
        this.size = size;
    }

    @NotNull
    @Override
    protected Collection<T>[] getInner() {
        return sets;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return "CompositeSet{" +
                "sets=" + Arrays.toString(sets) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositeSet<?> that = (CompositeSet<?>) o;

        return Arrays.equals(sets, that.sets);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(sets);
    }
}
