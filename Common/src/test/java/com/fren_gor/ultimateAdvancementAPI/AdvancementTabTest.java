package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.ultimateAdvancementAPI.tests.AutoInject;
import com.fren_gor.ultimateAdvancementAPI.tests.UAAPIExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.MockBukkit;

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
