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
    public boolean dispatchDoesToast(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {

        return doesShowToast(databaseManager.getTeamProgression(player));

    }

    @Override
    public boolean dispatchDoesAnnounceToChat(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        return doesAnnounceToChat(databaseManager.getTeamProgression(player));
    }

    @Override
    public ItemStack dispatchIcon(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        return getIcon(databaseManager.getTeamProgression(player));
    }

    @Override
    public String dispatchTitle(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        return getTitle(databaseManager.getTeamProgression(player));
    }

    @Override
    public BaseComponent[] dispatchTitleBaseComponent(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        return getTitleBaseComponent(databaseManager.getTeamProgression(player));
    }

    @Override
    public List<String> dispatchDescription(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        return getDescription(databaseManager.getTeamProgression(player));
    }

    @Override
    public List<BaseComponent[]> dispatchDescriptionBaseComponent(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        return getDescriptionBaseComponent(databaseManager.getTeamProgression(player));
    }

    @Override
    public AdvancementFrameType dispatchFrame(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        return getFrame(databaseManager.getTeamProgression(player));
    }

    @Override
    public float dispatchX(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        return getX(databaseManager.getTeamProgression(player));
    }

    @Override
    public float dispatchY(AbstractAdvancementDisplay display, Player player, DatabaseManager databaseManager) {
        return getY(databaseManager.getTeamProgression(player));
    }
}
