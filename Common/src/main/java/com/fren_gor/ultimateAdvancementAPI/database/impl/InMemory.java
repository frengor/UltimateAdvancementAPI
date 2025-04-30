package com.fren_gor.ultimateAdvancementAPI.database.impl;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.logging.Logger;

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

    /**
     * Creates an in-memory database connection.
     *
     * @param logger The plugin {@link Logger}.
     * @throws Exception If anything goes wrong.
     * @deprecated Use {@link #InMemory(AdvancementMain)} instead.
     */
    @Deprecated(forRemoval = true, since = "2.5.0")
    public InMemory(@NotNull Logger logger) throws Exception {
        super(logger);
    }
}
