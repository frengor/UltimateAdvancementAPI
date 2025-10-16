package com.fren_gor.ultimateAdvancementAPI.database.impl;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Class used to establish a connection to a database stored in-memory.
 */
public class InMemory extends SQLite {

    /**
     * Creates an in-memory database connection.
     *
     * @param main The instance of the main class of the API.
     * @throws Exception If anything goes wrong.
     */
    public InMemory(@NotNull AdvancementMain main) throws Exception {
        super(Objects.requireNonNull(main, "AdvancementMain is null.").getLogger());
    }
}
