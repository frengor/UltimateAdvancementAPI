package com.fren_gor.ultimateAdvancementAPI.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for handling UltimateAdvancementAPI versions.
 */
@UtilityClass
public class Versions {

    private static final String API_VERSION = "1.0.0";
    private static String nmsVersion = null;

    private static final List<String> SUPPORTED_VERSIONS = Collections.unmodifiableList(Arrays.asList("1.15", "1.15.1", "1.15.2"));

    private static final List<String> SUPPORTED_NMS_VERSIONS = Collections.unmodifiableList(Arrays.asList("v1_15_R1"));

    private static final Map<String, List<String>> NMS_TO_VERSIONS;

    static {
        Map<String, List<String>> map = new HashMap<>();
        map.put("v1_15_R1", Collections.unmodifiableList(Arrays.asList("1.15", "1.15.1", "1.15.2")));
        NMS_TO_VERSIONS = Collections.unmodifiableMap(map);
    }

    private static final Map<String, String> NMS_TO_FANCY;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("v1_15_R1", "1.15-1.15.2");
        NMS_TO_FANCY = Collections.unmodifiableMap(map);
    }

    /**
     * Gets the NMS version of the current server.
     * <p>This returns a {@link String} like {@code "v1_15_R1"}.
     * To remove initial {@code 'v'} see {@link #removeInitialV(String)}.
     *
     * @return The server NMS version.
     */
    @NotNull
    public static String getNMSVersion() {
        if (nmsVersion != null) {
            return nmsVersion;
        }
        return nmsVersion = Bukkit.getServer().getClass().getName().split("\\.")[3];
    }

    /**
     * Gets current API version.
     *
     * @return The API version.
     */
    public static String getApiVersion() {
        return API_VERSION;
    }

    /**
     * Gets an unmodifiable list of minecraft versions supported by this release of UltimateAdvancementAPI.
     * <p>Returned versions are like {@code "1.15"} or {@code "1.15.2"}.
     *
     * @return An unmodifiable list of minecraft versions supported by this release of UltimateAdvancementAPI.
     */
    @UnmodifiableView
    @NotNull
    public static List<@NotNull String> getSupportedVersions() {
        return SUPPORTED_VERSIONS;
    }

    /**
     * Gets an unmodifiable list of NMS versions supported by this release of UltimateAdvancementAPI.
     * <p>Returned versions are like {@code "v1_15_R1"}.
     *
     * @return An unmodifiable list of minecraft versions supported by this release of UltimateAdvancementAPI.
     */
    @UnmodifiableView
    @NotNull
    public static List<@NotNull String> getSupportedNMSVersions() {
        return SUPPORTED_NMS_VERSIONS;
    }

    /**
     * Gets the minecraft versions supported by this server NMS version.
     * <p>For example, for {@code "v1_15_R1"} this method returns {@code "1.15-1.15.2"}.
     *
     * @return The minecraft versions supported by this server NMS version,
     *         or {@code null} if the server version is not supported (see {@link #getSupportedNMSVersions()}).
     */
    @NotNull
    public static String getNMSVersionsRange() {
        return Objects.requireNonNull(getNMSVersionsRange(getNMSVersion()), "Server version is not supported!");
    }

    /**
     * Gets the minecraft versions supported by the provided NMS version.
     * <p>For example, for {@code "v1_15_R1"} this method returns {@code "1.15-1.15.2"}.
     *
     * @param version The NMS version.
     * @return The minecraft versions supported by the provided NMS version,
     *         or {@code null} if the provided version is not supported (see {@link #getSupportedNMSVersions()}).
     */
    @Nullable
    @Contract("null -> null")
    public static String getNMSVersionsRange(String version) {
        return NMS_TO_FANCY.get(version);
    }

    /**
     * Gets an unmodifiable list of the minecraft versions supported by this server NMS version.
     * <p>For example, for {@code "v1_15_R1"} this method returns a list containing
     * {@code "1.15"}, {@code "1.15.1"}, and {@code "1.15.2"}.
     *
     * @return An unmodifiable list of the minecraft versions supported by this server NMS version,
     *         or {@code null} if the server version is not supported (see {@link #getSupportedNMSVersions()}).
     */
    @UnmodifiableView
    @NotNull
    public static List<@NotNull String> getNMSVersionsList() {
        return Objects.requireNonNull(getNMSVersionsList(getNMSVersion()), "Server version is not supported!");
    }

    /**
     * Gets an unmodifiable list of the minecraft versions supported by the provided NMS version.
     * <p>For example, for {@code "v1_15_R1"} this method returns a list containing
     * {@code "1.15"}, {@code "1.15.1"}, and {@code "1.15.2"}.
     *
     * @param version The NMS version.
     * @return An unmodifiable list of the minecraft versions supported by the provided NMS version,
     *         or {@code null} if the provided version is not supported (see {@link #getSupportedNMSVersions()}).
     */
    @UnmodifiableView
    @Nullable
    @Contract("null -> null")
    public static List<@NotNull String> getNMSVersionsList(String version) {
        return NMS_TO_VERSIONS.get(version);
    }

    /**
     * Returns the input string unless it starts with {@code 'v'}. In that case, this method returns the string without the first letter.
     * If {@code null}, this method returns {@code null}.
     * <p>Examples:
     * <blockquote><pre>
     * removeInitialV("v1_15_R1") returns "1_15_R1"
     * removeInitialV("1_15_R1") returns "1_15_R1"
     * removeInitialV("") returns ""
     * removeInitialV("vv1_15_R1") returns "v1_15_R1"
     * removeInitialV(null) returns null
     * </pre></blockquote>
     *
     * @param string The input string
     * @return {@code null} if the string is {@code null}, the string itself if the first character isn't {@code 'v'},
     *         otherwise the string without the first character.
     */
    @Contract("null -> null; !null -> !null")
    public static String removeInitialV(String string) {
        return string == null || string.isEmpty() || string.charAt(0) != 'v' ? string : string.substring(1);
    }
}
