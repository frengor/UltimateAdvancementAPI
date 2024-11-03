package com.fren_gor.ultimateAdvancementAPI.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class CompositeCollectionTest {

    private Collection<Integer> coll;

    @BeforeEach
    void setUp() {
        coll = CompositeCollection.of(List.of(0, 1, 2), List.of(3, 4, 5), List.of(1, 3));
    }

    @Test
    void containTest() {
        for (int i = 0; i < 5; i++) {
            assertTrue(coll.contains(i));
        }
        assertTrue(coll.containsAll(List.of(0, 1, 2, 3, 4, 5)));
        assertFalse(coll.contains(6));
        assertFalse(coll.containsAll(List.of(0, 1, 2, 6, 5)));
    }

    @Test
    void iteratorTest() {
        checkIterator(coll, new int[]{0, 1, 2, 3, 4, 5, 1, 3});
        checkIterator(CompositeCollection.of(List.of(), List.of(0, 1, 2), List.of(1, 3)), new int[]{0, 1, 2, 1, 3});
        checkIterator(CompositeCollection.of(List.of(0, 1, 2), List.of(), List.of(1, 3)), new int[]{0, 1, 2, 1, 3});
        checkIterator(CompositeCollection.of(List.of(0, 1, 2), List.of(1, 3), List.of()), new int[]{0, 1, 2, 1, 3});
    }

    private static void checkIterator(Collection<Integer> coll, int[] expected) {
        int[] values = new int[expected.length];
        int i = 0;
        Iterator<Integer> it = coll.iterator();
        while (it.hasNext()) {
            values[i++] = it.next();
        }
        assertThrows(NoSuchElementException.class, it::next);
        assertArrayEquals(expected, values);
    }

    @Test
    void toArrayTest() {
        assertArrayEquals(new Integer[]{0, 1, 2, 3, 4, 5, 1, 3}, coll.toArray());
    }

    @Test
    void toArrayWithArgumentTest() {
        assertArrayEquals(new Integer[]{0, 1, 2, 3, 4, 5, 1, 3}, coll.toArray(new Integer[0]));
        assertArrayEquals(new Integer[]{0, 1, 2, 3, 4, 5, 1, 3}, coll.toArray(new Integer[8]));
        Integer[] arr = new Integer[10];
        Arrays.fill(arr, -1);
        assertArrayEquals(new Integer[]{0, 1, 2, 3, 4, 5, 1, 3, null, -1}, coll.toArray(arr));
    }
}
