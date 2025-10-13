package com.fren_gor.ultimateAdvancementAPI.advancement.display;

import com.fren_gor.ultimateAdvancementAPI.util.display.DefaultStyle;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
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
    @NotNull
    protected final ItemStack icon;

    /**
     * The title of the advancement.
     */
    @NotNull
    protected final BaseComponent title;

    /**
     * The default style of the title when displayed in the advancement GUI.
     */
    @NotNull
    protected final DefaultStyle defaultTitleStyle;

    /**
     * The default style of the title in the advancement's announcement message.
     */
    @NotNull
    protected final DefaultStyle announcementMessageDefaultTitleStyle;

    /**
     * The description of the advancement.
     */
    @Unmodifiable
    protected final List<BaseComponent> description;

    /**
     * The default style of the description when displayed in the advancement GUI.
     */
    @NotNull
    protected final DefaultStyle defaultDescriptionStyle;

    /**
     * The default style of the description in the advancement's announcement message.
     */
    @NotNull
    protected final DefaultStyle announcementMessageDefaultDescriptionStyle;

    /**
     * The shape of the advancement frame in the advancement GUI.
     */
    @NotNull
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
     * @param defaultTitleStyle The default style of the title when displayed in the advancement GUI.
     * @param amDefaultTitleStyle The default style of the title in the advancement's announcement message.
     * @param defaultDescStyle The default style of the description when displayed in the advancement GUI.
     * @param amDefaultDescStyle The default style of the description in the advancement's announcement message.
     * @param description The description of the advancement.
     */
    protected AdvancementDisplay(@NotNull ItemStack icon, @NotNull BaseComponent title, @NotNull List<BaseComponent> description, @NotNull DefaultStyle defaultTitleStyle, @NotNull DefaultStyle amDefaultTitleStyle, @NotNull DefaultStyle defaultDescStyle, @NotNull DefaultStyle amDefaultDescStyle, @NotNull AdvancementFrameType frame, boolean showToast, boolean announceChat, float x, float y) {
        Preconditions.checkNotNull(icon, "Icon is null.");
        Preconditions.checkNotNull(title, "Title is null.");
        Preconditions.checkNotNull(frame, "Frame is null.");
        Preconditions.checkNotNull(description, "Description is null.");
        Preconditions.checkNotNull(defaultTitleStyle, "Default title style is null.");
        Preconditions.checkNotNull(amDefaultTitleStyle, "Default announcement message title style is null.");
        Preconditions.checkNotNull(defaultDescStyle, "Default description style is null.");
        Preconditions.checkNotNull(amDefaultDescStyle, "Default announcement message description style is null.");
        Preconditions.checkArgument(Float.isFinite(x), "x is NaN or infinite.");
        Preconditions.checkArgument(Float.isFinite(y), "y is NaN or infinite.");
        Preconditions.checkArgument(x >= 0, "x is not zero or positive.");
        Preconditions.checkArgument(y >= 0, "y is not zero or positive.");

        this.icon = icon.clone();
        this.title = title.duplicate();
        this.defaultTitleStyle = defaultTitleStyle;
        this.announcementMessageDefaultTitleStyle = amDefaultTitleStyle;
        this.description = description.stream().map(BaseComponent::duplicate).toList();
        this.defaultDescriptionStyle = defaultDescStyle;
        this.announcementMessageDefaultDescriptionStyle = amDefaultDescStyle;
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
    @NotNull
    public DefaultStyle getDefaultTitleStyle() {
        return defaultTitleStyle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public DefaultStyle getAnnouncementMessageDefaultTitleStyle() {
        return announcementMessageDefaultTitleStyle;
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
    @NotNull
    public DefaultStyle getDefaultDescriptionStyle() {
        return defaultDescriptionStyle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public DefaultStyle getAnnouncementMessageDefaultDescriptionStyle() {
        return announcementMessageDefaultDescriptionStyle;
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
