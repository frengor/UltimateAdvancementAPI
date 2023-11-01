package com.fren_gor.ultimateAdvancementAPI.tests;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidVersionException;
import org.bukkit.craftbukkit.notMocked99_0_R3.NotMockedServerMock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdvancementMainTest {

    @Test
    void loadingWithUnsupportedVersion() {
        MockBukkit.mock(new NotMockedServerMock());
        MockBukkit.ensureMocking();
        try {
            MockPlugin pl = MockBukkit.createMockPlugin();
            AdvancementMain main = new AdvancementMain(pl);
            assertThrows(InvalidVersionException.class, main::load);
            assertThrows(InvalidVersionException.class, main::enableInMemory);
            assertThrows(InvalidVersionException.class, main::disable);
        } finally {
            MockBukkit.unmock();
        }
    }
}
