package com.fren_gor.ultimateAdvancementAPI.util;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AbstractAdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.AsyncExecutionException;
import com.fren_gor.ultimateAdvancementAPI.nms.util.ReflectionUtil;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.VanillaAdvancementDisablerWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.PreparedAdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import com.fren_gor.ultimateAdvancementAPI.util.display.DefaultStyle;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public final class AdvancementUtils {

    public static final MinecraftKeyWrapper ROOT_KEY, NOTIFICATION_KEY;
    private static final BaseComponent ADV_DESCRIPTION = new TextComponent("\nA notification.");
    private static final PreparedAdvancementWrapper PREPARED_ROOT;
    private static final AdvancementWrapper ROOT;

    static {
        ADV_DESCRIPTION.setColor(ChatColor.GRAY);

        try {
            ROOT_KEY = MinecraftKeyWrapper.craft("com.fren_gor", "root");
            NOTIFICATION_KEY = MinecraftKeyWrapper.craft("com.fren_gor", "notification");
            BaseComponent title = new TextComponent("Notifications"); // "§f§lNotifications§1§2§3§4§5§6§7§8§9§0"
            title.setColor(ChatColor.WHITE);
            title.setBold(true);
            BaseComponent description = new TextComponent("Notification page.\nClose and reopen advancements to hide.");
            description.setColor(ChatColor.GRAY);
            AdvancementDisplayWrapper display = AdvancementDisplayWrapper.craft(new ItemStack(Material.GRASS_BLOCK), title, description, AdvancementFrameTypeWrapper.TASK, 0, 0, "textures/block/stone.png");
            PREPARED_ROOT = PreparedAdvancementWrapper.craft(ROOT_KEY, 1);
            ROOT = PREPARED_ROOT.toAdvancementWrapper(display);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static final boolean COMPONENT_BUILDER_HAS_BUILD = ReflectionUtil.hasMethod(ComponentBuilder.class, "build");
    private static final boolean TEXT_COMPONENT_HAS_FROM_LEGACY = ReflectionUtil.hasMethod(TextComponent.class, "fromLegacy");

    /**
     * Displays a custom toast to a player.
     *
     * @param player A player to show the toast.
     * @param icon The displayed item of the toast.
     * @param title The displayed title of the toast.
     * @param frame The {@link AdvancementFrameType} of the toast.
     * @see UltimateAdvancementAPI#displayCustomToast(Player, ItemStack, String, AdvancementFrameType)
     */
    public static void displayToast(@NotNull Player player, @NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame) {
        displayToast(player, icon, fromLegacy(title), frame);
    }

    /**
     * Displays a custom toast to a player.
     *
     * @param player A player to show the toast.
     * @param icon The displayed item of the toast.
     * @param title The displayed title of the toast.
     * @param frame The {@link AdvancementFrameType} of the toast.
     * @see UltimateAdvancementAPI#displayCustomToast(Player, ItemStack, String, AdvancementFrameType)
     */
    public static void displayToast(@NotNull Player player, @NotNull ItemStack icon, @NotNull BaseComponent title, @NotNull AdvancementFrameType frame) {
        Preconditions.checkNotNull(player, "Player is null.");
        Preconditions.checkNotNull(icon, "Icon is null.");
        Preconditions.checkNotNull(title, "Title is null.");
        Preconditions.checkNotNull(frame, "AdvancementFrameType is null.");
        Preconditions.checkArgument(icon.getType() != Material.AIR, "ItemStack is air.");

        try {
            AdvancementDisplayWrapper display = AdvancementDisplayWrapper.craft(icon, title, ADV_DESCRIPTION, frame.getNMSWrapper(), 1, 0, true, false, false);
            AdvancementWrapper notification = AdvancementWrapper.craftBaseAdvancement(NOTIFICATION_KEY, PREPARED_ROOT, display, 1);
            PacketPlayOutAdvancementsWrapper.craftSendPacket(Map.of(
                    ROOT, 1,
                    notification, 1
            )).sendTo(player);
            PacketPlayOutAdvancementsWrapper.craftRemovePacket(Set.of(ROOT_KEY, NOTIFICATION_KEY)).sendTo(player);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    /*public static void displayToast(@NotNull Player player, @NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, @NotNull Advancement base) {
        Preconditions.checkNotNull(player, "Player is null.");
        Preconditions.checkNotNull(icon, "Icon is null.");
        Preconditions.checkNotNull(title, "Title is null.");
        Preconditions.checkNotNull(frame, "AdvancementFrameType is null.");
        Preconditions.checkNotNull(base, "Advancement is null.");
        Preconditions.checkArgument(base.isValid(), "Advancement isn't valid.");
        Preconditions.checkArgument(icon.getType() != Material.AIR, "ItemStack is air.");

        final MinecraftKeyWrapper key = getUniqueKey(base.getAdvancementTab()).getNMSWrapper();

        try {
            AdvancementDisplayWrapper display = AdvancementDisplayWrapper.craft(icon, title, ADV_DESCRIPTION, frame.getNMSWrapper(), base.getDisplay().getX() + 1, base.getDisplay().getY(), true, false, false);
            AdvancementWrapper adv = AdvancementWrapper.craftBaseAdvancement(key, base.getNMSWrapper(), display, 1);

            PacketPlayOutSelectAdvancementTabWrapper.craftSelectNone().sendTo(player);
            PacketPlayOutAdvancementsWrapper.craftSendPacket(Map.of(adv, 1)).sendTo(player);
            PacketPlayOutAdvancementsWrapper.craftRemovePacket(Set.of(key)).sendTo(player);
            PacketPlayOutSelectAdvancementTabWrapper.craftSelect(key).sendTo(player);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }*/

    public static void displayToastDuringUpdate(@NotNull Player player, @NotNull Advancement advancement) {
        Preconditions.checkNotNull(player, "Player is null.");
        Preconditions.checkNotNull(advancement, "Advancement is null.");
        Preconditions.checkArgument(advancement.isValid(), "Advancement isn't valid.");

        final AbstractAdvancementDisplay display = advancement.getDisplay();
        final TeamProgression pro = advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(player);
        final MinecraftKeyWrapper keyWrapper = getUniqueKey(advancement.getAdvancementTab()).getNMSWrapper();

        try {
            AdvancementDisplayWrapper displayWrapper = AdvancementDisplayWrapper.craft(display.dispatchGetIcon(player, pro), display.dispatchGetTitle(player, pro), ADV_DESCRIPTION, display.dispatchGetFrame(player, pro).getNMSWrapper(), 0, 0, true, false, false);
            AdvancementWrapper advWrapper = AdvancementWrapper.craftBaseAdvancement(keyWrapper, advancement.getNMSWrapper(), displayWrapper, 1);

            PacketPlayOutAdvancementsWrapper.craftSendPacket(Map.of(advWrapper, 1)).sendTo(player);
            PacketPlayOutAdvancementsWrapper.craftRemovePacket(Set.of(keyWrapper)).sendTo(player);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    private static AdvancementKey getUniqueKey(@NotNull AdvancementTab tab) {
        final String namespace = tab.getNamespace();
        StringBuilder builder = new StringBuilder("i");
        AdvancementKey key;
        while (tab.getAdvancement(key = new AdvancementKey(namespace, builder.toString())) != null) {
            builder.append('i');
        }
        return key;
    }

    /**
     * Disables vanilla advancements.
     * 
     * @throws Exception If disabling fails.
     * @see UltimateAdvancementAPI#disableVanillaAdvancements()
     */
    public static void disableVanillaAdvancements() throws Exception {
        VanillaAdvancementDisablerWrapper.disableVanillaAdvancements(true, false);
    }

    /**
     * Disables vanilla recipe advancements (i.e. the advancements which unlock recipes).
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
     *     // Code which handle the result
     * });}</pre>
     *
     * @param completableFuture The {@link CompletableFuture} which will complete in the future.
     * @param plugin The plugin which will schedule the task on Bukkit.
     * @param consumer The {@link BiConsumer} to execute. It is called with the result (or {@code null} if none) and the
     *         exception (or {@code null} if none) of the provided {@link CompletableFuture}.
     * @param <T> The value returned by the {@link CompletableFuture}.
     * @return A {@link CompletableFuture} with the same result or exception as the provided one.
     * @see BukkitScheduler
     * @since 3.0.0
     */
    @NotNull
    public static <T> CompletableFuture<T> runSync(@NotNull CompletableFuture<T> completableFuture, @NotNull Plugin plugin, @NotNull BiConsumer<T, Throwable> consumer) {
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
     *     // Code which handle the result
     * });}</pre>
     *
     * @param completableFuture The {@link CompletableFuture} which will complete in the future.
     * @param delay The delay in ticks to wait between the {@link CompletableFuture} competition and the execution of
     *         the provided consumer.
     * @param plugin The plugin which will schedule the task on Bukkit.
     * @param consumer The {@link BiConsumer} to execute. It is called with the result (or {@code null} if none) and the
     *         exception (or {@code null} if none) of the provided {@link CompletableFuture}.
     * @param <T> The value returned by the {@link CompletableFuture}.
     * @return A {@link CompletableFuture} with the same result or exception as the provided one.
     * @see BukkitScheduler
     * @since 3.0.0
     */
    @NotNull
    public static <T> CompletableFuture<T> runSync(@NotNull CompletableFuture<T> completableFuture, long delay, @NotNull Plugin plugin, @NotNull BiConsumer<T, Throwable> consumer) {
        Preconditions.checkNotNull(completableFuture, "CompletableFuture is null.");
        Preconditions.checkNotNull(plugin, "Plugin is null.");
        Preconditions.checkNotNull(consumer, "BiConsumer is null.");

        return completableFuture.whenComplete((result, err) -> {
            if (delay == 0 && Bukkit.isPrimaryThread()) {
                try {
                    consumer.accept(result, err);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "An exception occurred while executing runSync's consumer of " + plugin.getDescription().getFullName(), e);
                }
            } else {
                Bukkit.getScheduler().runTaskLater(plugin, () -> consumer.accept(result, err), delay);
            }
        });
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
    public static BaseComponent getAnnouncementMessage(@NotNull Advancement advancement, @NotNull Player advancementCompleter) {
        return getAnnouncementMessage(advancement, advancementCompleter, false);
    }

    @NotNull
    public static BaseComponent getAnnouncementMessage(@NotNull Advancement advancement, @NotNull Player advancementCompleter, boolean fancy) {
        Preconditions.checkNotNull(advancement, "Advancement is null.");
        Preconditions.checkNotNull(advancementCompleter, "Player is null.");

        AbstractAdvancementDisplay display = advancement.getDisplay();
        TeamProgression progression = advancement.getAdvancementTab().getDatabaseManager().getTeamProgression(advancementCompleter);
        AdvancementFrameType frame = display.dispatchGetFrame(advancementCompleter, progression);
        BaseComponent title = display.dispatchGetTitle(advancementCompleter, progression);
        List<BaseComponent> description = display.dispatchGetDescription(advancementCompleter, progression);
        DefaultStyle defaultAMTitleStyle = display.dispatchGetAnnouncementMessageDefaultTitleStyle(advancementCompleter, progression);
        DefaultStyle defaultAMDescriptionStyle = display.dispatchGetAnnouncementMessageDefaultDescriptionStyle(advancementCompleter, progression);
        String chatText = frame.getChatText();
        DefaultStyle frameStyle = frame.getStyle();

        Preconditions.checkNotNull(frame, "Display returned a null frame.");
        Preconditions.checkNotNull(title, "Display returned a null title.");
        Preconditions.checkNotNull(description, "Display returned a null description.");
        Preconditions.checkNotNull(chatText, "Frame returned a null chatText.");
        Preconditions.checkNotNull(frameStyle, "Frame returned a null style.");

        DefaultStyle titleStyle = defaultAMTitleStyle.mergeWith(frameStyle);
        DefaultStyle parenthesesStyle = fancy ? titleStyle : frameStyle;

        title = applyDefaultStyle(title, titleStyle);

        ComponentBuilder hoverText = new ComponentBuilder().append(title, FormatRetention.NONE);

        if (!description.isEmpty()) {
            if (fancy && startsWithNewLine(description.get(0)) == NO_NEW_LINE) {
                hoverText.append("\n\n", FormatRetention.NONE);
            } else {
                hoverText.append("\n", FormatRetention.NONE);
            }

            DefaultStyle descriptionStyle = defaultAMDescriptionStyle.mergeWith(frameStyle);
            hoverText.append(joinBaseComponents(new TextComponent("\n"), descriptionStyle, description), FormatRetention.NONE);
        }

        ComponentBuilder hoverBuilder = new ComponentBuilder("[");
        parenthesesStyle.applyTo(hoverBuilder);
        hoverBuilder.append(title, FormatRetention.NONE)
                .append("]", FormatRetention.NONE);
        parenthesesStyle.applyTo(hoverBuilder);

        TextComponent withHover = new TextComponent(hoverBuilder.create());
        withHover.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, hoverText.create()));

        var cb = new ComponentBuilder(advancementCompleter.getName() + ' ' + frame.getChatText() + ' ')
                .color(ChatColor.WHITE)
                .append(withHover, FormatRetention.NONE);
        return build(cb);
    }

    // Package-private for tests
    static final int EMPTY = 0;
    static final int NEW_LINE = 1;
    static final int NO_NEW_LINE = -1;

    // Returns:
    // * NO_NEW_LINE if component doesn't start with newLine
    // * NEW_LINE if component starts with newLine
    // * EMPTY if the component is empty (null or "")
    static int startsWithNewLine(BaseComponent component) { // Package-private for tests
        int r;
        if (component == null) {
            return EMPTY;
        } else if (component instanceof TextComponent textComponent) {
            r = startsWithNewLine(textComponent.getText());
            if (r != EMPTY) {
                return r;
            }
        } else {
            // Every non-text component is assumed not to start with \n
            return NO_NEW_LINE;
        }

        var extra = component.getExtra();
        if (extra == null) {
            return EMPTY;
        }
        for (var e : extra) {
            if (e != null && (r = startsWithNewLine(e)) != EMPTY) {
                return r;
            }
        }
        return EMPTY;
    }

    // Returns:
    // * NO_NEW_LINE if component doesn't start with newLine
    // * NEW_LINE if component starts with newLine
    // * EMPTY if the component is empty (null or "")
    static int startsWithNewLine(String text) { // Package-private for tests
        if (text == null) {
            return EMPTY;
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                return NEW_LINE;
            } else if (c == ChatColor.COLOR_CHAR) {
                i++; // Skip next character since it is a color code
            } else if (!Character.isWhitespace(c)) { // Continue if c is a white space
                return NO_NEW_LINE;
            }
        }
        return EMPTY;
    }

    /**
     * Equivalent of {@link ComponentBuilder#build()}. Present for compatibility with old versions.
     * <p>Use this method rather than {@link ComponentBuilder#build()} in UltimateAdvancementAPI code!
     *
     * @param builder The builder to build.
     * @return The resulting {@link BaseComponent}.
     * @see ComponentBuilder#build()
     * @see ComponentBuilder#create()
     */
    @NotNull
    public static BaseComponent build(@NotNull ComponentBuilder builder) {
        if (COMPONENT_BUILDER_HAS_BUILD) {
            return builder.build();
        }
        BaseComponent[] created = builder.create();
        // In old versions there isn't TextComponent.fromArray(...)
        return created.length == 1 ? created[0] : new TextComponent(created);
    }

    /**
     * Equivalent of {@link TextComponent#fromLegacy(String)}. Present for compatibility with old versions.
     * <p>Use this method rather than {@link TextComponent#fromLegacy(String)} in UltimateAdvancementAPI code!
     *
     * @param legacy The legacy string to convert.
     * @return The resulting {@link BaseComponent}.
     * @see TextComponent#fromLegacy(String)
     * @see TextComponent#fromLegacyText(String)
     */
    @NotNull
    public static BaseComponent fromLegacy(@NotNull String legacy) {
        Preconditions.checkNotNull(legacy, "Legacy string is null.");
        if (TEXT_COMPONENT_HAS_FROM_LEGACY) {
            return TextComponent.fromLegacy(legacy);
        }
        return new TextComponent(TextComponent.fromLegacyText(legacy));
    }

    /**
     * Joins the provided {@link BaseComponent}s into a single one, separating them with the specified delimiter.
     *
     * @param delimiter The delimiter to put in between the {@link BaseComponent}s.
     * @param defaultStyle The default style of the {@link BaseComponent}s to join. May be {@code null} if no default color should be applied.
     * @param toJoin The {@link BaseComponent}s to join.
     * @return A {@link BaseComponent} containing the joined components.
     */
    @NotNull
    public static BaseComponent joinBaseComponents(@NotNull BaseComponent delimiter, @Nullable DefaultStyle defaultStyle, @NotNull Iterable<? extends BaseComponent> toJoin) {
        Preconditions.checkNotNull(delimiter, "Delimiter is null.");
        Preconditions.checkNotNull(toJoin, "BaseComponents to join are null.");
        var iter = toJoin.iterator();
        if (!iter.hasNext()) {
            return new TextComponent();
        }
        var first = Objects.requireNonNull(iter.next(), "A BaseComponent to join is null.");
        ComponentBuilder joiner = new ComponentBuilder(first);
        while (iter.hasNext()) {
            BaseComponent component = Objects.requireNonNull(iter.next(), "A BaseComponent to join is null.");
            joiner.append(delimiter, FormatRetention.NONE).append(component, FormatRetention.NONE);
        }
        return applyDefaultStyle(build(joiner), defaultStyle);
    }

    /**
     * Applies the specified default style to the provided {@link BaseComponent}.
     *
     * @param text The {@link BaseComponent} to which the default style will be applied.
     * @param defaultStyle The default style to apply. May be {@code null} if no default style should be applied.
     * @return The provided {@link BaseComponent} with the default style applied.
     */
    @NotNull
    public static BaseComponent applyDefaultStyle(@NotNull BaseComponent text, @Nullable DefaultStyle defaultStyle) {
        if (defaultStyle == null || defaultStyle == DefaultStyle.MINECRAFT_DEFAULTS) {
            return text;
        }
        text = text.duplicate();
        defaultStyle.applyTo(text);
        return text;
    }

    private AdvancementUtils() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
