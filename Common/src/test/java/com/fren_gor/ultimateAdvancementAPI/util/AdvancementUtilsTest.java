package com.fren_gor.ultimateAdvancementAPI.util;

import com.fren_gor.ultimateAdvancementAPI.tests.UAAPIExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(UAAPIExtension.class)
public class AdvancementUtilsTest {

    @Test
    void intersperseTest() {
        assertTrue(AdvancementUtils.intersperse(List.of(), 42).isEmpty());
        assertEquals(List.of(1), AdvancementUtils.intersperse(List.of(1), 42));
        assertEquals(List.of(1, 42, 2), AdvancementUtils.intersperse(List.of(1, 2), 42));
        assertEquals(List.of(1, 42, 2, 42, 3), AdvancementUtils.intersperse(List.of(1, 2, 3), 42));
    }
}
