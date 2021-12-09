package com.fren_gor.ultimateAdvancementAPI.visibilities;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

/**
 * If an advancement implements this visibility, it will be visible only if the progression is greater than zero.
 * <p>This class is part of the Advancement Visibility System. See {@link IVisibility} for more information.
 */
public interface HiddenVisibility extends IVisibility {

    /**
     * Whether the specified team has a progression grater than zero for the provided advancement.
     *
     * @param advancement The advancement.
     * @param progression The team {@link TeamProgression}.
     * @return Whether the specified team has a progression grater than zero for the provided advancement.
     */
    @Override
    default boolean isVisible(@NotNull Advancement advancement, @NotNull TeamProgression progression) {
        Preconditions.checkNotNull(advancement, "Advancement is null.");
        Preconditions.checkNotNull(progression, "TeamProgression is null.");
        return advancement.getProgression(progression) > 0;
    }
}
