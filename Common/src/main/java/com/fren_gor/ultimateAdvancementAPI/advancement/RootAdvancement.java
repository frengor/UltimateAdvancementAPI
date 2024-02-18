package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.AdvancementUpdater;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.UUID;

/**
 * The first advancement of an advancement tree.
 * <p>It has no parents and stores the path to the background texture image of the tab. Also, it must be always visible.
 */
public class RootAdvancement extends Advancement {

    @LazyValue
    private PreparedAdvancementWrapper wrapper;

    /**
     * Creates a new {@code RootAdvancement} with a maximum progression of {@code 1}.
     *
     * @param advancementTab The advancement tab of the advancement.
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     */
    public RootAdvancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AbstractAdvancementDisplay display) {
        this(advancementTab, key, display, 1);
    }

    /**
     * Creates a new {@code RootAdvancement}.
     *
     * @param advancementTab The advancement tab of the advancement.
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param maxProgression The maximum advancement progression.
     */
    public RootAdvancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AbstractAdvancementDisplay display, @Range(from = 1, to = Integer.MAX_VALUE) int maxProgression) {
        super(advancementTab, key, display, maxProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdate(@NotNull TeamProgression teamProgression, @NotNull AdvancementUpdater advancementUpdater) {
        // Root advancements are always visible
        advancementUpdater.addRootAdvancement(getNMSWrapper(), display, getProgression(teamProgression));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public PreparedAdvancementWrapper getNMSWrapper() {
        if (wrapper != null) {
            return wrapper;
        }

        try {
            return wrapper = PreparedAdvancementWrapper.craft(key.getNMSWrapper(), maxProgression);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns whether the advancement is visible to the provided player.
     * <p>The root advancement must be always visible, so this method always returns {@code true}.
     *
     * @param player The player.
     * @return Always {@code true}.
     */
    @Override
    @Contract("_ -> true")
    public final boolean isVisible(@NotNull Player player) {
        return true;
    }

    /**
     * Returns whether the advancement is visible to the provided player.
     * <p>The root advancement must be always visible, so this method always returns {@code true}.
     *
     * @param uuid The {@link UUID} of the player.
     * @return Always {@code true}.
     */
    @Override
    @Contract("_ -> true")
    public final boolean isVisible(@NotNull UUID uuid) {
        return true;
    }

    /**
     * Returns whether the advancement is visible to the provided team.
     * <p>The root advancement must be always visible, so this method always returns {@code true}.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return Always {@code true}.
     */
    @Override
    @Contract("_ -> true")
    public final boolean isVisible(@NotNull TeamProgression progression) {
        return true;
    }
}
