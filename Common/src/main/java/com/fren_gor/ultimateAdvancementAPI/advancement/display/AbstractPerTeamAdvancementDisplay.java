package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
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
     * {@inheritDoc}
     *
     * @param progression The {@link TeamProgression} of the team for which the NMS wrapper is being made.
     */
    @NotNull
    public abstract PreparedAdvancementDisplayWrapper getNMSWrapper(@NotNull TeamProgression progression);

    @Override
    @NonExtendable
    public PreparedAdvancementDisplayWrapper dispatchGetNMSWrapper(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getNMSWrapper(teamProgression);
    }

    @Override
    @NonExtendable
    public boolean dispatchDoesToast(Player player, TeamProgression teamProgression) {
        return doesShowToast(teamProgression);
    }

    @Override
    @NonExtendable
    public boolean dispatchDoesToast(OfflinePlayer player, TeamProgression teamProgression) {
        return doesShowToast(teamProgression);
    }

    @Override
    @NonExtendable
    public boolean dispatchDoesAnnounceToChat(Player player, TeamProgression teamProgression) {
        return doesAnnounceToChat(teamProgression);
    }

    @Override
    @NonExtendable
    public boolean dispatchDoesAnnounceToChat(OfflinePlayer player, TeamProgression teamProgression) {
        return doesAnnounceToChat(teamProgression);
    }

    @Override
    @NonExtendable
    public ItemStack dispatchIcon(Player player, TeamProgression teamProgression) {
        return getIcon(teamProgression);
    }

    @Override
    @NonExtendable
    public ItemStack dispatchIcon(OfflinePlayer player, TeamProgression teamProgression) {
        return getIcon(teamProgression);
    }

    @Override
    @NonExtendable
    public String dispatchTitle(Player player, TeamProgression teamProgression) {
        return getTitle(teamProgression);
    }

    @Override
    @NonExtendable
    public String dispatchTitle(OfflinePlayer player, TeamProgression teamProgression) {
        return getTitle(teamProgression);
    }

    @Override
    @NonExtendable
    public BaseComponent[] dispatchTitleBaseComponent(Player player, TeamProgression teamProgression) {
        return getTitleBaseComponent(teamProgression);
    }

    @Override
    @NonExtendable
    public BaseComponent[] dispatchTitleBaseComponent(OfflinePlayer player, TeamProgression teamProgression) {
        return getTitleBaseComponent(teamProgression);
    }

    @Override
    @NonExtendable
    public List<String> dispatchDescription(Player player, TeamProgression teamProgression) {
        return getDescription(teamProgression);
    }

    @Override
    @NonExtendable
    public List<String> dispatchDescription(OfflinePlayer player, TeamProgression teamProgression) {
        return getDescription(teamProgression);
    }

    @Override
    @NonExtendable
    public List<BaseComponent[]> dispatchDescriptionBaseComponent(Player player, TeamProgression teamProgression) {
        return getDescriptionBaseComponent(teamProgression);
    }

    @Override
    @NonExtendable
    public List<BaseComponent[]> dispatchDescriptionBaseComponent(OfflinePlayer player, TeamProgression teamProgression) {
        return getDescriptionBaseComponent(teamProgression);
    }

    @Override
    @NonExtendable
    public AdvancementFrameType dispatchFrame(Player player, TeamProgression teamProgression) {
        return getFrame(teamProgression);
    }

    @Override
    @NonExtendable
    public AdvancementFrameType dispatchFrame(OfflinePlayer player, TeamProgression teamProgression) {
        return getFrame(teamProgression);
    }

    @Override
    @NonExtendable
    public float dispatchX(Player player, TeamProgression teamProgression) {
        return getX(teamProgression);
    }

    @Override
    @NonExtendable
    public float dispatchX(OfflinePlayer player, TeamProgression teamProgression) {
        return getX(teamProgression);
    }

    @Override
    @NonExtendable
    public float dispatchY(Player player, TeamProgression teamProgression) {
        return getY(teamProgression);
    }

    @Override
    @NonExtendable
    public float dispatchY(OfflinePlayer player, TeamProgression teamProgression) {
        return getY(teamProgression);
    }
}
