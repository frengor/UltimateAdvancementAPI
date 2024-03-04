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
 * A display which provides customized values based on the provided team.
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
     * @implNote The default implementation returns the title returned by {@link #getTitleBaseComponent(TeamProgression)} converted into a legacy string.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The title of the advancement as a legacy string.
     */
    @NotNull
    public String getTitle(@NotNull TeamProgression progression) {
        return TextComponent.toLegacyText(getTitleBaseComponent(progression));
    }

    /**
     * Returns the title of the advancement.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The title of the advancement.
     */
    @NotNull
    public abstract BaseComponent[] getTitleBaseComponent(@NotNull TeamProgression progression);

    /**
     * Returns the description of the advancement as a list of legacy strings.
     *
     * @implNote The default implementation returns the description returned by {@link #getDescriptionBaseComponent(TeamProgression)} converted into a list of legacy strings.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The description of the advancement as a list of legacy strings.
     */
    @NotNull
    public List<String> getDescription(@NotNull TeamProgression progression) {
        return getDescriptionBaseComponent(progression).stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The description of the advancement.
     */
    @NotNull
    public abstract List<BaseComponent[]> getDescriptionBaseComponent(@NotNull TeamProgression progression);

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
    public abstract PreparedAdvancementDisplayWrapper getNMSWrapper(@NotNull TeamProgression progression);

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
    public final String dispatchGetTitle(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getTitle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String dispatchGetTitle(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getTitle(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final BaseComponent[] dispatchGetTitleBaseComponent(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getTitleBaseComponent(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final BaseComponent[] dispatchGetTitleBaseComponent(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getTitleBaseComponent(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<String> dispatchGetDescription(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDescription(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<String> dispatchGetDescription(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDescription(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<BaseComponent[]> dispatchGetDescriptionBaseComponent(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getDescriptionBaseComponent(teamProgression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final List<BaseComponent[]> dispatchGetDescriptionBaseComponent(@NotNull OfflinePlayer player, @NotNull TeamProgression teamProgression) {
        return getDescriptionBaseComponent(teamProgression);
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
    public final PreparedAdvancementDisplayWrapper dispatchGetNMSWrapper(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getNMSWrapper(teamProgression);
    }
}
