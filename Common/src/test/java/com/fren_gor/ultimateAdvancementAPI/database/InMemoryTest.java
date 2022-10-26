package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.database.impl.InMemory;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTest {

    private InMemory db;

    @BeforeEach
    public void setUp() throws Exception {
        db = new InMemory(Logger.getLogger("InMemoryTest"));
        db.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        db.close();
        db = null;
    }

    @Test
    public void inMemoryTest() throws Exception {
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
