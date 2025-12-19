package com.fren_gor.ultimateAdvancementAPI.exceptions;

/**
 * This exception is thrown when the requested team is not present in the database.
 */
public class TeamNotRegisteredException extends NotRegisteredException {

    private final int teamId;

    public TeamNotRegisteredException(int teamId) {
        this(teamId, getDefaultErrorMessage(teamId));
    }

    public TeamNotRegisteredException(int teamId, String message) {
        super(message);
        this.teamId = teamId;
    }

    public TeamNotRegisteredException(int teamId, String message, Throwable cause) {
        super(message, cause);
        this.teamId = teamId;
    }

    public TeamNotRegisteredException(int teamId, Throwable cause) {
        this(teamId, getDefaultErrorMessage(teamId), cause);
    }

    protected TeamNotRegisteredException(int teamId, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.teamId = teamId;
    }

    /**
     * Gets the id of the team that was not present in the database.
     *
     * @return The id of the team that was not present in the database.
     */
    public int getTeamId() {
        return teamId;
    }

    private static String getDefaultErrorMessage(int teamId) {
        return "Could not find any team with id " + teamId;
    }
}
