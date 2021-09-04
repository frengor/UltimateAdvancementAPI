package com.fren_gor.ultimateAdvancementAPI.database;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * The {@code CacheFreeingOption} class represents the caching strategy that will be used by the caching system (see {@link DatabaseManager}).
 */
public final class CacheFreeingOption {
    /**
     * Available caching options.
     */
    public enum Option {
        /**
         * Don't cache the request.
         */
        DONT_CACHE,
        /**
         * Cache the request and remove it from the cache after a certain amount of ticks automatically.
         */
        AUTOMATIC,
        /**
         * Cache the request and leave it in the cache until a manual unload request is done.
         */
        MANUAL;
    }

    private static final CacheFreeingOption DONT_CACHE = new CacheFreeingOption(Option.DONT_CACHE, null);

    final Option option;
    final long ticks;
    final Plugin requester;

    private CacheFreeingOption(Option option, Plugin requester) {
        this.option = option;
        this.ticks = -1L;
        this.requester = requester;
    }

    private CacheFreeingOption(Option option, long ticks, Plugin requester) {
        this.option = option;
        this.ticks = ticks < 0 ? 0 : ticks;
        this.requester = requester;
    }

    /**
     * Gets a {@code CacheFreeingOption} instance with caching strategy {@link Option#DONT_CACHE}.
     *
     * @return A {@code CacheFreeingOption} instance with caching strategy {@link Option#DONT_CACHE}.
     */
    public static CacheFreeingOption DONT_CACHE() {
        return DONT_CACHE;
    }

    /**
     * Gets a {@code CacheFreeingOption} instance with caching strategy {@link Option#AUTOMATIC}.
     *
     * @param requester The {@link Plugin} that will make the request.
     * @param ticks The amount of ticks the requested player will remain in the cache.
     * @return A {@code CacheFreeingOption} instance with caching strategy {@link Option#AUTOMATIC}.
     */
    public static CacheFreeingOption AUTOMATIC(@NotNull Plugin requester, @Range(from = 0, to = Long.MAX_VALUE) long ticks) {
        Validate.notNull(requester, "Plugin is null.");
        Validate.isTrue(requester.isEnabled(), "Plugin isn't enabled.");
        return new CacheFreeingOption(Option.AUTOMATIC, ticks, requester);
    }

    /**
     * Gets a {@code CacheFreeingOption} instance with caching strategy {@link Option#MANUAL}.
     *
     * @param requester The {@link Plugin} that will make the request.
     * @return A {@code CacheFreeingOption} instance with caching strategy {@link Option#MANUAL}.
     */
    public static CacheFreeingOption MANUAL(@NotNull Plugin requester) {
        Validate.notNull(requester, "Plugin is null.");
        Validate.isTrue(requester.isEnabled(), "Plugin isn't enabled.");
        return new CacheFreeingOption(Option.MANUAL, requester);
    }

}
