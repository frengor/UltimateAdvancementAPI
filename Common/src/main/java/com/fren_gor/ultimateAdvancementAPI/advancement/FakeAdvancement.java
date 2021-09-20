package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.util.AfterHandle;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_15_R1.AdvancementProgress;
import net.minecraft.server.v1_15_R1.ChatComponentText;
import net.minecraft.server.v1_15_R1.Criterion;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.ADV_REWARDS;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementCriteria;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementRequirements;

/**
 * The {@code FakeAdvancement} class is a non-saved and non-registrable invisible advancement.
 * <p>This means that {@code FakeAdvancement}s are not saved into the database and that they cannot be registered in any tab.
 * <p>They are also not displayed by clients, but the connection to their parent is still visible.
 * Thus, they can be sent in packets to display a connection.
 * <p>Since {@code FakeAdvancement}s are not saved, many methods are not supported.
 */
public final class FakeAdvancement extends BaseAdvancement {

    private static final AtomicInteger FAKE_NUMBER = new AtomicInteger(1);

    private net.minecraft.server.v1_15_R1.Advancement mcAdvancement;

    /**
     * Creates a new {@code FakeAdvancement}.
     *
     * @param parent The parent of the advancement.
     * @param x The x coordinate of the advancement.
     * @param y The y coordinate of the advancement.
     */
    public FakeAdvancement(@NotNull Advancement parent, float x, float y) {
        this(parent, new FakeAdvancementDisplay(Material.GRASS_BLOCK, "FakeAdvancement", AdvancementFrameType.TASK, x, y));
    }

    /**
     * Creates a new {@code FakeAdvancement}.
     *
     * @param parent The parent of the advancement.
     * @param display The display information of this advancement.
     */
    public FakeAdvancement(@NotNull Advancement parent, @NotNull FakeAdvancementDisplay display) {
        super("fakeadvancement._-.-_." + FAKE_NUMBER.getAndIncrement(), display, parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public FakeAdvancementDisplay getDisplay() {
        return (FakeAdvancementDisplay) super.getDisplay();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public net.minecraft.server.v1_15_R1.Advancement getMinecraftAdvancement() {
        if (mcAdvancement != null) {
            return mcAdvancement;
        }

        Map<String, Criterion> advCriteria = getAdvancementCriteria(maxCriteria);
        return mcAdvancement = new net.minecraft.server.v1_15_R1.Advancement(getMinecraftKey(), parent.getMinecraftAdvancement(), display.getMinecraftDisplay(this), ADV_REWARDS, advCriteria, getAdvancementRequirements(advCriteria));
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method returns always {@code 0}.
     *
     * @return Always {@code 0}.
     */
    @Override
    public int getTeamCriteria(@NotNull Player player) {
        return 0;
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method returns always {@code 0}.
     *
     * @return Always {@code 0}.
     */
    @Override
    public int getTeamCriteria(@NotNull UUID uuid) {
        return 0;
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method returns always {@code 0}.
     *
     * @return Always {@code 0}.
     */
    @Override
    public int getTeamCriteria(@NotNull TeamProgression progression) {
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
     */
    // Must be kept to avoid accidental inclusion in UnsupportedOperationException methods down below
    @Override
    public void onUpdate(@NotNull TeamProgression teamProgression, @NotNull Set<net.minecraft.server.v1_15_R1.Advancement> advancementList, @NotNull Map<MinecraftKey, AdvancementProgress> progresses, @NotNull Set<MinecraftKey> added) {
        // Since isVisible() returns true for every input we can use the super method
        super.onUpdate(teamProgression, advancementList, progresses, added);
        // instead of this version down below
        /*net.minecraft.server.v1_15_R1.Advancement adv = getMinecraftAdvancement();
        advancementList.add(adv);

        // Inlining of getAdvancementProgress()
        AdvancementProgress advPrg = new AdvancementProgress();
        advPrg.a(adv.getCriteria(), adv.i());

        MinecraftKey key = getMinecraftKey();
        added.add(key);
        progresses.put(key, advPrg);*/
    }

    /**
     * The {@link AdvancementDisplay} used by {@link FakeAdvancement}s.
     *
     * @see FakeAdvancement
     */
    public static final class FakeAdvancementDisplay extends AdvancementDisplay {

        /**
         * Creates a new {@code FakeAdvancementDisplay}.
         *
         * @param icon The material of the item that will be shown in the GUI.
         * @param title The title of the advancement.
         * @param frame The shape of the advancement.
         * @param x The x coordinate of the advancement.
         * @param y The y coordinate of the advancement.
         */
        public FakeAdvancementDisplay(@NotNull Material icon, @NotNull String title, @NotNull AdvancementFrameType frame, float x, float y) {
            super(icon, title, frame, false, false, x, y, Collections.emptyList());
        }

        /**
         * Creates a new {@code FakeAdvancementDisplay}.
         *
         * @param icon The item that will be shown in the GUI.
         * @param title The title of the advancement.
         * @param frame The shape of the advancement.
         * @param x The x coordinate of the advancement.
         * @param y The y coordinate of the advancement.
         */
        public FakeAdvancementDisplay(@NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, float x, float y) {
            super(icon, title, frame, false, false, x, y, Collections.emptyList());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull
        public net.minecraft.server.v1_15_R1.AdvancementDisplay getMinecraftDisplay(@NotNull Advancement advancement) {
            net.minecraft.server.v1_15_R1.AdvancementDisplay advDisplay = new net.minecraft.server.v1_15_R1.AdvancementDisplay(getNMSIcon(), new ChatComponentText(title), new ChatComponentText(compactDescription), null, frame.getMinecraftFrameType(), false, false, true);
            advDisplay.a(x, y);
            return advDisplay;
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
    public @Nullable BaseComponent[] getAnnounceMessage(@NotNull Player player) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int incrementTeamCriteria(@NotNull UUID uuid) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int incrementTeamCriteria(@NotNull UUID uuid, boolean giveReward) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int incrementTeamCriteria(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int increment) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int incrementTeamCriteria(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int increment, boolean giveReward) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int incrementTeamCriteria(@NotNull Player player) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int incrementTeamCriteria(@NotNull Player player, boolean giveReward) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int incrementTeamCriteria(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int increment) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int incrementTeamCriteria(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int increment, boolean giveReward) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    protected @Range(from = 0, to = Integer.MAX_VALUE) int incrementTeamCriteria(@NotNull TeamProgression pro, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int increment, boolean giveRewards) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public void setCriteriaTeamProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public void setCriteriaTeamProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveReward) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public void setCriteriaTeamProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    public void setCriteriaTeamProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveReward) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    protected void setCriteriaTeamProgression(@NotNull TeamProgression pro, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveRewards) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     * Since {@code FakeAdvancement}s are not saved, this method always throws an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException Always when it's called.
     */
    @Override
    protected void handlePlayer(@NotNull TeamProgression pro, @Nullable Player player, int criteria, int old, boolean giveRewards, @Nullable AfterHandle afterHandle) {
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
    public boolean isShownTo(Player player) {
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
