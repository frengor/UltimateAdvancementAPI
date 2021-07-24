package com.fren_gor.ultimateAdvancementAPI.visibilities;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.multiParents.AbstractMultiParentsAdvancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

public interface VanillaVisibility extends IVisibility {

    @Override
    default boolean isVisible(@NotNull Advancement advancement, @NotNull TeamProgression progression) {
        Validate.notNull(advancement, "Advancement is null.");
        Validate.notNull(progression, "TeamProgression is null.");
        if (advancement.getTeamCriteria(progression) > 0)
            return true;

        if (advancement instanceof AbstractMultiParentsAdvancement) {
            return ((AbstractMultiParentsAdvancement) advancement).isAnyGrandparentGranted(progression);
        } else if (advancement instanceof BaseAdvancement) {

            Advancement parent = ((BaseAdvancement) advancement).getParent();

            if (parent.isGranted(progression)) {
                return true;
            }
            if (parent instanceof AbstractMultiParentsAdvancement) {
                return ((AbstractMultiParentsAdvancement) parent).isAnyParentGranted(progression);
            } else if (parent instanceof BaseAdvancement) {
                return ((BaseAdvancement) parent).getParent().isGranted(progression);
            }
            return false;
        }
        return false;
    }
}
