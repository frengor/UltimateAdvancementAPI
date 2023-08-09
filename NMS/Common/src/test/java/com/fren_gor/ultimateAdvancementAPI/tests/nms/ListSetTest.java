package com.fren_gor.ultimateAdvancementAPI.tests.nms;

import com.fren_gor.ultimateAdvancementAPI.nms.util.ListSet;
import org.junit.jupiter.api.AssertionFailureBuilder;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void toArrayTest() {
        Integer[] array = {0, 1, 5, 6, 7, -5};
        Set<Integer> original = Set.of(array);
        ListSet<Integer> toTest = new ListSet<>(original);

        assertSameElements(array, toTest.toArray());
    }

    @Test
    void toArrayWithArg0Test() {
        Integer[] array = {0, 1, 5, 6, 7, -5};
        Set<Integer> original = Set.of(array);
        ListSet<Integer> toTest = new ListSet<>(original);

        Integer[] arr = toTest.toArray(new Integer[0]);
        assertNotSame(array, arr);
        assertSameElements(array, arr);
    }

    @Test
    void toArrayWithArgLessSizeTest() {
        Integer[] array = {0, 1, 5, 6, 7, -5};
        Set<Integer> original = Set.of(array);
        ListSet<Integer> toTest = new ListSet<>(original);

        Integer[] arr = toTest.toArray(new Integer[3]);
        assertNotSame(array, arr);
        assertSameElements(array, arr);
    }

    @Test
    void toArrayWithArgCorrectSizeTest() {
        Integer[] array = {0, 1, 5, 6, 7, -5};
        Set<Integer> original = Set.of(array);
        ListSet<Integer> toTest = new ListSet<>(original);

        Integer[] arr = toTest.toArray(new Integer[array.length]);
        assertNotSame(array, arr);
        assertSameElements(array, arr);
    }

    @Test
    void toArrayWithArgLargerSizeTest() {
        Integer[] array = {0, 1, 5, 6, 7, -5};
        Set<Integer> original = Set.of(array);
        ListSet<Integer> toTest = new ListSet<>(original);

        Integer[] arr = new Integer[array.length + 5];
        Arrays.fill(arr, null);
        arr[array.length] = 0; // Make sure this element isn't null before calling toArray()
        assertSame(arr, toTest.toArray(arr));
        assertNull(arr[array.length]);
        assertSameElements(array, arr, false);
    }

    private void assertSameElements(Object[] arr1, Object[] arr2) {
        assertSameElements(arr1, arr2, true);
    }

    private void assertSameElements(Object[] arr1, Object[] arr2, boolean checkLength) {
        if (checkLength) {
            assertEquals(arr1.length, arr2.length, "Arrays have different lengths");
        }

        Object[] arr2copy = Arrays.copyOf(arr2, arr2.length); // Don't directly modify arr2
        loop:
        for (Object t1 : arr1) {
            if (t1 == null) {
                fail("An element of " + Arrays.toString(arr1) + " is null");
            }
            for (int i = 0; i < arr2copy.length; i++) {
                Object t2 = arr2copy[i];
                if (t2 != null && t1.equals(t2)) {
                    arr2copy[i] = null;
                    continue loop;
                }
            }
            AssertionFailureBuilder.assertionFailure().actual(arr2).expected(arr1).buildAndThrow();
        }
    }
}
