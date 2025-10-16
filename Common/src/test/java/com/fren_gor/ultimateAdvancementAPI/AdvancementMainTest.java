package com.fren_gor.ultimateAdvancementAPI;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidVersionException;
import com.fren_gor.ultimateAdvancementAPI.tests.MockedServerClass;
import com.fren_gor.ultimateAdvancementAPI.tests.NoAdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.tests.UAAPIExtension;
import org.bukkit.craftbukkit.notMocked99_0_R3.NotMockedServerMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(UAAPIExtension.class)
public class AdvancementMainTest {

    @Test
    @MockedServerClass(serverClass = NotMockedServerMock.class)
    @NoAdvancementMain
    void loadingWithUnsupportedVersion() {
        MockPlugin pl = MockBukkit.createMockPlugin();
        AdvancementMain main = new AdvancementMain(pl);
        assertThrows(InvalidVersionException.class, main::load);
        assertThrows(InvalidVersionException.class, () -> main.enable(() -> null));
        assertThrows(InvalidVersionException.class, main::disable);
    }
}
