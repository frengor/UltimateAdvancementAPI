package com.fren_gor.ultimateAdvancementAPI.database;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.fren_gor.ultimateAdvancementAPI.tests.Utils;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TeamProgressionTest {

    private ServerMock server;

    @Before
    public void setUp() throws Exception {
        server = Utils.mockServer();
    }

    @After
    public void tearDown() throws Exception {
        MockBukkit.unmock();
        server = null;
    }

    @Test
    public void creationTest() {
        PlayerMock player1 = server.addPlayer();
        PlayerMock player2 = server.addPlayer();
        PlayerMock player3 = server.addPlayer();
        player1.disconnect();
        player2.disconnect();
        player3.disconnect();
        TeamProgression pro = TeamProgressionFactory.createTeamProgression(0, player1.getUniqueId());
        assertEquals(0, pro.getTeamId());
        assertEquals(1, pro.getSize());
        assertTrue(pro.contains(player1));
        assertTrue(pro.contains(player1.getUniqueId()));
        assertFalse(pro.contains(player2.getUniqueId()));
        assertFalse(pro.contains(player2.getUniqueId()));
        assertFalse(pro.isValid());
        pro.inCache.set(true);
        assertTrue(pro.isValid());
        pro.addMember(player2.getUniqueId());
        assertTrue(pro.contains(player1));
        assertTrue(pro.contains(player1.getUniqueId()));
        assertTrue(pro.contains(player2));
        assertTrue(pro.contains(player2.getUniqueId()));
        assertFalse(pro.contains(player3));
        assertFalse(pro.contains(player3.getUniqueId()));
        assertTrue(List.of(player1.getUniqueId(), player2.getUniqueId(), player3.getUniqueId()).contains(pro.getAMember()));
    }

    @Test
    public void advancementProgressionTest() {
        AdvancementKey key1 = new AdvancementKey("namespace1", "key1");
        AdvancementKey key2 = new AdvancementKey("namespace1", "key2");
        AdvancementKey key3 = new AdvancementKey("namespace2", "key3");

        TeamProgression pro = TeamProgressionFactory.createTeamProgression(0, Map.of(key1, 10, key2, 5), Collections.emptyList());

        assertEquals(10, pro.getRawProgression(key1));
        assertEquals(5, pro.getRawProgression(key2));
        assertEquals(0, pro.getRawProgression(key3));
    }
}
