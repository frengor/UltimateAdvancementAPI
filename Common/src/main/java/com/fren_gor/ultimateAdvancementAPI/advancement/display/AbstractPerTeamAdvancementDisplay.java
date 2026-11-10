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
 * A display which provides customized values based on the specified team.
 */
public abstract non-sealed class AbstractPerTeamAdvancementDisplay extends AbstractAdvancementDisplay {

    /**
     * Returns whether the toast notification should be sent on advancement grant.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return Whether the toast notification should be sent on advancement grant.
     */
    public abstract boolean doesShowToast(@NotNull TeamProgression progression);

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    public abstract boolean doesAnnounceToChat(@NotNull TeamProgression progression);

    /**
     * Returns the icon of the advancement.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The icon of the advancement.
     */
    @NotNull
    public abstract ItemStack getIcon(@NotNull TeamProgression progression);

    /**
     * Returns the title of the advancement as a legacy string.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The title of the advancement as a legacy string.
     * @implSpec The default implementation returns the title returned by {@link #getTitle(TeamProgression)} converted into a legacy string.
     */
    @NotNull
    public String getLegacyTitle(@NotNull TeamProgression progression) {
        return getTitle(progression).toLegacy();
    }

    /**
     * Returns the title of the advancement.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The title of the advancement.
     */
    @NotNull
    public abstract IComponent getTitle(@NotNull TeamProgression progression);

    /**
     * Returns the default style of the title when displayed in the advancement GUI.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The default style of the title when displayed in the advancement GUI.
     *     An empty style can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link BungeeCordDefaultStyle#EMPTY}.
     */
    @NotNull
    public IDefaultStyle getDefaultTitleStyle(@NotNull TeamProgression progression) {
        return BungeeCordDefaultStyle.EMPTY;
    }

    /**
     * Returns the default style of the title in the advancement's announcement message.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The default style of the title in the advancement's announcement message.
     *     An empty style can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link BungeeCordDefaultStyle#EMPTY}.
     * @see IAnnouncementMessage
     * @see Advancement#getAnnouncementMessage(Player)
     */
    @NotNull
    public IDefaultStyle getAnnouncementMessageDefaultTitleStyle(@NotNull TeamProgression progression) {
        return BungeeCordDefaultStyle.EMPTY;
    }

    /**
     * Returns the description of the advancement as a list of legacy strings.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The description of the advancement as a list of legacy strings.
     * @implSpec The default implementation returns the description returned by {@link #getDescription(TeamProgression)} converted into a list of legacy strings.
     */
    @NotNull
    @Unmodifiable
    public List<String> getLegacyDescription(@NotNull TeamProgression progression) {
        return getDescription(progression).stream().map(IComponent::toLegacy).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The description of the advancement.
     */
    @NotNull
    @Unmodifiable
    public abstract List<IComponent> getDescription(@NotNull TeamProgression progression);

    /**
     * Returns the default style of the description when displayed in the advancement GUI.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The default style of the description when displayed in the advancement GUI.
     *     An empty style can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link BungeeCordDefaultStyle#EMPTY}.
     */
    @NotNull
    public IDefaultStyle getDefaultDescriptionStyle(@NotNull TeamProgression progression) {
        return BungeeCordDefaultStyle.EMPTY;
    }

    /**
     * Returns the default style of the description in the advancement's announcement message.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The default style of the description in the advancement's announcement message.
     *     An empty style can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link BungeeCordDefaultStyle#EMPTY}.
     * @see IAnnouncementMessage
     * @see Advancement#getAnnouncementMessage(Player)
     */
    @NotNull
    public IDefaultStyle getAnnouncementMessageDefaultDescriptionStyle(@NotNull TeamProgression progression) {
        return BungeeCordDefaultStyle.EMPTY;
    }

    /**
     * Returns the shape of the advancement frame in the advancement GUI.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
    public abstract AdvancementFrameType getFrame(@NotNull TeamProgression progression);

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The x coordinate.
     */
    public abstract float getX(@NotNull TeamProgression progression);

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The y coordinate.
     */
    public abstract float getY(@NotNull TeamProgression progression);

    /**
     * Returns the NMS wrapper of the display.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The NMS wrapper of the display.
     * @throws ReflectiveOperationException If the creation of the NMS wrapper fails due to a reflection error.
     */
    @NotNull
    public PreparedAdvancementDisplayWrapper getNMSWrapper(@NotNull TeamProgression progression) throws ReflectiveOperationException {
        IComponent title = getTitle(progression).applyDefaultStyle(getDefaultTitleStyle(progression));
        List<IComponent> description = getDescription(progression);
        IComponent descriptionComponent = description.isEmpty() ? BungeeCordComponent.EMPTY : title.empty()
            .applyDefaultStyle(getDefaultDescriptionStyle(progression))
            .setChildren(AdvancementUtils.intersperse(description, title.newLine()));
        return PreparedAdvancementDisplayWrapper.craft(getIcon(progression), title.toJson(), descriptionComponent.toJson(), getFrame(progression).getNMSWrapper(), getX(progression), getY(progression));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesShowToast(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return doesShowToast(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesShowToast(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return doesShowToast(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesAnnounceToChat(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return doesAnnounceToChat(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean dispatchDoesAnnounceToChat(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return doesAnnounceToChat(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final ItemStack dispatchGetIcon(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getIcon(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final ItemStack dispatchGetIcon(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getIcon(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final String dispatchGetLegacyTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getLegacyTitle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final String dispatchGetLegacyTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getLegacyTitle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IComponent dispatchGetTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getTitle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IComponent dispatchGetTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getTitle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetDefaultTitleStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDefaultTitleStyle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetDefaultTitleStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDefaultTitleStyle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetAnnouncementMessageDefaultTitleStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultTitleStyle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetAnnouncementMessageDefaultTitleStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultTitleStyle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Unmodifiable
    public final List<String> dispatchGetLegacyDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getLegacyDescription(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Unmodifiable
    public final List<String> dispatchGetLegacyDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getLegacyDescription(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Unmodifiable
    public final List<IComponent> dispatchGetDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDescription(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Unmodifiable
    public final List<IComponent> dispatchGetDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDescription(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetDefaultDescriptionStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDefaultDescriptionStyle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetDefaultDescriptionStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDefaultDescriptionStyle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetAnnouncementMessageDefaultDescriptionStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultDescriptionStyle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final IDefaultStyle dispatchGetAnnouncementMessageDefaultDescriptionStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultDescriptionStyle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final AdvancementFrameType dispatchGetFrame(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getFrame(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final AdvancementFrameType dispatchGetFrame(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getFrame(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetX(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getX(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetX(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getX(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetY(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getY(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final float dispatchGetY(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getY(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final PreparedAdvancementDisplayWrapper dispatchGetNMSWrapper(@NotNull Player player, @NotNull TeamProgression teamProgression) throws ReflectiveOperationException {
        return getNMSWrapper(teamProgression);
    }
}
