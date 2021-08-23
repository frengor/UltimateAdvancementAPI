package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementCriteriaUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.fren_gor.ultimateAdvancementAPI.util.AfterHandle;
import com.fren_gor.ultimateAdvancementAPI.visibilities.IVisibility;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.minecraft.server.v1_15_R1.AdvancementProgress;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.getAdvancementProgress;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.progressionFromPlayer;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.progressionFromUUID;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.runSync;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.uuidFromPlayer;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateCriteriaStrict;
import static com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils.validateIncrement;

public abstract class Advancement {

    @NotNull
    protected final AdvancementKey key;
    @NotNull
    protected final AdvancementTab advancementTab;
    @Getter
    @NotNull
    protected final AdvancementDisplay display;
    @Nullable
    private final Method iVisibilityMethod;

    @Range(from = 0, to = Integer.MAX_VALUE)
    protected final int maxCriteria;

    private Advancement() {
        throw new UnsupportedOperationException();
    }

    Advancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display) {
        this(advancementTab, key, display, 1);
    }

    Advancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display, @Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        Validate.isTrue(maxCriteria > 0, "Max criteria cannot be <= 0");
        this.advancementTab = Objects.requireNonNull(advancementTab, "AdvancementTab is null.");
        this.key = new AdvancementKey(advancementTab.getNamespace(), key);
        this.display = Objects.requireNonNull(display, "Display is null.");
        this.maxCriteria = maxCriteria;
        if (this instanceof IVisibility) {
            this.iVisibilityMethod = getIVisibilityMethod(getClass());
        } else {
            this.iVisibilityMethod = null;
        }
    }

    @NotNull
    public final AdvancementKey getKey() {
        return key;
    }

    @NotNull
    public final AdvancementTab getAdvancementTab() {
        return advancementTab;
    }

    @NotNull
    public final MinecraftKey getMinecraftKey() {
        return key.toMinecraftKey();
    }

    @Range(from = 1, to = Integer.MAX_VALUE)
    public final int getMaxCriteria() {
        return maxCriteria;
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getTeamCriteria(@NotNull Player player) {
        return getTeamCriteria(uuidFromPlayer(player));
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getTeamCriteria(@NotNull UUID uuid) {
        return getTeamCriteria(progressionFromUUID(uuid, this));
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getTeamCriteria(@NotNull TeamProgression progression) {
        Validate.notNull(progression, "TeamProgression is null.");
        return progression.getCriteria(this);
    }

    public boolean isGranted(@NotNull Player player) {
        return isGranted(uuidFromPlayer(player));
    }

    public boolean isGranted(@NotNull UUID uuid) {
        return isGranted(progressionFromUUID(uuid, this));
    }

    public boolean isGranted(@NotNull TeamProgression progression) {
        Validate.notNull(progression, "TeamProgression is null.");
        return getTeamCriteria(progression) >= maxCriteria;
    }

    /**
     * Get the message to be displayed in chat when an advancement is made.<br>
     * The message will be sent to everybody.
     *
     * @param player The player who's made the advancement.
     * @return The message to be displayed. If {@code null} no message will be displayed.
     */
    @Nullable
    public BaseComponent[] getAnnounceMessage(@NotNull Player player) {
        Validate.notNull(player, "Player is null.");
        ChatColor color = display.getFrame().getColor();
        return new ComponentBuilder(player.getName() + ' ' + display.getFrame().getChatText() + ' ')
                .color(ChatColor.WHITE)
                .append(new ComponentBuilder("[")
                                .color(color)
                                .event(new HoverEvent(Action.SHOW_TEXT, display.getChatDescription()))
                                .create()
                        , FormatRetention.NONE)
                .append(display.getChatTitle(), FormatRetention.EVENTS)
                .append(new ComponentBuilder("]")
                                .color(color)
                                .create()
                        , FormatRetention.EVENTS)
                .create();
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid) {
        return incrementTeamCriteria(uuid, true);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid, boolean giveReward) {
        return incrementTeamCriteria(uuid, 1, giveReward);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int increment) {
        return incrementTeamCriteria(uuid, increment, true);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int increment, boolean giveReward) {
        return incrementTeamCriteria(progressionFromUUID(uuid, this), Bukkit.getPlayer(uuid), increment, giveReward);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player) {
        return incrementTeamCriteria(player, true);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player, boolean giveReward) {
        return incrementTeamCriteria(player, 1, giveReward);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int increment) {
        return incrementTeamCriteria(player, increment, true);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int increment, boolean giveReward) {
        return incrementTeamCriteria(progressionFromPlayer(player, this), player, increment, giveReward);
    }

    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int incrementTeamCriteria(@NotNull TeamProgression pro, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int increment, boolean giveRewards) {
        Validate.notNull(pro, "TeamProgression is null.");
        validateIncrement(increment);

        int crit = getTeamCriteria(pro);
        if (crit >= maxCriteria) {
            return crit;
        }
        int newCriteria = crit + increment;
        if (newCriteria > maxCriteria) {
            newCriteria = maxCriteria;
        }
        setCriteriaTeamProgression(pro, player, newCriteria, giveRewards);
        return newCriteria;
    }

    public void setCriteriaTeamProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        setCriteriaTeamProgression(uuid, criteria, true);
    }

    public void setCriteriaTeamProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveReward) {
        setCriteriaTeamProgression(progressionFromUUID(uuid, this), Bukkit.getPlayer(uuid), criteria, giveReward);
    }

    public void setCriteriaTeamProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        setCriteriaTeamProgression(player, criteria, true);
    }

    public void setCriteriaTeamProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveReward) {
        setCriteriaTeamProgression(progressionFromPlayer(player, this), player, criteria, giveReward);
    }

    protected void setCriteriaTeamProgression(@NotNull TeamProgression pro, @Nullable Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveRewards) {
        Validate.notNull(pro, "TeamProgression is null.");
        validateCriteriaStrict(criteria, maxCriteria);

        final DatabaseManager ds = advancementTab.getDatabaseManager();
        int old = ds.updateCriteria(key, pro, criteria);

        try {
            Bukkit.getPluginManager().callEvent(new AdvancementCriteriaUpdateEvent(pro, old, criteria, this));
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        handlePlayer(pro, player, criteria, old, giveRewards, AfterHandle.UPDATE_ADVANCEMENTS_TO_TEAM);
    }

    protected void handlePlayer(@NotNull TeamProgression pro, @Nullable Player player, int criteria, int old, boolean giveRewards, @Nullable AfterHandle afterHandle) {
        Validate.notNull(pro, "TeamProgression is null.");
        if (criteria >= this.maxCriteria && old < this.maxCriteria) {
            if (player != null) {
                onGrant(player, giveRewards);
            } else {
                DatabaseManager ds = advancementTab.getDatabaseManager();
                player = pro.getAnOnlineMember(ds);
                if (player != null) {
                    onGrant(player, giveRewards);
                } else {
                    ds.setUnredeemed(key, giveRewards, pro);
                    return; // Skip advancement update, no player is online
                }
            }
        }
        if (afterHandle != null)
            afterHandle.apply(pro, player, this);
    }

    public void displayToastToPlayer(@NotNull Player player) {
        AdvancementUtils.displayToast(player, display.getIcon(), display.getTitle(), display.getFrame());
    }

    public boolean isVisible(@NotNull Player player) {
        return isVisible(uuidFromPlayer(player));
    }

    public boolean isVisible(@NotNull UUID uuid) {
        return isVisible(progressionFromUUID(uuid, this));
    }

    public boolean isVisible(@NotNull TeamProgression progression) {
        if (iVisibilityMethod != null) {
            try {
                return (boolean) iVisibilityMethod.invoke(this, this, progression);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Contract(pure = true, value = "null -> false")
    public boolean isShownTo(Player player) {
        return advancementTab.isShownTo(player);
    }

    public void onGrant(@NotNull Player player, boolean giveRewards) {
        Validate.notNull(player, "Player is null.");

        // Send complete messages
        if (display.doesAnnounceToChat()) {
            BaseComponent[] msg = getAnnounceMessage(player);
            if (msg != null)
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.spigot().sendMessage(msg);
                }
        }

        // Show Toast
        if (display.doesShowToast()) {
            // TODO Find a better solution
            runSync(advancementTab.getOwningPlugin(), () -> AdvancementUtils.displayToastDuringUpdate(player, this));
        }

        // Display handled by client (see AdvancementDisplay#showToast())
        if (giveRewards)
            giveReward(player);
    }

    public void grant(@NotNull Player player) {
        grant(player, true);
    }

    public void grant(@NotNull Player player, boolean giveRewards) {
        Validate.notNull(player, "Player is null.");
        setCriteriaTeamProgression(player, maxCriteria, giveRewards);
    }

    public void revoke(@NotNull Player player) {
        Validate.notNull(player, "Player is null.");
        setCriteriaTeamProgression(player, 0, false);
    }

    public void onUpdate(@NotNull TeamProgression teamProgression, @NotNull Set<net.minecraft.server.v1_15_R1.Advancement> advancementList, @NotNull Map<MinecraftKey, AdvancementProgress> progresses, @NotNull Set<MinecraftKey> added) {
        if (isVisible(teamProgression)) {
            net.minecraft.server.v1_15_R1.Advancement mcAdv = getMinecraftAdvancement();
            advancementList.add(mcAdv);
            MinecraftKey key = getMinecraftKey();
            added.add(key);
            progresses.put(key, getAdvancementProgress(mcAdv, getTeamCriteria(teamProgression)));
        }
    }

    public void giveReward(@NotNull Player player) {
    }

    public void onRegister() {
    }

    public void validateRegister() throws InvalidAdvancementException {
    }

    public void onDispose() {
    }

    public boolean isValid() {
        return advancementTab.isActive() && advancementTab.hasAdvancement(this);
    }

    /**
     * Should craft the NMS advancement once and returns it henceforth.
     *
     * @return The NMS advancement representing this advancement.
     */
    public abstract @NotNull net.minecraft.server.v1_15_R1.Advancement getMinecraftAdvancement();

    protected final <E extends Event> void registerEvent(@NotNull Class<E> eventClass, @NotNull Consumer<E> consumer) {
        advancementTab.getEventManager().register(this, eventClass, consumer);
    }

    protected final <E extends Event> void registerEvent(@NotNull Class<E> eventClass, @NotNull EventPriority priority, @NotNull Consumer<E> consumer) {
        advancementTab.getEventManager().register(this, eventClass, priority, consumer);
    }

    @Override
    public String toString() {
        return key.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Advancement that = (Advancement) o;

        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Nullable
    private Method getIVisibilityMethod(Class<? extends Advancement> clazz) {
        for (Class<?> i : clazz.getInterfaces()) {
            if (i != IVisibility.class && IVisibility.class.isAssignableFrom(i)) {
                try {
                    final Method m = i.getDeclaredMethod("isVisible", Advancement.class, TeamProgression.class);
                    if (m.isDefault()) {
                        return m;
                    }
                } catch (NoSuchMethodException e) {
                    // No method found, continue
                }
            }
        }
        Class<?> sClazz = clazz.getSuperclass();
        if (Advancement.class.isAssignableFrom(sClazz) && sClazz != Advancement.class) {
            return getIVisibilityMethod(sClazz.asSubclass(Advancement.class));
        }
        return null;
    }
}
