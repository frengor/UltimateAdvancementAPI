package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.announcementMessage.IAnnouncementMessage;
import com.fren_gor.ultimateAdvancementAPI.chat.IComponent;
import com.fren_gor.ultimateAdvancementAPI.chat.IDefaultStyle;
import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordComponent;
import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordDefaultStyle;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A display which provides customized values based on the specified player.
 *
 * @implSpec The default implementation of the methods which take a {@link Player} calls the respective method that takes an {@link OfflinePlayer}.
 */
public abstract non-sealed class AbstractPerPlayerAdvancementDisplay extends AbstractAdvancementDisplay {

    /**
     * Returns whether the toast notification should be sent on advancement grant.
     *
     * @param player The player.
     * @return Whether the toast notification should be sent on advancement grant.
     */
    public boolean doesShowToast(@NotNull Player player) {
        return doesShowToast((OfflinePlayer) player);
    }

    /**
     * Returns whether the toast notification should be sent on advancement grant.
     *
     * @param player The player.
     * @return Whether the toast notification should be sent on advancement grant.
     */
    public abstract boolean doesShowToast(@NotNull OfflinePlayer player);

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     *
     * @param player The player.
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    public boolean doesAnnounceToChat(@NotNull Player player) {
        return doesAnnounceToChat((OfflinePlayer) player);
    }

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     *
     * @param player The player.
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    public abstract boolean doesAnnounceToChat(@NotNull OfflinePlayer player);

    /**
     * Returns the icon of the advancement.
     *
     * @param player The player.
     * @return The icon of the advancement.
     */
    @NotNull
    public ItemStack getIcon(@NotNull Player player) {
        return getIcon((OfflinePlayer) player);
    }

    /**
     * Returns the icon of the advancement.
     *
     * @param player The player.
     * @return The icon of the advancement.
     */
    @NotNull
    public abstract ItemStack getIcon(@NotNull OfflinePlayer player);

    /**
     * Returns the title of the advancement as a legacy string.
     *
     * @param player The player.
     * @return The title of the advancement as a legacy string.
     * @implSpec The default implementation returns the title returned by {@link #getTitle(Player)} converted into a legacy string.
     */
    @NotNull
    public String getLegacyTitle(@NotNull Player player) {
        return getTitle(player).toLegacy();
    }

    /**
     * Returns the title of the advancement as a legacy string.
     *
     * @param player The player.
     * @return The title of the advancement as a legacy string.
     * @implSpec The default implementation returns the title returned by {@link #getTitle(OfflinePlayer)} converted into a legacy string.
     */
    @NotNull
    public String getLegacyTitle(@NotNull OfflinePlayer player) {
        return getTitle(player).toLegacy();
    }

    /**
     * Returns the title of the advancement.
     *
     * @param player The player.
     * @return The title of the advancement.
     */
    @NotNull
    public IComponent getTitle(@NotNull Player player) {
        return getTitle((OfflinePlayer) player);
    }

    /**
     * Returns the title of the advancement.
     *
     * @param player The player.
     * @return The title of the advancement.
     */
    @NotNull
    public abstract IComponent getTitle(@NotNull OfflinePlayer player);

    /**
     * Returns the default style of the title when displayed in the advancement GUI.
     *
     * @return The default style of the title when displayed in the advancement GUI.
     *     An empty style can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation calls {@link #getDefaultTitleStyle(OfflinePlayer)}.
     */
    @NotNull
    public IDefaultStyle getDefaultTitleStyle(@NotNull Player player) {
        return getDefaultTitleStyle(((OfflinePlayer) player));
    }

    /**
     * Returns the default style of the title when displayed in the advancement GUI.
     *
     * @param player The player.
     * @return The default style of the title when displayed in the advancement GUI.
     *     An empty style can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link BungeeCordDefaultStyle#EMPTY}.
     */
    @NotNull
    public IDefaultStyle getDefaultTitleStyle(@NotNull OfflinePlayer player) {
        return BungeeCordDefaultStyle.EMPTY;
    }

    /**
     * Returns the default style of the title in the advancement's announcement message.
     *
     * @param player The player.
     * @return The default style of the title in the advancement's announcement message.
     *     An empty style can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation calls {@link #getAnnouncementMessageDefaultTitleStyle(OfflinePlayer)}.
     * @see IAnnouncementMessage
     * @see Advancement#getAnnouncementMessage(Player)
     */
    @NotNull
    public IDefaultStyle getAnnouncementMessageDefaultTitleStyle(@NotNull Player player) {
        return getAnnouncementMessageDefaultTitleStyle((OfflinePlayer) player);
    }

    /**
     * Returns the default style of the title in the advancement's announcement message.
     *
     * @param player The player.
     * @return The default style of the title in the advancement's announcement message.
     *     An empty style can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link BungeeCordDefaultStyle#EMPTY}.
     * @see IAnnouncementMessage
     * @see Advancement#getAnnouncementMessage(Player)
     */
    @NotNull
    public IDefaultStyle getAnnouncementMessageDefaultTitleStyle(@NotNull OfflinePlayer player) {
        return BungeeCordDefaultStyle.EMPTY;
    }

    /**
     * Returns the description of the advancement as a list of legacy strings.
     *
     * @param player The player.
     * @return The description of the advancement as a list of legacy strings.
     * @implSpec The default implementation returns the description returned by {@link #getDescription(Player)} converted into a list of legacy strings.
     */
    @NotNull
    @Unmodifiable
    public List<String> getLegacyDescription(@NotNull Player player) {
        return getDescription(player).stream().map(IComponent::toLegacy).toList();
    }

    /**
     * Returns the description of the advancement as a list of legacy strings.
     *
     * @param player The player.
     * @return The description of the advancement as a list of legacy strings.
     * @implSpec The default implementation returns the description returned by {@link #getDescription(OfflinePlayer)} converted into a list of legacy strings.
     */
    @NotNull
    @Unmodifiable
    public List<String> getLegacyDescription(@NotNull OfflinePlayer player) {
        return getDescription(player).stream().map(IComponent::toLegacy).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @param player The player.
     * @return The description of the advancement.
     */
    @NotNull
    @Unmodifiable
    public List<IComponent> getDescription(@NotNull Player player) {
        return getDescription((OfflinePlayer) player);
    }

    /**
     * Returns the description of the advancement.
     *
     * @param player The player.
     * @return The description of the advancement.
     */
    @NotNull
    @Unmodifiable
    public abstract List<IComponent> getDescription(@NotNull OfflinePlayer player);

    /**
     * Returns the default style of the description when displayed in the advancement GUI.
     *
     * @param player The player.
     * @return The default style of the description when displayed in the advancement GUI.
     *     An empty style can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation calls {@link #getDefaultDescriptionStyle(OfflinePlayer)}.
     */
    @NotNull
    public IDefaultStyle getDefaultDescriptionStyle(@NotNull Player player) {
        return getDefaultDescriptionStyle((OfflinePlayer) player);
    }

    /**
     * Returns the default style of the description when displayed in the advancement GUI.
     *
     * @param player The player.
     * @return The default style of the description when displayed in the advancement GUI.
     *     An empty style can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link BungeeCordDefaultStyle#EMPTY}.
     */
    @NotNull
    public IDefaultStyle getDefaultDescriptionStyle(@NotNull OfflinePlayer player) {
        return BungeeCordDefaultStyle.EMPTY;
    }

    /**
     * Returns the default style of the description in the advancement's announcement message.
     *
     * @param player The player.
     * @return The default style of the description in the advancement's announcement message.
     *     An empty style can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation calls {@link #getAnnouncementMessageDefaultDescriptionStyle(OfflinePlayer)}.
     * @see IAnnouncementMessage
     * @see Advancement#getAnnouncementMessage(Player)
     */
    @NotNull
    public IDefaultStyle getAnnouncementMessageDefaultDescriptionStyle(@NotNull Player player) {
        return getAnnouncementMessageDefaultDescriptionStyle((OfflinePlayer) player);
    }

    /**
     * Returns the default style of the description in the advancement's announcement message.
     *
     * @param player The player.
     * @return The default style of the description in the advancement's announcement message.
     *     An empty style can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link BungeeCordDefaultStyle#EMPTY}.
     * @see IAnnouncementMessage
     * @see Advancement#getAnnouncementMessage(Player)
     */
    @NotNull
    public IDefaultStyle getAnnouncementMessageDefaultDescriptionStyle(@NotNull OfflinePlayer player) {
        return BungeeCordDefaultStyle.EMPTY;
    }

    /**
     * Returns the shape of the advancement frame in the advancement GUI.
     *
     * @param player The player.
     * @return The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
    public AdvancementFrameType getFrame(@NotNull Player player) {
        return getFrame((OfflinePlayer) player);
    }

    /**
     * Returns the shape of the advancement frame in the advancement GUI.
     *
     * @param player The player.
     * @return The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
    public abstract AdvancementFrameType getFrame(@NotNull OfflinePlayer player);

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @param player The player.
     * @return The x coordinate.
     */
    public float getX(@NotNull Player player) {
        return getX((OfflinePlayer) player);
    }

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @param player The player.
     * @return The x coordinate.
     */
    public abstract float getX(@NotNull OfflinePlayer player);

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @param player The player.
     * @return The y coordinate.
     */
    public float getY(@NotNull Player player) {
        return getY((OfflinePlayer) player);
    }

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @param player The player.
     * @return The y coordinate.
     */
    public abstract float getY(@NotNull OfflinePlayer player);

    /**
     * Returns the NMS wrapper of the display.
     *
     * @param player The player.
     * @return The NMS wrapper of the display.
     * @throws ReflectiveOperationException If the creation of the NMS wrapper fails due to a reflection error.
     */
    @NotNull
    public PreparedAdvancementDisplayWrapper getNMSWrapper(@NotNull Player player) throws ReflectiveOperationException {
        IComponent title = getTitle(player).applyDefaultStyle(getDefaultTitleStyle(player));
        List<IComponent> description = getDescription(player);
        IComponent descriptionComponent = description.isEmpty() ? BungeeCordComponent.EMPTY : title.empty()
            .applyDefaultStyle(getDefaultDescriptionStyle(player))
            .setChildren(AdvancementUtils.intersperse(description, title.newLine()));
        return PreparedAdvancementDisplayWrapper.craft(getIcon(player), title.toJson(), descriptionComponent.toJson(), getFrame(player).getNMSWrapper(), getX(player), getY(player));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesShowToast(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return doesShowToast(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesShowToast(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return doesShowToast(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesAnnounceToChat(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return doesAnnounceToChat(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesAnnounceToChat(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return doesAnnounceToChat(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final ItemStack dispatchGetIcon(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getIcon(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final ItemStack dispatchGetIcon(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getIcon(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final String dispatchGetLegacyTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getLegacyTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final String dispatchGetLegacyTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getLegacyTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IComponent dispatchGetTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IComponent dispatchGetTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetDefaultTitleStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDefaultTitleStyle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetDefaultTitleStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDefaultTitleStyle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetAnnouncementMessageDefaultTitleStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultTitleStyle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetAnnouncementMessageDefaultTitleStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultTitleStyle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Unmodifiable
    public final List<String> dispatchGetLegacyDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getLegacyDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Unmodifiable
    public final List<String> dispatchGetLegacyDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getLegacyDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Unmodifiable
    public final List<IComponent> dispatchGetDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Unmodifiable
    public final List<IComponent> dispatchGetDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetDefaultDescriptionStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDefaultDescriptionStyle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetDefaultDescriptionStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDefaultDescriptionStyle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetAnnouncementMessageDefaultDescriptionStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultDescriptionStyle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetAnnouncementMessageDefaultDescriptionStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultDescriptionStyle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final AdvancementFrameType dispatchGetFrame(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getFrame(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final AdvancementFrameType dispatchGetFrame(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getFrame(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetX(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getX(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetX(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getX(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetY(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getY(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetY(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getY(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final PreparedAdvancementDisplayWrapper dispatchGetNMSWrapper(@NotNull Player player, @NotNull TeamProgression teamProgression) throws ReflectiveOperationException {
        return getNMSWrapper(player);
    }
}
