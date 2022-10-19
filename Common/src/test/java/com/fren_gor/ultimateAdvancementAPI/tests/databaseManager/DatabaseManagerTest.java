package com.fren_gor.ultimateAdvancementAPI.tests.databaseManager;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.tests.Utils;
import org.bukkit.craftbukkit.mocked0_0_R1.VersionedServerMock;
import org.junit.After;
import org.junit.Before;

public class DatabaseManagerTest {

    private ServerMock server;
    private AdvancementMain advancementMain;
    private DatabaseManager databaseManager;

    @Before
    public void setUp() throws Exception {
        server = MockBukkit.mock(new VersionedServerMock());
        advancementMain = Utils.newAdvancementMain(MockBukkit.createMockPlugin("testPlugin"));
        databaseManager = new DatabaseManager(advancementMain);
    }

    @After
    public void tearDown() throws Exception {
        if (databaseManager != null) {
            databaseManager.close();
            databaseManager = null;
        }
        advancementMain = null;
        MockBukkit.unmock();
        server = null;
    }

    // TODO: Add more tests
}
