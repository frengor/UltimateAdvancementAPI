package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A builder for advancement displays.
 */
@SuppressWarnings("unchecked")
abstract class AdvancementDisplayBuilder<T extends AdvancementDisplayBuilder<?, ?>, R extends AdvancementDisplay>  {

    /**
     * The icon of the advancement in the advancement GUI.
     */
    protected final ItemStack icon;

    /**
     * The title of the advancement.
     */
    protected final String title;

    /**
     * The description of the advancement.
     */
    protected final List<String> description;

    /**
     * The shape of the advancement frame in the advancement GUI.
     */
    protected AdvancementFrameType frame;

    /**
     * The default color of the title and description.
     */
    protected ChatColor defaultColor;

    /**
     * Whether the toast notification should be sent on advancement grant.
     */
    protected boolean showToast = false;

    /**
     * Whether the advancement completion message should be sent on advancement grant.
     */
    protected boolean announceChat = false;

    /**
     * The advancement x coordinate.
     */
    protected float x;

    /**
     * The advancement y coordinate.
     */
    protected float y;

    /**
     * Construct a new builder for the advancement display.
     * <p>By default, the advancement won't show the toast message, and
     * won't announce the message in the chat upon completion.
     * <p>The default frame is {@link AdvancementFrameType#TASK}.
     *
     * @param icon The advancement's icon in the advancement GUI.
     * @param title The title of the advancement.
     * @param x The advancement x coordinate.
     * @param y The advancement y coordinate.
     * @param description The description of the advancement.
     */
    protected AdvancementDisplayBuilder(@NotNull ItemStack icon, @NotNull String title, float x, float y, @NotNull List<String> description) {
        this.icon = icon;
        this.title = title;
        this.x = x;
        this.y = y;
        this.description = description;
    }

    /**
     * Offset the current x and y coordinates based on the provided advancement.
     *
     * @param advancement The advancement to apply offset from.
     * @return This builder.
     */
    public T offset(@NotNull Advancement advancement) {
        return offset(advancement.getDisplay());
    }

    /**
     * Offset the current x and y coordinates based on the provided advancement display.
     *
     * @param display The advancement display to apply offset from.
     * @return This builder.
     */
    public T offset(@NotNull AdvancementDisplay display) {
        this.x += display.getX();
        this.y += display.getY();
        return (T) this;
    }

    /**
     * Set the shape of the advancement frame in the advancement GUI.
     *
     * @param frame The shape of the advancement frame in the advancement GUI.
     * @return This builder.
     */
    public T frame(@NotNull AdvancementFrameType frame) {
        this.frame = frame;
        return (T) this;
    }

    /**
     * Set the default color of the title and description.
     *
     * @param color The default color of the title and description.
     * @return This builder.
     */
    public T color(@NotNull ChatColor color) {
        this.defaultColor = color;
        return (T) this;
    }

    /**
     * Set the toast notification to be sent on advancement grant.
     *
     * @return This builder.
     */
    public T showToast() {
        this.showToast = true;
        return (T) this;
    }

    /**
     * Set the advancement completion message to be sent on advancement grant.
     *
     * @return This builder.
     */
    public T announceChat() {
        this.announceChat = true;
        return (T) this;
    }

    /**
     * Set the toast notification and the advancement completion message
     * to be sent on advancement grant.
     *
     * @return This builder.
     */
    public T announceAll() {
        return (T) showToast().announceChat();
    }

    /**
     * Builds the advancement display.
     *
     * @return The built advancement display.
     */
    public abstract R build();
}
