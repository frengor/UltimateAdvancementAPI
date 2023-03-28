package com.fren_gor.ultimateAdvancementAPI.database;

/**
 * {@code CacheFreeingOption} represents the caching strategy that will be used by the caching system.
 *
 * @see DatabaseManager
 */
public enum CacheFreeingOption {
    /**
     * Don't cache the request.
     */
    DONT_CACHE,
    /**
     * Cache the request and leave it in the cache until a manual unload request is done.
     */
    MANUAL
}
