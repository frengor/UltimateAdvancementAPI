package com.fren_gor.ultimateAdvancementAPI.util;

import com.fren_gor.ultimateAdvancementAPI.exceptions.IllegalKeyException;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import net.minecraft.server.v1_15_R1.ResourceKeyInvalidException;
import org.apache.commons.lang.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

public final class AdvancementKey implements Comparable<AdvancementKey> {

    @NotNull
    private final MinecraftKey minecraftKey;

    public AdvancementKey(@NotNull Plugin plugin, @NotNull String key) {
        this(plugin.getName().toLowerCase(Locale.ROOT), key);
    }

    public AdvancementKey(@NotNull String namespace, @NotNull String key) throws IllegalKeyException {
        checkNamespace(namespace);
        checkKey(key);
        try {
            this.minecraftKey = new MinecraftKey(namespace, key);
        } catch (ResourceKeyInvalidException e) {
            throw new IllegalKeyException(e.getMessage(), e);
        }
    }

    public AdvancementKey(@Nullable NamespacedKey key) {
        this(Objects.requireNonNull(key, "NamespacedKey is null.").getNamespace(), key.getKey());
    }

    public AdvancementKey(@NotNull MinecraftKey key) throws IllegalKeyException {
        this(Objects.requireNonNull(key, "MinecraftKey is null.").getNamespace(), key.getKey());
    }

    @NotNull
    public String getNamespace() {
        return minecraftKey.getNamespace();
    }

    @NotNull
    public String getKey() {
        return minecraftKey.getKey();
    }

    @NotNull
    public MinecraftKey toMinecraftKey() {
        return minecraftKey;
    }

    @NotNull
    public NamespacedKey toNamespacedKey() {
        return new NamespacedKey(minecraftKey.getNamespace(), minecraftKey.getKey());
    }

    public static AdvancementKey fromString(@NotNull String string) throws IllegalKeyException {
        int colon = string.indexOf(':');
        if (colon <= 0 || colon == string.length() - 1) {
            throw new IllegalKeyException("Illegal key '" + string + "'");
        }
        return new AdvancementKey(string.substring(0, colon), string.substring(colon + 1));
    }

    public static void checkNamespace(String namespace) throws IllegalArgumentException {
        Validate.notNull(namespace, "Namespace is null.");
        Validate.isTrue(!namespace.isEmpty(), "Namespace is empty.");
        Validate.isTrue(namespace.length() <= 127, "Too long namespace (max allowed is 127 chars).");
    }

    public static void checkKey(String key) throws IllegalArgumentException {
        Validate.notNull(key, "Key is null.");
        Validate.isTrue(!key.isEmpty(), "Key is empty.");
        Validate.isTrue(key.length() <= 127, "Too long key (max allowed is 127 chars).");
    }

    @Override
    public int compareTo(@NotNull AdvancementKey key) {
        return minecraftKey.compareTo(key.minecraftKey);
    }

    @Override
    public String toString() {
        return minecraftKey.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdvancementKey that = (AdvancementKey) o;

        return minecraftKey.equals(that.minecraftKey);
    }

    @Override
    public int hashCode() {
        return minecraftKey.hashCode();
    }

}
