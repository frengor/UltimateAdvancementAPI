package com.fren_gor.ultimateAdvancementAPI.visibilities;

import com.fren_gor.ultimateAdvancementAPI.advancement.multiParents.AbstractMultiParentsAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface VanillaVisibility extends IVisibility {

    @Override
    default boolean isVisible(@NotNull Advancement advancement, @NotNull UUID uuid) {
        Validate.notNull(advancement, "Advancement is null.");
        Validate.notNull(uuid, "UUID is null.");
        if (advancement.getTeamCriteria(uuid) > 0)
            return true;

        if (advancement instanceof AbstractMultiParentsAdvancement) {
            return ((AbstractMultiParentsAdvancement) advancement).isAnyGrandparentGranted(uuid);
        } else if (advancement instanceof BaseAdvancement) {

            Advancement parent = ((BaseAdvancement) advancement).getParent();

            if (parent.isGranted(uuid)) {
                return true;
            }
            if (parent instanceof AbstractMultiParentsAdvancement) {
                return ((AbstractMultiParentsAdvancement) parent).isAnyParentGranted(uuid);
            } else if (parent instanceof BaseAdvancement) {
                return ((BaseAdvancement) parent).getParent().isGranted(uuid);
            }
            return false;
        }
        return false;
    }
}
