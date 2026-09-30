package com.fren_gor.ultimateAdvancementAPI;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementUtils;
import com.fren_gor.ultimateAdvancementAPI.util.Versions;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class UpdateChecker {

    /**
     * Spigot resource id
     */
    private static final int RESOURCE_ID = 95585;

    /**
     * Hangar project id
     */
    private static final int HANGAR_ID = 2035;

    private static final String HANGAR_URL = "https://hangar.papermc.io/api/v1/projects/" + HANGAR_ID + "/latest";
    private static final String SPIGOT_URL = "https://api.spigotmc.org/legacy/update.php?resource=" + RESOURCE_ID;
    private static final String DOWNLOAD_URL = "https://modrinth.com/plugin/ultimateadvancementapi";

    private final AdvancementPlugin plugin;

    public UpdateChecker(@NotNull AdvancementPlugin plugin) {
        // This is called asynchronously
        this.plugin = Objects.requireNonNull(plugin, "AdvancementPlugin is null");
    }

    public void checkForUpdates() {
        // All the code here is called asynchronously

        try {
            boolean shouldUpdate = shouldUpdate(
                Versions.getApiVersion(),
                () -> fetchVersion(HANGAR_URL),
                channel -> fetchVersion(HANGAR_URL + "?channel=" + channel),
                () -> fetchVersion(SPIGOT_URL),
                err -> {
                    AdvancementUtils.runSync(this.plugin, () -> {
                        plugin.getLogger().log(Level.WARNING, "Could not look for updates, fallback used", err);
                    });
                }
            );

            if (shouldUpdate) {
                AdvancementUtils.runSync(this.plugin, () -> {
                    plugin.getLogger().info("A new version of " + plugin.getDescription().getName() + " is out! Download it at " + DOWNLOAD_URL);
                });
            }
        } catch (Exception e) {
            AdvancementUtils.runSync(this.plugin, () -> {
                plugin.getLogger().log(Level.WARNING, "Cannot look for updates", e);
            });
        }
    }

    // Default visibility for tests
    static boolean shouldUpdate(String apiVersion, Callable<String> getStableVersion, IChannelVersion getChannelVersion, Callable<String> fallbackGetVersion, Consumer<Exception> logFallback) throws Exception {
        try {
            return _shouldUpdate(apiVersion, getStableVersion.call(), getChannelVersion);
        } catch (Exception e) {
            // Try fallback
            try {
                String fallbackVer = fallbackGetVersion.call();
                logFallback.accept(e);
                if (apiVersion.equalsIgnoreCase(fallbackVer)) {
                    return false;
                }
            } catch (Exception ex) {
                e.addSuppressed(ex);
                throw e;
            }
        }
        return true;
    }

    // Default visibility for tests
    static boolean _shouldUpdate(String apiVersion, String stableVersion, IChannelVersion getChannelVersion) throws Exception {
        if (apiVersion.equalsIgnoreCase(stableVersion)) {
            return false;
        }

        String channel = getChannel(apiVersion);
        // Only check the channel (e.g. beta) if the current version is > than the stable version
        if (!channel.isEmpty() && isGreater(apiVersion, stableVersion)) {
            String version = getChannelVersion.getChannelVersion(channel);
            return !apiVersion.equalsIgnoreCase(version);
        }

        return true;
    }

    private String fetchVersion(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.addRequestProperty("User-Agent", this.plugin.getDescription().getName() + '/' + Versions.getApiVersion());
        conn.setConnectTimeout(60000);
        conn.setReadTimeout(60000);
        conn.setUseCaches(false);
        conn.setDoOutput(true);

        try (InputStream inputStream = conn.getInputStream(); Scanner scanner = new Scanner(inputStream)) {
            if (scanner.hasNextLine()) {
                return scanner.next();
            }
        }

        throw new IOException("Received an empty response from " + url);
    }

    // Default visibility for tests
    static String getChannel(String version) {
        int index = version.indexOf('-');
        if (index == -1) {
            return "";
        }
        String substr = version.substring(index + 1);
        // version may be in the form 3.0.0-beta-1
        return extractBeforeHyphen(substr);
    }

    // Returns whether verA > verB
    // Default visibility for tests
    static boolean isGreater(String verA, String verB) {
        String[] a = extractBeforeHyphen(verA).split("\\.");
        String[] b = extractBeforeHyphen(verB).split("\\.");
        for (int i = 0; i < Math.max(a.length, b.length); i++) {
            int A = i < a.length ? Integer.parseInt(a[i]) : 0;
            int B = i < b.length ? Integer.parseInt(b[i]) : 0;
            if (A > B) {
                return true;
            }
        }
        return false;
    }

    // Default visibility for tests
    static String extractBeforeHyphen(String version) {
        int index = version.indexOf('-');
        if (index == -1) {
            return version;
        }
        return version.substring(0, index);
    }

    @FunctionalInterface
    interface IChannelVersion {
        String getChannelVersion(String channel) throws Exception;
    }
}
