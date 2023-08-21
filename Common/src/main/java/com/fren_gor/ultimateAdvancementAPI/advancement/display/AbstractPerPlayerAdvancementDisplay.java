package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
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

public abstract class AbstractPerPlayerAdvancementDisplay extends AbstractAdvancementDisplay {

    /**
     * Returns whether the toast notification should be sent on advancement grant.
     *
     * @return Whether the toast notification should be sent on advancement grant.
     */
    public abstract boolean doesShowToast(@NotNull Player player);

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     *
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    public abstract boolean doesAnnounceToChat(@NotNull Player player);

    /**
     * Gets a clone of the icon.
     *
     * @return A clone of the icon.
     */
    @NotNull
    public abstract ItemStack getIcon(@NotNull Player player);

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public String getTitle(@NotNull Player player) {
        return TextComponent.toLegacyText(getTitleBaseComponent(player));
    }

    /**
     * Returns the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public abstract BaseComponent[] getTitleBaseComponent(@NotNull Player player);

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public List<String> getDescription(@NotNull Player player) {
        return getDescriptionBaseComponent(player).stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Returns the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @Unmodifiable
    public abstract List<BaseComponent[]> getDescriptionBaseComponent(@NotNull Player player);

    /**
     * Returns the shape of the advancement frame in the advancement GUI.
     *
     * @return The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
    public abstract AdvancementFrameType getFrame(@NotNull Player player);

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @return The x coordinate.
     */
    public abstract float getX(@NotNull Player player);

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @return The y coordinate.
     */
    public abstract float getY(@NotNull Player player);

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public BaseComponent[] getAnnounceMessage(@NotNull Player advancementCompleter, @NotNull Advancement advancement) {
        Preconditions.checkNotNull(advancementCompleter, "Player is null.");
        ChatColor color = getFrame(advancementCompleter).getColor();

        String title = getTitle(advancementCompleter);
        String description = String.join("\n", getDescription(advancementCompleter));
        return new ComponentBuilder(advancementCompleter.getName() + ' ' + getFrame(advancementCompleter).getChatText() + ' ')
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
    public abstract AdvancementDisplayWrapper getNMSWrapper(@NotNull Advancement advancement,@NotNull Player player);
}
