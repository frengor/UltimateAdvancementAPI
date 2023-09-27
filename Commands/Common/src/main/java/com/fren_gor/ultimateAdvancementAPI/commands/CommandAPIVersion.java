package com.fren_gor.ultimateAdvancementAPI.commands;

import net.byteflux.libby.LibraryManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Enum containing the used versions of <a href="https://github.com/JorelAli/CommandAPI">CommandAPI</a>.
 */
public enum CommandAPIVersion {
    LATEST("9.2.0", "EFbL3/btqNsS6xeZfgnbI5TwcWXxE2LdD6U161E6UGA=", "9_2_0", List.of(
            "v1_15_R1",
            "v1_16_R1",
            "v1_16_R2",
            "v1_16_R3",
            "v1_17_R1",
            "v1_18_R1",
            "v1_18_R2",
            "v1_19_R1",
            "v1_19_R2",
            "v1_19_R3",
            "v1_20_R1",
            "v1_20_R2"
    ));

    private final String version, checksum, suffix;
    private final List<String> supportedVersions;

    CommandAPIVersion(@NotNull String version, @NotNull String checksum, @NotNull String suffix, @NotNull List<String> supportedVersions) {
        this.version = version;
        this.checksum = checksum;
        this.suffix = suffix;
        this.supportedVersions = supportedVersions;
    }

    /**
     * Gets the fancy version.
     *
     * @return The fancy version.
     */
    @NotNull
    public String getVersion() {
        return version;
    }

    /**
     * Gets the Base64-encoded SHA-256 checksum of the downloaded jar.
     *
     * @return The Base64-encoded SHA-256 checksum of the downloaded jar.
     */
    @NotNull
    public String getChecksum() {
        return checksum;
    }

    /**
     * Gets the suffix of the package and classes that uses the <a href="https://github.com/JorelAli/CommandAPI">CommandAPI</a>.
     *
     * @return The suffix of the package and classes that uses the <a href="https://github.com/JorelAli/CommandAPI">CommandAPI</a>.
     * @see CommandAPIManager#loadManager(LibraryManager)
     */
    @NotNull
    public String getClasspathSuffix() {
        return suffix;
    }

    /**
     * Gets the list of the supported NMS versions.
     *
     * @return The list of the supported NMS versions.
     */
    @Unmodifiable
    @NotNull
    public List<String> getSupportedVersions() {
        return supportedVersions;
    }

    /**
     * Get the correct version of <a href="https://github.com/JorelAli/CommandAPI">CommandAPI</a> based on the provided NMS version.
     *
     * @param nms The NMS version, like "v1_17_R1".
     * @return The correct version of <a href="https://github.com/JorelAli/CommandAPI">CommandAPI</a>,
     *         or {@code null} if the NMS version is not supported.
     */
    @Nullable
    public static CommandAPIVersion getVersionToLoad(String nms) {
        for (CommandAPIVersion v : values()) {
            if (v.supportedVersions.contains(nms)) {
                return v;
            }
        }
        return null;
    }
}
