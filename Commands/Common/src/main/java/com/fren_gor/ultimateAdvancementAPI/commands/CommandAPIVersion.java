package com.fren_gor.ultimateAdvancementAPI.commands;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public enum CommandAPIVersion {
    v5_12("5.12", "lCfbzi9Nf/RzQl6GvSGU/DqjuxkGDWavTNXt6Vms50o=", "5_12", List.of("v1_15_R1", "v1_16_R1", "v1_16_R2", "v1_16_R3")),
    v6_3_1("6.3.1", "nQX6iAp9BHZPY+fmAFhcSjjVHdljk5GGGBNUpeExysc=", "6_3_1", List.of("v1_17_R1"));

    private final String version, checksum, suffix;
    private final List<String> supportedVersions;

    CommandAPIVersion(@NotNull String version, @NotNull String checksum, @NotNull String suffix, @NotNull List<String> supportedVersions) {
        this.version = version;
        this.checksum = checksum;
        this.suffix = suffix;
        this.supportedVersions = supportedVersions;
    }

    @NotNull
    public String getVersion() {
        return version;
    }

    @NotNull
    public String getChecksum() {
        return checksum;
    }

    @NotNull
    public String getClasspathSuffix() {
        return suffix;
    }

    @Unmodifiable
    @NotNull
    public List<String> getSupportedVersions() {
        return supportedVersions;
    }

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
