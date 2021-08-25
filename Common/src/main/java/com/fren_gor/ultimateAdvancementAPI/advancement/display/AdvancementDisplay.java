package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_15_R1.ChatComponentText;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * AdvancementDisplay groups all the graphical components into a class
 * and describes the aesthetic aspect of the advancement, that is, how it is
 * shown in the advancements GUI. Components such as title, description,
 * position in x and y etc. are saved. Instead, the parent or maxCriteria are saved in advancement class.
 * Usually the default color of the description is taken directly from the advancement frame type, but it can be specified in the appropriate constructor.
 */
public class AdvancementDisplay {

    /**
     * The icon of the advancement and tell which item will be displayed on the GUI.
     */
    protected final ItemStack icon;

    /**
     * Usually the BaseComponent[] object is used for packets, it contains the information about the advancement title that needs to be sent to the player as a packet.
     */
    protected final BaseComponent[] chatTitle = new BaseComponent[1]; // Make sure only 1 element is used, otherwise the chat bugs

    /**
     * Usually the BaseComponent[] object is used for packets, it contains the information about the advancement description that needs to be sent to the player as a packet.
     */
    protected final BaseComponent[] chatDescription = new BaseComponent[1]; // Make sure only 1 element is used, otherwise the chat bugs

    /**
     * The title of the advancement and it will be the displayed title.
     */
    protected final String title;

    /**
     * The title of the advancement but trimmed and without color codes.
     */
    protected final String rawTitle;

    /**
     * The description of the advancement.
     */
    @Unmodifiable
    protected final List<String> description;

    /**
     * The description of the advancement on a single String with "\n" between the lines of the description.
     */
    protected final String compactDescription;

    /**
     * The shape of the advancement.
     *
     * @see AdvancementFrameType
     */
    protected final AdvancementFrameType frame;

    /**
     * Tell if the toast message needs to be sent.
     */
    protected final boolean showToast;

    /**
     * Tell if the advancement completion message needs to be sent.
     */
    protected final boolean announceChat;

    /**
     * The advancement position relative to the x-axis.
     */
    protected final float x;

    /**
     * The advancement position relative to the y-axis.
     */
    protected final float y;

    /**
     * Create a new AdvancementDisplay.
     *
     * @param icon What item will be shown on the GUI.
     * @param title The displayed title of the advancement.
     * @param frame Which shape has the advancement.
     * @param showToast If it shows the toast message.
     * @param announceChat If it shows the announceChat message.
     * @param x X coordinate.
     * @param y Y coordinate..
     * @param description The description of the advancement.
     */
    public AdvancementDisplay(@NotNull Material icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull String... description) {
        this(icon, title, frame, showToast, announceChat, x, y, Arrays.asList(description));
    }

    /**
     * Create a new AdvancementDisplay.
     *
     * @param icon What item will be shown on the GUI.
     * @param title The displayed title of the advancement.
     * @param frame Which shape has the advancement.
     * @param showToast If it shows the toast message.
     * @param announceChat If it shows the announceChat message.
     * @param x X coordinate.
     * @param y Y coordinate..
     * @param description The description of the advancement.
     */
    public AdvancementDisplay(@NotNull Material icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull List<String> description) {
        this(new ItemStack(Objects.requireNonNull(icon, "Icon is null.")), title, frame, showToast, announceChat, x, y, description);
    }

    /**
     * Create a new AdvancementDisplay.
     *
     * @param icon What item will be shown on the GUI.
     * @param title The displayed title of the advancement.
     * @param frame Which shape has the advancement.
     * @param showToast If it shows the toast message.
     * @param announceChat If it shows the announceChat message.
     * @param x X coordinate.
     * @param y Y coordinate..
     * @param description The description of the advancement.
     */
    public AdvancementDisplay(@NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull String... description) {
        this(icon, title, frame, showToast, announceChat, x, y, Arrays.asList(description));
    }

    /**
     * Create a new AdvancementDisplay.
     *
     * @param icon What item will be shown on the GUI.
     * @param title The displayed title of the advancement.
     * @param frame Which shape has the advancement.
     * @param showToast If it shows the toast message.
     * @param announceChat If it shows the announceChat message.
     * @param x X coordinate.
     * @param y Y coordinate..
     * @param description The description of the advancement.
     */
    public AdvancementDisplay(@NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull List<String> description) {
        this(icon, title, frame, showToast, announceChat, x, y, Objects.requireNonNull(frame, "AdvancementFrameType is null.").getColor(), description);
    }

    /**
     * Create a new AdvancementDisplay.
     *
     * @param icon What item will be shown on the GUI.
     * @param title The displayed title of the advancement.
     * @param frame Which shape has the advancement.
     * @param showToast If it shows the toast message.
     * @param announceChat If it shows the announceChat message.
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param defaultColor Description default color.
     * @param description The description of the advancement.
     */
    public AdvancementDisplay(@NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull ChatColor defaultColor, @NotNull String... description) {
        this(icon, title, frame, showToast, announceChat, x, y, defaultColor, Arrays.asList(description));
    }

    /**
     * Create a new AdvancementDisplay.
     *
     * @param icon What item will be shown on the GUI.
     * @param title The displayed title of the advancement.
     * @param frame Which shape has the advancement.
     * @param showToast If it shows the toast message.
     * @param announceChat If it shows the announceChat message.
     * @param x X coordinate.
     * @param y Y coordinate.
     * @param defaultColor Description default color.
     * @param description The description of the advancement.
     */
    public AdvancementDisplay(@NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y, @NotNull ChatColor defaultColor, @NotNull List<String> description) {
        Validate.notNull(icon, "Icon is null.");
        Validate.notNull(title, "Title is null.");
        Validate.notNull(frame, "Frame is null.");
        Validate.notNull(defaultColor, "Default color is null.");
        Validate.notNull(description, "Description is null.");
        Validate.isTrue(isNoElementNull(description), "An element of the description is null.");
        Validate.isTrue(x >= 0, "x is not null or positive.");
        Validate.isTrue(y >= 0, "y is not null or positive.");

        this.icon = icon.clone();
        this.title = title;
        this.description = Collections.unmodifiableList(new ArrayList<>(description));

        // Remove trailing spaces and color codes
        String titleTrimmed = title.trim();
        int toSub = titleTrimmed.length();
        while (titleTrimmed.charAt(toSub - 2) == '§') {
            toSub -= 2;
        }
        this.rawTitle = titleTrimmed.substring(0, toSub).trim();

        this.chatTitle[0] = new TextComponent(defaultColor + rawTitle);
        // Old code, bugged for unknown reasons (found out that BaseComponent[] must have length 1 or it bugs in HoverEvents)
        //this.chatDescription = AdvancementUtils.fromStringList(title, this.description);

        if (this.description.isEmpty()) {
            this.compactDescription = "";
        } else {
            StringJoiner joiner = new StringJoiner("\n" + defaultColor, defaultColor.toString(), "");
            for (String s : this.description)
                joiner.add(s);

            this.compactDescription = joiner.toString();
        }
        if (compactDescription.isEmpty()) {
            this.chatDescription[0] = new TextComponent(defaultColor + rawTitle);
        } else {
            this.chatDescription[0] = new TextComponent(defaultColor + rawTitle + '\n' + compactDescription);
        }

        this.frame = frame;
        this.showToast = showToast;
        this.announceChat = announceChat;
        this.x = x;
        this.y = y;
    }

    /**
     * Returns whether the toast should be sent.
     *
     * @return whether the toast should be sent.
     */
    public boolean doesShowToast() {
        return showToast;
    }

    /**
     * Returns whether the advancement completion message should be sent.
     *
     * @return whether the advancement completion message should be sent.
     */
    public boolean doesAnnounceToChat() {
        return announceChat;
    }

    /**
     * Returns the BaseComponent array of title for the chat reason.
     *
     * @return the BaseComponent array of title.
     */
    public BaseComponent[] getChatTitle() {
        return chatTitle.clone();
    }

    /**
     * Returns the BaseComponent array of description for the chat reason.
     *
     * @return the BaseComponent array of title.
     */
    public BaseComponent[] getChatDescription() {
        return chatDescription.clone();
    }

    /**
     * Returns a clone of the icon.
     *
     * @return a clone of the icon.
     */
    @NotNull
    public ItemStack getIcon() {
        return icon.clone();
    }

    /**
     * Given an advancement, its display is returned based on the minecraft version. It is used in packets.
     *
     * @param advancement The advancement of which you want to get the minecraft display.
     * @return The minecraft advancement, that is, the real advancement display class.
     */
    @NotNull
    public net.minecraft.server.v1_15_R1.AdvancementDisplay getMinecraftDisplay(@NotNull Advancement advancement) {
        Validate.notNull(advancement, "Advancement is null.");
        MinecraftKey bg = null;
        if (advancement instanceof RootAdvancement) {
            bg = new MinecraftKey(((RootAdvancement) advancement).getBackgroundTexture());
        }

        net.minecraft.server.v1_15_R1.AdvancementDisplay advDisplay = new net.minecraft.server.v1_15_R1.AdvancementDisplay(getNMSIcon(), new ChatComponentText(title), new ChatComponentText(compactDescription), bg, frame.getMinecraftFrameType(), false, false, false);
        advDisplay.a(x, y);
        return advDisplay;
    }

    /**
     * Returns the icon according to the minecraft version. It is used in packets.
     *
     * @return The NMS icon.
     */
    @NotNull
    public net.minecraft.server.v1_15_R1.ItemStack getNMSIcon() {
        return CraftItemStack.asNMSCopy(icon);
    }

    private static <T> boolean isNoElementNull(@NotNull List<T> list) {
        for (T t : list) {
            if (t == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the title of the advancement.
     *
     * @return the title of the advancement.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns trimmed and without color codes title.
     *
     * @return the raw title
     */
    public String getRawTitle() {
        return rawTitle;
    }

    /**
     * Returns the description of the advancement.
     *
     * @return the description of the advancement.
     */
    @Unmodifiable
    public List<String> getDescription() {
        return description;
    }

    /**
     * Returns the compact description.
     *
     * @return the compact description.
     */
    public String getCompactDescription() {
        return compactDescription;
    }

    /**
     * Returns the frame type.
     *
     * @return the frame type.
     */
    public AdvancementFrameType getFrame() {
        return frame;
    }

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @return the x-axis.
     */
    public float getX() {
        return x;
    }

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @return the y-axis.
     */
    public float getY() {
        return y;
    }
}
