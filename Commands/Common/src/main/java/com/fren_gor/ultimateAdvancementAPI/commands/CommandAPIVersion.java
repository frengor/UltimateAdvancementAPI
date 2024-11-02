package com.fren_gor.ultimateAdvancementAPI.commands;

import com.google.common.base.Preconditions;
import net.byteflux.libby.LibraryManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

/**
 * Enum containing the used versions of <a href="https://github.com/JorelAli/CommandAPI">CommandAPI</a>.
 */
public enum CommandAPIVersion {
    v9_3_0("9.3.0", "K9cYHoWiGLP8z+cyYb5NIarVE9fzMeJRqHu1UJ+Li/U=", null, "9_3_0",
            List.of(
                    "v1_15_R1",
                    "v1_16_R1",
                    "v1_16_R2",
                    "v1_16_R3"
            )
    ),
    LATEST("9.6.1", "HTKgGxAxXD2FXntvUmxk5iGtbbzW0DU4WAgXNgvR+Dg=", "L1JmzpO50Tpk94xXGS0j3hXSxytXuCBI/zHvDrAK5tk=", "9_6_1",
            List.of(
                    "v1_17_R1",
                    "v1_18_R1",
                    "v1_18_R2",
                    "v1_19_R1",
                    "v1_19_R2",
                    "v1_19_R3",
                    "v1_20_R1",
                    "v1_20_R2",
                    "v1_20_R3",
                    "v1_20_R4",
                    "v1_21_R1",
                    "v1_21_R2"
            )
    );

    private final String version, suffix;
    final String checksum, mojangMappedChecksum; // Used in tests
    private final List<String> supportedVersions;

    CommandAPIVersion(@NotNull String version, @NotNull String checksum, @Nullable String mojangMappedChecksum, @NotNull String suffix, @NotNull List<String> supportedVersions) {
        this.version = Objects.requireNonNull(version, "Version is null.");
        this.checksum = Objects.requireNonNull(checksum, "Checksum is null.");
        this.mojangMappedChecksum = mojangMappedChecksum;
        this.suffix = Objects.requireNonNull(suffix, "Suffix is null.");
        this.supportedVersions = Objects.requireNonNull(supportedVersions, "SupportedVersions is null.");
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
        String checksum = MojangMappingsHandler.isMojangMapped() ? this.mojangMappedChecksum : this.checksum;
        Preconditions.checkArgument(checksum != null, "CommandAPI " + this.version + " doesn't support Mojang mappings.");
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
