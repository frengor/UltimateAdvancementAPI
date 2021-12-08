package com.fren_gor.ultimateAdvancementAPI.metrics;

import com.fren_gor.ultimateAdvancementAPI.AdvancementPlugin;
import com.fren_gor.ultimateAdvancementAPI.util.Versions;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BStats {

    /**
     * bStats resource id
     */
    private static final int BSTATS_ID = 12593;
    private static final Map<String, Map<String, Integer>> apiMcVersions = new HashMap<>(), apiNMSVersions = new HashMap<>();

    public static void init(AdvancementPlugin plugin) {
        apiMcVersions.put(Versions.getApiVersion(), Collections.singletonMap(Versions.getNMSVersionsRange(), 1));
        apiNMSVersions.put(Versions.getApiVersion(), Collections.singletonMap(Versions.removeInitialV(Versions.getNMSVersion()), 1));

        Metrics metrics = new Metrics(plugin, BSTATS_ID);

        metrics.addCustomChart(new SimplePie("vanilla_advancements", () -> plugin.getConfigManager().getDisableVanillaAdvancements() ? "Disabled" : "Not Disabled"));
        metrics.addCustomChart(new SimplePie("database_type", () -> plugin.getConfigManager().getStorageType().getFancyName()));
        metrics.addCustomChart(new DrilldownPie("api_-_minecraft_version", () -> apiMcVersions));
        metrics.addCustomChart(new DrilldownPie("api_-_nms_version", () -> apiNMSVersions));
    }
}
