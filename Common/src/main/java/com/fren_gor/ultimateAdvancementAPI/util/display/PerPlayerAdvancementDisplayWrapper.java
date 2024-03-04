package com.fren_gor.ultimateAdvancementAPI.util.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractPerPlayerAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Wrapper around an {@link AbstractPerPlayerAdvancementDisplay}.
 * <p>Every method calls the wrapped display provided in the constructor. To modify the behaviour of a method,
 * extend this class and override the necessary methods.
 */
public class PerPlayerAdvancementDisplayWrapper extends AbstractPerPlayerAdvancementDisplay {

    /**
     * The wrapped {@link AbstractPerPlayerAdvancementDisplay}.
     */
    protected final AbstractPerPlayerAdvancementDisplay wrapped;

    /**
     * Creates a new {@code PerPlayerAdvancementDisplayWrapper}.
     *
     * @param wrapped The wrapped {@link AbstractPerPlayerAdvancementDisplay}.
     */
    public PerPlayerAdvancementDisplayWrapper(@NotNull AbstractPerPlayerAdvancementDisplay wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "Wrapped AbstractPerPlayerAdvancementDisplay is null.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doesShowToast(@NotNull Player player) {
        return wrapped.doesShowToast(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doesShowToast(@NotNull OfflinePlayer player) {
        return wrapped.doesShowToast(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doesAnnounceToChat(@NotNull Player player) {
        return wrapped.doesAnnounceToChat(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doesAnnounceToChat(@NotNull OfflinePlayer player) {
        return wrapped.doesAnnounceToChat(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public ItemStack getIcon(@NotNull Player player) {
        return wrapped.getIcon(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public ItemStack getIcon(@NotNull OfflinePlayer player) {
        return wrapped.getIcon(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String getTitle(@NotNull Player player) {
        return wrapped.getTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String getTitle(@NotNull OfflinePlayer player) {
        return wrapped.getTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public BaseComponent[] getTitleBaseComponent(@NotNull Player player) {
        return wrapped.getTitleBaseComponent(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public BaseComponent[] getTitleBaseComponent(@NotNull OfflinePlayer player) {
        return wrapped.getTitleBaseComponent(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<String> getDescription(@NotNull Player player) {
        return wrapped.getDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<String> getDescription(@NotNull OfflinePlayer player) {
        return wrapped.getDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<BaseComponent[]> getDescriptionBaseComponent(@NotNull Player player) {
        return wrapped.getDescriptionBaseComponent(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<BaseComponent[]> getDescriptionBaseComponent(@NotNull OfflinePlayer player) {
        return wrapped.getDescriptionBaseComponent(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public AdvancementFrameType getFrame(@NotNull Player player) {
        return wrapped.getFrame(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public AdvancementFrameType getFrame(@NotNull OfflinePlayer player) {
        return wrapped.getFrame(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getX(@NotNull Player player) {
        return wrapped.getX(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getX(@NotNull OfflinePlayer player) {
        return wrapped.getX(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getY(@NotNull Player player) {
        return wrapped.getY(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getY(@NotNull OfflinePlayer player) {
        return wrapped.getY(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper getNMSWrapper(@NotNull Player player) {
        return wrapped.getNMSWrapper(player);
    }

    @Override
    public String toString() {
        return "PerPlayerAdvancementDisplayWrapper{" +
                "wrapped=" + wrapped +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PerPlayerAdvancementDisplayWrapper that = (PerPlayerAdvancementDisplayWrapper) o;

        return wrapped.equals(that.wrapped);
    }

    @Override
    public int hashCode() {
        return wrapped.hashCode();
    }
}
