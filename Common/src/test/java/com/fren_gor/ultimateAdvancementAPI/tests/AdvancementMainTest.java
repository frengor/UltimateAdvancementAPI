package com.fren_gor.ultimateAdvancementAPI.tests;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidVersionException;
import org.bukkit.craftbukkit.notMocked99_0_R3.NotMockedServerMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(UAAPIExtension.class)
public class AdvancementMainTest {

    @Test
    @MockedServerClass(serverClass = NotMockedServerMock.class)
    void loadingWithUnsupportedVersion() {
        MockPlugin pl = MockBukkit.createMockPlugin();
        AdvancementMain main = new AdvancementMain(pl);
        assertThrows(InvalidVersionException.class, main::load);
        assertThrows(InvalidVersionException.class, main::enableInMemory);
        assertThrows(InvalidVersionException.class, main::disable);
    }
}
