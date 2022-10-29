package com.fren_gor.ultimateAdvancementAPI.util;

import com.fren_gor.ultimateAdvancementAPI.exceptions.IllegalKeyException;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.google.common.base.Preconditions;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The {@code AdvancementKey} class represents a valid advancement namespaced key.
 * <p>The namespaced keys represented by this class match the following pattern:
 * {@code [a-z0-9_.-]{1,127}:[a-z0-9_.-/]{1,127}}.
 */
public final class AdvancementKey implements Comparable<AdvancementKey> {

    /**
     * Pattern every valid advancement key should match.
     */
    public static final Pattern VALID_ADVANCEMENT_KEY = Pattern.compile("[a-z0-9_.-]{1,127}:[a-z0-9_./\\-]{1,127}");

    /**
     * Pattern every valid namespace should match.
     */
    public static final Pattern VALID_NAMESPACE = Pattern.compile("[a-z0-9_.-]{1,127}");

    /**
     * Pattern every valid key should match.
     */
    public static final Pattern VALID_KEY = Pattern.compile("[a-z0-9_./\\-]{1,127}");

    @NotNull
    private final MinecraftKeyWrapper minecraftKey;

    /**
     * Creates a new {@code AdvancementKey} with the provided plugin's (lowercased) name as namespace and the specified key.
     *
     * @param plugin The plugin.
     * @param key The key. Must match the following pattern: {@code [[a-z0-9_.-/]{1,127}}.
     */
    public AdvancementKey(@NotNull Plugin plugin, @NotNull String key) {
        this(plugin.getName().toLowerCase(Locale.ROOT), key);
    }

    /**
     * Creates a new {@code AdvancementKey} with the provided namespace and key.
     *
     * @param namespace The namespace. Must match the following pattern: {@code [[a-z0-9_.-]{1,127}}.
     * @param key The key. Must match the following pattern: {@code [[a-z0-9_.-/]{1,127}}.
     * @throws IllegalKeyException If the namespace or the key is not valid.
     */
    public AdvancementKey(@NotNull String namespace, @NotNull String key) throws IllegalKeyException {
        checkNamespace(namespace);
        checkKey(key);
        try {
            this.minecraftKey = MinecraftKeyWrapper.craft(namespace, key);
        } catch (IllegalArgumentException e) {
            throw new IllegalKeyException(e.getMessage());
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new {@code AdvancementKey} from the specified {@link NamespacedKey}.
     *
     * @param key The {@link NamespacedKey}.
     */
    public AdvancementKey(@Nullable NamespacedKey key) {
        this(Objects.requireNonNull(key, "NamespacedKey is null.").getNamespace(), key.getKey());
    }

    /**
     * Creates a new {@code AdvancementKey} from the specified NMS {@code MinecraftKey}.
     *
     * @param key The {@code MinecraftKey}.
     * @throws IllegalKeyException If the namespace or the key is not valid. See {@link #AdvancementKey(String, String)}.
     */
    public AdvancementKey(@NotNull MinecraftKeyWrapper key) throws IllegalKeyException {
        // This way namespace and key checks are performed
        this(Objects.requireNonNull(key, "MinecraftKey is null.").getNamespace(), key.getKey());
    }

    /**
     * Gets the namespace of this namespaced key.
     *
     * @return The namespace of this namespaced key.
     */
    @NotNull
    public String getNamespace() {
        return minecraftKey.getNamespace();
    }

    /**
     * Gets the key of this namespaced key.
     *
     * @return The key of this namespaced key.
     */
    @NotNull
    public String getKey() {
        return minecraftKey.getKey();
    }

    /**
     * Gets the NMS wrapper of this {@code AdvancementKey} with the same namespace and key of this namespaced key.
     *
     * @return The NMS wrapper of this {@code AdvancementKey}
     */
    @NotNull
    public MinecraftKeyWrapper getNMSWrapper() {
        return minecraftKey;
    }

    /**
     * Gets a {@link NamespacedKey} with the same namespace and key of this namespaced key.
     *
     * @return This namespaced key as a {@link NamespacedKey}.
     */
    @NotNull
    public NamespacedKey toNamespacedKey() {
        return new NamespacedKey(minecraftKey.getNamespace(), minecraftKey.getKey());
    }

    /**
     * Creates an {@code AdvancementKey} from the provided string.
     * <p>The provided string must be a valid namespace key, or an {@link IllegalKeyException} is thrown.
     *
     * @param string The string.
     * @return An {@link AdvancementKey} from the string.
     * @throws IllegalKeyException If provided string is not a valid namespace key.
     */
    public static AdvancementKey fromString(@NotNull String string) throws IllegalKeyException {
        int colon;
        if (string == null || string.isEmpty() || (colon = string.indexOf(':')) <= 0 || colon == string.length() - 1) {
            throw new IllegalKeyException("Illegal key '" + string + "'");
        }
        return new AdvancementKey(string.substring(0, colon), string.substring(colon + 1));
    }

    /**
     * Checks whether the provided namespace is not {@code null}, not empty, and it's length is less or equals to 127.
     *
     * @param namespace The string to validate.
     * @throws IllegalArgumentException If the provided string is {@code null} or empty.
     * @throws IllegalKeyException If the provided string is longer than 127 characters.
     */
    public static void checkNamespace(String namespace) throws IllegalArgumentException, IllegalKeyException {
        Preconditions.checkNotNull(namespace, "Namespace is null.");
        Preconditions.checkArgument(!namespace.isEmpty(), "Namespace is empty.");
        if (namespace.length() > 127) {
            throw new IllegalKeyException("Too long namespace (max allowed is 127 chars).");
        }
    }

    /**
     * Checks whether the provided key is not {@code null}, not empty, and it's length is less or equals to 127.
     *
     * @param key The string to validate.
     * @throws IllegalArgumentException If the provided string is {@code null} or empty.
     * @throws IllegalKeyException If the provided string is longer than 127 characters.
     */
    public static void checkKey(String key) throws IllegalArgumentException, IllegalKeyException {
        Preconditions.checkNotNull(key, "Key is null.");
        Preconditions.checkArgument(!key.isEmpty(), "Key is empty.");
        if (key.length() > 127) {
            throw new IllegalKeyException("Too long key (max allowed is 127 chars).");
        }
    }

    /**
     * Compares this {@code AdvancementKey} to the provided one.
     *
     * @param key The other {@code AdvancementKey}.
     * @return A negative integer, zero, or a positive integer as this key is less than, equal to, or greater than the specified one.
     * @throws NullPointerException If the specified key is null
     */
    @Override
    public int compareTo(@NotNull AdvancementKey key) {
        return minecraftKey.compareTo(key.minecraftKey);
    }

    /**
     * Returns a string representation of this namespaced key.
     *
     * @return This namespaced key in the format "namespace:key".
     */
    @Override
    public String toString() {
        return minecraftKey.getNamespace() + ':' + minecraftKey.getKey();
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
