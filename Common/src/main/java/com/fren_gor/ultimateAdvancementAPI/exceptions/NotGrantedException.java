package com.fren_gor.ultimateAdvancementAPI.exceptions;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This exception is thrown when an operation couldn't be done since an advancement is not granted for a team.
 */
public class NotGrantedException extends RuntimeException {

    private final AdvancementKey advancementKey;
    private final int teamId;

    public NotGrantedException(@NotNull AdvancementKey advancementKey, int teamId) {
        this(advancementKey, teamId, getDefaultErrorMessage(advancementKey, teamId));
    }

    public NotGrantedException(@NotNull AdvancementKey advancementKey, int teamId, String message) {
        super(message);
        this.advancementKey = Objects.requireNonNull(advancementKey, "AdvancementKey is null.");
        this.teamId = teamId;
    }

    public NotGrantedException(@NotNull AdvancementKey advancementKey, int teamId, String message, Throwable cause) {
        super(message, cause);
        this.advancementKey = Objects.requireNonNull(advancementKey, "AdvancementKey is null.");
        this.teamId = teamId;
    }

    public NotGrantedException(@NotNull AdvancementKey advancementKey, int teamId, Throwable cause) {
        this(advancementKey, teamId, getDefaultErrorMessage(advancementKey, teamId), cause);
    }

    protected NotGrantedException(@NotNull AdvancementKey advancementKey, int teamId, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.advancementKey = Objects.requireNonNull(advancementKey, "AdvancementKey is null.");
        this.teamId = teamId;
    }

    /**
     * Gets the key of the advancement that was not granted.
     *
     * @return The key of the advancement that was not granted.
     */
    @NotNull
    public AdvancementKey getAdvancementKey() {
        return advancementKey;
    }

    /**
     * Gets the id of the team for which the advancement was not granted.
     *
     * @return The id of the team for which the advancement was not granted.
     */
    public int getTeamId() {
        return teamId;
    }

    private static String getDefaultErrorMessage(AdvancementKey advancementKey, int teamId) {
        return "Advancement " + advancementKey + " is not granted for team with id " + teamId;
    }
}
