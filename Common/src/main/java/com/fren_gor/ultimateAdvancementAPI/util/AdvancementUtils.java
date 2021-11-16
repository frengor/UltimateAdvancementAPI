package com.fren_gor.ultimateAdvancementAPI.util;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotLoadedException;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.MinecraftKeyWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.VanillaAdvancementDisablerWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementDisplayWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementFrameTypeWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.advancement.AdvancementWrapper;
import com.fren_gor.ultimateAdvancementAPI.nms.wrappers.packets.PacketPlayOutAdvancementsWrapper;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AdvancementUtils {

    public static final MinecraftKeyWrapper ROOT_KEY, NOTIFICATION_KEY;
    private static final String ADV_DESCRIPTION = "\n§7A notification.";
    private static final AdvancementWrapper ROOT;

    static {
        try {
            ROOT_KEY = MinecraftKeyWrapper.craft("com.fren_gor", "root");
            NOTIFICATION_KEY = MinecraftKeyWrapper.craft("com.fren_gor", "notification");
            AdvancementDisplayWrapper display = AdvancementDisplayWrapper.craft(new ItemStack(Material.GRASS_BLOCK), "§f§lNotifications§1§2§3§4§5§6§7§8§9§0", "§7Notification page.\n§7Close and reopen advancements to hide.", AdvancementFrameTypeWrapper.TASK, 0, 0, "textures/block/stone.png");
            ROOT = AdvancementWrapper.craftRootAdvancement(ROOT_KEY, display, 1);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
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
    public static void displayToast(@NotNull Player player, @NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame) {
        Validate.notNull(player, "Player is null.");
        Validate.notNull(icon, "Icon is null.");
        Validate.notNull(title, "Title is null.");
        Validate.notNull(frame, "AdvancementFrameType is null.");
        Validate.isTrue(icon.getType() != Material.AIR, "ItemStack is air.");

        try {
            AdvancementDisplayWrapper display = AdvancementDisplayWrapper.craft(icon, title, ADV_DESCRIPTION, frame.getNMSWrapper(), 1, 0, true, false, false);
            AdvancementWrapper notification = AdvancementWrapper.craftBaseAdvancement(NOTIFICATION_KEY, ROOT, display, 1);
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
        Validate.notNull(player, "Player is null.");
        Validate.notNull(icon, "Icon is null.");
        Validate.notNull(title, "Title is null.");
        Validate.notNull(frame, "AdvancementFrameType is null.");
        Validate.notNull(base, "Advancement is null.");
        Validate.isTrue(base.isValid(), "Advancement isn't valid.");
        Validate.isTrue(icon.getType() != Material.AIR, "ItemStack is air.");

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
        Validate.notNull(player, "Player is null.");
        Validate.notNull(advancement, "Advancement is null.");
        Validate.isTrue(advancement.isValid(), "Advancement isn't valid.");

        final AdvancementDisplay display = advancement.getDisplay();
        final MinecraftKeyWrapper keyWrapper = getUniqueKey(advancement.getAdvancementTab()).getNMSWrapper();

        try {
            AdvancementDisplayWrapper displayWrapper = AdvancementDisplayWrapper.craft(display.getIcon(), display.getTitle(), ADV_DESCRIPTION, display.getFrame().getNMSWrapper(), 0, 0, true, false, false);
            AdvancementWrapper advWrapper = AdvancementWrapper.craftBaseAdvancement(keyWrapper, advancement.getNMSWrapper(), displayWrapper, 1);

            PacketPlayOutAdvancementsWrapper.craftSendPacket(Map.of(advWrapper, 1)).sendTo(player);
            PacketPlayOutAdvancementsWrapper.craftRemovePacket(Set.of(keyWrapper)).sendTo(player);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    private static AdvancementKey getUniqueKey(AdvancementTab tab) {
        final String namespace = tab.getNamespace();
        StringBuilder builder = new StringBuilder("i");
        AdvancementKey key;
        while (tab.getAdvancement(key = new AdvancementKey(namespace, builder.toString())) != null) {
            builder.append('i');
        }
        return key;
    }

    /**
     * Disable vanilla advancement.
     *
     * @throws Exception If disabling goes wrong.
     * @see UltimateAdvancementAPI#disableVanillaAdvancements()
     */
    public static void disableVanillaAdvancements() throws Exception {
        VanillaAdvancementDisablerWrapper.disableVanillaAdvancements();
    }

    @NotNull
    public static BaseComponent[] fromStringList(@NotNull List<String> list) {
        return fromStringList(null, list);
    }

    @NotNull
    public static BaseComponent[] fromStringList(@Nullable String title, @NotNull List<String> list) {
        Validate.notNull(list);
        ComponentBuilder builder = new ComponentBuilder();
        if (title != null) {
            builder.append(TextComponent.fromLegacyText(title), FormatRetention.NONE);
            if (list.isEmpty()) {
                return builder.create();
            }
            builder.append("\n", FormatRetention.NONE);
        } else if (list.isEmpty()) {
            return builder.create();
        }
        int i = 0;
        for (String s : list) {
            builder.append(TextComponent.fromLegacyText(s), FormatRetention.NONE);
            if (++i < list.size()) // Don't append \n at the end
                builder.append("\n", FormatRetention.NONE);
        }
        return builder.create();
    }

    public static boolean startsWithEmptyLine(@NotNull String text) {
        Validate.notNull(text);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '§') {
                i++; // Skip next character since it is a color code
            } else {
                return c == '\n';
            }
        }
        return false;
    }

    @Contract("_ -> param1")
    public static int validateCriteria(int criteria) {
        if (criteria < 0) {
            throw new IllegalArgumentException("Criteria cannot be < 0");
        }
        return criteria;
    }

    public static void validateCriteriaStrict(int criteria, int maxCriteria) {
        validateCriteria(criteria);
        if (criteria > maxCriteria) {
            throw new IllegalArgumentException("Criteria cannot be greater than the max criteria (" + maxCriteria + ')');
        }
    }

    public static void validateIncrement(int increment) {
        if (increment <= 0) {
            throw new IllegalArgumentException("Increment cannot be zero or less.");
        }
    }

    @Contract("null -> fail; !null -> param1")
    public static TeamProgression validateTeamProgression(TeamProgression pro) {
        Validate.notNull(pro, "TeamProgression is null.");
        Validate.isTrue(pro.isValid(), "Invalid TeamProgression.");
        return pro;
    }

    public static void checkTeamProgressionNotNull(TeamProgression progression) {
        if (progression == null) {
            throw new UserNotLoadedException();
        }
    }

    public static void checkTeamProgressionNotNull(TeamProgression progression, UUID uuid) {
        if (progression == null) {
            throw new UserNotLoadedException(uuid);
        }
    }

    public static void checkSync() {
        Validate.isTrue(Bukkit.isPrimaryThread(), "Illegal async method call.");
    }

    public static void runSync(@NotNull AdvancementMain main, @NotNull Runnable runnable) {
        runSync(main.getOwningPlugin(), runnable);
    }

    public static void runSync(@NotNull Plugin plugin, @NotNull Runnable runnable) {
        runSync(plugin, 1, runnable);
    }

    public static void runSync(@NotNull AdvancementMain main, long delay, @NotNull Runnable runnable) {
        runSync(main.getOwningPlugin(), delay, runnable);
    }

    public static void runSync(@NotNull Plugin plugin, long delay, @NotNull Runnable runnable) {
        Validate.notNull(plugin, "Plugin is null.");
        Validate.notNull(runnable, "Runnable is null.");
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, runnable, delay);
    }

    @NotNull
    public static UUID uuidFromPlayer(@NotNull Player player) {
        Validate.notNull(player, "Player is null.");
        return player.getUniqueId();
    }

    @NotNull
    public static UUID uuidFromPlayer(@NotNull OfflinePlayer player) {
        Validate.notNull(player, "OfflinePlayer is null.");
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
        Validate.notNull(uuid, "UUID is null.");
        return tab.getDatabaseManager().getTeamProgression(uuid);
    }

    private AdvancementUtils() {
        throw new UnsupportedOperationException("Utility class.");
    }
}
