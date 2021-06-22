package com.fren_gor.ultimateAdvancementAPI.visibilities;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface HiddenVisibility extends IVisibility {

    @Override
    default boolean isVisible(@NotNull Advancement advancement, @NotNull UUID uuid) {
        Validate.notNull(advancement, "Advancement is null.");
        Validate.notNull(uuid, "UUID is null.");
        return advancement.getTeamCriteria(uuid) > 0;
    }
}
