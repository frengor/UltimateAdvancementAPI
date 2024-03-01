package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotLoadedException;
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

public abstract class AbstractAdvancementDisplay {

    /**
     * Returns whether the toast notification should be sent on advancement grant.
     *
     * @return Whether the toast notification should be sent on advancement grant.
     */
    public abstract boolean doesShowToast();

    @NonExtendable
    public boolean dispatchDoesToast(Player player, Advancement advancement) {
        return dispatchDoesToast(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public boolean dispatchDoesToast(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return doesShowToast();
    }

    @NonExtendable
    public boolean dispatchDoesToast(OfflinePlayer player, Advancement advancement) {
        return dispatchDoesToast(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public boolean dispatchDoesToast(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return doesShowToast();
    }

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     *
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    public abstract boolean doesAnnounceToChat();

    @NonExtendable
    public boolean dispatchDoesAnnounceToChat(Player player, Advancement advancement) {
        return dispatchDoesAnnounceToChat(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public boolean dispatchDoesAnnounceToChat(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return doesAnnounceToChat();
    }

    @NonExtendable
    public boolean dispatchDoesAnnounceToChat(OfflinePlayer player, Advancement advancement) {
        return dispatchDoesAnnounceToChat(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public boolean dispatchDoesAnnounceToChat(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return doesAnnounceToChat();
    }

    /**
     * Gets a clone of the icon.
     *
     * @return A clone of the icon.
     */
    @NotNull
    public abstract ItemStack getIcon();

    @NonExtendable
    public ItemStack dispatchIcon(Player player, Advancement advancement) throws UserNotLoadedException {
        return dispatchIcon(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public ItemStack dispatchIcon(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getIcon();
    }

    @NonExtendable
    public ItemStack dispatchIcon(OfflinePlayer player, Advancement advancement) throws UserNotLoadedException {
        return dispatchIcon(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public ItemStack dispatchIcon(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getIcon();
    }

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public String getTitle() {
        return TextComponent.toLegacyText(getTitleBaseComponent());
    }

    @NonExtendable
    public String dispatchTitle(Player player, Advancement advancement) throws UserNotLoadedException {
        return dispatchTitle(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public String dispatchTitle(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getTitle();
    }

    @NonExtendable
    public String dispatchTitle(OfflinePlayer player, Advancement advancement) throws UserNotLoadedException {
        return dispatchTitle(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public String dispatchTitle(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getTitle();
    }

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public abstract BaseComponent[] getTitleBaseComponent();

    @NonExtendable
    public BaseComponent[] dispatchTitleBaseComponent(Player player, Advancement advancement) throws UserNotLoadedException {
        return dispatchTitleBaseComponent(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public BaseComponent[] dispatchTitleBaseComponent(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getTitleBaseComponent();
    }

    @NonExtendable
    public BaseComponent[] dispatchTitleBaseComponent(OfflinePlayer player, Advancement advancement) throws UserNotLoadedException {
        return dispatchTitleBaseComponent(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public BaseComponent[] dispatchTitleBaseComponent(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getTitleBaseComponent();
    }

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public List<String> getDescription() {
        return getDescriptionBaseComponent().stream().map(TextComponent::toLegacyText).toList();
    }

    @NonExtendable
    public List<String> dispatchDescription(Player player, Advancement advancement) throws UserNotLoadedException {
        return dispatchDescription(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public List<String> dispatchDescription(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getDescription();
    }

    @NonExtendable
    public List<String> dispatchDescription(OfflinePlayer player, Advancement advancement) throws UserNotLoadedException {
        return dispatchDescription(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public List<String> dispatchDescription(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getDescription();
    }

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public abstract List<BaseComponent[]> getDescriptionBaseComponent();

    @NonExtendable
    public List<BaseComponent[]> dispatchDescriptionBaseComponent(Player player, Advancement advancement) throws UserNotLoadedException {
        return dispatchDescriptionBaseComponent(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public List<BaseComponent[]> dispatchDescriptionBaseComponent(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getDescriptionBaseComponent();
    }

    @NonExtendable
    public List<BaseComponent[]> dispatchDescriptionBaseComponent(OfflinePlayer player, Advancement advancement) throws UserNotLoadedException {
        return dispatchDescriptionBaseComponent(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public List<BaseComponent[]> dispatchDescriptionBaseComponent(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getDescriptionBaseComponent();
    }

    /**
     * Returns the shape of the advancement frame in the advancement GUI.
     *
     * @return The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
    public abstract AdvancementFrameType getFrame();

    @NonExtendable
    public AdvancementFrameType dispatchFrame(Player player, Advancement advancement) throws UserNotLoadedException {
        return dispatchFrame(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public AdvancementFrameType dispatchFrame(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getFrame();
    }

    @NonExtendable
    public AdvancementFrameType dispatchFrame(OfflinePlayer player, Advancement advancement) throws UserNotLoadedException {
        return dispatchFrame(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public AdvancementFrameType dispatchFrame(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getFrame();
    }

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @return The x coordinate.
     */
    public abstract float getX();

    @NonExtendable
    public float dispatchX(Player player, Advancement advancement) throws UserNotLoadedException {
        return dispatchX(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public float dispatchX(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getX();
    }

    @NonExtendable
    public float dispatchX(OfflinePlayer player, Advancement advancement) throws UserNotLoadedException {
        return dispatchX(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public float dispatchX(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getX();
    }

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @return The y coordinate.
     */
    public abstract float getY();

    @NonExtendable
    public float dispatchY(Player player, Advancement advancement) throws UserNotLoadedException {
        return dispatchY(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public float dispatchY(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getY();
    }

    @NonExtendable
    public float dispatchY(OfflinePlayer player, Advancement advancement) throws UserNotLoadedException {
        return dispatchY(player, advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player));
    }

    @NonExtendable
    public float dispatchY(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getY();
    }

    /**
     * Returns the NMS wrapper of the display.
     *
     * @return The NMS wrapper of the display.
     */
    @NotNull
    public abstract PreparedAdvancementDisplayWrapper getNMSWrapper();

    @NonExtendable
    public PreparedAdvancementDisplayWrapper dispatchGetNMSWrapper(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getNMSWrapper();
    }
}
