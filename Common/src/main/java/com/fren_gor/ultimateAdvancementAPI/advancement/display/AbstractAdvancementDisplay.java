package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotLoadedException;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

    public boolean dispatchDoesToast(Player player, Advancement advancement) {
        return dispatchDoesToast(player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public boolean dispatchDoesToast(Player player, DatabaseManager databaseManager) {
        return dispatchDoesToast(player, databaseManager.getTeamProgression(player));
    }

    public boolean dispatchDoesToast(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return doesShowToast();
    }

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     *
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    public abstract boolean doesAnnounceToChat();

    public boolean dispatchDoesAnnounceToChat(Player player, Advancement advancement) {
        return dispatchDoesAnnounceToChat(player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public boolean dispatchDoesAnnounceToChat(Player player, DatabaseManager databaseManager) {
        return dispatchDoesAnnounceToChat(player, databaseManager.getTeamProgression(player));
    }

    public boolean dispatchDoesAnnounceToChat(Player player, TeamProgression teamProgression) {
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

    public ItemStack dispatchIcon(Player player, Advancement advancement) {
        return dispatchIcon(player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public ItemStack dispatchIcon(Player player, DatabaseManager databaseManager) {
        return dispatchIcon(player, databaseManager.getTeamProgression(player));
    }

    public ItemStack dispatchIcon(Player player, TeamProgression teamProgression) {
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

    public String dispatchTitle(Player player, Advancement advancement) {
        return dispatchTitle(player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public String dispatchTitle(Player player, DatabaseManager databaseManager) {
        return dispatchTitle(player, databaseManager.getTeamProgression(player));
    }

    public String dispatchTitle(Player player, TeamProgression teamProgression) {
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

    public BaseComponent[] dispatchTitleBaseComponent(Player player, Advancement advancement) {
        return dispatchTitleBaseComponent(player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public BaseComponent[] dispatchTitleBaseComponent(Player player, DatabaseManager databaseManager) {
        return dispatchTitleBaseComponent(player, databaseManager.getTeamProgression(player));
    }

    public BaseComponent[] dispatchTitleBaseComponent(Player player, TeamProgression teamProgression) {
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

    public List<String> dispatchDescription(Player player, Advancement advancement) {
        return dispatchDescription(player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public List<String> dispatchDescription(Player player, DatabaseManager databaseManager) {
        return dispatchDescription(player, databaseManager.getTeamProgression(player));
    }

    public List<String> dispatchDescription(Player player, TeamProgression teamProgression) {
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

    public List<BaseComponent[]> dispatchDescriptionBaseComponent(Player player, Advancement advancement) {
        return dispatchDescriptionBaseComponent(player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public List<BaseComponent[]> dispatchDescriptionBaseComponent(Player player, DatabaseManager databaseManager) {
        return dispatchDescriptionBaseComponent(player, databaseManager.getTeamProgression(player));
    }

    public List<BaseComponent[]> dispatchDescriptionBaseComponent(Player player, TeamProgression teamProgression) {
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

    public AdvancementFrameType dispatchFrame(Player player, Advancement advancement) {
        return dispatchFrame(player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public AdvancementFrameType dispatchFrame(Player player, DatabaseManager databaseManager) {
        return dispatchFrame(player, databaseManager.getTeamProgression(player));
    }

    public AdvancementFrameType dispatchFrame(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getFrame();
    }

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @return The x coordinate.
     */
    public abstract float getX();

    public float dispatchX(Player player, Advancement advancement) {
        return dispatchX(player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public float dispatchX(Player player, DatabaseManager databaseManager) {
        return dispatchX(player, databaseManager.getTeamProgression(player));
    }

    public float dispatchX(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getX();
    }

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @return The y coordinate.
     */
    public abstract float getY();

    public float dispatchY(Player player, Advancement advancement) {
        return dispatchY(player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public float dispatchY(Player player, DatabaseManager databaseManager) {
        return dispatchY(player, databaseManager.getTeamProgression(player));
    }

    public float dispatchY(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getY();
    }

    /**
     * Returns the {@code AdvancementDisplay} NMS wrapper, using the provided advancement for construction (when necessary).
     *
     * @param advancement The advancement used, when necessary, to create the NMS wrapper. Must be not {@code null}.
     * @return The {@code AdvancementDisplay} NMS wrapper.
     */
    @NotNull
    public abstract AdvancementDisplayWrapper getNMSWrapper(@NotNull Advancement advancement);

}
