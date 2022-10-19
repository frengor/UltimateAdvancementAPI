package com.fren_gor.ultimateAdvancementAPI.tests.databaseManager;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.tests.Utils;
import org.bukkit.craftbukkit.serverVersion1_19_R1.VersionedServerMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DatabaseManagerTest {

    private ServerMock server;
    private AdvancementMain advancementMain;

    @Before
    public void setUp() throws Exception {
        server = MockBukkit.mock(new VersionedServerMock());
        advancementMain = Utils.newAdvancementMain(MockBukkit.createMockPlugin("testPlugin"));
    }

    @After
    public void tearDown() throws Exception {
        advancementMain = null;
        MockBukkit.unmock();
        server = null;
    }

    @Test
    public void newInMemorytest() throws Exception {
        DatabaseManager inMemory = getNewManager();
        inMemory.close();
    }

    // TODO: Add more tests

    private DatabaseManager getNewManager() throws Exception {
        return new DatabaseManager(advancementMain);
    }
}
