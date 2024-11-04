package com.fren_gor.ultimateAdvancementAPI.database;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import com.fren_gor.ultimateAdvancementAPI.tests.AutoInject;
import com.fren_gor.ultimateAdvancementAPI.tests.UAAPIExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(UAAPIExtension.class)
public class JoinEventWaiterTest {

    @AutoInject
    private ServerMock server;
    private JoinEventWaiter joinEventWaiter;
    @AutoInject
    private UUID uuid, uuid2;

    @BeforeEach
    void setUp() {
        joinEventWaiter = new JoinEventWaiter(MockBukkit.createMockPlugin());
    }

    @Test
    void normalSequenceTest() {
        final AtomicBoolean run = new AtomicBoolean();
        final AtomicBoolean oldRun = new AtomicBoolean();
        joinEventWaiter.onLogin(uuid);
        joinEventWaiter.onFinishLoading(uuid, 1, () -> {
            boolean old = run.getAndSet(true);
            oldRun.set(old);
        }, () -> fail("Runnable cancelled"));

        assertFalse(run.get());

        joinEventWaiter.onJoin(uuid);

        assertFalse(run.get());

        server.getScheduler().performOneTick();

        assertTrue(run.get());
        assertFalse(oldRun.get());

        joinEventWaiter.onQuit(uuid);
    }

    @Test
    void joinBeforeFinishLoadingTest() {
        final AtomicBoolean run = new AtomicBoolean();
        final AtomicBoolean oldRun = new AtomicBoolean();
        joinEventWaiter.onLogin(uuid);
        joinEventWaiter.onJoin(uuid);

        assertFalse(run.get());

        joinEventWaiter.onFinishLoading(uuid, 1, () -> {
            boolean old = run.getAndSet(true);
            oldRun.set(old);
        }, () -> fail("Runnable cancelled"));

        assertFalse(run.get());

        server.getScheduler().performOneTick();

        assertTrue(run.get());
        assertFalse(oldRun.get());

        joinEventWaiter.onQuit(uuid);
    }

    @Test
    void earlyQuitFinishLoadingTest() {
        final AtomicBoolean run = new AtomicBoolean();
        final AtomicBoolean cancelled  = new AtomicBoolean();
        joinEventWaiter.onLogin(uuid);
        joinEventWaiter.onFinishLoading(uuid, 1, () -> {
            assertFalse(run.getAndSet(true));
        }, () -> {
            assertFalse(cancelled.getAndSet(true));
        });

        assertFalse(run.get());
        assertFalse(cancelled.get());

        joinEventWaiter.onQuit(uuid);

        assertFalse(run.get());
        assertTrue(cancelled.get());

        server.getScheduler().performTicks(20);

        assertFalse(run.get());

        joinEventWaiter.onJoin(uuid);

        assertFalse(run.get());

        server.getScheduler().performTicks(20);

        assertFalse(run.get());
    }

    @Test
    void earlyQuitJoinTest() {
        final AtomicBoolean run = new AtomicBoolean();
        final AtomicBoolean cancelled = new AtomicBoolean();
        joinEventWaiter.onLogin(uuid);
        joinEventWaiter.onJoin(uuid);

        joinEventWaiter.onQuit(uuid);

        joinEventWaiter.onFinishLoading(uuid, 1, () -> {
            assertFalse(run.getAndSet(true));
        }, () -> {
            assertFalse(cancelled.getAndSet(true));
        });

        assertFalse(run.get());
        assertTrue(cancelled.get());

        server.getScheduler().performTicks(20);

        assertFalse(run.get());

        joinEventWaiter.onJoin(uuid);

        assertFalse(run.get());

        server.getScheduler().performTicks(20);

        assertFalse(run.get());
    }

    @Test
    void immediatelyQuitTest() {
        final AtomicBoolean run = new AtomicBoolean();
        final AtomicBoolean cancelled = new AtomicBoolean();
        joinEventWaiter.onLogin(uuid);
        joinEventWaiter.onQuit(uuid);

        joinEventWaiter.onFinishLoading(uuid, 1, () -> {
            assertFalse(run.getAndSet(true));
        }, () -> {
            assertFalse(cancelled.getAndSet(true));
        });

        assertFalse(run.get());
        assertTrue(cancelled.get());

        server.getScheduler().performTicks(20);

        assertFalse(run.get());

        joinEventWaiter.onJoin(uuid);

        assertFalse(run.get());

        server.getScheduler().performTicks(20);

        assertFalse(run.get());
    }

    @Test
    void doubleSchedulingTest() {
        // Test every possible combination of onJoin and onFinishLoading duplicated calls

        final AtomicBoolean cancelled = new AtomicBoolean();

        final Runnable onJoin = () -> joinEventWaiter.onJoin(uuid);
        final Runnable onFinish = () -> joinEventWaiter.onFinishLoading(uuid, 1, () -> {
        }, () -> {
            assertFalse(cancelled.getAndSet(true));
        });

        try {
            // onFinish -> onJoin -> onFinish

            joinEventWaiter.onLogin(uuid);
            onFinish.run();
            assertFalse(cancelled.get());
            onJoin.run();
            assertFalse(cancelled.get());
            onFinish.run();
            assertTrue(cancelled.get());
        } finally {
            server.getScheduler().performOneTick(); // Don't execute the onCancel of the last onFinish when calling onQuit
            joinEventWaiter.onQuit(uuid);
            cancelled.set(false);
        }

        try {
            // onJoin -> onFinish -> onFinish

            joinEventWaiter.onLogin(uuid);
            onJoin.run();
            assertFalse(cancelled.get());
            onFinish.run();
            assertFalse(cancelled.get());
            onFinish.run();
            assertTrue(cancelled.get());
        } finally {
            server.getScheduler().performOneTick(); // Don't execute the onCancel of the last onFinish when calling onQuit
            joinEventWaiter.onQuit(uuid);
            cancelled.set(false);
        }
    }

    @Test
    void closeTest() {
        final AtomicBoolean cancelled = new AtomicBoolean();
        final AtomicBoolean run2 = new AtomicBoolean();
        joinEventWaiter.onLogin(uuid);
        joinEventWaiter.onFinishLoading(uuid, 1, () -> {
            fail("Executed 1");
        }, () -> {
            assertFalse(cancelled.getAndSet(true));
        });

        assertFalse(cancelled.get());

        joinEventWaiter.onLogin(uuid2);
        joinEventWaiter.onFinishLoading(uuid2, 1, () -> {
            assertFalse(run2.getAndSet(true));
        }, () -> {
            fail("Cancelled 2");
        });
        joinEventWaiter.onJoin(uuid2);

        server.getScheduler().performOneTick();

        assertFalse(cancelled.get());
        assertTrue(run2.get());

        joinEventWaiter.onClose();

        assertTrue(cancelled.get());
    }

    @Test
    void noOnLoginTest() {
        final AtomicBoolean cancelled = new AtomicBoolean();
        joinEventWaiter.onFinishLoading(uuid, 1, () -> {
            fail("Executed 1");
        }, () -> {
            assertFalse(cancelled.getAndSet(true));
        });
        assertTrue(cancelled.get());
    }

    @Test
    void doubleOnFinishLoadingTest() {
        final AtomicBoolean cancelled = new AtomicBoolean();
        final AtomicBoolean run = new AtomicBoolean();
        joinEventWaiter.onLogin(uuid);
        joinEventWaiter.onFinishLoading(uuid, 1, () -> {
            fail("Executed");
        }, () -> {
            assertFalse(cancelled.getAndSet(true));
        });
        joinEventWaiter.onFinishLoading(uuid, 1, () -> {
            assertFalse(run.getAndSet(true));
        }, () -> {
            fail("Cancelled");
        });

        assertTrue(cancelled.get());

        joinEventWaiter.onJoin(uuid);

        server.getScheduler().performOneTick();

        assertTrue(run.get());
    }
}

