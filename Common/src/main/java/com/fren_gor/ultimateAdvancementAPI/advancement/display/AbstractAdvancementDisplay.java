package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
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

/**
 * A display which contains the graphical information of the advancement, like the title, description, icon, etc.
 * <p>Direct implementations of this abstract class are supposed to be immutable, that is every method call should always return the same value.
 * <br>To create per-player and per-team displays extend {@link AbstractPerPlayerAdvancementDisplay} and {@link AbstractPerTeamAdvancementDisplay}, respectively.
 * <p>A default implementation of an immutable display is {@link AdvancementDisplay}.
 * <br>
 * <h2>Dispatch methods</h2>
 * <p>To make it easier to work with displays, this class provides special {@code dispatch*} methods, which automatically
 * calls the per-player methods if the instance on which they're called is a per-player display,
 * the per-team methods if the instance is a per-team display, otherwise the methods of this class are called.
 * <p>Thus, they're useful when a method needs to be called on a display which isn't known if it's per-player, per-team or immutable. For example:
 * <pre> {@code public Material getMaterialOfIcon(AbstractAdvancementDisplay display, Player player) {
 *   UltimateAdvancementAPI API = ...;
 *
 *   // display can be anything! Instead of using a bunch of ifs and instanceof, use a dispatch method
 *   ItemStack icon = display.dispatchGetIcon(player, API.getTeamProgression(player));
 *
 *   return icon.getType();
 * }}</pre>
 * <h2>Optimizing immutable displays</h2>
 * <p>Classes which <i>directly</i> extends {@code AbstractAdvancementDisplay} (i.e. {@code class *className* extends AbstractAdvancementDisplay})
 * are considered immutable, so every method call should always return the same value.
 * <br>Thus, it's suggested to create and store the values to be returned by the various methods once and simply returning them afterward.
 * The most important method where this applies is {@link #getNMSWrapper()}, since reducing the creation of NMS wrappers
 * usually leads to improved performance.
 *
 * @see AbstractPerPlayerAdvancementDisplay
 * @see AbstractPerTeamAdvancementDisplay
 * @see AdvancementDisplay
 */
public abstract class AbstractAdvancementDisplay {

    /**
     * Returns whether the toast notification should be sent on advancement grant.
     *
     * @return Whether the toast notification should be sent on advancement grant.
     */
    public abstract boolean doesShowToast();

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     *
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    public abstract boolean doesAnnounceToChat();

    /**
     * Returns the icon of the advancement.
     *
     * @return The icon of the advancement.
     */
    @NotNull
    public abstract ItemStack getIcon();

    /**
     * Returns the title of the advancement as a legacy string.
     *
     * @implNote The default implementation returns the title returned by {@link #getTitleBaseComponent()} converted into a legacy string.
     *
     * @return The title of the advancement as a legacy string.
     */
    @NotNull
    public String getTitle() {
        return TextComponent.toLegacyText(getTitleBaseComponent());
    }

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public abstract BaseComponent[] getTitleBaseComponent();

    /**
     * Returns the description of the advancement as a list of legacy strings.
     *
     * @implNote The default implementation returns the description returned by {@link #getDescriptionBaseComponent()} converted into a list of legacy strings.
     *
     * @return The description of the advancement as a list of legacy strings.
     */
    @Unmodifiable
    public List<String> getDescription() {
        return getDescriptionBaseComponent().stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public abstract List<BaseComponent[]> getDescriptionBaseComponent();

    /**
     * Returns the shape of the advancement frame in the advancement GUI.
     *
     * @return The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
    public abstract AdvancementFrameType getFrame();

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @return The x coordinate.
     */
    public abstract float getX();

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @return The y coordinate.
     */
    public abstract float getY();

    /**
     * Returns the NMS wrapper of the display.
     *
     * @return The NMS wrapper of the display.
     */
    @NotNull
    public abstract PreparedAdvancementDisplayWrapper getNMSWrapper();

    /**
     * Dispatches the call to {@link #doesShowToast()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return Whether the toast notification should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public boolean dispatchDoesShowToast(Player player, AdvancementTab advancementTab) {
        return dispatchDoesShowToast(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #doesShowToast()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return Whether the toast notification should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public boolean dispatchDoesShowToast(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return doesShowToast();
    }

    /**
     * Dispatches the call to {@link #doesShowToast()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return Whether the toast notification should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public boolean dispatchDoesShowToast(OfflinePlayer player, AdvancementTab advancementTab) {
        return dispatchDoesShowToast(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #doesShowToast()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return Whether the toast notification should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public boolean dispatchDoesShowToast(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return doesShowToast();
    }

    /**
     * Dispatches the call to {@link #doesAnnounceToChat()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return Whether the advancement completion message should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public boolean dispatchDoesAnnounceToChat(Player player, AdvancementTab advancementTab) {
        return dispatchDoesAnnounceToChat(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #doesAnnounceToChat()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return Whether the advancement completion message should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public boolean dispatchDoesAnnounceToChat(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return doesAnnounceToChat();
    }

    /**
     * Dispatches the call to {@link #doesAnnounceToChat()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return Whether the advancement completion message should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public boolean dispatchDoesAnnounceToChat(OfflinePlayer player, AdvancementTab advancementTab) {
        return dispatchDoesAnnounceToChat(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #doesAnnounceToChat()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return Whether the advancement completion message should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public boolean dispatchDoesAnnounceToChat(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return doesAnnounceToChat();
    }

    /**
     * Dispatches the call to {@link #getIcon()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The icon of the advancement.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public ItemStack dispatchGetIcon(Player player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetIcon(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getIcon()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The icon of the advancement.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public ItemStack dispatchGetIcon(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getIcon();
    }

    /**
     * Dispatches the call to {@link #getIcon()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The icon of the advancement.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public ItemStack dispatchGetIcon(OfflinePlayer player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetIcon(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getIcon()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The icon of the advancement.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public ItemStack dispatchGetIcon(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getIcon();
    }

    /**
     * Dispatches the call to {@link #getTitle()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The title of the advancement as a legacy string.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public String dispatchGetTitle(Player player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetTitle(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getTitle()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The title of the advancement as a legacy string.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public String dispatchGetTitle(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getTitle();
    }

    /**
     * Dispatches the call to {@link #getTitle()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The title of the advancement as a legacy string.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public String dispatchGetTitle(OfflinePlayer player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetTitle(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getTitle()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The title of the advancement as a legacy string.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public String dispatchGetTitle(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getTitle();
    }

    /**
     * Dispatches the call to {@link #getTitleBaseComponent()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The title of the advancement.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public BaseComponent[] dispatchGetTitleBaseComponent(Player player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetTitleBaseComponent(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getTitleBaseComponent()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The title of the advancement.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public BaseComponent[] dispatchGetTitleBaseComponent(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getTitleBaseComponent();
    }

    /**
     * Dispatches the call to {@link #getTitleBaseComponent()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The title of the advancement.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public BaseComponent[] dispatchGetTitleBaseComponent(OfflinePlayer player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetTitleBaseComponent(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getTitleBaseComponent()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The title of the advancement.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public BaseComponent[] dispatchGetTitleBaseComponent(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getTitleBaseComponent();
    }

    /**
     * Dispatches the call to {@link #getDescription()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The description of the advancement as a list of legacy strings.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public List<String> dispatchGetDescription(Player player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetDescription(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getDescription()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The description of the advancement as a list of legacy strings.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public List<String> dispatchGetDescription(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getDescription();
    }

    /**
     * Dispatches the call to {@link #getDescription()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The description of the advancement as a list of legacy strings.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public List<String> dispatchGetDescription(OfflinePlayer player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetDescription(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getDescription()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The description of the advancement as a list of legacy strings.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public List<String> dispatchGetDescription(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getDescription();
    }

    /**
     * Dispatches the call to {@link #getDescriptionBaseComponent()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The description of the advancement.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public List<BaseComponent[]> dispatchGetDescriptionBaseComponent(Player player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetDescriptionBaseComponent(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getDescriptionBaseComponent()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The description of the advancement.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public List<BaseComponent[]> dispatchGetDescriptionBaseComponent(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getDescriptionBaseComponent();
    }

    /**
     * Dispatches the call to {@link #getDescriptionBaseComponent()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The description of the advancement.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public List<BaseComponent[]> dispatchGetDescriptionBaseComponent(OfflinePlayer player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetDescriptionBaseComponent(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getDescriptionBaseComponent()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The description of the advancement.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public List<BaseComponent[]> dispatchGetDescriptionBaseComponent(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getDescriptionBaseComponent();
    }

    /**
     * Dispatches the call to {@link #getFrame()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The shape of the advancement frame in the advancement GUI.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public AdvancementFrameType dispatchGetFrame(Player player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetFrame(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getFrame()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The shape of the advancement frame in the advancement GUI.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public AdvancementFrameType dispatchGetFrame(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getFrame();
    }

    /**
     * Dispatches the call to {@link #getFrame()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The shape of the advancement frame in the advancement GUI.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public AdvancementFrameType dispatchGetFrame(OfflinePlayer player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetFrame(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getFrame()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The shape of the advancement frame in the advancement GUI.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public AdvancementFrameType dispatchGetFrame(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getFrame();
    }

    /**
     * Dispatches the call to {@link #getX()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The x coordinate.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public float dispatchGetX(Player player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetX(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getX()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The x coordinate.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public float dispatchGetX(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getX();
    }

    /**
     * Dispatches the call to {@link #getX()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The x coordinate.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public float dispatchGetX(OfflinePlayer player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetX(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getX()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The x coordinate.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public float dispatchGetX(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getX();
    }

    /**
     * Dispatches the call to {@link #getY()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The y coordinate.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public float dispatchGetY(Player player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetY(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getY()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The y coordinate.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public float dispatchGetY(Player player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getY();
    }

    /**
     * Dispatches the call to {@link #getY()}.
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The y coordinate.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public float dispatchGetY(OfflinePlayer player, AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetY(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to {@link #getY()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The y coordinate.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public float dispatchGetY(OfflinePlayer player, TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getY();
    }

    /**
     * Dispatches the call to {@link #getNMSWrapper()}.
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The NMS wrapper of the display.
     * @see AbstractAdvancementDisplay
     */
    @NonExtendable
    public PreparedAdvancementDisplayWrapper dispatchGetNMSWrapper(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        // This method is overridden in per-player and per-team classes
        return getNMSWrapper();
    }
}
