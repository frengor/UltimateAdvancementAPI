package com.fren_gor.ultimateAdvancementAPI.visibilities;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.MultiParentsAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface ParentGrantedVisibility extends IVisibility {

    @Override
    default boolean isAdvancementVisible(@NotNull UUID uuid) {
        Validate.notNull(uuid, "UUID is null.");
        Advancement advancement = getAdvancement();
        if (advancement.getTeamCriteria(uuid) > 0)
            return true;

        if (advancement instanceof MultiParentsAdvancement) {
            return ((MultiParentsAdvancement) advancement).isAnyParentGranted(uuid);
        }
        if (advancement instanceof BaseAdvancement) {
            Advancement parent = ((BaseAdvancement) advancement).getParent();
            return parent instanceof RootAdvancement || parent.isGranted(uuid);
        }
        return false;
    }
}
