package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.announcementMessage.IAnnouncementMessage;
import com.fren_gor.ultimateAdvancementAPI.chat.IComponent;
import com.fren_gor.ultimateAdvancementAPI.chat.IDefaultStyle;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A display which is immutable, that is every method call should always return the same value.
 * <p>An implementation of an immutable display is {@link AdvancementDisplay}.
 * <br>
 * <h2>Optimizing immutable displays</h2>
 * <p>It's suggested to create and store the values to be returned by the various methods once and simply return them afterward
 * (this technique is also known as <a href="https://en.wikipedia.org/wiki/Memoization">memoization</a>).
 * The most important method where this applies is {@link #getNMSWrapper()}, since reducing the creation of NMS wrappers
 * usually leads to better performance.
 */
public abstract non-sealed class AbstractImmutableAdvancementDisplay extends AbstractAdvancementDisplay {

    @LazyValue
    private PreparedAdvancementDisplayWrapper wrapper;

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
     * @return The title of the advancement as a legacy string.
     * @implSpec The default implementation returns the title returned by {@link #getTitle()} converted into a legacy string.
     */
    @NotNull
    public String getLegacyTitle() {
        return getTitle().toLegacy();
    }

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public abstract IComponent getTitle();

    /**
     * Returns the default style of the title when displayed in the advancement GUI.
     *
     * @return The default style of the title when displayed in the advancement GUI.
     *     {@link IDefaultStyle#empty()} can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link IDefaultStyle#empty()}.
     */
    @NotNull
    public IDefaultStyle getDefaultTitleStyle() {
        return IDefaultStyle.empty();
    }

    /**
     * Returns the default style of the title in the advancement's announcement message.
     *
     * @return The default style of the title in the advancement's announcement message.
     *     {@link IDefaultStyle#empty()} can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link IDefaultStyle#empty()}.
     * @see IAnnouncementMessage
     * @see Advancement#getAnnouncementMessage(Player)
     */
    @NotNull
    public IDefaultStyle getAnnouncementMessageDefaultTitleStyle() {
        return IDefaultStyle.empty();
    }

    /**
     * Returns the description of the advancement as a list of legacy strings.
     *
     * @return The description of the advancement as a list of legacy strings.
     * @implSpec The default implementation returns the description returned by {@link #getDescription()} converted into a list of legacy strings.
     */
    @NotNull
    @Unmodifiable
    public List<String> getLegacyDescription() {
        return getDescription().stream().map(IComponent::toLegacy).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @NotNull
    @Unmodifiable
    public abstract List<IComponent> getDescription();

    /**
     * Returns the default style of the description when displayed in the advancement GUI.
     *
     * @return The default style of the description when displayed in the advancement GUI.
     *     {@link IDefaultStyle#empty()} can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link IDefaultStyle#empty()}.
     */
    @NotNull
    public IDefaultStyle getDefaultDescriptionStyle() {
        return IDefaultStyle.empty();
    }

    /**
     * Returns the default style of the description in the advancement's announcement message.
     *
     * @return The default style of the description in the advancement's announcement message.
     *     {@link IDefaultStyle#empty()} can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link IDefaultStyle#empty()}.
     * @see IAnnouncementMessage
     * @see Advancement#getAnnouncementMessage(Player)
     */
    @NotNull
    public IDefaultStyle getAnnouncementMessageDefaultDescriptionStyle() {
        return IDefaultStyle.empty();
    }

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
     * @throws ReflectiveOperationException If the creation of the NMS wrapper fails due to a reflection error.
     */
    @NotNull
    public PreparedAdvancementDisplayWrapper getNMSWrapper() throws ReflectiveOperationException {
        if (wrapper != null) {
            return wrapper;
        }

        IComponent title = getTitle().applyDefaultStyle(getDefaultTitleStyle());
        IComponent description = IComponent.empty()
            .applyDefaultStyle(getDefaultDescriptionStyle())
            .setChildren(AdvancementUtils.intersperse(getDescription(), IComponent.newLine()));
        return wrapper = PreparedAdvancementDisplayWrapper.craft(getIcon(), title.toJson(), description.toJson(), getFrame().getNMSWrapper(), getX(), getY());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesShowToast(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return doesShowToast();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesShowToast(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return doesShowToast();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesAnnounceToChat(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return doesAnnounceToChat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesAnnounceToChat(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return doesAnnounceToChat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final ItemStack dispatchGetIcon(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getIcon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final ItemStack dispatchGetIcon(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getIcon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final String dispatchGetLegacyTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getLegacyTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final String dispatchGetLegacyTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getLegacyTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IComponent dispatchGetTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IComponent dispatchGetTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetDefaultTitleStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDefaultTitleStyle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetDefaultTitleStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDefaultTitleStyle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetAnnouncementMessageDefaultTitleStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultTitleStyle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetAnnouncementMessageDefaultTitleStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultTitleStyle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Unmodifiable
    public final List<String> dispatchGetLegacyDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getLegacyDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Unmodifiable
    public final List<String> dispatchGetLegacyDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getLegacyDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Unmodifiable
    public final List<IComponent> dispatchGetDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Unmodifiable
    public final List<IComponent> dispatchGetDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetDefaultDescriptionStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDefaultDescriptionStyle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetDefaultDescriptionStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDefaultDescriptionStyle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetAnnouncementMessageDefaultDescriptionStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultDescriptionStyle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetAnnouncementMessageDefaultDescriptionStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultDescriptionStyle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final AdvancementFrameType dispatchGetFrame(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getFrame();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final AdvancementFrameType dispatchGetFrame(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getFrame();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetX(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getX();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetX(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getX();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetY(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getY();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetY(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getY();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final PreparedAdvancementDisplayWrapper dispatchGetNMSWrapper(@NotNull Player player, @NotNull TeamProgression teamProgression) throws ReflectiveOperationException {
        return getNMSWrapper();
    }
}
