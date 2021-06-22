package com.fren_gor.ultimateAdvancementAPI.database;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class CacheFreeingOption {
    public enum Option {
        DONT_CACHE, AUTOMATIC, MANUAL;
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

    public static CacheFreeingOption DONT_CACHE() {
        return DONT_CACHE;
    }

    public static CacheFreeingOption AUTOMATIC(@NotNull Plugin requester, @Range(from = 0, to = Long.MAX_VALUE) long ticks) {
        Validate.notNull(requester, "Plugin is null.");
        Validate.isTrue(requester.isEnabled(), "Plugin isn't enabled.");
        return new CacheFreeingOption(Option.AUTOMATIC, ticks, requester);
    }

    public static CacheFreeingOption MANUAL(@NotNull Plugin requester) {
        Validate.notNull(requester, "Plugin is null.");
        Validate.isTrue(requester.isEnabled(), "Plugin isn't enabled.");
        return new CacheFreeingOption(Option.MANUAL, requester);
    }

}
