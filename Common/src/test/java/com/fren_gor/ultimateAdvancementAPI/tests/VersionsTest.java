package com.fren_gor.ultimateAdvancementAPI.tests;

import com.fren_gor.ultimateAdvancementAPI.util.Versions;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.server_version.FakeServer;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static com.fren_gor.ultimateAdvancementAPI.util.Versions.getApiVersion;
import static com.fren_gor.ultimateAdvancementAPI.util.Versions.getNMSVersionsList;
import static com.fren_gor.ultimateAdvancementAPI.util.Versions.getNMSVersionsRange;
import static com.fren_gor.ultimateAdvancementAPI.util.Versions.getSupportedNMSVersions;
import static com.fren_gor.ultimateAdvancementAPI.util.Versions.removeInitialV;
import static org.junit.Assert.*;

public class VersionsTest {

    @Test
    public void removeInitialVTest() {
        assertEquals("1_15_R1", removeInitialV("v1_15_R1"));
        assertEquals("1_15_R1", removeInitialV("1_15_R1"));
        assertEquals("", removeInitialV(""));
        assertEquals("v1_15_R1", removeInitialV("vv1_15_R1"));
        assertNull(removeInitialV(null));
    }

    @Test
    public void getNMSVersionTest() {
        try (MockedStatic<Bukkit> bukkitMock = Mockito.mockStatic(Bukkit.class)) {
            bukkitMock.when(Bukkit::getServer).thenReturn(new FakeServer());
            assertSame("Mock failed", Bukkit.getServer().getClass(), FakeServer.class);
            assertEquals("server_version", Versions.getNMSVersion());
        }
    }

    @Test
    public void notNullTest() {
        assertNotNull(getApiVersion());
        assertNotNull(getSupportedNMSVersions());
        for (String nms : getSupportedNMSVersions()) {
            assertNotNull(nms);
            assertNotNull(getNMSVersionsList(nms));
            assertNotNull(getNMSVersionsRange(nms));
        }
    }
}
