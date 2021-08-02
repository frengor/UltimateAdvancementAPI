package com.fren_gor.ultimateAdvancementAPI.util;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface AfterHandle {

    void apply(@NotNull TeamProgression progression, @Nullable Player player, @NotNull Advancement advancement);

    public static final AfterHandle UPDATE_ADVANCEMENTS_TO_TEAM = (progression, player, adv) -> adv.getAdvancementTab().updateAdvancementsToTeam(progression);

}
