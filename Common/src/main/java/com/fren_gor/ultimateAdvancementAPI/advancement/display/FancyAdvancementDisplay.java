package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * The {@code FancyAdvancementDisplay} class provides a fancier graphical aspect to advancements than {@link AdvancementDisplay}.
 * The differences are the following:
 * <ul>
 *     <li>The default title color is {@link #DEFAULT_TITLE_COLOR};</li>
 *     <li>The default description color is {@link #DEFAULT_DESCRIPTION_COLOR};</li>
 *     <li>Title and description colors can be customized from constructors;</li>
 *     <li>An empty line is inserted (if not present) at the start of {@link AdvancementDisplay#chatDescription}
 *     in order to create a little separator between the title and the description in the competition chat message.</li>
 * </ul>
 */
public class FancyAdvancementDisplay extends AdvancementDisplay {

    /**
     * The default color of the title.
     */
    public static final ChatColor DEFAULT_TITLE_COLOR = ChatColor.WHITE;
    /**
     * The default color of the description.
     */
    public static final ChatColor DEFAULT_DESCRIPTION_COLOR = ChatColor.GRAY;

    /**
     * Creates a new {@code FancyAdvancementDisplay}.
     * <p>The advancement is positioned by the x and y coordinates in the advancement GUI. The origin is placed in the
     * upper-left corner of the advancement GUI. The x-axis points to the right (as usual), whereas the y-axis points downward.
     * Thus, the x and y coordinates must be positive.
     *
     * @param icon The material of the advancement's icon in the advancement GUI.
     * @param title The title of the advancement.
     * @param frame The shape of the advancement frame in the advancement GUI.
     * @param showToast Whether the toast notification should be sent on advancement grant.
     * @param announceChat Whether the advancement completion message should be sent on advancement grant.
     * @param x The advancement x coordinate. Must be positive.
     * @param y The advancement y coordinate. Must be positive.
     * @param description The description of the advancement.
     */
    public FancyAdvancementDisplay(@NotNull Material icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull String... description) {
        this(icon, title, frame, showToast, announceChat, x, y, Arrays.asList(description));
    }

    /**
     * Creates a new {@code FancyAdvancementDisplay}.
     * <p>The advancement is positioned by the x and y coordinates in the advancement GUI. The origin is placed in the
     * upper-left corner of the advancement GUI. The x-axis points to the right (as usual), whereas the y-axis points downward.
     * Thus, the x and y coordinates must be positive.
     *
     * @param icon The material of the advancement's icon in the advancement GUI.
     * @param title The title of the advancement.
     * @param frame The shape of the advancement frame in the advancement GUI.
     * @param showToast Whether the toast notification should be sent on advancement grant.
     * @param announceChat Whether the advancement completion message should be sent on advancement grant.
     * @param x The advancement x coordinate. Must be positive.
     * @param y The advancement y coordinate. Must be positive.
     * @param description The description of the advancement.
     */
    public FancyAdvancementDisplay(@NotNull Material icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull List<String> description) {
        this(new ItemStack(Objects.requireNonNull(icon, "Icon is null.")), title, frame, showToast, announceChat, x, y, description);
    }

    /**
     * Create a new {@code FancyAdvancementDisplay}.
     * <p>The advancement is positioned by the x and y coordinates in the advancement GUI. The origin is placed in the
     * upper-left corner of the advancement GUI. The x-axis points to the right (as usual), whereas the y-axis points downward.
     * Thus, the x and y coordinates must be positive.
     *
     * @param icon The advancement's icon in the advancement GUI.
     * @param title The title of the advancement.
     * @param frame The shape of the advancement frame in the advancement GUI.
     * @param showToast Whether the toast notification should be sent on advancement grant.
     * @param announceChat Whether the advancement completion message should be sent on advancement grant.
     * @param x The advancement x coordinate. Must be positive.
     * @param y The advancement y coordinate. Must be positive.
     * @param description The description of the advancement.
     */
    public FancyAdvancementDisplay(@NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull String... description) {
        this(icon, title, frame, showToast, announceChat, x, y, Arrays.asList(description));
    }

    /**
     * Create a new {@code FancyAdvancementDisplay}.
     * <p>The advancement is positioned by the x and y coordinates in the advancement GUI. The origin is placed in the
     * upper-left corner of the advancement GUI. The x-axis points to the right (as usual), whereas the y-axis points downward.
     * Thus, the x and y coordinates must be positive.
     *
     * @param icon The advancement's icon in the advancement GUI.
     * @param title The title of the advancement.
     * @param frame The shape of the advancement frame in the advancement GUI.
     * @param showToast Whether the toast notification should be sent on advancement grant.
     * @param announceChat Whether the advancement completion message should be sent on advancement grant.
     * @param x The advancement x coordinate. Must be positive.
     * @param y The advancement y coordinate. Must be positive.
     * @param description The description of the advancement.
     */
    public FancyAdvancementDisplay(@NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull List<String> description) {
        this(icon, title, frame, showToast, announceChat, x, y, DEFAULT_TITLE_COLOR, DEFAULT_DESCRIPTION_COLOR, description);
    }

    /**
     * Create a new {@code FancyAdvancementDisplay}.
     * <p>The advancement is positioned by the x and y coordinates in the advancement GUI. The origin is placed in the
     * upper-left corner of the advancement GUI. The x-axis points to the right (as usual), whereas the y-axis points downward.
     * Thus, the x and y coordinates must be positive.
     *
     * @param icon The advancement's icon in the advancement GUI.
     * @param title The title of the advancement.
     * @param frame The shape of the advancement frame in the advancement GUI.
     * @param showToast Whether the toast notification should be sent on advancement grant.
     * @param announceChat Whether the advancement completion message should be sent on advancement grant.
     * @param x The advancement x coordinate. Must be positive.
     * @param y The advancement y coordinate. Must be positive.
     * @param defaultTitleColor The default color of the title.
     * @param defaultDescriptionColor The default color of the description.
     * @param description The description of the advancement.
     */
    public FancyAdvancementDisplay(@NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull ChatColor defaultTitleColor, @NotNull ChatColor defaultDescriptionColor, @NotNull String... description) {
        this(icon, title, frame, showToast, announceChat, x, y, defaultTitleColor, defaultDescriptionColor, Arrays.asList(description));
    }

    /**
     * Create a new {@code FancyAdvancementDisplay}.
     * <p>The advancement is positioned by the x and y coordinates in the advancement GUI. The origin is placed in the
     * upper-left corner of the advancement GUI. The x-axis points to the right (as usual), whereas the y-axis points downward.
     * Thus, the x and y coordinates must be positive.
     *
     * @param icon The advancement's icon in the advancement GUI.
     * @param title The title of the advancement.
     * @param frame The shape of the advancement frame in the advancement GUI.
     * @param showToast Whether the toast notification should be sent on advancement grant.
     * @param announceChat Whether the advancement completion message should be sent on advancement grant.
     * @param x The advancement x coordinate. Must be positive.
     * @param y The advancement y coordinate. Must be positive.
     * @param defaultTitleColor The default color of the title.
     * @param defaultDescriptionColor The default color of the description.
     * @param description The description of the advancement.
     */
    public FancyAdvancementDisplay(@NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull ChatColor defaultTitleColor, @NotNull ChatColor defaultDescriptionColor, @NotNull List<String> description) {
        super(icon, title, frame, showToast, announceChat, x, y, defaultDescriptionColor, description);
        Preconditions.checkNotNull(defaultTitleColor, "Default title color is null.");

        this.chatTitle[0] = new TextComponent(defaultTitleColor + rawTitle);

        if (compactDescription.isEmpty()) {
            this.chatDescription[0] = new TextComponent(defaultTitleColor + rawTitle);
        } else {
            this.chatDescription[0] = new TextComponent(defaultTitleColor + rawTitle + (AdvancementUtils.startsWithEmptyLine(compactDescription) ? "\n" : "\n\n") + compactDescription);
        }
    }

    /**
     * A builder for {@link FancyAdvancementDisplay}.
     */
    public static class Builder extends AdvancementDisplayBuilder<Builder, FancyAdvancementDisplay> {

        /**
         * The default color of the title.
         */
        private ChatColor defaultTitleColor;

        /**
         * The default color of the description.
         */
        private ChatColor defaultDescriptionColor;

        /**
         * Construct a new builder for the {@link FancyAdvancementDisplay}.
         * <p>By default, the advancement won't show the toast message, and
         * won't announce the message in the chat upon completion.
         * <p>The default frame is {@link AdvancementFrameType#TASK}.
         *
         * @param icon The material of the advancement's icon in the advancement GUI.
         * @param title The title of the advancement.
         * @param x The advancement x coordinate.
         * @param y The advancement y coordinate.
         * @param description The description of the advancement.
         */
        public Builder(@NotNull Material icon, @NotNull String title, float x, float y, @NotNull String... description) {
            this(new ItemStack(icon), title, x, y, Arrays.asList(description));
        }

        /**
         * Construct a new builder for the {@link FancyAdvancementDisplay}.
         * <p>By default, the advancement won't show the toast message, and
         * won't announce the message in the chat upon completion.
         * <p>The default frame is {@link AdvancementFrameType#TASK}.
         *
         * @param icon The material of the advancement's icon in the advancement GUI.
         * @param title The title of the advancement.
         * @param x The advancement x coordinate.
         * @param y The advancement y coordinate.
         * @param description The description of the advancement.
         */
        public Builder(@NotNull Material icon, @NotNull String title, float x, float y, @NotNull List<String> description) {
            this(new ItemStack(icon), title, x, y, description);
        }

        /**
         * Construct a new builder for the {@link FancyAdvancementDisplay}.
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
        public Builder(@NotNull ItemStack icon, @NotNull String title, float x, float y, @NotNull String... description) {
            this(icon, title, x, y, Arrays.asList(description));
        }

        /**
         * Construct a new builder for the {@link FancyAdvancementDisplay}.
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
        public Builder(@NotNull ItemStack icon, @NotNull String title, float x, float y, @NotNull List<String> description) {
            super(icon, title, x, y, description);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Builder defaultColor(@NotNull ChatColor defaultColor) {
            return titleColor(defaultColor).descriptionColor(defaultColor);
        }

        /**
         * Set the default color of the title.
         *
         * @param titleColor The default color of the title.
         * @return This builder.
         */
        public Builder titleColor(@NotNull ChatColor titleColor) {
            this.defaultTitleColor = titleColor;
            return this;
        }

        /**
         * Set the default color of the description.
         *
         * @param descriptionColor The default color of the description.
         * @return This builder.
         */
        public Builder descriptionColor(@NotNull ChatColor descriptionColor) {
            this.defaultDescriptionColor = descriptionColor;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FancyAdvancementDisplay build() {
            if (frame == null) frame = AdvancementFrameType.TASK;
            if (defaultTitleColor == null) defaultTitleColor = DEFAULT_TITLE_COLOR;
            if (defaultDescriptionColor == null) defaultDescriptionColor = DEFAULT_DESCRIPTION_COLOR;
            return new FancyAdvancementDisplay(icon, title, frame, showToast, announceChat, x, y, defaultTitleColor, defaultDescriptionColor, description);
        }
    }
}
