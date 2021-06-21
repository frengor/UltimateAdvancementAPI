package com.fren_gor.ultimateAdvancementAPI.tests;

import org.bukkit.Bukkit;
import org.junit.AfterClass;
import org.junit.Assert;
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

    private static final Map<UUID, FakePlayer> players = new HashMap<>();
    private static final int PLAYER_TO_REGISTER = 4;
    private static Object testUserMetadataInstance;

    @BeforeClass
    public static void beforeClass() throws Exception {
        assert PLAYER_TO_REGISTER > 1 : "Invalid PLAYER_TO_REGISTER";

        bukkitMock = Mockito.mockStatic(Bukkit.class);
        for (int i = 0; i < PLAYER_TO_REGISTER; i++) {
            UUID uuid = UUID.randomUUID();
            FakePlayer pl = new FakePlayer(uuid);
            bukkitMock.when(() -> Bukkit.getPlayer(uuid)).thenReturn(pl);
            assertEquals(Bukkit.getPlayer(uuid), pl);
            players.put(uuid, pl);
        }
        TempUserMetadataClass = Class.forName("com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager$TempUserMetadata");

        constructor = TempUserMetadataClass.getConstructor(UUID.class);
        constructor.setAccessible(true);
        for (Method m : TempUserMetadataClass.getDeclaredMethods()) {
            m.setAccessible(true);
            methods.put(m.getName(), m);
        }
        testUserMetadataInstance = constructor.newInstance(Objects.requireNonNull(players.keySet().iterator().next(), "Couldn't find a player in players map."));
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

    private Method getMethod(String method) {
        Method m = methods.get(method);
        if (m == null) {
            Assert.fail("Couldn't find method '" + method + '\'');
        }
        return m;
    }

}
