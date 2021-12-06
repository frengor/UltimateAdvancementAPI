package com.fren_gor.ultimateAdvancementAPI.database.impl;

import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class InMemory extends SQLite {

    /**
     * Creates an in-memory database connection.
     *
     * @param logger The plugin {@link Logger}.
     * @throws Exception If anything goes wrong.
     */
    public InMemory(@NotNull Logger logger) throws Exception {
        super(logger);
    }
}
