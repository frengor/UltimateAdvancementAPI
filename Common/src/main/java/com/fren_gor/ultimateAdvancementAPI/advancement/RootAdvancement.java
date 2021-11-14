package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Objects;
import java.util.UUID;

/**
 * The first advancement of an advancement tree.
 * <p>It has no parents and stores the path to the background texture image of the tab. Also, it must be always visible.
 */
public class RootAdvancement extends Advancement {

    /**
     * The path of the background texture image.
     * <p>The corresponding image will be used as the background of the tab in the advancement GUI.
     */
    @NotNull
    private final String backgroundTexture;

    @LazyValue
    private AdvancementWrapper wrapper;

    /**
     * Creates a new {@code RootAdvancement} with a maximum criteria of {@code 1}.
     *
     * @param advancementTab The advancement tab of the advancement.
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param backgroundTexture The path of the background texture image (like "textures/block/stone.png").
     */
    public RootAdvancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display, @NotNull String backgroundTexture) {
        this(advancementTab, key, display, backgroundTexture, 1);
    }

    /**
     * Creates a new {@code RootAdvancement}.
     *
     * @param advancementTab The advancement tab of the advancement.
     * @param key The unique key of the advancement. It must be unique among the other advancements of the tab.
     * @param display The display information of this advancement.
     * @param backgroundTexture The path of the background texture image (like "textures/block/stone.png").
     * @param maxCriteria The maximum advancement criteria.
     */
    public RootAdvancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display, @NotNull String backgroundTexture, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        super(advancementTab, key, display, maxCriteria);
        this.backgroundTexture = Objects.requireNonNull(backgroundTexture, "Background texture is null.");
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public AdvancementWrapper getNMSWrapper() {
        if (wrapper != null) {
            return wrapper;
        }

        try {
            return wrapper = AdvancementWrapper.craftRootAdvancement(key.getNMSWrapper(), display.getNMSWrapper(this), maxCriteria);
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

    /**
     * Gets the path to the background texture image of the tab.
     *
     * @return The path to the background texture image of the tab.
     */
    @NotNull
    public String getBackgroundTexture() {
        return backgroundTexture;
    }
}
