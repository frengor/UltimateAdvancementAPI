package com.fren_gor.ultimateAdvancementAPI.database;

import com.fren_gor.ultimateAdvancementAPI.database.BlockingDBImpl.BlockedDB;
import com.fren_gor.ultimateAdvancementAPI.database.DBUtils.DBOperation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;

public class BlockedDBTest {

    @Test
    @Timeout(10)
    void blockedDBTest() throws Exception {
        BlockedDB blocked = new BlockedDB(DBOperation.CREATE_NEW_TEAM);
        CyclicBarrier latch = new CyclicBarrier(2);
        CompletableFuture<Void> cf = new CompletableFuture<>();
        Thread t = new Thread(() -> {
            try {
                latch.await();
                blocked.block();
                latch.await();
                cf.complete(null);
            } catch (Exception e) {
                cf.completeExceptionally(e);
            }
        });
        t.start();
        latch.await();
        blocked.waitBlock();
        blocked.resume();
        latch.await();
        cf.get(); // Assert no exception has been thrown
    }
}