package com.fren_gor.ultimateAdvancementAPI.visibilities;

import com.fren_gor.ultimateAdvancementAPI.advancement.AbstractMultiParentsAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface ParentGrantedVisibility extends IVisibility {

    @Override
    default boolean isVisible(@NotNull Advancement advancement, @NotNull UUID uuid) {
        Validate.notNull(advancement, "Advancement is null.");
        Validate.notNull(uuid, "UUID is null.");
        if (advancement.getTeamCriteria(uuid) > 0)
            return true;

        if (advancement instanceof AbstractMultiParentsAdvancement) {
            return ((AbstractMultiParentsAdvancement) advancement).isAnyParentGranted(uuid);
        }
        if (advancement instanceof BaseAdvancement) {
            return ((BaseAdvancement) advancement).getParent().isGranted(uuid);
        }
        return false;
    }
}
