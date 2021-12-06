package com.fren_gor.ultimateAdvancementAPI.tests;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.*;

public class TempUserMetadataTest {

    private static Class<?> TempUserMetadataClass;

    private static Constructor<?> constructor;
    private static final Map<String, Method> methods = new HashMap<>();

    private static MockedStatic<Bukkit> bukkitMock;

    private static final Map<UUID, Player> players = new HashMap<>();
    private static final int PLAYER_TO_REGISTER = 4;
    private static Object testUserMetadataInstance;

    @BeforeClass
    public static void beforeClass() throws Exception {
        assertTrue("Invalid PLAYER_TO_REGISTER", PLAYER_TO_REGISTER > 1);

        bukkitMock = Mockito.mockStatic(Bukkit.class);
        for (int i = 0; i < PLAYER_TO_REGISTER; i++) {
            UUID uuid = UUID.randomUUID();
            Player pl = InterfaceImplementer.newFakePlayer(uuid);

            bukkitMock.when(() -> Bukkit.getPlayer(uuid)).thenReturn(pl);
            assertEquals("Mock failed", Bukkit.getPlayer(uuid), pl);
            players.put(uuid, pl);
        }
        TempUserMetadataClass = Class.forName("com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager$TempUserMetadata");

        constructor = TempUserMetadataClass.getConstructor(UUID.class);
        constructor.setAccessible(true);
        for (Method m : TempUserMetadataClass.getDeclaredMethods()) {
            m.setAccessible(true);
            methods.put(m.getName(), m);
        }
    }

    @AfterClass
    public static void afterClass() throws Exception {
        bukkitMock.close();
        bukkitMock = null;
        players.clear();
        methods.clear();
        testUserMetadataInstance = null;
        TempUserMetadataClass = null;
        constructor = null;
    }

    @Before
    public void before() throws Exception {
        testUserMetadataInstance = constructor.newInstance(Objects.requireNonNull(players.keySet().iterator().next(), "Couldn't find a player in players map."));
    }

    @After
    public void after() throws Exception {
        testUserMetadataInstance = null;
    }

    @Test
    public void addAutoTest() throws Exception {
        final Method m = getMethod("addAuto");
        int old = 0x0000_ABCD;
        for (int i = 0; i < Character.MAX_VALUE; i++) {
            old = (int) m.invoke(testUserMetadataInstance, old);
            assertEquals(0x0000_ABCD, old & 0xFFFF);
        }
        assertEquals(0xFFFF_ABCD, old);
        int finalOld = old;
        assertThrows(RuntimeException.class, () -> {
            try {
                m.invoke(testUserMetadataInstance, finalOld);
            } catch (ReflectiveOperationException e) {
                throw e.getCause() != null ? e.getCause() : e;
            }
        });
    }

    @Test
    public void addManualTest() throws Exception {
        final Method m = getMethod("addManual");
        int old = 0xABCD_0000;
        for (int i = 0; i < Character.MAX_VALUE; i++) {
            old = (int) m.invoke(testUserMetadataInstance, old);
            assertEquals(0xABCD_0000, old & 0xFFFF_0000);
        }
        assertEquals(0xABCD_FFFF, old);
        int finalOld = old;
        assertThrows(RuntimeException.class, () -> {
            try {
                m.invoke(testUserMetadataInstance, finalOld);
            } catch (ReflectiveOperationException e) {
                throw e.getCause() != null ? e.getCause() : e;
            }
        });
    }

    @Test
    public void removeAutoTest() throws Exception {
        final Method m = getMethod("removeAuto");
        int old = 0xFFFF_ABCD;
        for (int i = 0; i < Character.MAX_VALUE; i++) {
            old = (int) m.invoke(testUserMetadataInstance, old);
            assertEquals(0x0000_ABCD, old & 0xFFFF);
        }
        assertEquals(0x0000_ABCD, old);
        old = (int) m.invoke(testUserMetadataInstance, old);
        assertEquals(0x0000_ABCD, old);
    }

    @Test
    public void removeManualTest() throws Exception {
        final Method m = getMethod("removeManual");
        int old = 0xABCD_FFFF;
        for (int i = 0; i < Character.MAX_VALUE; i++) {
            old = (int) m.invoke(testUserMetadataInstance, old);
            assertEquals(0xABCD_0000, old & 0xFFFF0000);
        }
        assertEquals(0xABCD_0000, old);
        old = (int) m.invoke(testUserMetadataInstance, old);
        assertEquals(0xABCD_0000, old);
    }

    @Test
    public void addRequest() throws Exception {
        final Method m = getMethod("addRequest");
        final Plugin p1 = InterfaceImplementer.newFakePlugin("Test1");
        final Plugin p2 = InterfaceImplementer.newFakePlugin("Test2");
        assertAutoManual(0, 0, p1);
        assertAutoManual(0, 0, p2);
        for (int i = 1; i <= Character.MAX_VALUE; i++) {
            m.invoke(testUserMetadataInstance, p1, true);
            assertAutoManual(i, 0, p1);
            m.invoke(testUserMetadataInstance, p2, true);
            assertAutoManual(i, 0, p2);
        }
        for (int i = 1; i <= Character.MAX_VALUE; i++) {
            m.invoke(testUserMetadataInstance, p1, false);
            assertAutoManual(Character.MAX_VALUE, i, p1);
            m.invoke(testUserMetadataInstance, p2, false);
            assertAutoManual(Character.MAX_VALUE, i, p2);
        }
        assertAutoManual(0, 0, InterfaceImplementer.newFakePlugin("Another")); // Just to be sure
        assertThrows(RuntimeException.class, () -> {
            try {
                m.invoke(testUserMetadataInstance, p1, true);
            } catch (ReflectiveOperationException e) {
                throw e.getCause() != null ? e.getCause() : e;
            }
        });
        assertThrows(RuntimeException.class, () -> {
            try {
                m.invoke(testUserMetadataInstance, p1, false);
            } catch (ReflectiveOperationException e) {
                throw e.getCause() != null ? e.getCause() : e;
            }
        });
    }

    @Test
    public void removeRequest() throws Exception {
        final Method add = getMethod("addRequest");
        final Method m = getMethod("removeRequest");
        final Plugin p1 = InterfaceImplementer.newFakePlugin("Test1");
        final Plugin p2 = InterfaceImplementer.newFakePlugin("Test2");

        // Prepare tests, addRequest method is tested above
        for (int i = 0; i < Character.MAX_VALUE; i++) {
            add.invoke(testUserMetadataInstance, p1, true);
            add.invoke(testUserMetadataInstance, p1, false);
            add.invoke(testUserMetadataInstance, p2, true);
            add.invoke(testUserMetadataInstance, p2, false);
        }
        assertAutoManual(Character.MAX_VALUE, Character.MAX_VALUE, p1);
        assertAutoManual(Character.MAX_VALUE, Character.MAX_VALUE, p2);
        for (int i = Character.MAX_VALUE - 1; i >= 0; i--) {
            m.invoke(testUserMetadataInstance, p1, true);
            assertAutoManual(i, Character.MAX_VALUE, p1);
            m.invoke(testUserMetadataInstance, p2, true);
            assertAutoManual(i, Character.MAX_VALUE, p2);
        }
        for (int i = Character.MAX_VALUE - 1; i >= 0; i--) {
            m.invoke(testUserMetadataInstance, p1, false);
            assertAutoManual(0, i, p1);
            m.invoke(testUserMetadataInstance, p2, false);
            assertAutoManual(0, i, p2);
        }
        m.invoke(testUserMetadataInstance, p1, true);
        assertAutoManual(0, 0, p1);
        m.invoke(testUserMetadataInstance, p1, false);
        assertAutoManual(0, 0, p1);
    }

    private void assertAutoManual(int auto, int manual, @NotNull Plugin pl) throws Exception {
        final Method gA = getMethod("getAuto");
        final Method gM = getMethod("getManual");
        Assert.assertEquals(auto, gA.invoke(testUserMetadataInstance, pl));
        Assert.assertEquals(manual, gM.invoke(testUserMetadataInstance, pl));
    }

    private Method getMethod(String method) {
        Method m = methods.get(method);
        if (m == null) {
            Assert.fail("Couldn't find method '" + method + '\'');
        }
        return m;
    }

}
