package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * The default implementation of an immutable display.
 *
 * @see AbstractAdvancementDisplay
 * @see AbstractImmutableAdvancementDisplay
 */
public class AdvancementDisplay extends AbstractImmutableAdvancementDisplay {

    /**
     * The icon of the advancement in the advancement GUI.
     */
    protected final ItemStack icon;

    /**
     * The title of the advancement.
     */
    protected final BaseComponent title;

    /**
     * The default color of the title when displayed in the advancement GUI.
     */
    @Nullable
    protected final ChatColor defaultTitleColor;

    /**
     * The default color of the title in the advancement's announcement message.
     */
    @Nullable
    protected final ChatColor announcementMessageDefaultTitleColor;

    /**
     * The description of the advancement.
     */
    @Unmodifiable
    protected final List<BaseComponent> description;

    /**
     * The default color of the description when displayed in the advancement GUI.
     */
    @Nullable
    protected final ChatColor defaultDescriptionColor;

    /**
     * The default color of the description in the advancement's announcement message.
     */
    @Nullable
    protected final ChatColor announcementMessageDefaultDescriptionColor;

    /**
     * The shape of the advancement frame in the advancement GUI.
     */
    protected final AdvancementFrameType frame;

    /**
     * Whether the toast notification should be sent on advancement grant.
     */
    protected final boolean showToast;

    /**
     * Whether the advancement completion message should be sent on advancement grant.
     */
    protected final boolean announceChat;

    /**
     * The advancement x coordinate.
     */
    protected final float x;

    /**
     * The advancement y coordinate.
     */
    protected final float y;

    /**
     * Creates a new {@code AdvancementDisplay}.
     * <p>The advancement is positioned by the x and y coordinates in the advancement GUI. The origin is placed in the
     * upper-left corner of the advancement GUI. The x-axis points to the right (as usual), whereas the y-axis points downward.
     * Thus, the x and y coordinates must be positive.
     *
     * @param icon The advancement's icon in the advancement GUI.
     * @param title The title of the advancement.
     * @param frame The shape of the advancement frame in the advancement GUI.
     * @param showToast Whether the toast notification should be sent on advancement grant.
     * @param announceChat Whether the advancement completion message should be sent on advancement grant.
     * @param x The advancement x coordinate. Must be not negative.
     * @param y The advancement y coordinate. Must be not negative.
     * @param defaultTitleColor The default color of the title when displayed in the advancement GUI.
     * @param amDefaultTitleColor The default color of the title in the advancement's announcement message.
     * @param defaultDescColor The default color of the description when displayed in the advancement GUI.
     * @param amDefaultDescColor The default color of the description in the advancement's announcement message.
     * @param description The description of the advancement.
     */
    protected AdvancementDisplay(@NotNull ItemStack icon, @NotNull BaseComponent title, @NotNull List<BaseComponent> description, @Nullable ChatColor defaultTitleColor, @Nullable ChatColor amDefaultTitleColor, @Nullable ChatColor defaultDescColor, @Nullable ChatColor amDefaultDescColor, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y) {
        Preconditions.checkNotNull(icon, "Icon is null.");
        Preconditions.checkNotNull(title, "Title is null.");
        Preconditions.checkNotNull(frame, "Frame is null.");
        Preconditions.checkNotNull(description, "Description is null.");
        Preconditions.checkArgument(Float.isFinite(x), "x is NaN or infinite.");
        Preconditions.checkArgument(Float.isFinite(y), "y is NaN or infinite.");
        Preconditions.checkArgument(x >= 0, "x is not zero or positive.");
        Preconditions.checkArgument(y >= 0, "y is not zero or positive.");

        this.icon = icon.clone();
        this.title = title.duplicate();
        this.defaultTitleColor = defaultTitleColor;
        this.announcementMessageDefaultTitleColor = amDefaultTitleColor;
        this.description = description.stream().map(BaseComponent::duplicate).toList();
        this.defaultDescriptionColor = defaultDescColor;
        this.announcementMessageDefaultDescriptionColor = amDefaultDescColor;
        this.frame = frame;
        this.showToast = showToast;
        this.announceChat = announceChat;
        this.x = x;
        this.y = y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doesShowToast() {
        return showToast;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doesAnnounceToChat() {
        return announceChat;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public ItemStack getIcon() {
        return icon.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public BaseComponent getTitle() {
        return title.duplicate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ChatColor getDefaultTitleColor() {
        return defaultTitleColor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ChatColor getAnnouncementMessageDefaultTitleColor() {
        return announcementMessageDefaultTitleColor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Unmodifiable
    public List<BaseComponent> getDescription() {
        return description.stream().map(BaseComponent::duplicate).toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ChatColor getDefaultDescriptionColor() {
        return defaultDescriptionColor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ChatColor getAnnouncementMessageDefaultDescriptionColor() {
        return announcementMessageDefaultDescriptionColor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public AdvancementFrameType getFrame() {
        return frame;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getX() {
        return x;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getY() {
        return y;
    }
}
