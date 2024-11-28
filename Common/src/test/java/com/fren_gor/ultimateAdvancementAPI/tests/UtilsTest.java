package com.fren_gor.ultimateAdvancementAPI.tests;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(UAAPIExtension.class)
public class UtilsTest {
    @AutoInject
    private AdvancementMain main;

    @Test
    void libraryManagerMockingTest() {
        assertDoesNotThrow(() -> main.getLibbyManager().addMavenCentral());
        assertThrows(UnsupportedOperationException.class, () -> main.getLibbyManager().addJitPack());
        assertThrows(UnsupportedOperationException.class, () -> main.getLibbyManager().loadLibrary(null));
    }
}
