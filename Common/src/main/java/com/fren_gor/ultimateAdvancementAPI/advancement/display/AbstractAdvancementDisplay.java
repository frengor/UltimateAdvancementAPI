package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
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

    public boolean dispatchDoesToast(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchDoesToast(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public boolean dispatchDoesToast(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        // This method is overridden in per-player and per-team classes
        return display.doesShowToast();
    }

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     *
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    public abstract boolean doesAnnounceToChat();

    public boolean dispatchDoesAnnounceToChat(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchDoesAnnounceToChat(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public boolean dispatchDoesAnnounceToChat(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        // This method is overridden in per-player and per-team classes
        return display.doesAnnounceToChat();
    }

    /**
     * Gets a clone of the icon.
     *
     * @return A clone of the icon.
     */
    @NotNull
    public abstract ItemStack getIcon();

    public ItemStack dispatchIcon(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchIcon(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public ItemStack dispatchIcon(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        // This method is overridden in per-player and per-team classes
        return display.getIcon();
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

    public String dispatchTitle(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchTitle(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public String dispatchTitle(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        // This method is overridden in per-player and per-team classes
        return display.getTitle();
    }

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public abstract BaseComponent[] getTitleBaseComponent();

    public BaseComponent[] dispatchTitleBaseComponent(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchTitleBaseComponent(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public BaseComponent[] dispatchTitleBaseComponent(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        // This method is overridden in per-player and per-team classes
        return display.getTitleBaseComponent();
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

    public List<String> dispatchDescription(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchDescription(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public List<String> dispatchDescription(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        // This method is overridden in per-player and per-team classes
        return display.getDescription();
    }

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public abstract List<BaseComponent[]> getDescriptionBaseComponent();

    public List<BaseComponent[]> dispatchDescriptionBaseComponent(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchDescriptionBaseComponent(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public List<BaseComponent[]> dispatchDescriptionBaseComponent(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        // This method is overridden in per-player and per-team classes
        return display.getDescriptionBaseComponent();
    }

    /**
     * Returns the shape of the advancement frame in the advancement GUI.
     *
     * @return The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
    public abstract AdvancementFrameType getFrame();

    public AdvancementFrameType dispatchFrame(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchFrame(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public AdvancementFrameType dispatchFrame(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        // This method is overridden in per-player and per-team classes
        return display.getFrame();
    }

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @return The x coordinate.
     */
    public abstract float getX();

    public float dispatchX(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchX(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public float dispatchX(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        // This method is overridden in per-player and per-team classes
        return display.getX();
    }

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @return The y coordinate.
     */
    public abstract float getY();

    public float dispatchY(AbstractAdvancementDisplay display, Player player, Advancement advancement) {
        return dispatchY(display, player, advancement.getAdvancementTab().getDatabaseManager());
    }

    public float dispatchY(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        // This method is overridden in per-player and per-team classes
        return display.getY();
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
