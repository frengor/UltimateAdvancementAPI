package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.eventManagerAPI.EventManager;
import com.fren_gor.ultimateAdvancementAPI.advancement.Advancement;
import com.fren_gor.ultimateAdvancementAPI.commands.UltimateAdvancementAPICommand;
import com.fren_gor.ultimateAdvancementAPI.database.DatabaseManager;
import com.fren_gor.ultimateAdvancementAPI.exceptions.DuplicatedException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import dev.jorel.commandapi.CommandAPI;
import lombok.Getter;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AdvancementMain extends JavaPlugin {

    @Getter
    private static AdvancementMain instance;
    @Getter
    private EventManager eventManager;
    @Getter
    private DatabaseManager databaseManager;
    private final Map<String, AdvancementTab> tabs = new HashMap<>();
    private final Map<Plugin, List<AdvancementTab>> pluginMap = new HashMap<>();

    @Override
    public void onLoad() {
        instance = this;
        CommandAPI.onLoad(false);
        UltimateAdvancementAPICommand.register();
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable(this);
        eventManager = new EventManager(this);

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        try {
            // Run it sync to avoid using a not initialized database
            databaseManager = new DatabaseManager(this, new File(getDataFolder(), "database.db"));
            //databaseManager = new DatabaseManager(this, "root", "", "database", "127.0.0.1", 3306, 10, 6000);
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        eventManager.register(this, PluginDisableEvent.class, EventPriority.HIGHEST, e -> unregisterAdvancementTabs(e.getPlugin()));

        UltimateAdvancementAPI.main = this;

    }

    @Override
    public void onDisable() {
        UltimateAdvancementAPI.main = null;
        // eventManager is disabled automatically since pl is disabling.
        pluginMap.clear();
        Iterator<AdvancementTab> it = tabs.values().iterator();
        while (it.hasNext()) {
            try {
                AdvancementTab tab = it.next();
                if (tab.isActive()) {
                    tab.dispose();
                }
                it.remove();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        if (databaseManager != null)
            databaseManager.unregister();
    }

    @NotNull
    @Contract("_, _ -> new")
    public AdvancementTab createAdvancementTab(@NotNull Plugin plugin, @NotNull String namespace) throws DuplicatedException {
        Validate.notNull(plugin, "Plugin is null.");
        Validate.notNull(namespace, "Namespace is null.");
        if (tabs.containsKey(namespace)) {
            throw new DuplicatedException("An AdvancementTab with '" + namespace + "' namespace already exists.");
        }

        AdvancementTab tab = new AdvancementTab(plugin, databaseManager, namespace);
        tabs.put(namespace, tab);
        pluginMap.computeIfAbsent(plugin, p -> new LinkedList<>()).add(tab);
        return tab;
    }

    @Nullable
    public AdvancementTab getAdvancementTab(@NotNull String namespace) {
        Validate.notNull(namespace, "Namespace is null.");
        return tabs.get(namespace);
    }

    public boolean isAdvancementTabRegistered(@NotNull String namespace) {
        Validate.notNull(namespace, "Namespace is null.");
        return tabs.containsKey(namespace);
    }

    @UnmodifiableView
    @NotNull
    public Collection<@NotNull AdvancementTab> getPluginAdvancementTabs(@NotNull Plugin plugin) {
        Validate.notNull(plugin, "Plugin is null.");
        return Collections.unmodifiableCollection(pluginMap.getOrDefault(plugin, Collections.emptyList()));
    }

    public void unregisterAdvancementTab(@NotNull String namespace) {
        Validate.notNull(namespace, "Namespace is null.");
        AdvancementTab tab = tabs.remove(namespace);
        if (tab != null)
            tab.dispose();
    }

    public void unregisterAdvancementTabs(@NotNull Plugin plugin) {
        Validate.notNull(plugin, "Plugin is null.");
        List<AdvancementTab> tabs = pluginMap.remove(plugin);
        if (tabs != null) {
            for (AdvancementTab t : tabs)
                unregisterAdvancementTab(t.getNamespace());
        }
    }

    @Nullable
    public Advancement getAdvancement(@NotNull String namespacedKey) {
        int colon = namespacedKey.indexOf(':');
        if (colon <= 0 || colon == namespacedKey.length() - 1) {
            throw new IllegalArgumentException("Malformed NamespacedKey '" + namespacedKey + "'");
        }
        return getAdvancement(namespacedKey.substring(0, colon), namespacedKey.substring(colon + 1));
    }

    @Nullable
    public Advancement getAdvancement(@NotNull String namespace, @NotNull String key) {
        Validate.notNull(namespace, "Namespace is null.");
        Validate.notNull(key, "Key is null.");
        return getAdvancement(new AdvancementKey(namespace, key));
    }

    @Nullable
    public Advancement getAdvancement(@NotNull AdvancementKey namespacedKey) {
        Validate.notNull(namespacedKey, "AdvancementKey is null.");
        AdvancementTab tab = tabs.get(namespacedKey.getNamespace());
        if (tab == null || !tab.isActive())
            return null;
        return tab.getAdvancement(namespacedKey);
    }

    @NotNull
    @UnmodifiableView
    @Contract(pure = true)
    public Set<@NotNull String> getAdvancementTabNamespaces() {
        return Collections.unmodifiableSet(tabs.keySet());
    }

    @NotNull
    @Contract(pure = true, value = "_ -> new")
    public List<@NotNull String> filterNamespaces(@Nullable String input) {
        List<String> l = new LinkedList<>();
        if (input == null || input.isEmpty()) {
            for (Entry<String, AdvancementTab> e : tabs.entrySet()) {
                if (e.getValue().isActive()) {
                    l.addAll(e.getValue().getAdvancementsAsStrings());
                }
            }
        } else {
            int index = input.indexOf(':');
            if (index != -1) {
                String sub = input.substring(0, index);
                for (Entry<String, AdvancementTab> e : tabs.entrySet()) {
                    if (e.getValue().isActive() && e.getKey().equals(sub)) {
                        for (String s : e.getValue().getAdvancementsAsStrings()) {
                            if (s.startsWith(input))
                                l.add(s);
                        }
                    }
                }
            } else {
                for (Entry<String, AdvancementTab> e : tabs.entrySet()) {
                    if (e.getValue().isActive() && e.getKey().startsWith(input)) {
                        l.addAll(e.getValue().getAdvancementsAsStrings());
                    }
                }
            }
        }
        return l;
    }

    @UnmodifiableView
    @NotNull
    public Collection<@NotNull AdvancementTab> getTabs() {
        return Collections.unmodifiableCollection(tabs.values());
    }

    public void updatePlayer(@NotNull Player player) {
        Validate.notNull(player, "Player is null.");
        for (AdvancementTab tab : tabs.values()) {
            if (tab.isActive() && tab.isShownTo(player)) {
                tab.updateEveryAdvancement(player);
            }
        }
    }
}
