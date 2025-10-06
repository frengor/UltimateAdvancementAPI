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
    v9_3_0("9.3.0",
            "commandapi-bukkit-shade",
            null,
            "K9cYHoWiGLP8z+cyYb5NIarVE9fzMeJRqHu1UJ+Li/U=",
            null,
            "9_3_0",
            List.of(
                    // Versions without a mojang-mapped jar
                    "v1_15_R1",
                    "v1_16_R1",
                    "v1_16_R2",
                    "v1_16_R3"
            )
    ),
    v9_7_0("9.7.0",
            "commandapi-bukkit-shade",
            "commandapi-bukkit-shade-mojang-mapped",
            "q1XHz7oYfdoeous1MOQoP7zopfjdnxMh9Za1ToN0x2s=",
            "TFSpIzREfb8ChAkZ/sl1Nmk/ACUpuNk5c4PARCFlj80=",
            "9_7_0",
            List.of(
                    "v1_17_R1",
                    "v1_18_R1",
                    "v1_18_R2",
                    "v1_19_R1",
                    "v1_19_R2",
                    "v1_19_R3"
            )
    ),
    v10_1_2("10.1.2",
            "commandapi-bukkit-shade",
            "commandapi-bukkit-shade-mojang-mapped",
            "vwfO0UPX+wKoOgbv0SLS6YmzBjf0Inkuh6e00LKcekg=",
            "K1BBUo+b2ZReI2qsuSlWAGmpG0WAyEfDcZOnZS7u6z4=",
            "10_1_2",
            List.of(
                    "v1_20_R1",
                    "v1_20_R2",
                    "v1_20_R3",
                    "v1_20_R4",
                    "v1_21_R1",
                    "v1_21_R2",
                    "v1_21_R3",
                    "v1_21_R4"
            )
    ),
    LATEST("11.0.0",
            "commandapi-spigot-shade",
            "commandapi-paper-shade",
            "Fj53byvXMUNTOWVGshgqXxcvCz5Q7K9Ipk9LHlv+FNM=",
            "g0vXatzD0v726X589IJRv4LsUaANZsS5s7WQXMvWKds=",
            "11_0_0",
            List.of(
                    "v1_21_R5",
                    "v1_21_R6"
            )
    );

    private final String version, suffix;
    final String artifactId, mojangMappedArtifactId, checksum, mojangMappedChecksum; // Used in tests
    private final List<String> supportedVersions;

    CommandAPIVersion(@NotNull String version,
                      @NotNull String artifactId,
                      @Nullable String mojangMappedArtifactId,
                      @NotNull String checksum,
                      @Nullable String mojangMappedChecksum,
                      @NotNull String suffix,
                      @NotNull List<String> supportedVersions) {
        this.version = Objects.requireNonNull(version, "Version is null.");
        this.artifactId = Objects.requireNonNull(artifactId, "Artifact id is null.");
        this.mojangMappedArtifactId = mojangMappedArtifactId;
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
     * Gets the artifact id of the <a href="https://github.com/JorelAli/CommandAPI">CommandAPI</a>.
     *
     * @return The artifact id of the <a href="https://github.com/JorelAli/CommandAPI">CommandAPI</a>.
     */
    public String getArtifactId() {
        String artifactId = MojangMappingsHandler.isMojangMapped() ? this.mojangMappedArtifactId : this.artifactId;
        Preconditions.checkArgument(artifactId != null, "CommandAPI " + this.version + " doesn't support Mojang mappings.");
        return artifactId;
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
     * Gets the suffix of the package that uses the <a href="https://github.com/JorelAli/CommandAPI">CommandAPI</a>.
     *
     * @return The suffix of the package that uses the <a href="https://github.com/JorelAli/CommandAPI">CommandAPI</a>.
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
