package com.fren_gor.ultimateAdvancementAPI;

import be.seeseemelk.mockbukkit.MockBukkit;
import com.fren_gor.ultimateAdvancementAPI.tests.AutoInject;
import com.fren_gor.ultimateAdvancementAPI.tests.UAAPIExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(UAAPIExtension.class)
public class AdvancementTabTest {
    @AutoInject
    private AdvancementMain main;

    @Test
    void disableBeforeRegisteringAdvsTest() {
        String nameapace = "a-namespace";
        AdvancementTab tab = main.createAdvancementTab(MockBukkit.createMockPlugin(),nameapace,"background/texture");
        main.unregisterAdvancementTab(nameapace);
    }
}
