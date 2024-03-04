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
     * @implNote The default implementation returns the title returned by {@link #getTitleBaseComponent(Player)} converted into a legacy string.
     *
     * @param player The player.
     * @return The title of the advancement as a legacy string.
     */
    @NotNull
    public String getTitle(@NotNull Player player) {
        return TextComponent.toLegacyText(getTitleBaseComponent(player));
    }

    /**
     * Returns the title of the advancement as a legacy string.
     *
     * @implNote The default implementation returns the title returned by {@link #getTitleBaseComponent(OfflinePlayer)} converted into a legacy string.
     *
     * @param player The player.
     * @return The title of the advancement as a legacy string.
     */
    @NotNull
    public String getTitle(@NotNull OfflinePlayer player) {
        return TextComponent.toLegacyText(getTitleBaseComponent(player));
    }

    /**
     * Returns the title of the advancement.
     *
     * @param player The player.
     * @return The title of the advancement.
     */
    @NotNull
    public BaseComponent[] getTitleBaseComponent(@NotNull Player player) {
        return getTitleBaseComponent((OfflinePlayer) player);
    }

    /**
     * Returns the title of the advancement.
     *
     * @param player The player.
     * @return The title of the advancement.
     */
    @NotNull
    public abstract BaseComponent[] getTitleBaseComponent(@NotNull OfflinePlayer player);

    /**
     * Returns the description of the advancement as a list of legacy strings.
     *
     * @implNote The default implementation returns the description returned by {@link #getDescriptionBaseComponent(Player)} converted into a list of legacy strings.
     *
     * @param player The player.
     * @return The description of the advancement as a list of legacy strings.
     */
    @NotNull
    public List<String> getDescription(@NotNull Player player) {
        return getDescriptionBaseComponent(player).stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement as a list of legacy strings.
     *
     * @implNote The default implementation returns the description returned by {@link #getDescriptionBaseComponent(OfflinePlayer)} converted into a list of legacy strings.
     *
     * @param player The player.
     * @return The description of the advancement as a list of legacy strings.
     */
    @NotNull
    public List<String> getDescription(@NotNull OfflinePlayer player) {
        return getDescriptionBaseComponent(player).stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @param player The player.
     * @return The description of the advancement.
     */
    @NotNull
    public List<BaseComponent[]> getDescriptionBaseComponent(@NotNull Player player) {
        return getDescriptionBaseComponent((OfflinePlayer) player);
    }

    /**
     * Returns the description of the advancement.
     *
     * @param player The player.
     * @return The description of the advancement.
     */
    @NotNull
    public abstract List<BaseComponent[]> getDescriptionBaseComponent(@NotNull OfflinePlayer player);

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
    public abstract PreparedAdvancementDisplayWrapper getNMSWrapper(@NotNull Player player);

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
    public final String dispatchGetTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String dispatchGetTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final BaseComponent[] dispatchGetTitleBaseComponent(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getTitleBaseComponent(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final BaseComponent[] dispatchGetTitleBaseComponent(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getTitleBaseComponent(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<String> dispatchGetDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<String> dispatchGetDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<BaseComponent[]> dispatchGetDescriptionBaseComponent(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDescriptionBaseComponent(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<BaseComponent[]> dispatchGetDescriptionBaseComponent(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDescriptionBaseComponent(player);
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
    public final PreparedAdvancementDisplayWrapper dispatchGetNMSWrapper(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getNMSWrapper(player);
    }
}
