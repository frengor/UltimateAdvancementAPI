package com.fren_gor.ultimateAdvancementAPI.visibilities;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.multiParents.AbstractMultiParentsAdvancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

/**
 * If an advancement implements this visibility, it will be visible only if the parent/s is/are granted.
 */
public interface ParentGrantedVisibility extends IVisibility {

    /**
     * {@inheritDoc}
     *
     * @return {@code true} if the parent/s of the advancement is/are granted.
     */
    @Override
    default boolean isVisible(@NotNull Advancement advancement, @NotNull TeamProgression progression) {
        Validate.notNull(advancement, "Advancement is null.");
        Validate.notNull(progression, "TeamProgression is null.");
        if (advancement.getTeamCriteria(progression) > 0)
            return true;

        if (advancement instanceof AbstractMultiParentsAdvancement) {
            return ((AbstractMultiParentsAdvancement) advancement).isAnyParentGranted(progression);
        }
        if (advancement instanceof BaseAdvancement) {
            return ((BaseAdvancement) advancement).getParent().isGranted(progression);
        }
        return false;
    }
}
