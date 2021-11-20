package com.fren_gor.ultimateAdvancementAPI.visibilities;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.multiParents.AbstractMultiParentsAdvancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

/**
 * If an advancement implements this visibility, it will be visible only if every one of its parents are granted.
 * <p>This class is part of the Advancement Visibility System. See {@link IVisibility} for more information.
 */
public interface ParentGrantedVisibility extends IVisibility {

    /**
     * Whether any parent of the provided advancement is granted for the specified team.
     *
     * @param advancement The advancement.
     * @param progression The team {@link TeamProgression}.
     * @return Whether every parent of the provided advancement is granted for the specified team.
     */
    @Override
    default boolean isVisible(@NotNull Advancement advancement, @NotNull TeamProgression progression) {
        Validate.notNull(advancement, "Advancement is null.");
        Validate.notNull(progression, "TeamProgression is null.");
        if (advancement.getProgression(progression) > 0)
            return true;

        if (advancement instanceof AbstractMultiParentsAdvancement multiParent) {
            return multiParent.isAnyParentGranted(progression);
        }
        if (advancement instanceof BaseAdvancement base) {
            return base.getParent().isGranted(progression);
        }
        return false;
    }
}
