package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
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
     * The color of the description, if not changed will be gray by default.
     */
    public static final ChatColor DEFAULT_COLOR = ChatColor.GRAY;

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
        this(icon, title, frame, showToast, announceChat, x, y, DEFAULT_COLOR, description);
    }

    /**
     * {@link FancyAdvancementDisplay#FancyAdvancementDisplay(ItemStack, String, AdvancementFrameType, boolean, boolean, float, float, ChatColor, List)}
     */
    public FancyAdvancementDisplay(@NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull ChatColor defaultColor, @NotNull String... description) {
        this(icon, title, frame, showToast, announceChat, x, y, defaultColor, Arrays.asList(description));
    }

    /**
     * {@link AdvancementDisplay#AdvancementDisplay(ItemStack, String, AdvancementFrameType, boolean, boolean, float, float, ChatColor, List)}
     */
    public FancyAdvancementDisplay(@NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, ChatColor defaultColor, @NotNull List<String> description) {
        super(icon, title, frame, showToast, announceChat, x, y, defaultColor, description);

        if (compactDescription.isEmpty()) {
            this.chatDescription[0] = new TextComponent(rawTitle);
        } else {
            this.chatDescription[0] = new TextComponent(rawTitle + (AdvancementUtils.startsWithEmptyLine(compactDescription) ? "\n" : "\n\n") + compactDescription);
        }
    }
}
