package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import net.minecraft.server.v1_15_R1.Criterion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.ADV_REWARDS;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementCriteria;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementRequirements;

/**
 * RootAdvancement directly extends Advancement.
 * <p>It represents the first advancement of a tab.
 * <p>Therefore, there must be only one RootAdvancement per tab and it has no parent advancement.
 * <p>RootAdvancement must be always visible and it contains the background texture path.
 */
public class RootAdvancement extends Advancement {

    /**
     * The background texture path.
     * <p>The corresponding image will be used as advancement GUI background.
     */
    @NotNull
    private final String backgroundTexture;

    private net.minecraft.server.v1_15_R1.Advancement mcAdvancement;

    /**
     * Create a new RootAdvancement.
     *
     * @param advancementTab The {@link AdvancementTab} instance that the advancement belongs to.
     * @param key The unique key of the advancement.
     * @param display The {@link AdvancementDisplay} instance of the advancement.
     * @param backgroundTexture The background texture path.
     */
    public RootAdvancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display, @NotNull String backgroundTexture) {
        this(advancementTab, key, display, backgroundTexture, 1);
    }

    /**
     * Create a new RootAdvancement.
     *
     * @param advancementTab The {@link AdvancementTab} instance that the advancement belongs to.
     * @param key The unique key of the advancement.
     * @param display The {@link AdvancementDisplay} instance of the advancement.
     * @param backgroundTexture The background texture path.
     * @param maxCriteria The times the advancement action should be done.
     */
    public RootAdvancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display, @NotNull String backgroundTexture, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        super(advancementTab, key, display, maxCriteria);
        this.backgroundTexture = Objects.requireNonNull(backgroundTexture, "Background texture is null.");
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    public net.minecraft.server.v1_15_R1.Advancement getMinecraftAdvancement() {
        if (mcAdvancement != null) {
            return mcAdvancement;
        }

        Map<String, Criterion> advCrit = getAdvancementCriteria(maxCriteria);
        return mcAdvancement = new net.minecraft.server.v1_15_R1.Advancement(getMinecraftKey(), null, display.getMinecraftDisplay(this), ADV_REWARDS, advCrit, getAdvancementRequirements(advCrit));
    }

    /**
     * The root advancement must be always visible.
     *
     * @param player From which player to know whether the advancement is visible to the team.
     * @return {@code true}.
     */
    @Override
    @Contract("_ -> true")
    public final boolean isVisible(@NotNull Player player) {
        return true;
    }

    /**
     * The root advancement must be always visible.
     *
     * @param uuid From which UUID player to know whether the advancement is visible to the team.
     * @return {@code true}.
     */
    @Override
    @Contract("_ -> true")
    public final boolean isVisible(@NotNull UUID uuid) {
        return true;
    }

    /**
     * The root advancement must be always visible.
     *
     * @param progression From which {@link TeamProgression} to know whether the advancement is visible to the team.
     * @return {@code true}.
     */
    @Override
    @Contract("_ -> true")
    public final boolean isVisible(@NotNull TeamProgression progression) {
        return true;
    }

    /**
     * Returns the path of the background texture as {@link String}.
     *
     * @return The path of the background texture.
     */
    public String getBackgroundTexture() {
        return backgroundTexture;
    }
}
