package com.fren_gor.ultimateAdvancementAPI.util;

import com.fren_gor.ultimateAdvancementAPI.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotLoadedException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_15_R1.AdvancementProgress;
import net.minecraft.server.v1_15_R1.AdvancementRewards;
import net.minecraft.server.v1_15_R1.Advancements;
import net.minecraft.server.v1_15_R1.Blocks;
import net.minecraft.server.v1_15_R1.ChatComponentText;
import net.minecraft.server.v1_15_R1.Criterion;
import net.minecraft.server.v1_15_R1.CriterionProgress;
import net.minecraft.server.v1_15_R1.MinecraftKey;
import net.minecraft.server.v1_15_R1.PacketPlayOutAdvancements;
import net.minecraft.server.v1_15_R1.PlayerConnection;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

@UtilityClass
public class AdvancementUtils {

    private static final Field advancementRoots, advancementTasks;

    static {
        Field c = null, d = null;
        try {
            c = Advancements.class.getDeclaredField("c");
            c.setAccessible(true);
            d = Advancements.class.getDeclaredField("d");
            d.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        advancementRoots = c;
        advancementTasks = d;
    }

    public static final MinecraftKey IMPOSSIBLE = new MinecraftKey("minecraft", "impossible");
    public static final MinecraftKey NOTIFICATION_KEY = new MinecraftKey("com.fren_gor", "notification"), ROOT_KEY = new MinecraftKey("com.fren_gor", "root");
    public static final AdvancementRewards ADV_REWARDS = new AdvancementRewards(0, new MinecraftKey[0], new MinecraftKey[0], null);
    private static final ChatComponentText ADV_DESCRIPTION = new ChatComponentText("\n§7A notification.");
    private static final Map<String, Criterion> ADV_CRITERIA_MAP = Collections.singletonMap("criterion", new Criterion(() -> IMPOSSIBLE));
    private static final String[][] ADV_REQUIREMENTS = {{"criterion"}};
    private static final AdvancementProgress ADV_PROGRESS = new AdvancementProgress();
    private static final net.minecraft.server.v1_15_R1.Advancement ROOT;
    private static final Map<MinecraftKey, AdvancementProgress> PROGRESSIONS;

    static {
        ADV_PROGRESS.a(ADV_CRITERIA_MAP, ADV_REQUIREMENTS);
        CriterionProgress criterion = ADV_PROGRESS.getCriterionProgress("criterion");
        assert criterion != null;
        criterion.b();
        net.minecraft.server.v1_15_R1.AdvancementDisplay display = new net.minecraft.server.v1_15_R1.AdvancementDisplay(new net.minecraft.server.v1_15_R1.ItemStack(Blocks.GRASS_BLOCK.getItem()), new ChatComponentText("§f§lNotifications§1§2§3§4§5§6§7§8§9§0"), new ChatComponentText("§7Notification page.\n§7Close and reopen advancements to hide."), new MinecraftKey("textures/block/stone.png"), net.minecraft.server.v1_15_R1.AdvancementFrameType.TASK, false, false, false);
        ROOT = new net.minecraft.server.v1_15_R1.Advancement(ROOT_KEY, null, display, ADV_REWARDS, ADV_CRITERIA_MAP, ADV_REQUIREMENTS);

        PROGRESSIONS = new HashMap<>();
        PROGRESSIONS.put(ROOT_KEY, ADV_PROGRESS);
        PROGRESSIONS.put(NOTIFICATION_KEY, ADV_PROGRESS);
    }

    public static void displayToast(@NotNull Player player, @NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame) {
        Validate.notNull(player, "Player is null.");
        Validate.notNull(icon, "Icon is null.");
        Validate.notNull(title, "Title is null.");
        Validate.notNull(frame, "AdvancementFrameType is null.");
        Validate.isTrue(icon.getType() != Material.AIR, "ItemStack is air.");

        net.minecraft.server.v1_15_R1.AdvancementDisplay mcDisplay = new net.minecraft.server.v1_15_R1.AdvancementDisplay(CraftItemStack.asNMSCopy(icon), new ChatComponentText(title), ADV_DESCRIPTION, null, frame.getMinecraftFrameType(), true, false, false);
        net.minecraft.server.v1_15_R1.Advancement mcAdv = new net.minecraft.server.v1_15_R1.Advancement(NOTIFICATION_KEY, ROOT, mcDisplay, ADV_REWARDS, ADV_CRITERIA_MAP, ADV_REQUIREMENTS);

        mcDisplay.a(1, 0);

        PacketPlayOutAdvancements sendPacket = new PacketPlayOutAdvancements(false, Lists.newArrayList(ROOT, mcAdv), Collections.emptySet(), PROGRESSIONS);
        PacketPlayOutAdvancements removePacket = new PacketPlayOutAdvancements(false, Collections.emptySet(), Sets.newHashSet(ROOT_KEY, NOTIFICATION_KEY), Collections.emptyMap());

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        connection.sendPacket(sendPacket);
        connection.sendPacket(removePacket);
    }

    /*public static void displayToast(@NotNull Player player, @NotNull ItemStack icon, @NotNull String title, @NotNull AdvancementFrameType frame, @NotNull Advancement base) {
        Validate.notNull(player, "Player is null.");
        Validate.notNull(icon, "Icon is null.");
        Validate.notNull(title, "Title is null.");
        Validate.notNull(frame, "AdvancementFrameType is null.");
        Validate.notNull(base, "Advancement is null.");
        Validate.isTrue(base.isValid(), "Advancement isn't valid.");
        Validate.isTrue(icon.getType() != Material.AIR, "ItemStack is air.");

        AdvancementTab tab = base.getAdvancementTab();
        MinecraftKey key = new AdvancementKey(tab.getNamespace(), getUniqueKey(tab)).toMinecraftKey();

        net.minecraft.server.v1_15_R1.AdvancementDisplay mcDisplay = new net.minecraft.server.v1_15_R1.AdvancementDisplay(CraftItemStack.asNMSCopy(icon), new ChatComponentText(title), ADV_DESCRIPTION, null, frame.getMinecraftFrameType(), true, false, false);
        mcDisplay.a(base.getDisplay().getX() + 1, base.getDisplay().getY());
        net.minecraft.server.v1_15_R1.Advancement mcAdv = new net.minecraft.server.v1_15_R1.Advancement(key, base.getMinecraftAdvancement(), mcDisplay, ADV_REWARDS, ADV_CRITERIA_MAP, ADV_REQUIREMENTS);

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        PacketPlayOutAdvancements sendPacket = new PacketPlayOutAdvancements(false, Collections.singletonList(mcAdv), Collections.emptySet(), Collections.singletonMap(key, ADV_PROGRESS));
        PacketPlayOutAdvancements removePacket = new PacketPlayOutAdvancements(false, Collections.emptySet(), Collections.singleton(key), Collections.emptyMap());
        PacketPlayOutSelectAdvancementTab noTab = new PacketPlayOutSelectAdvancementTab(), thisTab = new PacketPlayOutSelectAdvancementTab(tab.getRootAdvancement().getMinecraftKey());

        connection.sendPacket(noTab);
        connection.sendPacket(sendPacket);
        connection.sendPacket(removePacket);
        connection.sendPacket(thisTab);
    }*/

    public static void displayToastDuringUpdate(@NotNull Player player, @NotNull Advancement advancement) {
        Validate.notNull(player, "Player is null.");
        Validate.notNull(advancement, "Advancement is null.");
        Validate.isTrue(advancement.isValid(), "Advancement isn't valid.");

        AdvancementDisplay display = advancement.getDisplay();
        AdvancementTab tab = advancement.getAdvancementTab();
        MinecraftKey key = new AdvancementKey(tab.getNamespace(), getUniqueKey(tab)).toMinecraftKey();

        net.minecraft.server.v1_15_R1.AdvancementDisplay mcDisplay = new net.minecraft.server.v1_15_R1.AdvancementDisplay(CraftItemStack.asNMSCopy(display.getIcon()), new ChatComponentText(display.getTitle()), ADV_DESCRIPTION, null, display.getFrame().getMinecraftFrameType(), true, false, false);
        net.minecraft.server.v1_15_R1.Advancement mcAdv = new net.minecraft.server.v1_15_R1.Advancement(key, advancement.getMinecraftAdvancement(), mcDisplay, ADV_REWARDS, ADV_CRITERIA_MAP, ADV_REQUIREMENTS);

        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

        PacketPlayOutAdvancements sendPacket = new PacketPlayOutAdvancements(false, Collections.singletonList(mcAdv), Collections.emptySet(), Collections.singletonMap(key, ADV_PROGRESS));
        PacketPlayOutAdvancements removePacket = new PacketPlayOutAdvancements(false, Collections.emptySet(), Collections.singleton(key), Collections.emptyMap());

        // Useless since advancements are updating when called
        // PacketPlayOutSelectAdvancementTab noTab = new PacketPlayOutSelectAdvancementTab(), thisTab = new PacketPlayOutSelectAdvancementTab(tab.getRootAdvancement().getMinecraftKey());

        // connection.sendPacket(noTab);
        connection.sendPacket(sendPacket);
        connection.sendPacket(removePacket);
        // connection.sendPacket(thisTab);
    }

    private static String getUniqueKey(AdvancementTab tab) {
        StringBuilder builder = new StringBuilder("i");
        while (tab.getAdvancement(new AdvancementKey(tab.getNamespace(), builder.toString())) != null) {
            builder.append('i');
        }
        return builder.toString();
    }

    @NotNull
    public static Map<String, Criterion> getAdvancementCriteria(@Range(from = 1, to = Integer.MAX_VALUE) int maxCriteria) {
        Validate.isTrue(maxCriteria >= 1, "Max criteria must be >= 1.");

        Map<String, Criterion> advCriteria = Maps.newHashMapWithExpectedSize(maxCriteria);
        for (int i = 0; i < maxCriteria; i++) {
            advCriteria.put(String.valueOf(i), new Criterion(() -> IMPOSSIBLE));
        }

        return advCriteria;
    }

    @NotNull
    public static String[][] getAdvancementRequirements(@NotNull Map<String, Criterion> advCriteria) {
        Validate.notNull(advCriteria, "Advancement criteria map is null.");

        String[][] array = new String[advCriteria.size()][1];
        int index = 0;
        for (String name : advCriteria.keySet()) {
            array[index++][0] = name;
        }

        return array;
    }

    @NotNull
    public static net.minecraft.server.v1_15_R1.AdvancementProgress getAdvancementProgress(@NotNull net.minecraft.server.v1_15_R1.Advancement mcAdv, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) {
        Validate.notNull(mcAdv, "NMS Advancement is null.");
        Validate.isTrue(criteria >= 0, "Criteria must be >= 0.");

        AdvancementProgress advPrg = new AdvancementProgress();
        advPrg.a(mcAdv.getCriteria(), mcAdv.i());

        for (int i = 0; i < criteria; i++) {
            CriterionProgress criteriaPrg = advPrg.getCriterionProgress(String.valueOf(i));
            if (criteriaPrg != null) {
                criteriaPrg.b();
            }
        }

        return advPrg;
    }

    @SuppressWarnings("unchecked")
    public static void disableVanillaAdvancements() throws Exception {
        Advancements registry = ((CraftServer) Bukkit.getServer()).getServer().getAdvancementData().REGISTRY;

        if (registry.advancements.isEmpty()) {
            return;
        }

        final Set<net.minecraft.server.v1_15_R1.Advancement> advRoots = (Set<net.minecraft.server.v1_15_R1.Advancement>) advancementRoots.get(registry);
        final Set<net.minecraft.server.v1_15_R1.Advancement> advTasks = (Set<net.minecraft.server.v1_15_R1.Advancement>) advancementTasks.get(registry);

        Set<MinecraftKey> removed = Sets.newHashSetWithExpectedSize(registry.advancements.size());

        Iterator<Entry<MinecraftKey, net.minecraft.server.v1_15_R1.Advancement>> it = registry.advancements.entrySet().iterator();
        while (it.hasNext()) {
            Entry<MinecraftKey, net.minecraft.server.v1_15_R1.Advancement> e = it.next();

            if (e.getKey().getNamespace().equals("minecraft")) {
                // Unregister it
                net.minecraft.server.v1_15_R1.Advancement adv = e.getValue();
                if (adv.b() == null) {
                    // If parent is null then the adv is root
                    advRoots.remove(adv);
                } else {
                    advTasks.remove(adv);
                }
                it.remove();
                removed.add(e.getKey());
            }
        }

        // Remove advancements from players
        PacketPlayOutAdvancements removePacket = new PacketPlayOutAdvancements(false, Collections.emptySet(), removed, Collections.emptyMap());

        for (Player player : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(removePacket);
        }
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
            builder.append("\n\n", FormatRetention.NONE);
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

    public static void validateCriteria(int criteria) {
        if (criteria < 0) {
            throw new IllegalArgumentException("Criteria cannot be < 0");
        }
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
        return tab.getDatabaseManager().getProgression(uuid);
    }

}
