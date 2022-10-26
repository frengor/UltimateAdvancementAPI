package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.database.impl.InMemory;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

public class InMemoryTest {

    @Test
    public void inMemoryTest() throws Exception {
        InMemory m = new InMemory(Logger.getLogger("InMemoryTest"));
        m.setUp();
        m.clearUpTeams();
        m.close();
    }
}
