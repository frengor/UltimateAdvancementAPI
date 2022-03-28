package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * The {@code IAdvancementDisplay} interface represents the graphical information of one (or even more) advancement(s),
 * like title, description, position, icon, etc.
 * <p>Many methods of this interface takes as the first argument a (nullable) player, which can be used, for example,
 * to implement a localized display (i.e. where the title and description changes based on the player language).
 * Another example might be a display where toast notifications are enabled only for some players.
 * <p>When the player parameter is {@code null}, then the method should return a not per-player value (i.e. a <i>default value</i>).
 * Going back to the example from before with the localized display, calling {@link #getTitle(Player) getTitle}(null)
 * may return the English title, since no player was specified.
 */
public interface IAdvancementDisplay {

    /**
     * Returns the advancement position relative to the x-axis.
     * <p>This is, by default, the same as calling {@link #getX(Player) getX}(null).
     *
     * @return The x coordinate.
     */
    default float getX() {
        return getX(null);
    }

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @param player The player who is seeing this advancement, or {@code null} for a non per-player return value.
     * @return The x coordinate.
     */
    float getX(@Nullable Player player);

    /**
     * Returns the advancement position relative to the y-axis.
     * <p>This is, by default, the same as calling {@link #getY(Player) getY}(null).
     *
     * @return The y coordinate.
     */
    default float getY() {
        return getY(null);
    }

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @param player The player who is seeing this advancement, or {@code null} for a non per-player return value.
     * @return The y coordinate.
     */
    float getY(@Nullable Player player);

    /**
     * Returns whether the toast notification should be sent on advancement grant.
     * <p>This is, by default, the same as calling {@link #doesShowToast(Player) doesShowToast}(null).
     *
     * @return Whether the toast notification should be sent on advancement grant.
     */
    default boolean doesShowToast() {
        return doesShowToast(null);
    }

    /**
     * Returns whether the toast notification should be sent on advancement grant.
     *
     * @param player The player who is seeing this advancement, or {@code null} for a non per-player return value.
     * @return Whether the toast notification should be sent on advancement grant.
     */
    boolean doesShowToast(@Nullable Player player);

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     * <p>This is, by default, the same as calling {@link #doesAnnounceToChat(Player) doesAnnounceToChat}(null).
     *
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    default boolean doesAnnounceToChat() {
        return doesAnnounceToChat(null);
    }

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     *
     * @param player The player who is seeing this advancement, or {@code null} for a non per-player return value.
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    boolean doesAnnounceToChat(@Nullable Player player);

    /**
     * Gets a clone of the icon.
     * <p>This is, by default, the same as calling {@link #getIcon(Player) getIcon}(null).
     *
     * @return A clone of the icon.
     */
    @NotNull
    default ItemStack getIcon() {
        return getIcon(null);
    }

    /**
     * Gets a clone of the icon.
     *
     * @param player The player who is seeing this advancement, or {@code null} for a non per-player return value.
     * @return A clone of the icon.
     */
    @NotNull
    ItemStack getIcon(@Nullable Player player);

    /**
     * Returns the title of the advancement.
     * <p>This is, by default, the same as calling {@link #getTitle(Player) getTitle}(null).
     *
     * @return The title of the advancement.
     */
    @NotNull
    default String getTitle() {
        return getTitle(null);
    }

    /**
     * Returns the title of the advancement.
     *
     * @param player The player who is seeing this advancement, or {@code null} for a non per-player return value.
     * @return The title of the advancement.
     */
    @NotNull
    String getTitle(@Nullable Player player);

    /**
     * Returns the description of the advancement.
     * <p>This is, by default, the same as calling {@link #getDescription(Player) getDescription}(null).
     *
     * @return An unmodifiable list containing the description of the advancement.
     */
    @NotNull
    @Unmodifiable
    default List<String> getDescription() {
        return getDescription(null);
    }

    /**
     * Returns the description of the advancement.
     *
     * @param player The player who is seeing this advancement, or {@code null} for a non per-player return value.
     * @return An unmodifiable list containing the description of the advancement.
     */
    @NotNull
    @Unmodifiable
    List<String> getDescription(@Nullable Player player);

    /**
     * Gets the {@link BaseComponent} array that contains the title to display on the chat.
     * <p>Note that the return value of this function might differ from the return of {@link #getTitle()} when displayed.
     * <p>Used by {@link Advancement#getAnnounceMessage(Player)}.
     * <p>This is, by default, the same as calling {@link #getChatTitle(Player) getChatTitle}(null).
     *
     * @return The {@link BaseComponent} array that contains the fancy title.
     */
    @NotNull
    default BaseComponent[] getChatTitle() {
        return getChatTitle(null);
    }

    /**
     * Gets the {@link BaseComponent} array that contains the title to display on the chat.
     * <p>Note that the return value of this function might differ from the return of {@link #getTitle(Player)} when displayed.
     * <p>Used by {@link Advancement#getAnnounceMessage(Player)}.
     *
     * @param player The player who is seeing this advancement, or {@code null} for a non per-player return value.
     * @return The {@link BaseComponent} array that contains the fancy title.
     */
    @NotNull
    BaseComponent[] getChatTitle(@Nullable Player player);

    /**
     * Gets the {@link BaseComponent} array that contains the description to display on the chat.
     * <p>Note that the return value of this function might differ from the return of {@link #getDescription()} when displayed.
     * <p>Used by {@link Advancement#getAnnounceMessage(Player)}.
     * <p>This is, by default, the same as calling {@link #getChatDescription(Player) getChatDescription}(null).
     *
     * @return The {@link BaseComponent} array that contains the fancy description.
     */
    @NotNull
    default BaseComponent[] getChatDescription() {
        return getChatDescription(null);
    }

    /**
     * Gets the {@link BaseComponent} array that contains the description to display on the chat.
     * <p>Note that the return value of this function might differ from the return of {@link #getDescription(Player)} when displayed.
     * <p>Used by {@link Advancement#getAnnounceMessage(Player)}.
     *
     * @param player The player who is seeing this advancement, or {@code null} for a non per-player return value.
     * @return The {@link BaseComponent} array that contains the fancy description.
     */
    @NotNull
    BaseComponent[] getChatDescription(@Nullable Player player);

    /**
     * Returns the shape of the advancement frame in the advancement GUI.
     * <p>This is, by default, the same as calling {@link #getFrame(Player) getFrame}(null).
     *
     * @return The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
    default AdvancementFrameType getFrame() {
        return getFrame(null);
    }

    /**
     * Returns the shape of the advancement frame in the advancement GUI.
     *
     * @param player The player who is seeing this advancement, or {@code null} for a non per-player return value.
     * @return The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
    AdvancementFrameType getFrame(@Nullable Player player);

    /**
     * Returns the {@code AdvancementDisplay} NMS wrapper, using the provided player and advancement for construction (when necessary).
     * <p>This is, by default, the same as calling {@link #getNMSWrapper(Player, Advancement) getNMSWrapper}(null, anAdvancement).
     *
     * @param advancement The advancement used, when necessary, to create the NMS wrapper. Must be not {@code null}.
     * @return The {@code AdvancementDisplay} NMS wrapper.
     */
    @NotNull
    default AdvancementDisplayWrapper getNMSWrapper(@NotNull Advancement advancement) {
        return getNMSWrapper(null, advancement);
    }

    /**
     * Returns the {@code AdvancementDisplay} NMS wrapper, using the provided player and advancement for construction (when necessary).
     *
     * @param player The player who is seeing this advancement, or {@code null} for a non per-player return value.
     * @param advancement The advancement used, when necessary, to create the NMS wrapper. Must be not {@code null}.
     * @return The {@code AdvancementDisplay} NMS wrapper.
     */
    @NotNull
    AdvancementDisplayWrapper getNMSWrapper(@Nullable Player player, @NotNull Advancement advancement);
}
