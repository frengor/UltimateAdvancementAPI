package com.fren_gor.ultimateAdvancementAPI.visibilities;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

/**
 * If an advancement implements this visibility, it will be visible only if the criteria progression is more than 0.
 */
public interface HiddenVisibility extends IVisibility {

    /**
     * {@inheritDoc}
     *
     * @return {@code true} if the team progression is more than 0.
     */
    @Override
    default boolean isVisible(@NotNull Advancement advancement, @NotNull TeamProgression progression) {
        Validate.notNull(advancement, "Advancement is null.");
        Validate.notNull(progression, "TeamProgression is null.");
        return advancement.getTeamCriteria(progression) > 0;
    }
}
