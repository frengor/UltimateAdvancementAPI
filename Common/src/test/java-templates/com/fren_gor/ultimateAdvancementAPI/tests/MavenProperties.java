package com.fren_gor.ultimateAdvancementAPI.tests;

/**
 * Maven properties as defined in the pom.xml
 */
public final class MavenProperties {

    /**
     * Version of the API as defined in the pom.xml
     */
    public static final String API_VERSION = "${project.version}";

    private MavenProperties() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
