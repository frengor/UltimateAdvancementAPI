package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.announcementMessage.IAnnouncementMessage;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.fren_gor.ultimateAdvancementAPI.util.display.DefaultStyle;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A display which provides customized values based on the specified team.
 */
public abstract class AbstractPerTeamAdvancementDisplay extends AbstractAdvancementDisplay {

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
        return TextComponent.toLegacyText(getTitle(progression));
    }

    /**
     * Returns the title of the advancement.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The title of the advancement.
     */
    @NotNull
    public abstract BaseComponent getTitle(@NotNull TeamProgression progression);

    /**
     * Returns the default style of the title when displayed in the advancement GUI.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The default style of the title when displayed in the advancement GUI.
     *         {@link DefaultStyle#MINECRAFT_DEFAULTS} can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link DefaultStyle#MINECRAFT_DEFAULTS}.
     */
    @NotNull
    public DefaultStyle getDefaultTitleStyle(@NotNull TeamProgression progression) {
        return DefaultStyle.MINECRAFT_DEFAULTS;
    }

    /**
     * Returns the default style of the title in the advancement's announcement message.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The default style of the title in the advancement's announcement message.
     *         {@link DefaultStyle#MINECRAFT_DEFAULTS} can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link DefaultStyle#MINECRAFT_DEFAULTS}.
     * @see IAnnouncementMessage
     * @see Advancement#getAnnouncementMessage(Player)
     */
    @NotNull
    public DefaultStyle getAnnouncementMessageDefaultTitleStyle(@NotNull TeamProgression progression) {
        return DefaultStyle.MINECRAFT_DEFAULTS;
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
        return getDescription(progression).stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The description of the advancement.
     */
    @NotNull
    @Unmodifiable
    public abstract List<BaseComponent> getDescription(@NotNull TeamProgression progression);

    /**
     * Returns the default style of the description when displayed in the advancement GUI.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The default style of the description when displayed in the advancement GUI.
     *         {@link DefaultStyle#MINECRAFT_DEFAULTS} can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link DefaultStyle#MINECRAFT_DEFAULTS}.
     */
    @NotNull
    public DefaultStyle getDefaultDescriptionStyle(@NotNull TeamProgression progression) {
        return DefaultStyle.MINECRAFT_DEFAULTS;
    }

    /**
     * Returns the default style of the description in the advancement's announcement message.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The default style of the description in the advancement's announcement message.
     *         {@link DefaultStyle#MINECRAFT_DEFAULTS} can be returned if Minecraft's default style should be used.
     * @implSpec The default implementation returns {@link DefaultStyle#MINECRAFT_DEFAULTS}.
     * @see IAnnouncementMessage
     * @see Advancement#getAnnouncementMessage(Player)
     */
    @NotNull
    public DefaultStyle getAnnouncementMessageDefaultDescriptionStyle(@NotNull TeamProgression progression) {
        return DefaultStyle.MINECRAFT_DEFAULTS;
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
     */
    @NotNull
    public PreparedAdvancementDisplayWrapper getNMSWrapper(@NotNull TeamProgression progression) throws ReflectiveOperationException {
        BaseComponent title = AdvancementUtils.applyDefaultStyle(getTitle(progression), getDefaultTitleStyle(progression));
        BaseComponent description = AdvancementUtils.joinBaseComponents(new TextComponent("\n"), getDefaultDescriptionStyle(progression), getDescription(progression));
        return PreparedAdvancementDisplayWrapper.craft(getIcon(progression), title, description, getFrame(progression).getNMSWrapper(), getX(progression), getY(progression));
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
    public final BaseComponent dispatchGetTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getTitle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final BaseComponent dispatchGetTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getTitle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final DefaultStyle dispatchGetDefaultTitleStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDefaultTitleStyle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final DefaultStyle dispatchGetDefaultTitleStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDefaultTitleStyle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final DefaultStyle dispatchGetAnnouncementMessageDefaultTitleStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultTitleStyle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final DefaultStyle dispatchGetAnnouncementMessageDefaultTitleStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
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
    public final List<BaseComponent> dispatchGetDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDescription(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Unmodifiable
    public final List<BaseComponent> dispatchGetDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDescription(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final DefaultStyle dispatchGetDefaultDescriptionStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDefaultDescriptionStyle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final DefaultStyle dispatchGetDefaultDescriptionStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDefaultDescriptionStyle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final DefaultStyle dispatchGetAnnouncementMessageDefaultDescriptionStyle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultDescriptionStyle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public final DefaultStyle dispatchGetAnnouncementMessageDefaultDescriptionStyle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
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
