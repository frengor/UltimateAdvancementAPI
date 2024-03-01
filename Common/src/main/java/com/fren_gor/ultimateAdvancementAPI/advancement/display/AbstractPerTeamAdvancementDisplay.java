package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * A display which provides customized values based on the provided team.
 * <p>The overload of the methods without parameters (i.e. the methods inherited from the super class) should return
 * values which are "good enough" for every team.
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
    @Unmodifiable
    public List<String> getDescription(@NotNull TeamProgression progression) {
        return getDescriptionBaseComponent(progression).stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @param progression The {@link TeamProgression} of the team.
     * @return The description of the advancement.
     */
    @Unmodifiable
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
     * @hidden
     */
    @Override
    @NonExtendable
    public boolean dispatchDoesShowToast(Player player, TeamProgression teamProgression) {
        return doesShowToast(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public boolean dispatchDoesShowToast(OfflinePlayer player, TeamProgression teamProgression) {
        return doesShowToast(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public boolean dispatchDoesAnnounceToChat(Player player, TeamProgression teamProgression) {
        return doesAnnounceToChat(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public boolean dispatchDoesAnnounceToChat(OfflinePlayer player, TeamProgression teamProgression) {
        return doesAnnounceToChat(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public ItemStack dispatchGetIcon(Player player, TeamProgression teamProgression) {
        return getIcon(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public ItemStack dispatchGetIcon(OfflinePlayer player, TeamProgression teamProgression) {
        return getIcon(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public String dispatchGetTitle(Player player, TeamProgression teamProgression) {
        return getTitle(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public String dispatchGetTitle(OfflinePlayer player, TeamProgression teamProgression) {
        return getTitle(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public BaseComponent[] dispatchGetTitleBaseComponent(Player player, TeamProgression teamProgression) {
        return getTitleBaseComponent(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public BaseComponent[] dispatchGetTitleBaseComponent(OfflinePlayer player, TeamProgression teamProgression) {
        return getTitleBaseComponent(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public List<String> dispatchGetDescription(Player player, TeamProgression teamProgression) {
        return getDescription(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public List<String> dispatchGetDescription(OfflinePlayer player, TeamProgression teamProgression) {
        return getDescription(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public List<BaseComponent[]> dispatchGetDescriptionBaseComponent(Player player, TeamProgression teamProgression) {
        return getDescriptionBaseComponent(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public List<BaseComponent[]> dispatchGetDescriptionBaseComponent(OfflinePlayer player, TeamProgression teamProgression) {
        return getDescriptionBaseComponent(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public AdvancementFrameType dispatchGetFrame(Player player, TeamProgression teamProgression) {
        return getFrame(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public AdvancementFrameType dispatchGetFrame(OfflinePlayer player, TeamProgression teamProgression) {
        return getFrame(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public float dispatchGetX(Player player, TeamProgression teamProgression) {
        return getX(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public float dispatchGetX(OfflinePlayer player, TeamProgression teamProgression) {
        return getX(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public float dispatchGetY(Player player, TeamProgression teamProgression) {
        return getY(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public float dispatchGetY(OfflinePlayer player, TeamProgression teamProgression) {
        return getY(teamProgression);
    }

    /**
     * @hidden
     */
    @Override
    @NonExtendable
    public PreparedAdvancementDisplayWrapper dispatchGetNMSWrapper(@NotNull Player player, @NotNull TeamProgression teamProgression) {
        return getNMSWrapper(teamProgression);
    }
}
