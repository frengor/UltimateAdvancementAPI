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

public abstract class AbstractPerPlayerAdvancementDisplay extends AbstractAdvancementDisplay {

    /**
     * Returns whether the toast notification should be sent on advancement grant.
     *
     * @return Whether the toast notification should be sent on advancement grant.
     */
    public boolean doesShowToast(@NotNull Player player) {
        return doesShowToast((OfflinePlayer) player);
    }

    /**
     * Returns whether the toast notification should be sent on advancement grant.
     *
     * @return Whether the toast notification should be sent on advancement grant.
     */
    public abstract boolean doesShowToast(@NotNull OfflinePlayer player);

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     *
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    public boolean doesAnnounceToChat(@NotNull Player player) {
        return doesAnnounceToChat((OfflinePlayer) player);
    }

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     *
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    public abstract boolean doesAnnounceToChat(@NotNull OfflinePlayer player);

    /**
     * Gets a clone of the icon.
     *
     * @return A clone of the icon.
     */
    @NotNull
    public ItemStack getIcon(@NotNull Player player) {
        return getIcon((OfflinePlayer) player);
    }

    /**
     * Gets a clone of the icon.
     *
     * @return A clone of the icon.
     */
    @NotNull
    public abstract ItemStack getIcon(@NotNull OfflinePlayer player);

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public String getTitle(@NotNull Player player) {
        return TextComponent.toLegacyText(getTitleBaseComponent(player));
    }

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public String getTitle(@NotNull OfflinePlayer player) {
        return TextComponent.toLegacyText(getTitleBaseComponent(player));
    }

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public BaseComponent[] getTitleBaseComponent(@NotNull Player player) {
        return getTitleBaseComponent((OfflinePlayer) player);
    }

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public abstract BaseComponent[] getTitleBaseComponent(@NotNull OfflinePlayer player);

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public List<String> getDescription(@NotNull Player player) {
        return getDescriptionBaseComponent(player).stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public List<String> getDescription(@NotNull OfflinePlayer player) {
        return getDescriptionBaseComponent(player).stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public List<BaseComponent[]> getDescriptionBaseComponent(@NotNull Player player) {
        return getDescriptionBaseComponent((OfflinePlayer) player);
    }

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public abstract List<BaseComponent[]> getDescriptionBaseComponent(@NotNull OfflinePlayer player);

    /**
     * Returns the shape of the advancement frame in the advancement GUI.
     *
     * @return The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
    public AdvancementFrameType getFrame(@NotNull Player player) {
        return getFrame((OfflinePlayer) player);
    }

    /**
     * Returns the shape of the advancement frame in the advancement GUI.
     *
     * @return The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
    public abstract AdvancementFrameType getFrame(@NotNull OfflinePlayer player);

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @return The x coordinate.
     */
    public float getX(@NotNull Player player) {
        return getX((OfflinePlayer) player);
    }

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @return The x coordinate.
     */
    public abstract float getX(@NotNull OfflinePlayer player);

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @return The y coordinate.
     */
    public float getY(@NotNull Player player) {
        return getY((OfflinePlayer) player);
    }

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @return The y coordinate.
     */
    public abstract float getY(@NotNull OfflinePlayer player);

    /**
     * {@inheritDoc}
     *
     * @param player The player for which the NMS wrapper is being made.
     */
    @NotNull
    public abstract PreparedAdvancementDisplayWrapper getNMSWrapper(@NotNull Player player);

    @Override
    @NonExtendable
    public boolean dispatchDoesToast(Player player, TeamProgression teamProgression) {
        return doesShowToast(player);
    }

    @Override
    @NonExtendable
    public boolean dispatchDoesToast(OfflinePlayer player, TeamProgression teamProgression) {
        return doesShowToast(player);
    }

    @Override
    @NonExtendable
    public boolean dispatchDoesAnnounceToChat(Player player, TeamProgression teamProgression) {
        return doesAnnounceToChat(player);
    }

    @Override
    @NonExtendable
    public boolean dispatchDoesAnnounceToChat(OfflinePlayer player, TeamProgression teamProgression) {
        return doesAnnounceToChat(player);
    }

    @Override
    @NonExtendable
    public ItemStack dispatchIcon(Player player, TeamProgression teamProgression) {
        return getIcon(player);
    }

    @Override
    @NonExtendable
    public ItemStack dispatchIcon(OfflinePlayer player, TeamProgression teamProgression) {
        return getIcon(player);
    }

    @Override
    @NonExtendable
    public String dispatchTitle(Player player, TeamProgression teamProgression) {
        return getTitle(player);
    }

    @Override
    @NonExtendable
    public String dispatchTitle(OfflinePlayer player, TeamProgression teamProgression) {
        return getTitle(player);
    }

    @Override
    @NonExtendable
    public BaseComponent[] dispatchTitleBaseComponent(Player player, TeamProgression teamProgression) {
        return getTitleBaseComponent(player);
    }

    @Override
    @NonExtendable
    public BaseComponent[] dispatchTitleBaseComponent(OfflinePlayer player, TeamProgression teamProgression) {
        return getTitleBaseComponent(player);
    }

    @Override
    @NonExtendable
    public List<String> dispatchDescription(Player player, TeamProgression teamProgression) {
        return getDescription(player);
    }

    @Override
    @NonExtendable
    public List<String> dispatchDescription(OfflinePlayer player, TeamProgression teamProgression) {
        return getDescription(player);
    }

    @Override
    @NonExtendable
    public List<BaseComponent[]> dispatchDescriptionBaseComponent(Player player, TeamProgression teamProgression) {
        return getDescriptionBaseComponent(player);
    }

    @Override
    @NonExtendable
    public List<BaseComponent[]> dispatchDescriptionBaseComponent(OfflinePlayer player, TeamProgression teamProgression) {
        return getDescriptionBaseComponent(player);
    }

    @Override
    @NonExtendable
    public AdvancementFrameType dispatchFrame(Player player, TeamProgression teamProgression) {
        return getFrame(player);
    }

    @Override
    @NonExtendable
    public AdvancementFrameType dispatchFrame(OfflinePlayer player, TeamProgression teamProgression) {
        return getFrame(player);
    }

    @Override
    @NonExtendable
    public float dispatchX(Player player, TeamProgression teamProgression) {
        return getX(player);
    }

    @Override
    @NonExtendable
    public float dispatchX(OfflinePlayer player, TeamProgression teamProgression) {
        return getX(player);
    }

    @Override
    @NonExtendable
    public float dispatchY(Player player, TeamProgression teamProgression) {
        return getY(player);
    }

    @Override
    @NonExtendable
    public float dispatchY(OfflinePlayer player, TeamProgression teamProgression) {
        return getY(player);
    }

    @Override
    @NonExtendable
    public PreparedAdvancementDisplayWrapper dispatchGetNMSWrapper(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getNMSWrapper(player);
    }
}
