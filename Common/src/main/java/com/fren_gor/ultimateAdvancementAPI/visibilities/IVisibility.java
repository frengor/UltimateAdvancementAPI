package com.fren_gor.ultimateAdvancementAPI.visibilities;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Configure the visibility system.
 * <p>Override {@link IVisibility#isVisible(Advancement, TeamProgression)} and set up your custom visibility behavior.
 */
public interface IVisibility {

    /**
     * Whether the advancement is visible when the method is called.
     *
     * @param advancement An advancement.
     * @param progression The team progression.
     * @return Whether the advancement is visible.
     */
    boolean isVisible(@NotNull Advancement advancement, @NotNull TeamProgression progression);

}
