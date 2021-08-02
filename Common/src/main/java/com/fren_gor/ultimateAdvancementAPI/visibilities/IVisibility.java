package com.fren_gor.ultimateAdvancementAPI.visibilities;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface IVisibility {

    boolean isVisible(@NotNull Advancement advancement, @NotNull TeamProgression progression);

}
