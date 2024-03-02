package com.fren_gor.ultimateAdvancementAPI.util.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractImmutableAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Wrapper around an {@link AbstractImmutableAdvancementDisplay}.
 * <p>Every method calls the wrapped display provided in the constructor. To modify the behaviour of a method,
 * extend this class and override the necessary methods.
 */
public class ImmutableAdvancementDisplayWrapper extends AbstractImmutableAdvancementDisplay {

    /**
     * The wrapped {@link AbstractImmutableAdvancementDisplay}.
     */
    protected final AbstractImmutableAdvancementDisplay wrapped;

    /**
     * Creates a new {@code ImmutableAdvancementDisplayWrapper}.
     *
     * @param wrapped The wrapped {@link AbstractImmutableAdvancementDisplay}.
     */
    public ImmutableAdvancementDisplayWrapper(@NotNull AbstractImmutableAdvancementDisplay wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "Wrapped AbstractImmutableAdvancementDisplay is null.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doesShowToast() {
        return wrapped.doesShowToast();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doesAnnounceToChat() {
        return wrapped.doesAnnounceToChat();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public ItemStack getIcon() {
        return wrapped.getIcon();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String getTitle() {
        return wrapped.getTitle();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public BaseComponent[] getTitleBaseComponent() {
        return wrapped.getTitleBaseComponent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<String> getDescription() {
        return wrapped.getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<BaseComponent[]> getDescriptionBaseComponent() {
        return wrapped.getDescriptionBaseComponent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public AdvancementFrameType getFrame() {
        return wrapped.getFrame();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getX() {
        return wrapped.getX();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getY() {
        return wrapped.getY();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper getNMSWrapper() {
        return wrapped.getNMSWrapper();
    }

    @Override
    public String toString() {
        return "ImmutableAdvancementDisplayWrapper{" +
                "wrapped=" + wrapped +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutableAdvancementDisplayWrapper that = (ImmutableAdvancementDisplayWrapper) o;

        return wrapped.equals(that.wrapped);
    }

    @Override
    public int hashCode() {
        return wrapped.hashCode();
    }
}
