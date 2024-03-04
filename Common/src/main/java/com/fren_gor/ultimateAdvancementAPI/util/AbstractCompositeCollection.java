package com.fren_gor.ultimateAdvancementAPI.util;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @hidden
 */
@Internal
abstract class AbstractCompositeCollection<T> implements Collection<T> {

    @NotNull
    protected abstract Collection<T>[] getInner();

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        for (var c : getInner()) {
            if (c.contains(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        for (Object o : collection) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        Object[] ret = new Object[size()];
        int i = 0;
        for (var set : getInner()) {
            Object[] obj = set.toArray();
            System.arraycopy(obj, 0, ret, i, obj.length);
            i += obj.length;
        }
        return ret;
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <E> E @NotNull [] toArray(E[] a) {
        Class<?> clazz = a.getClass().getComponentType();
        if (a.length < size()) {
            a = (E[]) Array.newInstance(clazz, size());
        }
        int i = 0;
        for (var collection : getInner()) {
            E[] obj = (E[]) Array.newInstance(clazz, collection.size());
            obj = collection.toArray(obj);
            System.arraycopy(obj, 0, a, i, obj.length);
            i += obj.length;
        }
        if (i < a.length) {
            a[i] = null;
        }
        return a;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new CompositeIterator<>(getInner());
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    private static final class CompositeIterator<T> implements Iterator<T> {
        private final Collection<T>[] collections;
        private Iterator<T> currentIter;
        private int nextColl;

        public CompositeIterator(@NotNull Collection<T>[] collections) {
            this.collections = collections;
            if (collections.length > 0) {
                currentIter = collections[0].iterator();
                nextColl = 1;
            }
        }

        @Override
        public boolean hasNext() {
            if (currentIter.hasNext()) {
                return true;
            }
            if (nextColl < collections.length) {
                currentIter = collections[nextColl++].iterator();
                return hasNext();
            }
            return false;
        }

        @Override
        public T next() {
            try {
                return currentIter.next();
            } catch (NoSuchElementException ignored) {
                if (nextColl < collections.length) {
                    currentIter = collections[nextColl++].iterator();
                    return next();
                } else {
                    throw new NoSuchElementException();
                }
            }
        }
    }
}
