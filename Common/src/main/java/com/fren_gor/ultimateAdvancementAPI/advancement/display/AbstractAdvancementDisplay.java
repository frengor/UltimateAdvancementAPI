package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.IllegalOperationException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotLoadedException;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A display which contains the graphical information of the advancement, like the title, description, icon, etc.
 * <p>It is extended only by {@link AbstractImmutableAdvancementDisplay}, {@link AbstractPerTeamAdvancementDisplay} and {@link AbstractPerPlayerAdvancementDisplay}.
 * It cannot be extended by any other class, which should extend one of those instead.
 * <p>To create immutable, per-team and per-player displays extend {@link AbstractImmutableAdvancementDisplay}, {@link AbstractPerTeamAdvancementDisplay} and {@link AbstractPerPlayerAdvancementDisplay}, respectively.
 * <p>A default implementation of an immutable display is {@link AdvancementDisplay}.
 * <br>
 * <h2>Dispatch methods</h2>
 * <p>To make it easier to work with displays, this class provides special <i>dispatch methods</i>, which automatically
 * calls the correct method on the correct subclass.
 * <p>Thus, they're useful when a method needs to be called on a display which isn't known if it's per-player, per-team or immutable. For example:
 * <pre> {@code public Material getMaterialOfIcon(AbstractAdvancementDisplay display, Player player) {
 *   UltimateAdvancementAPI API = ...;
 *
 *   // display can be anything! Instead of using a bunch of ifs and instanceof, use a dispatch method
 *   ItemStack icon = display.dispatchGetIcon(player, API.getTeamProgression(player));
 *
 *   return icon.getType();
 * }}</pre>
 *
 * @see AbstractImmutableAdvancementDisplay
 * @see AbstractPerPlayerAdvancementDisplay
 * @see AbstractPerTeamAdvancementDisplay
 * @see AdvancementDisplay
 */
public abstract class AbstractAdvancementDisplay {

    AbstractAdvancementDisplay() {
        // Validate class inheritance. This makes sure no reflection is being used to make an invalid AbstractAdvancementDisplay
        if (!(this instanceof AbstractImmutableAdvancementDisplay || this instanceof AbstractPerPlayerAdvancementDisplay || this instanceof AbstractPerTeamAdvancementDisplay)) {
            throw new IllegalOperationException(getClass().getName() + " is not an instance of AbstractImmutableAdvancementDisplay, AbstractPerTeamAdvancementDisplay or AbstractPerPlayerAdvancementDisplay.");
        }
    }

    /**
     * Dispatches the call to doesShowToast(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return Whether the toast notification should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    public final boolean dispatchDoesShowToast(@NotNull Player player, @NotNull AdvancementTab advancementTab) {
        return dispatchDoesShowToast(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to doesShowToast(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return Whether the toast notification should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    public abstract boolean dispatchDoesShowToast(@NotNull Player player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to doesShowToast(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return Whether the toast notification should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    public final boolean dispatchDoesShowToast(@NotNull OfflinePlayer player, @NotNull AdvancementTab advancementTab) {
        return dispatchDoesShowToast(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to doesShowToast(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return Whether the toast notification should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    public abstract boolean dispatchDoesShowToast(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to doesAnnounceToChat(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return Whether the advancement completion message should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    public final boolean dispatchDoesAnnounceToChat(@NotNull Player player, @NotNull AdvancementTab advancementTab) {
        return dispatchDoesAnnounceToChat(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to doesAnnounceToChat(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return Whether the advancement completion message should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    public abstract boolean dispatchDoesAnnounceToChat(@NotNull Player player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to doesAnnounceToChat(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return Whether the advancement completion message should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    public final boolean dispatchDoesAnnounceToChat(@NotNull OfflinePlayer player, @NotNull AdvancementTab advancementTab) {
        return dispatchDoesAnnounceToChat(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to doesAnnounceToChat(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return Whether the advancement completion message should be sent on advancement grant.
     * @see AbstractAdvancementDisplay
     */
    public abstract boolean dispatchDoesAnnounceToChat(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getIcon(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The icon of the advancement.
     * @see AbstractAdvancementDisplay
     */
    public final ItemStack dispatchGetIcon(@NotNull Player player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetIcon(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getIcon(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The icon of the advancement.
     * @see AbstractAdvancementDisplay
     */
    public abstract ItemStack dispatchGetIcon(@NotNull Player player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getIcon(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The icon of the advancement.
     * @see AbstractAdvancementDisplay
     */
    public final ItemStack dispatchGetIcon(@NotNull OfflinePlayer player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetIcon(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getIcon(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The icon of the advancement.
     * @see AbstractAdvancementDisplay
     */
    public abstract ItemStack dispatchGetIcon(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getTitle(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The title of the advancement as a legacy string.
     * @see AbstractAdvancementDisplay
     */
    public final String dispatchGetTitle(@NotNull Player player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetTitle(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getTitle(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The title of the advancement as a legacy string.
     * @see AbstractAdvancementDisplay
     */
    public abstract String dispatchGetTitle(@NotNull Player player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getTitle(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The title of the advancement as a legacy string.
     * @see AbstractAdvancementDisplay
     */
    public final String dispatchGetTitle(@NotNull OfflinePlayer player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetTitle(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getTitle(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The title of the advancement as a legacy string.
     * @see AbstractAdvancementDisplay
     */
    public abstract String dispatchGetTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getTitleBaseComponent(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The title of the advancement.
     * @see AbstractAdvancementDisplay
     */
    public final BaseComponent[] dispatchGetTitleBaseComponent(@NotNull Player player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetTitleBaseComponent(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getTitleBaseComponent(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The title of the advancement.
     * @see AbstractAdvancementDisplay
     */
    public abstract BaseComponent[] dispatchGetTitleBaseComponent(@NotNull Player player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getTitleBaseComponent(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The title of the advancement.
     * @see AbstractAdvancementDisplay
     */
    public final BaseComponent[] dispatchGetTitleBaseComponent(@NotNull OfflinePlayer player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetTitleBaseComponent(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getTitleBaseComponent(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The title of the advancement.
     * @see AbstractAdvancementDisplay
     */
    public abstract BaseComponent[] dispatchGetTitleBaseComponent(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getDescription(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The description of the advancement as a list of legacy strings.
     * @see AbstractAdvancementDisplay
     */
    public final List<String> dispatchGetDescription(@NotNull Player player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetDescription(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getDescription(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The description of the advancement as a list of legacy strings.
     * @see AbstractAdvancementDisplay
     */
    public abstract List<String> dispatchGetDescription(@NotNull Player player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getDescription(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The description of the advancement as a list of legacy strings.
     * @see AbstractAdvancementDisplay
     */
    public final List<String> dispatchGetDescription(@NotNull OfflinePlayer player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetDescription(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getDescription(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The description of the advancement as a list of legacy strings.
     * @see AbstractAdvancementDisplay
     */
    public abstract List<String> dispatchGetDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getDescriptionBaseComponent(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The description of the advancement.
     * @see AbstractAdvancementDisplay
     */
    public final List<BaseComponent[]> dispatchGetDescriptionBaseComponent(@NotNull Player player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetDescriptionBaseComponent(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getDescriptionBaseComponent(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The description of the advancement.
     * @see AbstractAdvancementDisplay
     */
    public abstract List<BaseComponent[]> dispatchGetDescriptionBaseComponent(@NotNull Player player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getDescriptionBaseComponent(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The description of the advancement.
     * @see AbstractAdvancementDisplay
     */
    public final List<BaseComponent[]> dispatchGetDescriptionBaseComponent(@NotNull OfflinePlayer player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetDescriptionBaseComponent(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getDescriptionBaseComponent(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The description of the advancement.
     * @see AbstractAdvancementDisplay
     */
    public abstract List<BaseComponent[]> dispatchGetDescriptionBaseComponent(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getFrame(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The shape of the advancement frame in the advancement GUI.
     * @see AbstractAdvancementDisplay
     */
    public final AdvancementFrameType dispatchGetFrame(@NotNull Player player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetFrame(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getFrame(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The shape of the advancement frame in the advancement GUI.
     * @see AbstractAdvancementDisplay
     */
    public abstract AdvancementFrameType dispatchGetFrame(@NotNull Player player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getFrame(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The shape of the advancement frame in the advancement GUI.
     * @see AbstractAdvancementDisplay
     */
    public final AdvancementFrameType dispatchGetFrame(@NotNull OfflinePlayer player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetFrame(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getFrame(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The shape of the advancement frame in the advancement GUI.
     * @see AbstractAdvancementDisplay
     */
    public abstract AdvancementFrameType dispatchGetFrame(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getX(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The x coordinate.
     * @see AbstractAdvancementDisplay
     */
    public final float dispatchGetX(@NotNull Player player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetX(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getX(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The x coordinate.
     * @see AbstractAdvancementDisplay
     */
    public abstract float dispatchGetX(@NotNull Player player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getX(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The x coordinate.
     * @see AbstractAdvancementDisplay
     */
    public final float dispatchGetX(@NotNull OfflinePlayer player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetX(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getX(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The x coordinate.
     * @see AbstractAdvancementDisplay
     */
    public abstract float dispatchGetX(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getY(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The y coordinate.
     * @see AbstractAdvancementDisplay
     */
    public final float dispatchGetY(@NotNull Player player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetY(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getY(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The y coordinate.
     * @see AbstractAdvancementDisplay
     */
    public abstract float dispatchGetY(@NotNull Player player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getY(...).
     *
     * @param player The player used to dispatch the call.
     * @param advancementTab The advancement tab used to dispatch the call.
     * @return The y coordinate.
     * @see AbstractAdvancementDisplay
     */
    public final float dispatchGetY(@NotNull OfflinePlayer player, @NotNull AdvancementTab advancementTab) throws UserNotLoadedException {
        return dispatchGetY(player, advancementTab.getDatabaseManager().getTeamProgression(player));
    }

    /**
     * Dispatches the call to getY(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The y coordinate.
     * @see AbstractAdvancementDisplay
     */
    public abstract float dispatchGetY(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression);

    /**
     * Dispatches the call to getNMSWrapper(...).
     *
     * @param player The player used to dispatch the call.
     * @param teamProgression The team used to dispatch the call.
     * @return The NMS wrapper of the display.
     * @see AbstractAdvancementDisplay
     */
    public abstract PreparedAdvancementDisplayWrapper dispatchGetNMSWrapper(@NotNull Player player, @NotNull TeamProgression teamProgression);
}
