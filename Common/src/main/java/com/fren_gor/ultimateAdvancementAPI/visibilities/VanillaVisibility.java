package com.fren_gor.ultimateAdvancementAPI.visibilities;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.multiParents.AbstractMultiParentsAdvancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

/**
 * If an advancement implements this visibility, it will be visible only if either every one of its parent or its grandparent are granted.
 * <p>This class is part of the Advancement Visibility System. See {@link IVisibility} for more information.
 */
public interface VanillaVisibility extends IVisibility {

    /**
     * Whether either any parent or any grandparent of the provided advancement is granted for the specified team.
     *
     * @param advancement The advancement.
     * @param progression The team {@link TeamProgression}.
     * @return Whether either any parent or any grandparent of the provided advancement is granted for the specified team.
     */
    @Override
    default boolean isVisible(@NotNull Advancement advancement, @NotNull TeamProgression progression) {
        Preconditions.checkNotNull(advancement, "Advancement is null.");
        Preconditions.checkNotNull(progression, "TeamProgression is null.");
        if (advancement.getProgression(progression) > 0)
            return true;

        if (advancement instanceof AbstractMultiParentsAdvancement multiParent) {
            return multiParent.isAnyGrandparentGranted(progression);
        } else if (advancement instanceof BaseAdvancement base) {

            Advancement parent = base.getParent();

            if (parent.isGranted(progression)) {
                return true;
            }
            if (parent instanceof AbstractMultiParentsAdvancement multiParent) {
                return multiParent.isAnyParentGranted(progression);
            } else if (parent instanceof BaseAdvancement baseA) {
                return baseA.getParent().isGranted(progression);
            }
            return false;
        }
        return false;
    }
}
