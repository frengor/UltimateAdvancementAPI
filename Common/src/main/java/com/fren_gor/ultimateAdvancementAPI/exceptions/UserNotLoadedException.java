package com.fren_gor.ultimateAdvancementAPI.exceptions;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * This exception is thrown when the requested user is not loaded into the caching system.
 */
public class UserNotLoadedException extends RuntimeException {

    private final UUID user;

    public UserNotLoadedException(@NotNull UUID user) {
        this(user, getDefaultErrorMessage(user));
    }

    public UserNotLoadedException(@NotNull UUID user, String message) {
        super(message);
        this.user = Objects.requireNonNull(user, "User is null.");
    }

    public UserNotLoadedException(@NotNull UUID user, String message, Throwable cause) {
        super(message, cause);
        this.user = Objects.requireNonNull(user, "User is null.");
    }

    public UserNotLoadedException(@NotNull UUID user, Throwable cause) {
        this(user, getDefaultErrorMessage(user), cause);
    }

    protected UserNotLoadedException(@NotNull UUID user, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.user = Objects.requireNonNull(user, "User is null.");
    }

    /**
     * Gets the {@link UUID} of the user who was not loaded into the caching system.
     *
     * @return The {@link UUID} of the user who was not loaded into the caching system.
     */
    @NotNull
    public UUID getUser() {
        return user;
    }

    private static String getDefaultErrorMessage(UUID user) {
        return "Could not load user " + user;
    }
}
