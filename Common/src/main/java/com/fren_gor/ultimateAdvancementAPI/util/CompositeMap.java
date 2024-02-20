package com.fren_gor.ultimateAdvancementAPI.util;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @hidden
 */
@Internal
public final class CompositeMap<K, V> implements Map<K, V> {
    private final int size;
    private final Map<K, V>[] maps;

    @SafeVarargs
    public static <K, V> Map<K, V> of(@NotNull Map<K, V>... maps) {
        return Collections.unmodifiableMap(new CompositeMap<>(maps));
    }

    private CompositeMap(@NotNull Map<K, V>[] maps) {
        this.maps = Objects.requireNonNull(maps);
        int size = 0;
        for (var map : maps) {
            size += map.size();
        }
        this.size = size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsKey(Object o) {
        for (var map : maps) {
            if (map.containsKey(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object o) {
        for (var map : maps) {
            if (map.containsValue(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object o) {
        for (var map : maps) {
            V v = map.get(o);
            if (v != null) {
                return v;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        @SuppressWarnings("unchecked")
        Set<K>[] sets = new Set[maps.length];
        int i = 0;
        for (var map : maps) {
            sets[i++] = map.keySet();
        }
        return new CompositeSet<>(sets);
    }

    @NotNull
    @Override
    public Collection<V> values() {
        @SuppressWarnings("unchecked")
        Collection<V>[] collections = new Collection[maps.length];
        int i = 0;
        for (var map : maps) {
            collections[i++] = map.values();
        }
        return new CompositeCollection<>(collections);
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        @SuppressWarnings("unchecked")
        Set<Entry<K, V>>[] sets = new Set[maps.length];
        int i = 0;
        for (var map : maps) {
            sets[i++] = map.entrySet();
        }
        return new CompositeSet<>(sets);
    }

    @Nullable
    @Override
    public V put(K k, V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "CompositeMap{" +
                "maps=" + Arrays.toString(maps) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositeMap<?, ?> that = (CompositeMap<?, ?>) o;

        return Arrays.equals(maps, that.maps);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(maps);
    }
}
