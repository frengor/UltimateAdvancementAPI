package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractImmutableAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractPerPlayerAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractPerTeamAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplayBuilder;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.database.ProgressionUpdateResult;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.AfterHandle;
import com.fren_gor.ultimateAdvancementAPI.util.LazyValue;
import com.fren_gor.ultimateAdvancementAPI.util.display.ImmutableAdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.display.PerPlayerAdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.display.PerTeamAdvancementDisplayWrapper;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * The {@code FakeAdvancement} class is a non-saved and non-registrable invisible advancement.
 * <p>This means that {@code FakeAdvancement}s are not saved into the database and that they cannot be registered in any tab.
 * <p>They are also not displayed by clients, but the connection to their parent is still visible.
 * Thus, they can be sent in packets to display a connection.
 * <p>Since {@code FakeAdvancement}s are not saved, many methods are not supported.
 */
public class FakeAdvancement extends BaseAdvancement {

    private static final AtomicInteger FAKE_NUMBER = new AtomicInteger(1);

    /**
     * Creates a new {@code FakeAdvancement}.
     *
     * @param parent The parent of the advancement.
     * @param x The x coordinate of the advancement.
     * @param y The y coordinate of the advancement.
     */
    public FakeAdvancement(@NotNull Advancement parent, float x, float y) {
        this(parent, new AdvancementDisplayBuilder(Material.GRASS_BLOCK, "FakeAdvancement").x(x).y(y).build());
    }

    /**
     * Creates a new {@code FakeAdvancement}.
     *
     * @param parent The parent of the advancement.
     * @param display The display information of this advancement.
     */
    public FakeAdvancement(@NotNull Advancement parent, @NotNull AbstractAdvancementDisplay display) {
        super("fakeadvancement._-.-_." + FAKE_NUMBER.getAndIncrement(), wrapDisplay(display), parent);
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method returns always {@code 0}.
     *
     * @return Always {@code 0}.
     */
    @Override
    public int getProgression(@NotNull Player player) {
        return 0;
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method returns always {@code 0}.
     *
     * @return Always {@code 0}.
     */
    @Override
    public int getProgression(@NotNull UUID uuid) {
        return 0;
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method returns always {@code 0}.
     *
     * @return Always {@code 0}.
     */
    @Override
    public int getProgression(@NotNull TeamProgression progression) {
        return 0;
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method returns always {@code true}.
     *
     * @return Always {@code true}.
     */
    @Override
    @Contract("_ -> true")
    public boolean isVisible(@NotNull Player player) {
        return true;
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method returns always {@code true}.
     *
     * @return Always {@code true}.
     */
    @Override
    @Contract("_ -> true")
    public boolean isVisible(@NotNull UUID uuid) {
        return true;
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method returns always {@code true}.
     *
     * @return Always {@code true}.
     */
    @Override
    @Contract("_ -> true")
    public boolean isVisible(@NotNull TeamProgression progression) {
        return true;
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always returns {@code null}.
     *
     * @return Always {@code null}.
     */
    @Override
    @Nullable
    @Contract("_ -> null")
    public Function<@NotNull Player, @Nullable BaseComponent> getAnnouncementMessage(@NotNull Player advancementCompleter) {
        return null;
    }

    private static AbstractAdvancementDisplay wrapDisplay(@NotNull AbstractAdvancementDisplay display) {
        Preconditions.checkNotNull(display, "AbstractAdvancementDisplay is null.");

        if (display instanceof AbstractImmutableAdvancementDisplay immutable) {
            return new ImmutableAdvancementDisplayWrapper(immutable) {
                // Optimize since the display is immutable
                @LazyValue
                private PreparedAdvancementDisplayWrapper wrapper;

                @Override
                @NotNull
                public PreparedAdvancementDisplayWrapper getNMSWrapper() {
                    if (wrapper != null) {
                        return wrapper;
                    }

                    try {
                        // Only x and y matters here, since FakeAdvancements are invisible
                        return wrapper = PreparedAdvancementDisplayWrapper.craft(new ItemStack(Material.GRASS_BLOCK), new TextComponent("FakeAdvancement"), new TextComponent(""), AdvancementFrameType.GOAL.getNMSWrapper(), wrapped.getX(), wrapped.getY(), false, false, true);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        } else if (display instanceof AbstractPerTeamAdvancementDisplay perTeam) {
            return new PerTeamAdvancementDisplayWrapper(perTeam) {
                @Override
                @NotNull
                public PreparedAdvancementDisplayWrapper getNMSWrapper(@NotNull TeamProgression progression) {
                    try {
                        // Only x and y matters here, since FakeAdvancements are invisible
                        return PreparedAdvancementDisplayWrapper.craft(new ItemStack(Material.GRASS_BLOCK), new TextComponent("FakeAdvancement"), new TextComponent(""), AdvancementFrameType.GOAL.getNMSWrapper(), wrapped.getX(progression), wrapped.getY(progression), false, false, true);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        } else if (display instanceof AbstractPerPlayerAdvancementDisplay perPlayer) {
            return new PerPlayerAdvancementDisplayWrapper(perPlayer) {
                @Override
                public @NotNull PreparedAdvancementDisplayWrapper getNMSWrapper(@NotNull Player player) {
                    try {
                        // Only x and y matters here, since FakeAdvancements are invisible
                        return PreparedAdvancementDisplayWrapper.craft(new ItemStack(Material.GRASS_BLOCK), new TextComponent("FakeAdvancement"), new TextComponent(""), AdvancementFrameType.GOAL.getNMSWrapper(), wrapped.getX(player), wrapped.getY(player), false, false, true);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        } else {
            // Should never happen
            throw new ClassCastException(display.getClass().getName() + " is not an immutable, per-team or per-player display.");
        }
    }

    // ============ Overridden methods which throw an UnsupportedOperationException ============

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public boolean isGranted(@NotNull Player player) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public boolean isGranted(@NotNull UUID uuid) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public boolean isGranted(@NotNull TeamProgression progression) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull UUID uuid) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull UUID uuid, boolean giveReward) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int increment) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int increment, boolean giveReward) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull Player player) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull Player player, boolean giveReward) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull Player player, int increment) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull Player player, int increment, boolean giveReward) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    protected CompletableFuture<ProgressionUpdateResult> incrementProgression(@NotNull TeamProgression pro, @Nullable Player player, int increment, boolean giveRewards) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int progression) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int progression, boolean giveReward) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int progression) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int progression, boolean giveReward) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    protected CompletableFuture<ProgressionUpdateResult> setProgression(@NotNull TeamProgression pro, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int progression, boolean giveRewards) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    protected void handlePlayer(@NotNull TeamProgression pro, @Nullable Player player, int newProgression, int oldProgression, boolean giveRewards, @Nullable AfterHandle afterHandle) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public void displayToastToPlayer(@NotNull Player player) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public void onGrant(@NotNull Player player, boolean giveRewards) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public void giveReward(@NotNull Player player) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public void grant(@NotNull Player player) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public void grant(@NotNull Player player, boolean giveRewards) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public void revoke(@NotNull Player player) {
        throw new UnsupportedOperationException();
    }
}
