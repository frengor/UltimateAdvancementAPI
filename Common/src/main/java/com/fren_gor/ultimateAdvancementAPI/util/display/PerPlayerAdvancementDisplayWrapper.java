package com.fren_gor.ultimateAdvancementAPI.util.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractPerPlayerAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public String getLegacyTitle(@NotNull Player player) {
        return wrapped.getLegacyTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String getLegacyTitle(@NotNull OfflinePlayer player) {
        return wrapped.getLegacyTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public BaseComponent getTitle(@NotNull Player player) {
        return wrapped.getTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public BaseComponent getTitle(@NotNull OfflinePlayer player) {
        return wrapped.getTitle(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ChatColor getDefaultTitleColor(@NotNull Player player) {
        return wrapped.getDefaultTitleColor(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ChatColor getDefaultTitleColor(@NotNull OfflinePlayer player) {
        return wrapped.getDefaultTitleColor(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ChatColor getAnnouncementMessageDefaultTitleColor(@NotNull Player player) {
        return wrapped.getAnnouncementMessageDefaultTitleColor(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ChatColor getAnnouncementMessageDefaultTitleColor(@NotNull OfflinePlayer player) {
        return wrapped.getAnnouncementMessageDefaultTitleColor(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<String> getLegacyDescription(@NotNull Player player) {
        return wrapped.getLegacyDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<String> getLegacyDescription(@NotNull OfflinePlayer player) {
        return wrapped.getLegacyDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<BaseComponent> getDescription(@NotNull Player player) {
        return wrapped.getDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<BaseComponent> getDescription(@NotNull OfflinePlayer player) {
        return wrapped.getDescription(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ChatColor getDefaultDescriptionColor(@NotNull Player player) {
        return wrapped.getDefaultDescriptionColor(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ChatColor getDefaultDescriptionColor(@NotNull OfflinePlayer player) {
        return wrapped.getDefaultDescriptionColor(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ChatColor getAnnouncementMessageDefaultDescriptionColor(@NotNull Player player) {
        return wrapped.getAnnouncementMessageDefaultDescriptionColor(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ChatColor getAnnouncementMessageDefaultDescriptionColor(@NotNull OfflinePlayer player) {
        return wrapped.getAnnouncementMessageDefaultDescriptionColor(player);
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
    public PreparedAdvancementDisplayWrapper getNMSWrapper(@NotNull Player player) throws ReflectiveOperationException {
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
