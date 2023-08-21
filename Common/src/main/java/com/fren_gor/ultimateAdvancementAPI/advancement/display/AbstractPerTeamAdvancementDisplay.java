package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public abstract class AbstractPerTeamAdvancementDisplay extends AbstractAdvancementDisplay {

    /**
     * Returns whether the toast notification should be sent on advancement grant.
     *
     * @return Whether the toast notification should be sent on advancement grant.
     */
    public abstract boolean doesShowToast(@NotNull TeamProgression progression);

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     *
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    public abstract boolean doesAnnounceToChat(@NotNull TeamProgression progression);

    /**
     * Gets a clone of the icon.
     *
     * @return A clone of the icon.
     */
    @NotNull
    public abstract ItemStack getIcon(@NotNull TeamProgression progression);

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public String getTitle(@NotNull TeamProgression progression) {
        return TextComponent.toLegacyText(getTitleBaseComponent(progression));
    }

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public abstract BaseComponent[] getTitleBaseComponent(@NotNull TeamProgression progression);

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public List<String> getDescription(@NotNull TeamProgression progression) {
        return getDescriptionBaseComponent(progression).stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public abstract List<BaseComponent[]> getDescriptionBaseComponent(@NotNull TeamProgression progression);

    /**
     * Returns the shape of the advancement frame in the advancement GUI.
     *
     * @return The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
    public abstract AdvancementFrameType getFrame(@NotNull TeamProgression progression);

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @return The x coordinate.
     */
    public abstract float getX(@NotNull TeamProgression progression);

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @return The y coordinate.
     */
    public abstract float getY(@NotNull TeamProgression progression);

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public BaseComponent[] getAnnounceMessage(@NotNull Player advancementCompleter, @NotNull Advancement advancement) {
        Preconditions.checkNotNull(advancementCompleter, "Player is null.");
        TeamProgression teamProgression = advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(advancementCompleter);

        ChatColor color = getFrame(teamProgression).getColor();

        String title = getTitle(teamProgression);
        String description = String.join("\n", getDescription(teamProgression));

        return new ComponentBuilder(advancementCompleter.getName() + ' ' + getFrame(teamProgression).getChatText() + ' ')
                .color(ChatColor.WHITE)
                .append(new ComponentBuilder("[")
                                .color(color)
                                .event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("")
                                        .append(title, FormatRetention.NONE)
                                        .append("\n")
                                        .append(description, FormatRetention.NONE)
                                        .create()))
                                .create()
                        , FormatRetention.NONE)
                .append(title, FormatRetention.EVENTS)
                .append(new ComponentBuilder("]")
                                .color(color)
                                .create()
                        , FormatRetention.EVENTS)
                .create();
    }

    /**
     * Returns the {@code AdvancementDisplay} NMS wrapper, using the provided advancement for construction (when necessary).
     *
     * @param advancement The advancement used, when necessary, to create the NMS wrapper. Must be not {@code null}.
     * @return The {@code AdvancementDisplay} NMS wrapper.
     */
    @NotNull
    public abstract AdvancementDisplayWrapper getNMSWrapper(@NotNull Advancement advancement, @NotNull TeamProgression progression);
}
