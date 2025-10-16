package com.fren_gor.ultimateAdvancementAPI.tests.database;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.database.IDatabase;
import com.fren_gor.ultimateAdvancementAPI.database.impl.InMemory;

public final class DatabaseImpls {
    private final InMemory raw;
    private final BlockingDBImpl blocking;
    private final FallibleDBImpl fallible;

    public DatabaseImpls(AdvancementMain main) throws Exception {
        this.raw = new InMemory(main);
        this.blocking = new BlockingDBImpl(raw);
        this.fallible = new FallibleDBImpl(blocking);
    }

    public IDatabase getIDatabase() {
        return this.fallible;
    }

    public InMemory getRawDB() {
        return raw;
    }

    public BlockingDBImpl getBlocking() {
        return blocking;
    }

    public FallibleDBImpl getFallible() {
        return fallible;
    }
}
