package com.fren_gor.ultimateAdvancementAPI.commands;

import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import net.byteflux.libby.logging.adapters.JDKLogAdapter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class CommandAPIVersionTest {

    @Rule
    public TemporaryFolder tmpFolder = TemporaryFolder.builder().assureDeletion().build();

    @Test
    public void testDownloads() throws Exception {
        boolean failed = false;

        // Try to download every CommandAPI version. This is mainly used to catch checksum errors
        for (CommandAPIVersion version : CommandAPIVersion.values()) {
            Logger logger = Logger.getLogger(getClass().getSimpleName() + "-" + version.getVersion());
            MockLibraryManager manager = new MockLibraryManager(logger, version);

            // Don't fail fast in order to catch every checksum error
            failed = downloadCommandAPI(manager, logger, version) || failed;
        }

        if (failed) {
            fail("Failed to download CommandAPI!");
        }
    }

    private boolean downloadCommandAPI(LibraryManager manager, Logger logger, CommandAPIVersion version) {
        boolean failed = false;
        manager.addMavenCentral();

        Library commandAPI = Library.builder()
                .groupId("dev{}jorel")
                .artifactId("commandapi-bukkit-shade")
                .version(version.getVersion())
                .checksum(version.checksum)
                .build();

        try {
            assertTrue(Files.exists(manager.downloadLibrary(commandAPI)));
        } catch (Exception e) {
            failed = true;
            logger.severe("Failed to download " + commandAPI.toString());
            // e.printStackTrace();
        }

        if (version.mojangMappedChecksum != null) {
            Library commandAPIMojangMapped = Library.builder()
                    .groupId("dev{}jorel")
                    .artifactId("commandapi-bukkit-shade-mojang-mapped")
                    .version(version.getVersion())
                    .checksum(version.mojangMappedChecksum)
                    .build();

            try {
                assertTrue(Files.exists(manager.downloadLibrary(commandAPIMojangMapped)));
            } catch (Exception e) {
                failed = true;
                logger.severe("Failed to download " + commandAPIMojangMapped.toString());
                // e.printStackTrace();
            }
        }
        return failed;
    }

    private final class MockLibraryManager extends LibraryManager {
        public MockLibraryManager(Logger logger, CommandAPIVersion version) throws IOException {
            super(new JDKLogAdapter(logger), tmpFolder.newFolder(version.getClasspathSuffix()).toPath(), ".libs");
        }

        @Override
        protected void addToClasspath(Path file) {
            throw new UnsupportedOperationException();
        }
    }
}
