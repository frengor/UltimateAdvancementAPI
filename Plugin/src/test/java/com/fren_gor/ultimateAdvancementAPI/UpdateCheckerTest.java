package com.fren_gor.ultimateAdvancementAPI;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.fren_gor.ultimateAdvancementAPI.UpdateChecker._shouldUpdate;
import static com.fren_gor.ultimateAdvancementAPI.UpdateChecker.extractBeforeHyphen;
import static com.fren_gor.ultimateAdvancementAPI.UpdateChecker.getChannel;
import static com.fren_gor.ultimateAdvancementAPI.UpdateChecker.isGreater;
import static com.fren_gor.ultimateAdvancementAPI.UpdateChecker.shouldUpdate;
import static org.junit.jupiter.api.Assertions.*;

public class UpdateCheckerTest {

    @Test
    void shouldUpdateTest() throws Exception {
        assertFalse(_shouldUpdate("2.8.1", "2.8.1", UpdateCheckerTest::unused));
        assertTrue(_shouldUpdate("2.8.1", "2.8.2", UpdateCheckerTest::unused));
        assertTrue(_shouldUpdate("2.8.2-beta", "2.8.2", UpdateCheckerTest::unused));
        assertFalse(_shouldUpdate("3.0.0-beta", "2.8.1", c -> "3.0.0-beta"));
        assertTrue(_shouldUpdate("3.0.0-beta-2", "2.8.1", c -> "3.0.0-beta-3"));
        assertTrue(_shouldUpdate("3.0.0-beta-2", "3.0.0", c -> "3.0.0-beta-2"));
        assertTrue(_shouldUpdate("3.0.0-beta-2", "4.0.0", c -> "3.0.0-beta-2"));
    }

    @Test
    void shouldUpdateFallbackTest() throws Exception {
        AtomicBoolean hasRun1 = new AtomicBoolean(false);
        AtomicBoolean hasRun2 = new AtomicBoolean(false);
        assertFalse(shouldUpdate("1.0.0", UpdateCheckerTest::intentionalError, UpdateCheckerTest::unused, () -> "1.0.0", e -> {
            assertIntentionalError(e);
            hasRun1.set(true);
        }));
        assertTrue(shouldUpdate("1.0.0", UpdateCheckerTest::intentionalError, UpdateCheckerTest::unused, () -> "2.0.0", e -> {
            assertIntentionalError(e);
            hasRun2.set(true);
        }));
        assertTrue(hasRun1.get());
        assertTrue(hasRun2.get());

        // Test that the suppressed error is actually present
        var e = assertThrows(Exception.class, () -> {
            shouldUpdate("1.0.0", UpdateCheckerTest::intentionalError, UpdateCheckerTest::unused, () -> {
                throw new IllegalStateException("Expected");
            }, UpdateCheckerTest::unused);
        });
        assertIntentionalError(e);
        assertSame(IllegalStateException.class, e.getSuppressed()[0].getClass());
    }

    @Test
    void getChannelTest() {
        assertEquals("", getChannel(""));
        assertEquals("", getChannel("-"));
        assertEquals("beta", getChannel("-beta"));
        assertEquals("beta", getChannel("-beta-"));
        assertEquals("beta", getChannel("-beta-7"));
        assertEquals("", getChannel("1-"));
        assertEquals("", getChannel("1.0"));
        assertEquals("", getChannel("1.2.3"));
        assertEquals("", getChannel("3.0.1-"));
        assertEquals("beta", getChannel("3.0.1-beta"));
        assertEquals("beta", getChannel("3.0.1-beta-5"));
    }

    @Test
    void isGreaterTest() {
        assertTrue(isGreater("1", "0"));
        assertFalse(isGreater("0", "1"));
        assertFalse(isGreater("1", "1"));
        assertTrue(isGreater("1.0", "0.1"));
        assertFalse(isGreater("0.1", "1.1"));
        assertFalse(isGreater("1.1", "1.1"));
        assertTrue(isGreater("0.2", "0.1"));
        assertFalse(isGreater("0.1", "0.2"));
        assertFalse(isGreater("0.1", "0.1"));
        assertFalse(isGreater("0.1.0", "0.1"));
        assertFalse(isGreater("0.1.0", "0.1.1"));
        assertTrue(isGreater("0.1.1", "0.1"));
        assertFalse(isGreater("0.1.0", "0.1.0"));
        assertTrue(isGreater("2.3.4", "2.3.2"));
        assertFalse(isGreater("2.3.4", "2.3.4"));
        assertFalse(isGreater("2.3.4", "3.3.4"));
    }

    @Test
    void extractBeforeHyphenTest() {
        assertEquals("", extractBeforeHyphen(""));
        assertEquals("", extractBeforeHyphen("-"));
        assertEquals("", extractBeforeHyphen("-beta"));
        assertEquals("1", extractBeforeHyphen("1"));
        assertEquals("1.0", extractBeforeHyphen("1.0"));
        assertEquals("1.2.3", extractBeforeHyphen("1.2.3"));
        assertEquals("3.0.1", extractBeforeHyphen("3.0.1-"));
        assertEquals("3.0.1", extractBeforeHyphen("3.0.1-beta"));
        assertEquals("3.0.1", extractBeforeHyphen("3.0.1-beta-5"));
    }

    private static <T, U> U unused(T ignored) {
        throw new RuntimeException("Should have not been run");
    }

    private static <U> U intentionalError() throws Exception {
        throw new Exception("Expected exception");
    }

    private static void assertIntentionalError(Exception e) {
        assertSame(Exception.class, e.getClass());
        assertEquals("Expected exception", e.getMessage());
    }
}
