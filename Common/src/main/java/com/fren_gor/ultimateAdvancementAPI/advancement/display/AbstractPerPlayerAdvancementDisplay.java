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

import java.util.List;

/**
 * A display which provides customized values based on the provided player.
 *
 * @implNote The default implementation of the methods which takes a {@link Player} calls the {@link OfflinePlayer} methods.
 */
public abstract class AbstractPerPlayerAdvancementDisplay extends AbstractAdvancementDisplay {

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
     * @implNote The default implementation returns the title returned by {@link #getTitle(Player)} converted into a legacy string.
     *
     * @param player The player.
     * @return The title of the advancement as a legacy string.
     */
    @NotNull
    public String getLegacyTitle(@NotNull Player player) {
        return TextComponent.toLegacyText(getTitle(player));
    }

    /**
     * Returns the title of the advancement as a legacy string.
     *
     * @implNote The default implementation returns the title returned by {@link #getTitle(OfflinePlayer)} converted into a legacy string.
     *
     * @param player The player.
     * @return The title of the advancement as a legacy string.
     */
    @NotNull
    public String getLegacyTitle(@NotNull OfflinePlayer player) {
        return TextComponent.toLegacyText(getTitle(player));
    }

    /**
     * Returns the title of the advancement.
     *
     * @param player The player.
     * @return The title of the advancement.
     */
    @NotNull
    public BaseComponent getTitle(@NotNull Player player) {
        return getTitle((OfflinePlayer) player);
    }

    /**
     * Returns the title of the advancement.
     *
     * @param player The player.
     * @return The title of the advancement.
     */
    @NotNull
    public abstract BaseComponent getTitle(@NotNull OfflinePlayer player);

    /**
     * Returns the default color of the title when displayed in the advancement GUI.
     *
     * @param player The player.
     * @return The default color of the title when displayed in the advancement GUI
     *         or {@code null} to use Minecraft's default color.
     * @implSpec The default implementation returns {@code null}.
     */
    @Nullable
    public ChatColor getDefaultTitleColor(@NotNull Player player) {
        return getDefaultTitleColor(((OfflinePlayer) player));
    }

    /**
     * Returns the default color of the title when displayed in the advancement GUI.
     *
     * @param player The player.
     * @return The default color of the title when displayed in the advancement GUI
     *         or {@code null} to use Minecraft's default color.
     * @implSpec The default implementation returns {@code null}.
     */
    @Nullable
    public ChatColor getDefaultTitleColor(@NotNull OfflinePlayer player) {
        return null;
    }

    /**
     * Returns the default color of the title in the advancement's announcement message.
     *
     * @param player The player.
     * @return The default color of the title in the advancement's announcement message
     *         or {@code null} to use the frame's color (i.e. the color returned by calling
     *         {@link AdvancementFrameType#getColor() getColor()} on the frame returned by {@link #getFrame(Player) getFrame}).
     * @implSpec The default implementation returns {@code null}.
     * @see IAnnounceMessage
     * @see Advancement#getAnnounceMessage(Player)
     */
    @Nullable
    public ChatColor getAnnouncementMessageDefaultTitleColor(@NotNull Player player) {
        return getAnnouncementMessageDefaultTitleColor((OfflinePlayer) player);
    }

    /**
     * Returns the default color of the title in the advancement's announcement message.
     *
     * @param player The player.
     * @return The default color of the title in the advancement's announcement message
     *         or {@code null} to use the frame's color (i.e. the color returned by calling
     *         {@link AdvancementFrameType#getColor() getColor()} on the frame returned by {@link #getFrame(OfflinePlayer) getFrame}).
     * @implSpec The default implementation returns {@code null}.
     * @see IAnnounceMessage
     * @see Advancement#getAnnounceMessage(Player)
     */
    @Nullable
    public ChatColor getAnnouncementMessageDefaultTitleColor(@NotNull OfflinePlayer player) {
        return null;
    }

    /**
     * Returns the description of the advancement as a list of legacy strings.
     *
     * @implNote The default implementation returns the description returned by {@link #getDescription(Player)} converted into a list of legacy strings.
     *
     * @param player The player.
     * @return The description of the advancement as a list of legacy strings.
     */
    @NotNull
    public List<String> getLegacyDescription(@NotNull Player player) {
        return getDescription(player).stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement as a list of legacy strings.
     *
     * @implNote The default implementation returns the description returned by {@link #getDescription(OfflinePlayer)} converted into a list of legacy strings.
     *
     * @param player The player.
     * @return The description of the advancement as a list of legacy strings.
     */
    @NotNull
    public List<String> getLegacyDescription(@NotNull OfflinePlayer player) {
        return getDescription(player).stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @param player The player.
     * @return The description of the advancement.
     */
    @NotNull
    public List<BaseComponent> getDescription(@NotNull Player player) {
        return getDescription((OfflinePlayer) player);
    }

    /**
     * Returns the description of the advancement.
     *
     * @param player The player.
     * @return The description of the advancement.
     */
    @NotNull
    public abstract List<BaseComponent> getDescription(@NotNull OfflinePlayer player);

    /**
     * Returns the default color of the description when displayed in the advancement GUI.
     *
     * @param player The player.
     * @return The default color of the description when displayed in the advancement GUI
     *         or {@code null} to use Minecraft's default color.
     * @implSpec The default implementation returns {@code null}.
     */
    @Nullable
    public ChatColor getDefaultDescriptionColor(@NotNull Player player) {
        return getDefaultDescriptionColor((OfflinePlayer) player);
    }

    /**
     * Returns the default color of the description when displayed in the advancement GUI.
     *
     * @param player The player.
     * @return The default color of the description when displayed in the advancement GUI
     *         or {@code null} to use Minecraft's default color.
     * @implSpec The default implementation returns {@code null}.
     */
    @Nullable
    public ChatColor getDefaultDescriptionColor(@NotNull OfflinePlayer player) {
        return null;
    }

    /**
     * Returns the default color of the description in the advancement's announcement message.
     *
     * @param player The player.
     * @return The default color of the description in the advancement's announcement message
     *         or {@code null} to use the frame's color (i.e. the color returned by calling
     *         {@link AdvancementFrameType#getColor() getColor()} on the frame returned by {@link #getFrame(Player) getFrame}).
     * @implSpec The default implementation returns {@code null}.
     * @see IAnnounceMessage
     * @see Advancement#getAnnounceMessage(Player)
     */
    @Nullable
    public ChatColor getAnnouncementMessageDefaultDescriptionColor(@NotNull Player player) {
        return getAnnouncementMessageDefaultDescriptionColor((OfflinePlayer) player);
    }

    /**
     * Returns the default color of the description in the advancement's announcement message.
     *
     * @param player The player.
     * @return The default color of the description in the advancement's announcement message
     *         or {@code null} to use the frame's color (i.e. the color returned by calling
     *         {@link AdvancementFrameType#getColor() getColor()} on the frame returned by {@link #getFrame(OfflinePlayer) getFrame}).
     * @implSpec The default implementation returns {@code null}.
     * @see IAnnounceMessage
     * @see Advancement#getAnnounceMessage(Player)
     */
    @Nullable
    public ChatColor getAnnouncementMessageDefaultDescriptionColor(@NotNull OfflinePlayer player) {
        return null;
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
     */
    @NotNull
    public PreparedAdvancementDisplayWrapper getNMSWrapper(@NotNull Player player) throws ReflectiveOperationException {
        BaseComponent title = AdvancementUtils.applyDefaultColor(getTitle(player), getDefaultTitleColor(player));
        BaseComponent description = AdvancementUtils.joinBaseComponents(new TextComponent("\n"), getDefaultDescriptionColor(player), getDescription(player));
        return PreparedAdvancementDisplayWrapper.craft(getIcon(player), title, description, getFrame(player).getNMSWrapper(), getX(player), getY(player));
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
    public final ItemStack dispatchGetIcon(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getIcon(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ItemStack dispatchGetIcon(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getIcon(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String dispatchGetLegacyTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getLegacyTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String dispatchGetLegacyTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getLegacyTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final BaseComponent dispatchGetTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final BaseComponent dispatchGetTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetDefaultTitleColor(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDefaultTitleColor(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetDefaultTitleColor(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDefaultTitleColor(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetAnnouncementMessageDefaultTitleColor(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultTitleColor(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetAnnouncementMessageDefaultTitleColor(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultTitleColor(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<String> dispatchGetLegacyDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getLegacyDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<String> dispatchGetLegacyDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getLegacyDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<BaseComponent> dispatchGetDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<BaseComponent> dispatchGetDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetDefaultDescriptionColor(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDefaultDescriptionColor(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetDefaultDescriptionColor(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDefaultDescriptionColor(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetAnnouncementMessageDefaultDescriptionColor(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultDescriptionColor(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public final ChatColor dispatchGetAnnouncementMessageDefaultDescriptionColor(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getAnnouncementMessageDefaultDescriptionColor(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final AdvancementFrameType dispatchGetFrame(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getFrame(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    public final PreparedAdvancementDisplayWrapper dispatchGetNMSWrapper(@NotNull Player player, @NotNull TeamProgression teamProgression) throws ReflectiveOperationException {
        return getNMSWrapper(player);
    }
}
