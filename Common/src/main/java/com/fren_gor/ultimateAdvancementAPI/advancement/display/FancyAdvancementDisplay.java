package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Inherited;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * FancyAdvancementDisplay is an AdvancementDisplay child class. It has as a difference that puts an empty line between the title and the description in the hover message of the advancement completion message.
 */
public class FancyAdvancementDisplay extends AdvancementDisplay {

    /**
     * The default color of the title, if not changed it will be white.
     */
    public static final ChatColor DEFAULT_TITLE_COLOR = ChatColor.WHITE;
    /**
     * The default color of the description, if not changed it will be gray.
     */
    public static final ChatColor DEFAULT_DESCRIPTION_COLOR = ChatColor.GRAY;

    /**
     * {@link FancyAdvancementDisplay#FancyAdvancementDisplay(ItemStack, String, AdvancementFrameType, boolean, boolean, float, float, ChatColor, List)}
     */
    public FancyAdvancementDisplay(@NotNull Material icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull String... description) {
        this(icon, title, frame, showToast, announceChat, x, y, Arrays.asList(description));
    }

    /**
     * {@link FancyAdvancementDisplay#FancyAdvancementDisplay(ItemStack, String, AdvancementFrameType, boolean, boolean, float, float, ChatColor, List)}
     */
    public FancyAdvancementDisplay(@NotNull Material icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull List<String> description) {
        this(new ItemStack(Objects.requireNonNull(icon, "Icon is null.")), title, frame, showToast, announceChat, x, y, description);
    }

    /**
     * {@link FancyAdvancementDisplay#FancyAdvancementDisplay(ItemStack, String, AdvancementFrameType, boolean, boolean, float, float, ChatColor, List)}
     */
    public FancyAdvancementDisplay(@NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull String... description) {
        this(icon, title, frame, showToast, announceChat, x, y, Arrays.asList(description));
    }

    /**
     * {@link FancyAdvancementDisplay#FancyAdvancementDisplay(ItemStack, String, AdvancementFrameType, boolean, boolean, float, float, ChatColor, List)}
     */
    public FancyAdvancementDisplay(@NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull List<String> description) {
        this(icon, title, frame, showToast, announceChat, x, y, DEFAULT_TITLE_COLOR, DEFAULT_DESCRIPTION_COLOR, description);
    }

    public FancyAdvancementDisplay(@NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull ChatColor defaultTitleColor, @NotNull ChatColor defaultDescriptionColor, @NotNull String... description) {
        this(icon, title, frame, showToast, announceChat, x, y, defaultTitleColor, defaultDescriptionColor, Arrays.asList(description));
    }

    public FancyAdvancementDisplay(@NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull ChatColor defaultTitleColor, @NotNull ChatColor defaultDescriptionColor, @NotNull List<String> description) {
        super(icon, title, frame, showToast, announceChat, x, y, defaultDescriptionColor, description);
        Validate.notNull(defaultTitleColor, "Default title color is null.");

        this.chatTitle[0] = new TextComponent(defaultTitleColor + rawTitle);

        if (compactDescription.isEmpty()) {
            this.chatDescription[0] = new TextComponent(defaultTitleColor + rawTitle);
        } else {
            this.chatDescription[0] = new TextComponent(defaultTitleColor + rawTitle + (AdvancementUtils.startsWithEmptyLine(compactDescription) ? "\n" : "\n\n") + compactDescription);
        }
    }
}
