package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A display which is immutable, that is every method call should always return the same value.
 * <p>A default implementation of an immutable display is {@link AdvancementDisplay}.
 * <br>
 * <h2>Optimizing immutable displays</h2>
 * <p>It's suggested to create and store the values to be returned by the various methods once and simply return them afterward
 * (this technique is also known as <a href="https://en.wikipedia.org/wiki/Memoization">memoization</a>).
 * The most important method where this applies is {@link #getNMSWrapper()}, since reducing the creation of NMS wrappers
 * usually leads to better performance.
 */
public abstract class AbstractImmutableAdvancementDisplay extends AbstractAdvancementDisplay {

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
    @NotNull
    public List<String> getDescription() {
        return getDescriptionBaseComponent().stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @NotNull
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
     * {@inheritDoc}
     */
    public final boolean dispatchDoesShowToast(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return doesShowToast();
    }

    /**
     * {@inheritDoc}
     */
    public final boolean dispatchDoesShowToast(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return doesShowToast();
    }

    /**
     * {@inheritDoc}
     */
    public final boolean dispatchDoesAnnounceToChat(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return doesAnnounceToChat();
    }

    /**
     * {@inheritDoc}
     */
    public final boolean dispatchDoesAnnounceToChat(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return doesAnnounceToChat();
    }

    /**
     * {@inheritDoc}
     */
    public final ItemStack dispatchGetIcon(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getIcon();
    }

    /**
     * {@inheritDoc}
     */
    public final ItemStack dispatchGetIcon(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getIcon();
    }

    /**
     * {@inheritDoc}
     */
    public final String dispatchGetTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getTitle();
    }

    /**
     * {@inheritDoc}
     */
    public final String dispatchGetTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getTitle();
    }

    /**
     * {@inheritDoc}
     */
    public final BaseComponent[] dispatchGetTitleBaseComponent(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getTitleBaseComponent();
    }

    /**
     * {@inheritDoc}
     */
    public final BaseComponent[] dispatchGetTitleBaseComponent(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getTitleBaseComponent();
    }

    /**
     * {@inheritDoc}
     */
    public final List<String> dispatchGetDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    public final List<String> dispatchGetDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    public final List<BaseComponent[]> dispatchGetDescriptionBaseComponent(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDescriptionBaseComponent();
    }

    /**
     * {@inheritDoc}
     */
    public final List<BaseComponent[]> dispatchGetDescriptionBaseComponent(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDescriptionBaseComponent();
    }

    /**
     * {@inheritDoc}
     */
    public final AdvancementFrameType dispatchGetFrame(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getFrame();
    }

    /**
     * {@inheritDoc}
     */
    public final AdvancementFrameType dispatchGetFrame(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getFrame();
    }

    /**
     * {@inheritDoc}
     */
    public final float dispatchGetX(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getX();
    }

    /**
     * {@inheritDoc}
     */
    public final float dispatchGetX(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getX();
    }

    /**
     * {@inheritDoc}
     */
    public final float dispatchGetY(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getY();
    }

    /**
     * {@inheritDoc}
     */
    public final float dispatchGetY(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getY();
    }

    /**
     * {@inheritDoc}
     */
    public final PreparedAdvancementDisplayWrapper dispatchGetNMSWrapper(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getNMSWrapper();
    }
}
