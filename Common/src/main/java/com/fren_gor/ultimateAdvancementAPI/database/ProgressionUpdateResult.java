package com.fren_gor.ultimateAdvancementAPI.database;

import org.jetbrains.annotations.Range;

/**
 * The result of a progression update.
 *
 * @param oldProgression The old progression before the update.
 * @param newProgression The new progression after the update.
 */
public record ProgressionUpdateResult(@Range(from = 0, to = Integer.MAX_VALUE) int oldProgression,
                                      @Range(from = 0, to = Integer.MAX_VALUE) int newProgression) {
    /**
     * Gets the old progression before the update.
     *
     * @return The old progression before the update.
     */
    @Override
    public int oldProgression() {
        return oldProgression;
    }

    /**
     * Gets the new progression after the update.
     *
     * @return The new progression after the update.
     */
    @Override
    public int newProgression() {
        return newProgression;
    }
}
