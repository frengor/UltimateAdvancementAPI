package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter.Coord;
import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;

/**
 * A builder for advancement displays.
 *
 * @since 2.1.0
 */
@SuppressWarnings("unchecked")
public abstract class AdvancementDisplayBuilder<T extends AdvancementDisplayBuilder<T, R>, R extends AdvancementDisplay> {

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
    @Unmodifiable
    protected List<String> description = List.of();

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
     * @param title The title of the advancement.
     */
    protected AdvancementDisplayBuilder(@NotNull Material icon, @NotNull String title) {
        this.icon = new ItemStack(Objects.requireNonNull(icon, "Icon is null."));
        this.title = Objects.requireNonNull(title, "Title is null.");
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
    protected AdvancementDisplayBuilder(@NotNull ItemStack icon, @NotNull String title) {
        this.icon = Objects.requireNonNull(icon, "Icon is null.").clone();
        this.title = Objects.requireNonNull(title, "Title is null.");
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
    public T coords(@NotNull CoordAdapter adapter, @NotNull AdvancementKey key) {
        Preconditions.checkNotNull(adapter, "CoordAdapter is null.");
        Coord coord = adapter.getXAndY(Objects.requireNonNull(key, "AdvancementKey is null."));
        coords(coord.x(), coord.y());
        return (T) this;
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
    public T coords(float x, float y) {
        x(x);
        y(y);
        return (T) this;
    }

    /**
     * Sets the x and y coordinates of the advancement in the advancement GUI.
     * <p>The origin is placed in the upper-left corner of the advancement GUI. The x-axis points to the right (as usual).
     *
     * @param x The advancement x coordinate. Must be not negative.
     * @return This builder.
     */
    @NotNull
    public T x(float x) {
        Preconditions.checkArgument(x >= 0, "x is not zero or positive.");
        this.x = x;
        return (T) this;
    }

    /**
     * Sets the y coordinate of the advancement in the advancement GUI.
     * <p>The origin is placed in the upper-left corner of the advancement GUI. The y-axis points downward.
     *
     * @param y The advancement y coordinate. Must be not negative.
     * @return This builder.
     */
    @NotNull
    public T y(float y) {
        Preconditions.checkArgument(y >= 0, "y is not zero or positive.");
        this.y = y;
        return (T) this;
    }

    /**
     * Sets the description of the advancement.
     *
     * @param description The description of the advancement.
     * @return This builder.
     */
    @NotNull
    public T description(@NotNull String... description) {
        this.description = List.of(description);
        return (T) this;
    }

    /**
     * Sets the description of the advancement.
     *
     * @param description The description of the advancement.
     * @return This builder.
     */
    @NotNull
    public T description(@NotNull List<String> description) {
        this.description = List.copyOf(description);
        return (T) this;
    }

    /**
     * Sets the shape of the advancement frame in the advancement GUI.
     *
     * @param frame The shape of the advancement frame in the advancement GUI.
     * @return This builder.
     */
    @NotNull
    public T frame(@NotNull AdvancementFrameType frame) {
        this.frame = Objects.requireNonNull(frame, "Frame is null.");
        return (T) this;
    }

    /**
     * Sets the shape of the advancement frame to {@link AdvancementFrameType#TASK}.
     *
     * @return This builder.
     */
    @NotNull
    public T taskFrame() {
        return frame(AdvancementFrameType.TASK);
    }

    /**
     * Sets the shape of the advancement frame to {@link AdvancementFrameType#GOAL}.
     *
     * @return This builder.
     */
    @NotNull
    public T goalFrame() {
        return frame(AdvancementFrameType.GOAL);
    }

    /**
     * Sets the shape of the advancement frame to {@link AdvancementFrameType#CHALLENGE}.
     *
     * @return This builder.
     */
    @NotNull
    public T challengeFrame() {
        return frame(AdvancementFrameType.CHALLENGE);
    }

    /**
     * Enables the toast notification sent on advancement grant.
     *
     * @return This builder.
     */
    @NotNull
    public T showToast() {
        return showToast(true);
    }

    /**
     * Enables or disables the toast notification sent on advancement grant.
     *
     * @param showToast Whether to show the toast notification on advancement grant.
     * @return This builder.
     */
    public T showToast(boolean showToast) {
        this.showToast = showToast;
        return (T) this;
    }

    /**
     * Enables the advancement completion message sent on advancement grant.
     *
     * @return This builder.
     */
    @NotNull
    public T announceChat() {
        return announceChat(true);
    }

    /**
     * Enables or disables the advancement completion message sent on advancement grant.
     *
     * @param announceChat Whether to send the advancement completion message on advancement grant.
     * @return This builder.
     */
    @NotNull
    public T announceChat(boolean announceChat) {
        this.announceChat = announceChat;
        return (T) this;
    }

    /**
     * Builds the advancement display.
     *
     * @return The built advancement display.
     */
    @NotNull
    public abstract R build();

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
     * Gets the title of the advancement.
     *
     * @return The title of the advancement.
     */
    @NotNull
    public String getTitle() {
        return title;
    }

    /**
     * Gets the description of the advancement.
     *
     * @return The description of the advancement.
     */
    @NotNull
    public List<String> getDescription() {
        return description;
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
}
