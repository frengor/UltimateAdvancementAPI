package com.fren_gor.ultimateAdvancementAPI.database;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.fren_gor.ultimateAdvancementAPI.database.impl.InMemory;
import com.fren_gor.ultimateAdvancementAPI.tests.Utils;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTest {

    private ServerMock server;
    private InMemory db;

    @BeforeEach
    void init() throws Exception {
        server = Utils.mockServer();
        db = new InMemory(Logger.getLogger("InMemoryTest"));
        db.setUp();
    }

    @AfterEach
    void tearDown() throws Exception {
        db.close();
        db = null;
        MockBukkit.unmock();
        server = null;
    }

    @Test
    void inMemoryTest() throws Exception {
        UUID uuid = UUID.randomUUID();
        var res = db.loadOrRegisterPlayer(uuid, "Dummy");
        assertTrue(res.getKey().contains(uuid));
        assertTrue(res.getKey().everyMemberMatch(u -> u.equals(uuid)));
        assertTrue(res.getValue()); // This is a new team

        AdvancementKey key = new AdvancementKey("test", "adv");
        db.updateAdvancement(key, res.getKey().getTeamId(), 10);

        boolean executedAtLeastOnce = false;
        for (var entry : db.getTeamAdvancements(res.getKey().getTeamId()).entrySet()) {
            executedAtLeastOnce = true;
            assertEquals(key, entry.getKey());
            assertEquals(10, entry.getValue());
        }
        assertTrue(executedAtLeastOnce);
    }
}
