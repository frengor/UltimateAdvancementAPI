package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.announceMessage.IAnnounceMessage;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter.Coord;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A builder for {@link AdvancementDisplay}.
 *
 * @since 2.1.0
 */
public class AdvancementDisplayBuilder {

    /**
     * The icon of the advancement in the advancement GUI.
     */
    protected ItemStack icon;

    /**
     * The title of the advancement.
     */
    protected BaseComponent title;

    /**
     * The default color of the title when displayed in the advancement GUI.
     * <p>{@code null} to use Minecraft's default color.
     */
    @Nullable
    protected ChatColor defaultTitleColor = null;

    /**
     * The default color of the title in the advancement's announcement message.
     * <p>{@code null} to use the frame's color (i.e. the color returned by calling
     * {@link AdvancementFrameType#getColor() getColor()} on the {@code frame}).
     */
    @Nullable
    protected ChatColor announcementMessageDefaultTitleColor = null;

    /**
     * The description of the advancement.
     */
    @Unmodifiable
    protected List<BaseComponent> description = List.of();

    /**
     * The default color of the description.
     * <p>{@code null} to use Minecraft's default color.
     */
    @Nullable
    protected ChatColor defaultDescriptionColor = null;

    /**
     * The default color of the description in the advancement's announcement message.
     * <p>{@code null} to use the frame's color (i.e. the color returned by calling
     * {@link AdvancementFrameType#getColor() getColor()} on the {@code frame}).
     */
    @Nullable
    protected ChatColor announcementMessageDefaultDescriptionColor = null;

    /**
     * The shape of the advancement frame in the advancement GUI.
     */
    protected AdvancementFrameType frame = AdvancementFrameType.TASK;

    /**
     * Whether the toast notification should be sent on advancement grant.
     */
    protected boolean showToast = false;

    /**
     * Whether the advancement completion message should be sent on advancement grant.
     */
    protected boolean announceChat = false;

    /**
     * The advancement x coordinate. Must be &gt;= 0.
     */
    protected float x = 0;

    /**
     * The advancement y coordinate. Must be &gt;= 0.
     */
    protected float y = 0;

    /**
     * Creates a new {@code AdvancementDisplayBuilder}.
     * <p>By default, the advancement display returned by {@link #build()} won't show both the toast message and
     * the announcement message in the chat upon advancement completion.
     * <p>The default {@code frame} is {@link AdvancementFrameType#TASK}.
     *
     * @param icon The material of the advancement's icon in the advancement GUI.
     * @param legacyTitle The title of the advancement as a legacy string.
     */
    public AdvancementDisplayBuilder(@NotNull Material icon, @NotNull String legacyTitle) {
        this(icon, AdvancementUtils.fromLegacy(Objects.requireNonNull(legacyTitle, "Title is null.")));
    }

    /**
     * Creates a new {@code AdvancementDisplayBuilder}.
     * <p>By default, the advancement display returned by {@link #build()} won't show both the toast message and
     * the announcement message in the chat upon advancement completion.
     * <p>The default {@code frame} is {@link AdvancementFrameType#TASK}.
     *
     * @param icon The advancement's icon in the advancement GUI.
     * @param legacyTitle The title of the advancement as a legacy string.
     */
    public AdvancementDisplayBuilder(@NotNull ItemStack icon, @NotNull String legacyTitle) {
        this(icon, AdvancementUtils.fromLegacy(Objects.requireNonNull(legacyTitle, "Title is null.")));
    }

    /**
     * Creates a new {@code AdvancementDisplayBuilder}.
     * <p>By default, the advancement display returned by {@link #build()} won't show both the toast message and
     * the announcement message in the chat upon advancement completion.
     * <p>The default {@code frame} is {@link AdvancementFrameType#TASK}.
     *
     * @param icon The material of the advancement's icon in the advancement GUI.
     * @param title The title of the advancement.
     */
    public AdvancementDisplayBuilder(@NotNull Material icon, @NotNull BaseComponent title) {
        this.icon = new ItemStack(Objects.requireNonNull(icon, "Icon is null."));
        this.title = Objects.requireNonNull(title, "Title is null.").duplicate();
    }

    /**
     * Creates a new {@code AdvancementDisplayBuilder}.
     * <p>By default, the advancement display returned by {@link #build()} won't show both the toast message and
     * the announcement message in the chat upon advancement completion.
     * <p>The default {@code frame} is {@link AdvancementFrameType#TASK}.
     *
     * @param icon The advancement's icon in the advancement GUI.
     * @param title The title of the advancement.
     */
    public AdvancementDisplayBuilder(@NotNull ItemStack icon, @NotNull BaseComponent title) {
        this.icon = Objects.requireNonNull(icon, "Icon is null.").clone();
        this.title = Objects.requireNonNull(title, "Title is null.").duplicate();
    }

    /**
     * Sets the coordinates of the advancement in the advancement GUI, taking them from the provided {@link CoordAdapter}.
     *
     * @param adapter The {@link CoordAdapter} from which the coordinates are taken.
     * @param key The {@link AdvancementKey} of the advancement.
     * @return This builder.
     * @see CoordAdapter
     */
    @NotNull
    public AdvancementDisplayBuilder coords(@NotNull CoordAdapter adapter, @NotNull AdvancementKey key) {
        Preconditions.checkNotNull(adapter, "CoordAdapter is null.");
        Coord coord = adapter.getXAndY(Objects.requireNonNull(key, "AdvancementKey is null."));
        coords(coord.x(), coord.y());
        return this;
    }

    /**
     * Sets the x and y coordinates of the advancement in the advancement GUI.
     * <p>The origin is placed in the upper-left corner of the advancement GUI.
     * The x-axis points to the right (as usual), whereas the y-axis points downward.
     * Thus, the x and y coordinates must be positive.
     *
     * @param x The advancement x coordinate. Must be not negative.
     * @param y The advancement y coordinate. Must be not negative.
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder coords(float x, float y) {
        x(x);
        y(y);
        return this;
    }

    /**
     * Sets the x and y coordinates of the advancement in the advancement GUI.
     * <p>The origin is placed in the upper-left corner of the advancement GUI. The x-axis points to the right (as usual).
     *
     * @param x The advancement x coordinate. Must be not negative.
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder x(float x) {
        Preconditions.checkArgument(x >= 0, "x is not zero or positive.");
        this.x = x;
        return this;
    }

    /**
     * Sets the y coordinate of the advancement in the advancement GUI.
     * <p>The origin is placed in the upper-left corner of the advancement GUI. The y-axis points downward.
     *
     * @param y The advancement y coordinate. Must be not negative.
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder y(float y) {
        Preconditions.checkArgument(y >= 0, "y is not zero or positive.");
        this.y = y;
        return this;
    }

    /**
     * Sets the icon of the advancement.
     *
     * @param icon The material of the advancement's icon.
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder icon(@NotNull Material icon) {
        this.icon = new ItemStack(Objects.requireNonNull(icon, "Icon is null."));
        return this;
    }

    /**
     * Sets the icon of the advancement.
     *
     * @param icon The advancement's icon.
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder icon(@NotNull ItemStack icon) {
        this.icon = Objects.requireNonNull(icon, "Icon is null.").clone();
        return this;
    }

    /**
     * Sets the title of the advancement.
     *
     * @param legacyTitle The title of the advancement as a legacy string.
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder title(@NotNull String legacyTitle) {
        this.title = AdvancementUtils.fromLegacy(Objects.requireNonNull(legacyTitle, "Title is null."));
        return this;
    }

    /**
     * Sets the title of the advancement.
     *
     * @param title The title of the advancement.
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder title(@NotNull BaseComponent title) {
        this.title = Objects.requireNonNull(title, "Title is null.").duplicate();
        return this;
    }

    /**
     * Sets the description of the advancement.
     *
     * @param legacyDescription The description of the advancement as a list of legacy strings.
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder description(@NotNull String... legacyDescription) {
        Preconditions.checkNotNull(legacyDescription, "Description is null.");
        this.description = Arrays.stream(legacyDescription).map(AdvancementUtils::fromLegacy).toList();
        return this;
    }

    /**
     * Sets the description of the advancement.
     *
     * @param description The description of the advancement.
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder description(@NotNull BaseComponent... description) {
        Preconditions.checkNotNull(description, "Description is null.");
        this.description = Arrays.stream(description).map(BaseComponent::duplicate).toList();
        return this;
    }

    /**
     * Sets the description of the advancement.
     *
     * @param description The description of the advancement.
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder description(@NotNull List<BaseComponent> description) {
        Preconditions.checkNotNull(description, "Description is null.");
        this.description = description.stream().map(BaseComponent::duplicate).toList();
        return this;
    }

    /**
     * Sets the shape of the advancement frame in the advancement GUI.
     *
     * @param frame The shape of the advancement frame in the advancement GUI.
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder frame(@NotNull AdvancementFrameType frame) {
        this.frame = Objects.requireNonNull(frame, "Frame is null.");
        return this;
    }

    /**
     * Sets the shape of the advancement frame to {@link AdvancementFrameType#TASK}.
     *
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder taskFrame() {
        return frame(AdvancementFrameType.TASK);
    }

    /**
     * Sets the shape of the advancement frame to {@link AdvancementFrameType#GOAL}.
     *
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder goalFrame() {
        return frame(AdvancementFrameType.GOAL);
    }

    /**
     * Sets the shape of the advancement frame to {@link AdvancementFrameType#CHALLENGE}.
     *
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder challengeFrame() {
        return frame(AdvancementFrameType.CHALLENGE);
    }

    /**
     * Enables the toast notification sent on advancement grant.
     *
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder showToast() {
        return showToast(true);
    }

    /**
     * Enables or disables the toast notification sent on advancement grant.
     *
     * @param showToast Whether to show the toast notification on advancement grant.
     * @return This builder.
     */
    public AdvancementDisplayBuilder showToast(boolean showToast) {
        this.showToast = showToast;
        return this;
    }

    /**
     * Enables the advancement completion message sent on advancement grant.
     *
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder announceChat() {
        return announceChat(true);
    }

    /**
     * Enables or disables the advancement completion message sent on advancement grant.
     *
     * @param announceChat Whether to send the advancement completion message on advancement grant.
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder announceChat(boolean announceChat) {
        this.announceChat = announceChat;
        return this;
    }

    /**
     * Sets the default color of the title when displayed in the advancement GUI.
     *
     * @param defaultColor The default color of the title when displayed in the advancement GUI
     *         or {@code null} to use Minecraft's default color.
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder defaultTitleColor(@Nullable ChatColor defaultColor) {
        this.defaultTitleColor = defaultColor;
        return this;
    }

    /**
     * Sets the default color of the title in the advancement's announcement message.
     *
     * @param defaultColor The default color of the title in the advancement's announcement message
     *         or {@code null} to use the frame's color (i.e. the color returned by calling
     *         {@link AdvancementFrameType#getColor() getColor()} on the {@link #frame frame}).
     * @return This builder.
     * @see IAnnounceMessage
     * @see Advancement#getAnnounceMessage(Player)
     */
    @NotNull
    public AdvancementDisplayBuilder announcementMessageDefaultTitleColor(@Nullable ChatColor defaultColor) {
        this.announcementMessageDefaultTitleColor = defaultColor;
        return this;
    }

    /**
     * Sets the default color of the description when displayed in the advancement GUI.
     *
     * @param defaultColor The default color of the description when displayed in the advancement GUI
     *         or {@code null} to use Minecraft's default color.
     * @return This builder.
     */
    @NotNull
    public AdvancementDisplayBuilder defaultDescriptionColor(@Nullable ChatColor defaultColor) {
        this.defaultDescriptionColor = defaultColor;
        return this;
    }

    /**
     * Sets the default color of the description in the advancement's announcement message.
     *
     * @param defaultColor The default color of the description in the advancement's announcement message
     *         or {@code null} to use the frame's color (i.e. the color returned by calling
     *         {@link AdvancementFrameType#getColor() getColor()} on the {@link #frame frame}).
     * @return This builder.
     * @see IAnnounceMessage
     * @see Advancement#getAnnounceMessage(Player)
     */
    @NotNull
    public AdvancementDisplayBuilder announcementMessageDefaultDescriptionColor(@Nullable ChatColor defaultColor) {
        this.announcementMessageDefaultDescriptionColor = defaultColor;
        return this;
    }

    /**
     * Builds the advancement display.
     *
     * @return The built advancement display.
     */
    @NotNull
    public AdvancementDisplay build() {
        return new AdvancementDisplay(icon, title, description, defaultTitleColor, announcementMessageDefaultTitleColor, defaultDescriptionColor, announcementMessageDefaultDescriptionColor, frame, showToast, announceChat, x, y);
    }

    /**
     * Gets the icon of the advancement in the advancement GUI.
     *
     * @return The icon of the advancement in the advancement GUI.
     */
    @NotNull
    public ItemStack getIcon() {
        return icon.clone();
    }

    /**
     * Gets the title of the advancement as a legacy string.
     *
     * @return The title of the advancement as a legacy string.
     */
    @NotNull
    public String getLegacyTitle() {
        return TextComponent.toLegacyText(title);
    }

    /**
     * Gets the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public BaseComponent getTitle() {
        return title.duplicate();
    }

    /**
     * Gets the description of the advancement as a list of legacy strings.
     *
     * @return The description of the advancement as a list of legacy strings.
     */
    @NotNull
    @Unmodifiable
    public List<String> getLegacyDescription() {
        return description.stream().map(TextComponent::toLegacyText).toList();
    }

    /**
     * Gets the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @NotNull
    @Unmodifiable
    public List<BaseComponent> getDescription() {
        return description.stream().map(BaseComponent::duplicate).toList();
    }

    /**
     * Gets the shape of the advancement frame in the advancement GUI.
     *
     * @return The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
    public AdvancementFrameType getFrame() {
        return frame;
    }

    /**
     * Returns whether the toast notification should be sent on advancement grant.
     *
     * @return Whether the toast notification should be sent on advancement grant.
     */
    public boolean doesShowToast() {
        return showToast;
    }

    /**
     * Returns whether the advancement completion message should be sent on advancement grant.
     *
     * @return Whether the advancement completion message should be sent on advancement grant.
     */
    public boolean doesAnnounceToChat() {
        return announceChat;
    }

    /**
     * Returns the advancement position relative to the x-axis.
     *
     * @return The x coordinate.
     */
    public float getX() {
        return x;
    }

    /**
     * Returns the advancement position relative to the y-axis.
     *
     * @return The y coordinate.
     */
    public float getY() {
        return y;
    }

    /**
     * Gets the default color of the title when displayed in the advancement GUI.
     *
     * @return The default color of the title when displayed in the advancement GUI
     *         or {@code null} if Minecraft's default color should be used.
     */
    @Nullable
    public ChatColor getDefaultTitleColor() {
        return defaultTitleColor;
    }

    /**
     * Returns the default color of the title in the advancement's announcement message.
     *
     * @return The default color of the title in the advancement's announcement message
     *         or {@code null} if the frame's color (i.e. the color returned by calling
     *         {@link AdvancementFrameType#getColor() getColor()} on the {@link #frame frame}) should be used.
     * @see IAnnounceMessage
     * @see Advancement#getAnnounceMessage(Player)
     */
    @Nullable
    public ChatColor getAnnouncementMessageDefaultTitleColor() {
        return announcementMessageDefaultTitleColor;
    }

    /**
     * Returns the default color of the description when displayed in the advancement GUI.
     *
     * @return The default color of the description when displayed in the advancement GUI
     *         or {@code null} if Minecraft's default color should be used.
     */
    @Nullable
    public ChatColor getDefaultDescriptionColor() {
        return defaultDescriptionColor;
    }

    /**
     * Returns the default color of the description in the advancement's announcement message.
     *
     * @return The default color of the description in the advancement's announcement message
     *         or {@code null} if the frame's color (i.e. the color returned by calling
     *         {@link AdvancementFrameType#getColor() getColor()} on the {@link #frame frame}) should be used.
     * @see IAnnounceMessage
     * @see Advancement#getAnnounceMessage(Player)
     */
    @Nullable
    public ChatColor getAnnouncementMessageDefaultDescriptionColor() {
        return announcementMessageDefaultDescriptionColor;
    }
}
