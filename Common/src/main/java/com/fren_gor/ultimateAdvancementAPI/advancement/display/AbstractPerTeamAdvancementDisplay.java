package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public abstract class AbstractPerTeamAdvancementDisplay extends AbstractAdvancementDisplay {

    /**
     * Returns whether the toast notification should be sent on advancement grant.
     *
     * @return Whether the toast notification should be sent on advancement grant.
     */
    public abstract boolean doesShowToast(@NotNull TeamProgression progression);

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     *
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    public abstract boolean doesAnnounceToChat(@NotNull TeamProgression progression);

    /**
     * Gets a clone of the icon.
     *
     * @return A clone of the icon.
     */
    @NotNull
    public abstract ItemStack getIcon(@NotNull TeamProgression progression);

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public String getTitle(@NotNull TeamProgression progression) {
        return TextComponent.toLegacyText(getTitleBaseComponent(progression));
    }

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public abstract BaseComponent[] getTitleBaseComponent(@NotNull TeamProgression progression);

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public List<String> getDescription(@NotNull TeamProgression progression) {
        return getDescriptionBaseComponent(progression).stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public abstract List<BaseComponent[]> getDescriptionBaseComponent(@NotNull TeamProgression progression);

    /**
     * Returns the shape of the advancement frame in the advancement GUI.
     *
     * @return The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
    public abstract AdvancementFrameType getFrame(@NotNull TeamProgression progression);

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @return The x coordinate.
     */
    public abstract float getX(@NotNull TeamProgression progression);

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @return The y coordinate.
     */
    public abstract float getY(@NotNull TeamProgression progression);

    /**
     * Returns the {@code AdvancementDisplay} NMS wrapper, using the provided advancement for construction (when necessary).
     *
     * @param advancement The advancement used, when necessary, to create the NMS wrapper. Must be not {@code null}.
     * @return The {@code AdvancementDisplay} NMS wrapper.
     */
    @NotNull
    public abstract AdvancementDisplayWrapper getNMSWrapper(@NotNull Advancement advancement, @NotNull TeamProgression progression);

    @Override
    public boolean dispatchDoesToast(Player player, TeamProgression teamProgression) {
        return doesShowToast(teamProgression);
    }

    @Override
    public boolean dispatchDoesAnnounceToChat(Player player, TeamProgression teamProgression) {
        return doesAnnounceToChat(teamProgression);
    }

    @Override
    public ItemStack dispatchIcon(Player player, TeamProgression teamProgression) {
        return getIcon(teamProgression);
    }

    @Override
    public String dispatchTitle(Player player, TeamProgression teamProgression) {
        return getTitle(teamProgression);
    }

    @Override
    public BaseComponent[] dispatchTitleBaseComponent(Player player, TeamProgression teamProgression) {
        return getTitleBaseComponent(teamProgression);
    }

    @Override
    public List<String> dispatchDescription(Player player, TeamProgression teamProgression) {
        return getDescription(teamProgression);
    }

    @Override
    public List<BaseComponent[]> dispatchDescriptionBaseComponent(Player player, TeamProgression teamProgression) {
        return getDescriptionBaseComponent(teamProgression);
    }

    @Override
    public AdvancementFrameType dispatchFrame(Player player, TeamProgression teamProgression) {
        return getFrame(teamProgression);
    }

    @Override
    public float dispatchX(Player player, TeamProgression teamProgression) {
        return getX(teamProgression);
    }

    @Override
    public float dispatchY(Player player, TeamProgression teamProgression) {
        return getY(teamProgression);
    }
}