package com.fren_gor.ultimateAdvancementAPI.tests.nms;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ListSet;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ListSetTest {

    @Test
    public void notNullAdditionTest() {
        Set<Integer> original = Set.of(0, 1, 5, 6, 7, -5);
        ListSet<Integer> toTest = new ListSet<>(original);

        // Check that they are equals
        assertTrue(original.containsAll(toTest));
        assertTrue(toTest.containsAll(original));
    }

    @Test
    public void nullAdditionTest() {
        Set<Integer> original = Set.of(0, 1, 5, 6, 7, -5);
        Set<Integer> withNull = new HashSet<>(original);
        withNull.add(null); // List.of doesn't accept null elements
        ListSet<Integer> toTest = new ListSet<>(withNull);

        // Check that they are equals
        assertTrue(original.containsAll(toTest));
        assertTrue(toTest.containsAll(original));
    }

    @Test
    public void equalsTest() {
        Set<Integer> original = Set.of(0, 1, 5, 6, 7, -5);
        ListSet<Integer> toTest1 = new ListSet<>(original);
        ListSet<Integer> toTest2 = new ListSet<>(original);
        assertEquals(toTest1, toTest2);
    }

    @Test
    public void notEqualsTest() {
        Set<Integer> set1 = Set.of(0, 1, 5, 6, 7, -5);
        ListSet<Integer> toTest1 = new ListSet<>(set1);
        Set<Integer> set2 = Set.of(0, 1, 5, 6, 7, 9);
        ListSet<Integer> toTest2 = new ListSet<>(set2);
        assertNotEquals(toTest1, toTest2);
    }

    @Test
    public void sizeTest() {
        Set<Integer> original = Set.of(0, 1, 5, 6, 7, -5);
        ListSet<Integer> toTest = new ListSet<>(original);

        assertEquals(original.size(), toTest.size());
    }

    @Test
    public void iteratorTest() {
        Set<Integer> original = Set.of(0, 1, 5, 6, 7, -5);
        ListSet<Integer> toTest = new ListSet<>(original);

        int counter = 0;
        for (Integer i : toTest) {
            counter++;
            assertTrue(original.contains(i));
        }
        assertEquals(original.size(), counter);
    }
}
