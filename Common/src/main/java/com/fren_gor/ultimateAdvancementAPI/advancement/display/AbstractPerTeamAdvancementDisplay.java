package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.announceMessage.IAnnounceMessage;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
     * Returns the default color of the title when displayed in the advancement GUI.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The default color of the title when displayed in the advancement GUI
     *         or {@code null} to use Minecraft's default color.
     * @implSpec The default implementation returns {@code null}.
     */
    @Nullable
    public ChatColor getDefaultTitleColor(@NotNull TeamProgression progression) {
        return null;
    }

    /**
     * Returns the default color of the title in the advancement's announcement message.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The default color of the title in the advancement's announcement message
     *         or {@code null} to use the frame's color (i.e. the color returned by calling
     *         {@link AdvancementFrameType#getColor() getColor()} on the frame returned by {@link #getFrame(TeamProgression) getFrame}).
     * @implSpec The default implementation returns {@code null}.
     * @see IAnnounceMessage
     * @see Advancement#getAnnounceMessage(Player)
     */
    @Nullable
    public ChatColor getAnnouncementMessageDefaultTitleColor(@NotNull TeamProgression progression) {
        return null;
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
     * Returns the default color of the description when displayed in the advancement GUI.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The default color of the description when displayed in the advancement GUI
     *         or {@code null} to use Minecraft's default color.
     * @implSpec The default implementation returns {@code null}.
     */
    @Nullable
    public ChatColor getDefaultDescriptionColor(@NotNull TeamProgression progression) {
        return null;
    }

    /**
     * Returns the default color of the description in the advancement's announcement message.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The default color of the description in the advancement's announcement message
     *         or {@code null} to use the frame's color (i.e. the color returned by calling
     *         {@link AdvancementFrameType#getColor() getColor()} on the frame returned by {@link #getFrame(TeamProgression) getFrame}).
     * @implSpec The default implementation returns {@code null}.
     * @see IAnnounceMessage
     * @see Advancement#getAnnounceMessage(Player)
     */
    @Nullable
    public ChatColor getAnnouncementMessageDefaultDescriptionColor(@NotNull TeamProgression progression) {
        return null;
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
        BaseComponent title = AdvancementUtils.applyDefaultColor(getTitle(progression), getDefaultTitleColor(progression));
        BaseComponent description = AdvancementUtils.joinBaseComponents(new TextComponent("\n"), getDefaultDescriptionColor(progression), getDescription(progression));
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
    public final ItemStack dispatchGetIcon(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getIcon(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ItemStack dispatchGetIcon(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getIcon(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String dispatchGetLegacyTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getLegacyTitle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String dispatchGetLegacyTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getLegacyTitle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final BaseComponent dispatchGetTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getTitle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final BaseComponent dispatchGetTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getTitle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetDefaultTitleColor(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDefaultTitleColor(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetDefaultTitleColor(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDefaultTitleColor(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetAnnouncementMessageDefaultTitleColor(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultTitleColor(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetAnnouncementMessageDefaultTitleColor(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultTitleColor(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unmodifiable
    public final List<String> dispatchGetLegacyDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getLegacyDescription(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unmodifiable
    public final List<String> dispatchGetLegacyDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getLegacyDescription(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unmodifiable
    public final List<BaseComponent> dispatchGetDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDescription(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Unmodifiable
    public final List<BaseComponent> dispatchGetDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDescription(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetDefaultDescriptionColor(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDefaultDescriptionColor(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetDefaultDescriptionColor(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDefaultDescriptionColor(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetAnnouncementMessageDefaultDescriptionColor(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultDescriptionColor(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetAnnouncementMessageDefaultDescriptionColor(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultDescriptionColor(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AdvancementFrameType dispatchGetFrame(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getFrame(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    public final PreparedAdvancementDisplayWrapper dispatchGetNMSWrapper(@NotNull Player player, @NotNull TeamProgression teamProgression) throws ReflectiveOperationException {
        return getNMSWrapper(teamProgression);
    }
}
