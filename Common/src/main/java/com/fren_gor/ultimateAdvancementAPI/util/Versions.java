package com.fren_gor.ultimateAdvancementAPI.util;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class for handling UltimateAdvancementAPI versions.
 */
public class Versions {

    private static final String API_VERSION = "2.1.1";

    private static final List<String> SUPPORTED_NMS_VERSIONS = List.of("v1_15_R1", "v1_16_R1", "v1_16_R2", "v1_16_R3", "v1_17_R1", "v1_18_R1");

    private static final Map<String, List<String>> NMS_TO_VERSIONS = Map.of(
            "v1_15_R1", List.of("1.15", "1.15.1", "1.15.2"),
            "v1_16_R1", List.of("1.16", "1.16.1", "1.16.2"),
            "v1_16_R2", List.of("1.16.3", "1.16.4"),
            "v1_16_R3", List.of("1.16.5"),
            "v1_17_R1", List.of("1.17", "1.17.1"),
            "v1_18_R1", List.of("1.18", "1.18.1")
    );

    private static final Map<String, String> NMS_TO_FANCY = Map.of(
            "v1_15_R1", "1.15-1.15.2",
            "v1_16_R1", "1.16-1.16.2",
            "v1_16_R2", "1.16.3-1.16.4",
            "v1_16_R3", "1.16.5",
            "v1_17_R1", "1.17-1.17.1",
            "v1_18_R1", "1.18-1.18.1"
    );

    private static final List<String> SUPPORTED_VERSIONS = SUPPORTED_NMS_VERSIONS.stream()
            .flatMap(s -> NMS_TO_VERSIONS.get(s).stream())
            .toList();

    /**
     * Gets the NMS version of the current server.
     * <p>This returns a {@link String} like {@code "v1_15_R1"}.
     * To remove initial {@code 'v'} see {@link #removeInitialV(String)}.
     *
     * @return The server NMS version.
     */
    @NotNull
    public static String getNMSVersion() {
        return ReflectionUtil.COMPLETE_VERSION;
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

    private Versions() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
