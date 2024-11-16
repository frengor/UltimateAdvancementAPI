package com.fren_gor.ultimateAdvancementAPI.util.display;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractPerTeamAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Wrapper around an {@link AbstractPerTeamAdvancementDisplay}.
 * <p>Every method calls the wrapped display provided in the constructor. To modify the behaviour of a method,
 * extend this class and override the necessary methods.
 */
public class PerTeamAdvancementDisplayWrapper extends AbstractPerTeamAdvancementDisplay {

    /**
     * The wrapped {@link AbstractPerTeamAdvancementDisplay}.
     */
    protected final AbstractPerTeamAdvancementDisplay wrapped;

    /**
     * Creates a new {@code PerTeamAdvancementDisplayWrapper}.
     *
     * @param wrapped The wrapped {@link AbstractPerTeamAdvancementDisplay}.
     */
    public PerTeamAdvancementDisplayWrapper(@NotNull AbstractPerTeamAdvancementDisplay wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "Wrapped AbstractPerTeamAdvancementDisplay is null.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doesShowToast(@NotNull TeamProgression progression) {
        return wrapped.doesShowToast(progression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean doesAnnounceToChat(@NotNull TeamProgression progression) {
        return wrapped.doesAnnounceToChat(progression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public ItemStack getIcon(@NotNull TeamProgression progression) {
        return wrapped.getIcon(progression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String getLegacyTitle(@NotNull TeamProgression progression) {
        return wrapped.getLegacyTitle(progression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public BaseComponent getTitle(@NotNull TeamProgression progression) {
        return wrapped.getTitle(progression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<String> getLegacyDescription(@NotNull TeamProgression progression) {
        return wrapped.getLegacyDescription(progression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public List<BaseComponent> getDescription(@NotNull TeamProgression progression) {
        return wrapped.getDescription(progression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public AdvancementFrameType getFrame(@NotNull TeamProgression progression) {
        return wrapped.getFrame(progression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getX(@NotNull TeamProgression progression) {
        return wrapped.getX(progression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getY(@NotNull TeamProgression progression) {
        return wrapped.getY(progression);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public PreparedAdvancementDisplayWrapper getNMSWrapper(@NotNull TeamProgression progression) throws ReflectiveOperationException {
        return wrapped.getNMSWrapper(progression);
    }

    @Override
    public String toString() {
        return "PerTeamAdvancementDisplayWrapper{" +
                "wrapped=" + wrapped +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PerTeamAdvancementDisplayWrapper that = (PerTeamAdvancementDisplayWrapper) o;

        return wrapped.equals(that.wrapped);
    }

    @Override
    public int hashCode() {
        return wrapped.hashCode();
    }
}
