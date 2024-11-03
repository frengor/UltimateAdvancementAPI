package com.fren_gor.ultimateAdvancementAPI.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CompositeMapTest {

    private Map<Integer, Integer> map;

    @BeforeEach
    void setUp() {
        map = CompositeMap.of(Map.of(0, 1, 2, 3), Map.of(4, 5, 6, 7), Map.of(2, -1, 4, -2));
    }

    @Test
    void containsKeyTest() {
        for (int i = 0; i <= 6; i += 2) {
            assertTrue(map.containsKey(i));
        }
        assertFalse(map.containsKey(1));
    }

    @Test
    void containsValueTest() {
        for (int i = 1; i <= 7; i += 2) {
            assertTrue(map.containsValue(i));
        }
        assertTrue(map.containsValue(-1));
        assertTrue(map.containsValue(-2));
        assertFalse(map.containsValue(4));
    }

    @Test
    void getTest() {
        Integer i1 = map.get(0);
        assertNotNull(i1);
        assertEquals(1, i1);

        Integer i2 = map.get(6);
        assertNotNull(i1);
        assertEquals(7, i2);

        Integer i3 = map.get(2);
        assertNotNull(i3);
        assertEquals(3, i3);

        assertNull(map.get(-1));
    }

    @Test
    void keySetTest() {
        // Coping into two new sets is necessary since the equals method requires them to be the same class to work
        Set<Integer> actual = new HashSet<>(map.keySet());
        Set<Integer> expected = new HashSet<>(Set.of(0, 2, 4, 6));
        assertEquals(expected, actual);
    }

    @Test
    void valuesTest() {
        // Coping into two new lists is necessary since the equals method requires them to be the same class to work
        List<Integer> actual = new ArrayList<>(map.values());
        List<Integer> expected = new ArrayList<>(List.of(1, 3, 5, 7, -1, -2));

        // Sort them so that element order doesn't count
        Collections.sort(actual);
        Collections.sort(expected);

        assertEquals(expected, actual);
    }

    @Test
    void entrySetTest() {
        // Coping into two new lists is necessary since the equals method requires them to be the same class to work
        List<Entry<Integer, Integer>> actual = new ArrayList<>(map.entrySet());
        List<Entry<Integer, Integer>> expected = new ArrayList<>(List.of(
                Map.entry(0, 1),
                Map.entry(2,3),
                Map.entry(4, 5),
                Map.entry(6, 7),
                Map.entry(2, -1),
                Map.entry(4, -2)
        ));

        Comparator<Entry<Integer, Integer>> comparator = (e1, e2) -> {
            int comp = e1.getKey() - e2.getKey();
            if (comp == 0) {
                return e1.getValue() - e2.getValue();
            }
            return comp;
        };

        // Sort them so that element order doesn't count
        actual.sort(comparator);
        expected.sort(comparator);

        assertEquals(expected, actual);
    }
}
