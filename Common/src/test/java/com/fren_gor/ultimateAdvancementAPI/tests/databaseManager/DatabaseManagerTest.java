package com.fren_gor.ultimateAdvancementAPI.tests.databaseManager;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.tests.InterfaceImplementer;
import com.fren_gor.ultimateAdvancementAPI.tests.Utils;
import org.bukkit.Bukkit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

public class DatabaseManagerTest {

    private MockedStatic<Bukkit> bukkit;
    private AdvancementMain advancementMain;

    @Before
    public void setUp() throws Exception {
        bukkit = Utils.mockServer();
        advancementMain = Utils.newAdvancementMain(InterfaceImplementer.newFakePlugin("testPlugin"));
    }

    @After
    public void tearDown() throws Exception {
        advancementMain = null;
        bukkit.close();
        bukkit = null;
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
