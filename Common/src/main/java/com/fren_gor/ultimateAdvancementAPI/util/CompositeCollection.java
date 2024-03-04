package com.fren_gor.ultimateAdvancementAPI.util;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * @hidden
 */
@Internal
public final class CompositeCollection<T> extends AbstractCompositeCollection<T> {
    private final int size;
    private final Collection<T>[] collections;

    @SafeVarargs
    public static <T> Collection<T> of(@NotNull Collection<T>... collections) {
        return Collections.unmodifiableCollection(new CompositeCollection<>(collections));
    }

    CompositeCollection(@NotNull Collection<T>[] collections) {
        this.collections = Objects.requireNonNull(collections);
        int size = 0;
        for (var collection : collections) {
            size += collection.size();
        }
        this.size = size;
    }

    @NotNull
    @Override
    protected Collection<T>[] getInner() {
        return collections;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public String toString() {
        return "CompositeCollection{" +
                "collections=" + Arrays.toString(collections) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositeCollection<?> that = (CompositeCollection<?>) o;

        return Arrays.equals(collections, that.collections);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(collections);
    }
}
