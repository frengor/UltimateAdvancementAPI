package com.fren_gor.ultimateAdvancementAPI.visibilities;

import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface HiddenVisibility extends IVisibility {

    @Override
    default boolean isAdvancementVisible(@NotNull UUID uuid) {
        Validate.notNull(uuid, "UUID is null.");
        return getAdvancement().getTeamCriteria(uuid) > 0;
    }
}
