package com.fren_gor.ultimateAdvancementAPI.util;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.chat.IComponent;
import com.fren_gor.ultimateAdvancementAPI.chat.IDefaultStyle;
import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordComponent;
import com.fren_gor.ultimateAdvancementAPI.chat.bungeecord.BungeeCordDefaultStyle;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.AsyncExecutionException;
import com.fren_gor.ultimateAdvancementAPI.nms.util.JsonString;
import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.VanillaAdvancementDisablerWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public final class AdvancementUtils {

    /**
     * The {@code show_advancement_messages} game rule (previously called {@code announceAdvancements}).
     */
    public static final GameRule<Boolean> SHOW_ADVANCEMENT_MESSAGES_GAMERULE = getShowAdvancementMessagesGamerule();

    private static final String NOTIFICATION_NAMESPACE;
    private static final AtomicInteger NOTIFICATION_KEY_NUMBER = new AtomicInteger(0);
    private static final MinecraftKeyWrapper NOTIFICATION_ROOT_KEY;
    private static final JsonString ADV_DESCRIPTION;
    private static final PreparedAdvancementWrapper PREPARED_ROOT;
    private static final AdvancementWrapper ROOT;
    private static final IDefaultStyle WHITE_DEFAULT_STYLE = BungeeCordDefaultStyle.of(ChatColor.WHITE);
    private static final boolean PLAYER_HAS_ADVENTURE_DISPLAY_NAME = ReflectionUtil.hasMethod(Player.class, "displayName");

    static {
        NOTIFICATION_NAMESPACE = AdvancementKey.RESERVED_NAMESPACE_PREFIX + "notification-" + UUID.randomUUID();

        ADV_DESCRIPTION = BungeeCordComponent.of("A toast notification.\n\nClose and reopen advancements to hide.")
            .applyDefaultStyle(BungeeCordDefaultStyle.of(ChatColor.GRAY))
            .toJson();

        try {
            NOTIFICATION_ROOT_KEY = MinecraftKeyWrapper.craft(NOTIFICATION_NAMESPACE, "root");
            AdvancementDisplayWrapper display = AdvancementDisplayWrapper.craft(
                new ItemStack(Material.BARRIER),
                BungeeCordComponent.of("Notification").toJson(),
                BungeeCordComponent.EMPTY.toJson(),
                AdvancementFrameTypeWrapper.TASK,
                0, 0, false, false, true, "textures/block/stone.png");
            PREPARED_ROOT = PreparedAdvancementWrapper.craft(NOTIFICATION_ROOT_KEY, 1);
            ROOT = PREPARED_ROOT.toAdvancementWrapper(display);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Displays a custom toast to a player.
     *
     * @param player The player the toast will be shown to.
     * @param icon The displayed item of the toast.
     * @param title The displayed title of the toast.
     * @param frame The {@link AdvancementFrameType} of the toast.
     * @see UltimateAdvancementAPI#displayCustomToast(Player, ItemStack, String, AdvancementFrameType)
     */
    public static void displayToast(@NotNull Player player, @NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame) {
        displayToast(player, icon, BungeeCordComponent.ofLegacy(title), frame);
    }

    /**
     * Displays a custom toast to a player.
     *
     * @param player The player the toast will be shown to.
     * @param icon The displayed item of the toast.
     * @param title The displayed title of the toast.
     * @param frame The {@link AdvancementFrameType} of the toast.
     * @see UltimateAdvancementAPI#displayCustomToast(Player, ItemStack, String, AdvancementFrameType)
     */
    public static void displayToast(@NotNull Player player, @NotNull ItemStack icon, @NotNull IComponent title, @NotNull AdvancementFrameType frame) {
        Preconditions.checkNotNull(player, "Player is null.");
        Preconditions.checkNotNull(icon, "Icon is null.");
        Preconditions.checkNotNull(title, "Title is null.");
        Preconditions.checkNotNull(frame, "AdvancementFrameType is null.");
        Preconditions.checkArgument(icon.getType() != Material.AIR, "ItemStack is air.");
        Preconditions.checkArgument(player.isOnline(), "Player is not online.");

        try {
            MinecraftKeyWrapper notification_key = MinecraftKeyWrapper.craft(NOTIFICATION_NAMESPACE, AdvancementKey.RESERVED_KEY_PREFIX + "notification-" + NOTIFICATION_KEY_NUMBER.getAndIncrement());
            AdvancementDisplayWrapper display = AdvancementDisplayWrapper.craft(icon, title.toJson(), ADV_DESCRIPTION, frame.getNMSWrapper(), 0.2f, 0, true, false, false);
            AdvancementWrapper notification = AdvancementWrapper.craftBaseAdvancement(notification_key, PREPARED_ROOT, display, 1);
            PacketPlayOutAdvancementsWrapper.craftSendPacket(Map.of(
                ROOT, 0,
                notification, 1
            )).sendTo(player);
            PacketPlayOutAdvancementsWrapper.craftRemovePacket(Set.of(NOTIFICATION_ROOT_KEY, notification_key)).sendTo(player);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    public static void displayToastDuringUpdate(@NotNull Player player, @NotNull Advancement advancement) {
        Preconditions.checkNotNull(player, "Player is null.");
        Preconditions.checkNotNull(advancement, "Advancement is null.");
        Preconditions.checkArgument(advancement.isValid(), "Advancement isn't valid.");
        Preconditions.checkArgument(player.isOnline(), "Player is not online.");

        final AbstractAdvancementDisplay display = advancement.getDisplay();
        final TeamProgression pro = advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player);

        try {
            MinecraftKeyWrapper keyWrapper;
            try {
                keyWrapper = MinecraftKeyWrapper.craft(advancement.getKey().getNamespace(), AdvancementKey.RESERVED_KEY_PREFIX + "notification-" + advancement.getKey().getKey());
            } catch (IllegalArgumentException e) {
                // Fallback in case the key cannot be constructed
                keyWrapper = MinecraftKeyWrapper.craft(advancement.getKey().getNamespace(), AdvancementKey.RESERVED_KEY_PREFIX + "notification-" + NOTIFICATION_KEY_NUMBER.getAndIncrement());
            }

            AdvancementDisplayWrapper displayWrapper = AdvancementDisplayWrapper.craft(display.dispatchGetIcon(player, pro), display.dispatchGetTitle(player, pro).toJson(), ADV_DESCRIPTION, display.dispatchGetFrame(player, pro).getNMSWrapper(), 0, 0, true, false, false);
            AdvancementWrapper advWrapper = AdvancementWrapper.craftBaseAdvancement(keyWrapper, advancement.getNMSWrapper(), displayWrapper, 1);

            PacketPlayOutAdvancementsWrapper.craftSendPacket(Map.of(advWrapper, 1)).sendTo(player);
            PacketPlayOutAdvancementsWrapper.craftRemovePacket(Set.of(keyWrapper)).sendTo(player);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disables vanilla advancements.
     * <p>Does not persist between datapack reloads (i.e. {@code /minecraft:reload}).
     *
     * @throws Exception If disabling fails.
     * @see UltimateAdvancementAPI#disableVanillaAdvancements()
     */
    public static void disableVanillaAdvancements() throws Exception {
        VanillaAdvancementDisablerWrapper.disableVanillaAdvancements(true, false);
    }

    /**
     * Disables vanilla recipe advancements (i.e. the advancements which unlock recipes).
     * <p>Does not persist between datapack reloads (i.e. {@code /minecraft:reload}).
     *
     * @throws Exception If disabling fails.
     * @see UltimateAdvancementAPI#disableVanillaRecipeAdvancements()
     */
    public static void disableVanillaRecipeAdvancements() throws Exception {
        VanillaAdvancementDisablerWrapper.disableVanillaAdvancements(false, true);
    }

    @Contract("_ -> param1")
    public static int validateProgressionValue(int progression) {
        if (progression < 0) {
            throw new IllegalArgumentException("Progression value cannot be < 0");
        }
        return progression;
    }

    public static void validateProgressionValueStrict(int progression, int maxProgression) {
        validateProgressionValue(progression);
        if (progression > maxProgression) {
            throw new IllegalArgumentException("Progression value cannot be greater than the maximum progression (" + maxProgression + ')');
        }
    }

    @Contract("null -> fail; !null -> param1")
    public static TeamProgression validateTeamProgression(TeamProgression pro) {
        Preconditions.checkNotNull(pro, "TeamProgression is null.");
        Preconditions.checkArgument(pro.isValid(), "Invalid TeamProgression.");
        return pro;
    }

    public static void checkSync() {
        if (!Bukkit.isPrimaryThread())
            throw new AsyncExecutionException("Illegal async method call. This method can be called only from the main thread.");
    }

    @NotNull
    public static BukkitTask runSync(@NotNull AdvancementMain main, @NotNull Runnable runnable) {
        return runSync(main.getOwningPlugin(), runnable);
    }

    @NotNull
    public static BukkitTask runSync(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        return runSync(plugin, 0, runnable);
    }

    @NotNull
    public static BukkitTask runSync(@NotNull AdvancementMain main, long delay, @NotNull Runnable runnable) {
        return runSync(main.getOwningPlugin(), delay, runnable);
    }

    @NotNull
    public static BukkitTask runSync(@NotNull Plugin plugin, long delay, @NotNull Runnable runnable) {
        Preconditions.checkNotNull(plugin, "Plugin is null.");
        Preconditions.checkNotNull(runnable, "Runnable is null.");
        return Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    /**
     * Schedules the provided consumer to execute on the main thread when the {@link CompletableFuture} completes.
     * <p>Example usage:
     * <pre> {@code runSync(completableFuture, myPlugin, (result, err) -> {
     *     if (err != null) {
     *         // An error occurred
     *         // Code which handle the error
     *         return;
     *     }
     *
     *     // Code which handles the result
     * });}</pre>
     *
     * @param completableFuture The {@link CompletableFuture} which will complete in the future.
     * @param plugin The plugin which will schedule the task on Bukkit.
     * @param consumer The {@link BiConsumer} to execute. It is called with the result (or {@code null} if none) and the
     *     exception (or {@code null} if none) of the provided {@link CompletableFuture}.
     * @param <T> The value returned by the {@link CompletableFuture}.
     * @return A {@link CompletableFuture} completed after the consumer runs.
     * @see BukkitScheduler
     * @since 3.0.0
     */
    @NotNull
    public static <T> CompletableFuture<Void> runSync(@NotNull CompletableFuture<T> completableFuture, @NotNull Plugin plugin, @NotNull BiConsumer<T, Throwable> consumer) {
        return runSync(completableFuture, 0, plugin, consumer);
    }

    /**
     * Schedules the provided consumer to execute on the main thread when the {@link CompletableFuture} completes.
     * <p>Example usage:
     * <pre> {@code runSync(completableFuture, 0, myPlugin, (result, err) -> {
     *     if (err != null) {
     *         // An error occurred
     *         // Code which handle the error
     *         return;
     *     }
     *
     *     // Code which handles the result
     * });}</pre>
     *
     * @param completableFuture The {@link CompletableFuture} which will complete in the future.
     * @param delay The delay in ticks to wait between the {@link CompletableFuture} competition and the execution of
     *     the provided consumer.
     * @param plugin The plugin which will schedule the task on Bukkit.
     * @param consumer The {@link BiConsumer} to execute. It is called with the result (or {@code null} if none) and the
     *     exception (or {@code null} if none) of the provided {@link CompletableFuture}.
     * @param <T> The value returned by the {@link CompletableFuture}.
     * @return A {@link CompletableFuture} completed after the consumer runs.
     * @see BukkitScheduler
     * @since 3.0.0
     */
    @NotNull
    public static <T> CompletableFuture<Void> runSync(@NotNull CompletableFuture<T> completableFuture, long delay, @NotNull Plugin plugin, @NotNull BiConsumer<T, Throwable> consumer) {
        Preconditions.checkNotNull(completableFuture, "CompletableFuture is null.");
        Preconditions.checkNotNull(plugin, "Plugin is null.");
        Preconditions.checkNotNull(consumer, "BiConsumer is null.");
        if (!plugin.isEnabled()) {
            throw new IllegalStateException("Plugin is not enabled.");
        }

        CompletableFuture<Void> toReturn = new CompletableFuture<>();
        BiConsumer<T, Throwable> wrapped = (result, err) -> {
            try {
                consumer.accept(result, err);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "An exception occurred while executing runSync's consumer of " + plugin.getDescription().getFullName(), e);
            } finally {
                toReturn.complete(null);
            }
        };

        completableFuture.whenComplete((result, err) -> {
            if (delay == 0 && Bukkit.isPrimaryThread()) {
                wrapped.accept(result, err);
            } else {
                Bukkit.getScheduler().runTaskLater(plugin, () -> wrapped.accept(result, err), delay);
            }
        });

        return toReturn;
    }

    @NotNull
    public static UUID uuidFromPlayer(@NotNull Player player) {
        Preconditions.checkNotNull(player, "Player is null.");
        return player.getUniqueId();
    }

    @NotNull
    public static UUID uuidFromPlayer(@NotNull OfflinePlayer player) {
        Preconditions.checkNotNull(player, "OfflinePlayer is null.");
        return player.getUniqueId();
    }

    @NotNull
    public static TeamProgression progressionFromPlayer(@NotNull Player player, @NotNull Advancement advancement) {
        return progressionFromPlayer(player, advancement.getAdvancementTab());
    }

    @NotNull
    public static TeamProgression progressionFromUUID(@NotNull UUID uuid, @NotNull Advancement advancement) {
        return progressionFromUUID(uuid, advancement.getAdvancementTab());
    }

    @NotNull
    public static TeamProgression progressionFromPlayer(@NotNull Player player, @NotNull AdvancementTab tab) {
        return progressionFromUUID(uuidFromPlayer(player), tab);
    }

    @NotNull
    public static TeamProgression progressionFromUUID(@NotNull UUID uuid, @NotNull AdvancementTab tab) {
        Preconditions.checkNotNull(uuid, "UUID is null.");
        return tab.getDatabaseManager().getTeamProgression(uuid);
    }

    @NotNull
    public static IComponent getAnnouncementMessage(@NotNull Advancement advancement, @NotNull Player advancementCompleter) {
        return getAnnouncementMessage(advancement, advancementCompleter, false);
    }

    @NotNull
    public static IComponent getAnnouncementMessage(@NotNull Advancement advancement, @NotNull Player advancementCompleter, boolean fancy) {
        Preconditions.checkNotNull(advancement, "Advancement is null.");
        Preconditions.checkNotNull(advancementCompleter, "Player is null.");

        AbstractAdvancementDisplay display = advancement.getDisplay();
        TeamProgression progression = advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(advancementCompleter);
        AdvancementFrameType frame = display.dispatchGetFrame(advancementCompleter, progression);
        IComponent title = display.dispatchGetTitle(advancementCompleter, progression);
        List<IComponent> description = display.dispatchGetDescription(advancementCompleter, progression);
        IDefaultStyle defaultAMTitleStyle = display.dispatchGetAnnouncementMessageDefaultTitleStyle(advancementCompleter, progression);
        IDefaultStyle defaultAMDescriptionStyle = display.dispatchGetAnnouncementMessageDefaultDescriptionStyle(advancementCompleter, progression);
        IComponent chatText = frame.getChatText();
        IDefaultStyle frameStyle = frame.getStyle();

        Preconditions.checkNotNull(frame, "Display returned a null frame.");
        Preconditions.checkNotNull(title, "Display returned a null title.");
        Preconditions.checkNotNull(description, "Display returned a null description.");
        for (IComponent d : description) {
            Preconditions.checkNotNull(d, "Display returned a description with a null line.");
        }
        Preconditions.checkNotNull(defaultAMTitleStyle, "Display returned a null announcement message default title style.");
        Preconditions.checkNotNull(defaultAMDescriptionStyle, "Display returned a null announcement message default description style.");
        Preconditions.checkNotNull(chatText, "Frame returned a null chatText.");
        Preconditions.checkNotNull(frameStyle, "Frame returned a null style.");

        IDefaultStyle titleStyle = frameStyle.merge(defaultAMTitleStyle);
        IDefaultStyle parenthesesStyle = fancy ? titleStyle : frameStyle;
        IDefaultStyle descriptionStyle = frameStyle.merge(defaultAMDescriptionStyle);

        title = title.applyDefaultStyle(titleStyle);

        List<IComponent> hoverText = new ArrayList<>(4);
        hoverText.add(title);

        if (!description.isEmpty()) {
            IComponent firstLine = description.get(0);
            hoverText.add(firstLine.newLine());
            if (fancy && !firstLine.isEmptyOrStartsWithNewLine()) {
                hoverText.add(firstLine.newLine());
            }

            IComponent hoverDesc = firstLine.empty()
                .applyDefaultStyle(descriptionStyle)
                .setChildren(intersperse(description, firstLine.newLine()));
            hoverText.add(hoverDesc);
        }

        IComponent hoverComponent = title.empty()
            .setChildren(List.of(
                title.fromPlain("[").applyDefaultStyle(parenthesesStyle),
                title,
                title.fromPlain("]").applyDefaultStyle(parenthesesStyle)
            ))
            .addHoverEvent(title.empty().setChildren(hoverText));

        IComponent playerName = PLAYER_HAS_ADVENTURE_DISPLAY_NAME ?
            PaperUtils.getDisplayName(advancementCompleter) :
            title.fromLegacy(advancementCompleter.getDisplayName());

        IComponent msgComponent = title.empty()
            .applyDefaultStyle(WHITE_DEFAULT_STYLE)
            .setChildren(List.of(
                playerName,
                title.space(),
                chatText,
                title.space()
            ));

        return title.empty().setChildren(List.of(
            msgComponent,
            hoverComponent
        ));
    }

    @NotNull
    public static <T> List<T> intersperse(@NotNull List<T> list, @NotNull T element) {
        Preconditions.checkNotNull(list, "List is null.");
        Preconditions.checkNotNull(element, "Object to intersperse is null.");
        if (list.isEmpty()) {
            return list;
        }
        List<T> result = new ArrayList<>(list.size() * 2 - 1);
        var it = list.iterator();
        result.add(it.next());
        while (it.hasNext()) {
            result.add(element);
            result.add(it.next());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static GameRule<Boolean> getShowAdvancementMessagesGamerule() {
        try {
            // Spigot 1.21.11+
            return (GameRule<Boolean>) GameRule.class.getDeclaredField("SHOW_ADVANCEMENT_MESSAGES").get(null);
        } catch (NoSuchFieldException e) {
            // Spigot <= 1.21.10, Paper all versions
            return GameRule.ANNOUNCE_ADVANCEMENTS;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private AdvancementUtils() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
