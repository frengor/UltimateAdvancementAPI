package com.fren_gor.ultimateAdvancementAPI.visibilities;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface IVisibility {

    boolean isVisible(@NotNull Advancement advancement, @NotNull UUID uuid);

}
