package com.fren_gor.ultimateAdvancementAPI.advancement;

import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.events.advancement.AdvancementCriteriaUpdateEvent;
import com.fren_gor.ultimateAdvancementAPI.exceptions.InvalidAdvancementException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.fren_gor.ultimateAdvancementAPI.util.AfterHandle;
import com.fren_gor.ultimateAdvancementAPI.visibilities.IVisibility;
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

/**
 * The Advancement class is the parent class of every advancement. It provides the basic methods and fields to work with advancements.
 * Let's go over what information Advancement class stores/provides (so, which properties every advancement has):
 * A unique namespaced key (i.e. "advtab1:myadvancement"), which identifies the advancement and is stored into an AdvancementKey instance. It is composed by a namespace (the part before colon :), which is the namespace of the advancement tab, and a key (the part after colon :). So, for example, an advancement with namespaced key "mytab:myadv" is an advancement inside "mytab" tab and with "myadv" key. Also, every advancement inside a tab must have a unique key. Note that the namespaced key is not visible to the players in the advancement GUI;
 * An AdvancementDisplay instance, which describes the advancement visual information like title, description, icon and position in the GUI;
 * The maximum criteria. The criteria is a positive integer which represents the progression of a player. The maximum criteria is the times the advancement action should be done.
 */
public abstract class Advancement {

    /**
     * The unique advancement key, which identifies the advancement.
     */
    @NotNull
    protected final AdvancementKey key;

    /**
     * The advancement tab it belongs to.
     */
    @NotNull
    protected final AdvancementTab advancementTab;

    /**
     * The advancement display that describes it visually.
     */
    @NotNull
    protected final AdvancementDisplay display;

    /**
     * The method that tells if the advancement is visible or not.
     */
    @Nullable
    private final Method iVisibilityMethod;

    /**
     * The maximum criteria is the times the advancement action should be done.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    protected final int maxCriteria;

    private Advancement() {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a new Advancement.
     *
     * @param advancementTab The advancement tab it belongs to.
     * @param key The unique key of the advancement, just the name.
     * @param display The advancement display that describes it visually.
     */
    Advancement(@NotNull AdvancementTab advancementTab, @NotNull String key, @NotNull AdvancementDisplay display) {
        this(advancementTab, key, display, 1);
    }

    /**
     * Create a new Advancement.
     *
     * @param advancementTab The advancement tab it belongs to.
     * @param key The unique key of the advancement, just the name.
     * @param display The advancement display that describes it visually.
     * @param maxCriteria The times the advancement action should be done.
     */
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

    /**
     * Returns the AdvancementKey of the advancement.
     *
     * @return the AdvancementKey.
     */
    @NotNull
    public final AdvancementKey getKey() {
        return key;
    }

    /**
     * Returns the advancement tab it belongs to.
     *
     * @return the advancement tab.
     */
    @NotNull
    public final AdvancementTab getAdvancementTab() {
        return advancementTab;
    }

    /**
     * Returns the MinecraftKey of the advancement; It is used in packets.
     *
     * @return the MinecraftKey.
     */
    @NotNull
    public final MinecraftKey getMinecraftKey() {
        return key.toMinecraftKey();
    }

    /**
     * Returns the maxCriteria.
     *
     * @return the maxCriteria.
     */
    @Range(from = 1, to = Integer.MAX_VALUE)
    public final int getMaxCriteria() {
        return maxCriteria;
    }

    /**
     * Returns the current criteria of the team.
     *
     * @param player A player that belongs to the team.
     * @return The current criteria of the team.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getTeamCriteria(@NotNull Player player) {
        return getTeamCriteria(uuidFromPlayer(player));
    }

    /**
     * Returns the current criteria of the team.
     *
     * @param uuid A UUID player that belongs to the team.
     * @return The current criteria of the team.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getTeamCriteria(@NotNull UUID uuid) {
        return getTeamCriteria(progressionFromUUID(uuid, this));
    }

    /**
     * Returns the current criteria of the team.
     *
     * @param progression The {@link TeamProgression} that belongs to the team.
     * @return The current criteria of the team.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getTeamCriteria(@NotNull TeamProgression progression) {
        Validate.notNull(progression, "TeamProgression is null.");
        return progression.getCriteria(this);
    }

    /**
     * Returns whether the advancement is completed or not.
     *
     * @param player A player that belongs to the team.
     * @return Whether the advancement is completed or not.
     */
    public boolean isGranted(@NotNull Player player) {
        return isGranted(uuidFromPlayer(player));
    }

    /**
     * Returns whether the advancement is completed or not.
     *
     * @param uuid A UUID player that belongs to the team.
     * @return Whether the advancement is completed or not.
     */
    public boolean isGranted(@NotNull UUID uuid) {
        return isGranted(progressionFromUUID(uuid, this));
    }

    /**
     * Returns whether the advancement is completed or not.
     *
     * @param progression The {@link TeamProgression} that belongs to the team.
     * @return Whether the advancement is completed or not.
     */
    public boolean isGranted(@NotNull TeamProgression progression) {
        Validate.notNull(progression, "TeamProgression is null.");
        return getTeamCriteria(progression) >= maxCriteria;
    }

    /**
     * Returns the message to be displayed in chat when an advancement is made.<br>
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

    /**
     * Increase the progression of the team by one.
     * <p>If the advancement is completed this method calls {@link #giveReward(Player)}.
     *
     * @param uuid A UUID player that belongs to the team.
     * @return The new criteria progression.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid) {
        return incrementTeamCriteria(uuid, true);
    }

    /**
     * Increase the progression of the team by one.
     *
     * @param uuid A UUID player that belongs to the team.
     * @param giveReward Whether call {@link #giveReward(Player)} when the advancement is completed.
     * @return The new criteria progression.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid, boolean giveReward) {
        return incrementTeamCriteria(uuid, 1, giveReward);
    }

    /**
     * Increase the progression of the team.
     * <p>If the advancement is completed this method calls {@link #giveReward(Player)}.
     *
     * @param uuid A UUID player that belongs to the team.
     * @param increment Amount of the increment.
     * @return The new criteria progression.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int increment) {
        return incrementTeamCriteria(uuid, increment, true);
    }

    /**
     * Increase the progression of the team.
     *
     * @param uuid A UUID player that belongs to the team.
     * @param increment Amount of the increment.
     * @param giveReward Whether call {@link #giveReward(Player)} when the advancement is completed.
     * @return The new criteria progression.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int increment, boolean giveReward) {
        return incrementTeamCriteria(progressionFromUUID(uuid, this), Bukkit.getPlayer(uuid), increment, giveReward);
    }

    /**
     * Increase the progression of the team by one.
     * <p>If the advancement is completed this method calls {@link #giveReward(Player)}.
     *
     * @param player A player that belongs to the team.
     * @return The new criteria progression.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player) {
        return incrementTeamCriteria(player, true);
    }

    /**
     * Increase the progression of the team by one.
     *
     * @param player A player that belongs to the team.
     * @param giveReward Whether call {@link #giveReward(Player)} when the advancement is completed.
     * @return The new criteria progression.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player, boolean giveReward) {
        return incrementTeamCriteria(player, 1, giveReward);
    }

    /**
     * Increase the progression of the team.
     * <p>If the advancement is completed this method calls {@link #giveReward(Player)}.
     *
     * @param player A player that belongs to the team.
     * @param increment Amount of the increment.
     * @return The new criteria progression.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int increment) {
        return incrementTeamCriteria(player, increment, true);
    }

    /**
     * Increase the progression of the team.
     *
     * @param player A player that belongs to the team.
     * @param increment Amount of the increment.
     * @param giveReward Whether call {@link #giveReward(Player)} when the advancement is completed.
     * @return The new criteria progression.
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int incrementTeamCriteria(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int increment, boolean giveReward) {
        return incrementTeamCriteria(progressionFromPlayer(player, this), player, increment, giveReward);
    }

    /**
     * Increase the progression of the team.
     *
     * @param pro The {@link TeamProgression} that belongs to the team.
     * @param player A player that belongs to the team.
     * @param increment Amount of the increment.
     * @param giveRewards Whether call {@link #giveReward(Player)} when the advancement is completed.
     * @return The new criteria progression.
     */
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

    /**
     * Set the team progression value.
     * <p>If the advancement is completed this method calls {@link #giveReward(Player)}.
     *
     * @param uuid A UUID player that belongs to the team.
     * @param criteria The new criteria value to set.
     */
    public void setCriteriaTeamProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        setCriteriaTeamProgression(uuid, criteria, true);
    }

    /**
     * Set the team progression value.
     *
     * @param uuid A UUID player that belongs to the team.
     * @param criteria The new criteria value to set.
     * @param giveReward Whether call {@link #giveReward(Player)} when the advancement is completed.
     */
    public void setCriteriaTeamProgression(@NotNull UUID uuid, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveReward) {
        setCriteriaTeamProgression(progressionFromUUID(uuid, this), Bukkit.getPlayer(uuid), criteria, giveReward);
    }

    /**
     * Set the team progression value.
     * <p>If the advancement is completed this method calls {@link #giveReward(Player)}.
     *
     * @param player A player that belongs to the team.
     * @param criteria The new criteria value to set.
     */
    public void setCriteriaTeamProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        setCriteriaTeamProgression(player, criteria, true);
    }

    /**
     * Set the team progression value.
     *
     * @param player A player that belongs to the team.
     * @param criteria The new criteria value to set.
     * @param giveReward Whether call {@link #giveReward(Player)} when the advancement is completed.
     */
    public void setCriteriaTeamProgression(@NotNull Player player, @Range(from = 0, to = Integer.MAX_VALUE) int criteria, boolean giveReward) {
        setCriteriaTeamProgression(progressionFromPlayer(player, this), player, criteria, giveReward);
    }

    /**
     * Set the team progression value.
     *
     * @param pro The {@link TeamProgression} that belongs to the team.
     * @param player A player that belongs to the team.
     * @param criteria The new criteria value to set.
     * @param giveRewards Whether call {@link #giveReward(Player)} when the advancement is completed.
     */
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

    //TODO
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

    /**
     * Display the toast of this advancement to a player.
     *
     * @param player To whom to show the toast.
     */
    public void displayToastToPlayer(@NotNull Player player) {
        AdvancementUtils.displayToast(player, display.getIcon(), display.getTitle(), display.getFrame());
    }

    /**
     * Returns if the advancement is visible to a player.
     *
     * @param player From which player to know whether the advancement is visible to the team.
     * @return Whether the advancement is visible to the player.
     */
    public boolean isVisible(@NotNull Player player) {
        return isVisible(uuidFromPlayer(player));
    }

    /**
     * Returns if the advancement is visible to a player.
     *
     * @param uuid From which UUID player to know whether the advancement is visible to the team.
     * @return Whether the advancement is visible to the player.
     */
    public boolean isVisible(@NotNull UUID uuid) {
        return isVisible(progressionFromUUID(uuid, this));
    }

    /**
     * Returns if the advancement is visible to a player.
     *
     * @param progression From which {@link TeamProgression} to know whether the advancement is visible to the team.
     * @return Whether the advancement is visible to the player.
     */
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

    /**
     * Returns whether the advancement tab is shown to a player.
     *
     * @param player From which player to know whether the advancement is visible to the team.
     * @return Whether the advancement tab is shown to a player.
     */
    @Contract(pure = true, value = "null -> false")
    public boolean isShownTo(Player player) {
        return advancementTab.isShownTo(player);
    }

    /**
     * This method is called when the advancement is about to be completed.
     * <p>If necessary show the toast, send in chat the completion of the advancement and call {@link #giveReward(Player)}
     *
     * @param player Which player completed the advancement.
     * @param giveRewards Whether call {@link #giveReward(Player)}.
     */
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

    /**
     * Grant the advancement to the player's team.
     * <p>This method calls {@link #giveReward(Player)}.
     *
     * @param player A player of the team.
     */
    public void grant(@NotNull Player player) {
        grant(player, true);
    }

    /**
     * Grant the advancement to the player's team.
     *
     * @param player A player of the team.
     * @param giveRewards Whether call {@link #giveReward(Player)}.
     */
    public void grant(@NotNull Player player, boolean giveRewards) {
        Validate.notNull(player, "Player is null.");
        setCriteriaTeamProgression(player, maxCriteria, giveRewards);
    }

    /**
     * Revoke the advancement to the player's team.
     *
     * @param player A player of the team.
     */
    public void revoke(@NotNull Player player) {
        Validate.notNull(player, "Player is null.");
        setCriteriaTeamProgression(player, 0, false);
    }

    //TODO
    public void onUpdate(@NotNull TeamProgression teamProgression, @NotNull Set<net.minecraft.server.v1_15_R1.Advancement> advancementList, @NotNull Map<MinecraftKey, AdvancementProgress> progresses, @NotNull Set<MinecraftKey> added) {
        if (isVisible(teamProgression)) {
            net.minecraft.server.v1_15_R1.Advancement mcAdv = getMinecraftAdvancement();
            advancementList.add(mcAdv);
            MinecraftKey key = getMinecraftKey();
            added.add(key);
            progresses.put(key, getAdvancementProgress(mcAdv, getTeamCriteria(teamProgression)));
        }
    }

    /**
     * Gives the rewards to the player if the advancement is completed.
     * <p>It does not give any reward by default. Override the method to change the behavior.
     *
     * @param player The player that has done the advancement.
     */
    public void giveReward(@NotNull Player player) {

    }

    /**
     * This method is called when the advancement is registered.
     * <p>It does not do anything by default. Override the method to change the behavior.
     * <p>You can't use all methods in {@link AdvancementTab} because this method is called before the advancement tab registration.
     */
    public void onRegister() {

    }

    /**
     * Checks if the advancement is valid.
     * <p>It does not do anything by default. Override the method to change the behavior.
     * <p>For example in the BaseAdvancement it checks if the parent is valid, that is if it belongs to the same tab。
     * <p>This mean is called after the registration of {@link AdvancementTab} and this allows the use of all the methods of its.
     *
     * @throws InvalidAdvancementException If the check fails.
     */
    public void validateRegister() throws InvalidAdvancementException {

    }

    /**
     * This method is called when the advancement is disposed.
     * <p>It does not do anything by default. Override the method to change the behavior.
     */
    public void onDispose() {

    }

    /**
     * This method checks if the advancement is valid, that is if the advancement tab of the advancement is active and if the advancement belongs to the declared advancement tab.
     *
     * @return Whether the advancement is valid.
     */
    public boolean isValid() {
        return advancementTab.isActive() && advancementTab.hasAdvancement(this);
    }

    /**
     * Should craft the NMS advancement once and returns it henceforth.
     *
     * @return The NMS advancement representing this advancement.
     */
    public abstract @NotNull net.minecraft.server.v1_15_R1.Advancement getMinecraftAdvancement();

    /**
     * Registers and describes the behavior of the event.
     *
     * @param eventClass The event class.
     * @param consumer The code to run when the event occurs.
     */
    protected final <E extends Event> void registerEvent(@NotNull Class<E> eventClass, @NotNull Consumer<E> consumer) {
        advancementTab.getEventManager().register(this, eventClass, consumer);
    }

    /**
     * Registers and describes the behavior of the event.
     *
     * @param eventClass The event class.
     * @param priority The priority of the event. See {@link EventPriority}.
     * @param consumer The code to run when the event occurs.
     */
    protected final <E extends Event> void registerEvent(@NotNull Class<E> eventClass, @NotNull EventPriority priority, @NotNull Consumer<E> consumer) {
        advancementTab.getEventManager().register(this, eventClass, priority, consumer);
    }

    /**
     * Returns the advancement namespaced key as {@link String}.
     * <p>Calling this method is equivalent to call {@code getKey().toString()}.
     *
     * @return The advancement namespaced key as {@link String}.
     * @see AdvancementKey#toString()
     */
    @Override
    public String toString() {
        return key.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Advancement that = (Advancement) o;

        return key.equals(that.key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return key.hashCode();
    }

    /**
     * Gets the right {@link IVisibility} subinterface method that will be used by the advancement visibility system.
     *
     * @param clazz The class to analyze.
     * @return The right {@link IVisibility#isVisible(Advancement, TeamProgression)} method or {@code null}.
     */
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

    /**
     * Returns the AdvancementDisplay.
     *
     * @return the AdvancementDisplay instance.
     */
    public AdvancementDisplay getDisplay() {
        return display;
    }

}
