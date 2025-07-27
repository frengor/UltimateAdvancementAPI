package com.fren_gor.ultimateAdvancementAPI.metrics;

import com.fren_gor.ultimateAdvancementAPI.AdvancementPlugin;
import com.fren_gor.ultimateAdvancementAPI.util.Versions;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BStats {

    /**
     * bStats resource id
     */
    private static final int BSTATS_ID = 12593;
    private static final Map<String, Map<String, Integer>> apiMcVersions = new HashMap<>(), apiNMSVersions = new HashMap<>();

    public static void init(@NotNull AdvancementPlugin plugin) {
        @Nullable String versionsRange = Versions.getNMSVersionsRange();

        Metrics metrics = new Metrics(plugin, BSTATS_ID);

        metrics.addCustomChart(new SimplePie("disabled_vanilla_advancements", () -> getDisabledAdvancementsLabel(plugin)));
        metrics.addCustomChart(new SimplePie("database_type", () -> plugin.getConfigManager().getStorageType().getFancyName()));

        if (versionsRange != null) {
            apiMcVersions.put(Versions.getApiVersion(), Collections.singletonMap(Versions.getNMSVersionsRange(), 1));
            metrics.addCustomChart(new DrilldownPie("api_-_minecraft_version", () -> apiMcVersions));
        }

        Versions.getNMSVersion().ifPresent(version -> {
            apiNMSVersions.put(Versions.getApiVersion(), Collections.singletonMap(Versions.removeInitialV(version), 1));
            metrics.addCustomChart(new DrilldownPie("api_-_nms_version", () -> apiNMSVersions));
        });
    }

    @NotNull
    private static String getDisabledAdvancementsLabel(@NotNull AdvancementPlugin plugin) {
        boolean disabledAdvs = plugin.getConfigManager().getDisableVanillaAdvancements();
        boolean disabledRecipeAdvs = plugin.getConfigManager().getDisableVanillaRecipeAdvancements();

        if (disabledAdvs && disabledRecipeAdvs) {
            return "All Disabled";
        } else if (disabledAdvs) {
            return "Only Vanilla Advs Disabled";
        } else if (disabledRecipeAdvs) {
            return "Only Recipe Advs Disabled";
        } else {
            return "None Disabled";
        }
    }
}
