package com.fren_gor.ultimateAdvancementAPI.nms.util;

import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.AbstractWrapper;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Immutable copy of the non-null elements of a {@link Set}.
 * <p>The implementation uses an immutable array-based list to store the elements of the original {@link Set}
 * in order to minimize the copy operation cost.
 * <p>Since {@code ListSet} is immutable and contains only the elements of one other {@link Set},
 * it respects all the properties of a {@link Set}.
 * <p>This class is thread safe.
 *
 * @param <E> The type of the elements of this {@link Set}.
 */
public final class ListSet<E> extends AbstractSet<E> implements Set<E> {

    private final E[] elements;
    private final int size;

    /**
     * Creates a new {@code ListSet} containing the elements of the provided {@link Set}.
     *
     * @param elements The elements to copy into this {@link Set}. {@code null} elements are not added to the {@code ListSet}.
     * @throws NullPointerException If the provided {@link Set} is {@code null}.
     */
    public ListSet(@NotNull Set<E> elements) {
        Preconditions.checkNotNull(elements, "Set is null.");
        @SuppressWarnings("unchecked")
        E[] array = (E[]) new Object[elements.size()];
        int i = 0;
        for (E e : elements) {
            if (e != null)
                array[i++] = e;
        }
        this.elements = array;
        size = i;
    }

    private ListSet(@NotNull E[] elements, @Range(from = 0, to = Integer.MAX_VALUE) int size) {
        this.elements = elements;
        this.size = size;
    }

    /**
     * Creates a new {@code ListSet} containing the NMS objects associated with the elements of the provided {@link Set}.
     * <p>{@link AbstractWrapper#toNMS()} is called on every non-null element of the provided {@link Set}.
     *
     * @param elements The {@link AbstractWrapper}s to convert to their NMS associated
     * @param <T> The type of the elements in the provided {@link Set}.
     * @return A new {@code ListSet} containing the NMS objects associated with the elements of the provided {@link Set}.
     * @throws NullPointerException If the provided {@link Set} is {@code null}.
     */
    @NotNull
    @Contract(pure = true, value = "_ -> new")
    public static <T extends AbstractWrapper> ListSet<?> fromWrapperSet(@NotNull Set<T> elements) {
        Preconditions.checkNotNull(elements, "Set is null.");
        Object[] array = new Object[elements.size()];
        int i = 0;
        for (T t : elements) {
            if (t != null) {
                Object nms = t.toNMS();
                if (nms != null) // Double check not-nullity
                    array[i++] = nms;
            }
        }
        return new ListSet<>(array, i);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public E[] toArray() {
        @SuppressWarnings("unchecked")
        E[] array = (E[]) new Object[this.size];
        System.arraycopy(this.elements, 0, array, 0, this.size);
        return array;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] array) {
        if (array.length < this.size) {
            array = (T[]) Array.newInstance(array.getClass().getComponentType(), this.size);
        } else if (array.length > this.size) {
            array[this.size] = null;
        }

        System.arraycopy(this.elements, 0, array, 0, this.size);
        return array;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private int current = 0;

            @Override
            public boolean hasNext() {
                return current < size;
            }

            @Override
            public E next() {
                // `elements` is never modified, so this is fine
                return elements[current++];
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return size;
    }
}
