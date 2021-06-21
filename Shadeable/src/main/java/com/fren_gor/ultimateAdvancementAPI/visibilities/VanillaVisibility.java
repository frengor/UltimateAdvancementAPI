package com.fren_gor.ultimateAdvancementAPI.visibilities;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.BaseAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.MultiParentsAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
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

        if (advancement instanceof MultiParentsAdvancement) {
            return ((MultiParentsAdvancement) advancement).isAnyGrandparentGranted(uuid);
        } else if (advancement instanceof BaseAdvancement) {

            Advancement parent = ((BaseAdvancement) advancement).getParent();

            if (parent instanceof RootAdvancement || parent.isGranted(uuid)) {
                return true;
            }
            if (parent instanceof MultiParentsAdvancement) {
                return ((MultiParentsAdvancement) parent).isAnyParentGranted(uuid);
            } else if (parent instanceof BaseAdvancement) {
                Advancement grandParent = ((BaseAdvancement) parent).getParent();
                return grandParent instanceof RootAdvancement || grandParent.isGranted(uuid);
            }
            return false;
        }
        return false;
    }
}
