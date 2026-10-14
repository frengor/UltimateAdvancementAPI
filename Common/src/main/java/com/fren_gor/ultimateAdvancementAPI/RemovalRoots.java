package com.fren_gor.ultimateAdvancementAPI;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

/** Selects keys for the client's recursive advancement removal operation. */
final class RemovalRoots {
    private RemovalRoots() {}

    static <T, K> Set<K> select(Collection<T> nodes, Function<T, K> key, Function<T, T> parent) {
        Set<K> keys = new HashSet<>();
        for (T node : nodes) keys.add(key.apply(node));
        Set<K> roots = new HashSet<>(keys);
        for (T node : nodes) {
            Set<K> visited = new HashSet<>();
            visited.add(key.apply(node));
            for (T ancestor = parent.apply(node); ancestor != null; ancestor = parent.apply(ancestor)) {
                K ancestorKey = key.apply(ancestor);
                if (!visited.add(ancestorKey)) throw new IllegalArgumentException("Cyclic advancement parent chain");
                if (keys.contains(ancestorKey)) {
                    roots.remove(key.apply(node));
                    break;
                }
            }
        }
        return roots;
    }
}
