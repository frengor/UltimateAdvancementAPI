package com.fren_gor.ultimateAdvancementAPI.exceptions;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

/**
 * This exception is thrown when the requested user is not present in the database.
 */
public class UserNotRegisteredException extends NotRegisteredException {

    private final UUID user;

    public UserNotRegisteredException(@NotNull UUID user) {
        this(user, getDefaultErrorMessage(user));
    }

    public UserNotRegisteredException(@NotNull UUID user, String message) {
        super(message);
        this.user = Objects.requireNonNull(user, "User is null.");
    }

    public UserNotRegisteredException(@NotNull UUID user, String message, Throwable cause) {
        super(message, cause);
        this.user = Objects.requireNonNull(user, "User is null.");
    }

    public UserNotRegisteredException(@NotNull UUID user, Throwable cause) {
        this(user, getDefaultErrorMessage(user), cause);
    }

    protected UserNotRegisteredException(@NotNull UUID user, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.user = Objects.requireNonNull(user, "User is null.");
    }

    /**
     * Gets the {@link UUID} of the user who was not present in the database.
     *
     * @return The {@link UUID} of the user who was not present in the database.
     */
    @NotNull
    public UUID getUser() {
        return user;
    }

    private static String getDefaultErrorMessage(UUID user) {
        return "Could not find user " + user;
    }
}
