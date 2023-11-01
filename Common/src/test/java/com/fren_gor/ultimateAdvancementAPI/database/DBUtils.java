package com.fren_gor.ultimateAdvancementAPI.database;

public final class DBUtils {

    public enum DBOperation {
        GET_TEAM_ID,
        GET_TEAM_MEMBERS,
        GET_TEAM_ADVANCEMENTS,
        LOAD_OR_REGISTER_PLAYER,
        LOAD_UUID,
        CREATE_NEW_TEAM,
        UPDATE_ADVANCEMENT,
        GET_UNREDEEMED,
        SET_UNREDEEMED,
        IS_UNREDEEMED,
        UNSET_UNREDEEMED,
        UNREGISTER_PLAYER,
        MOVE_PLAYER,
        MOVE_PLAYER_IN_NEW_TEAM,
        GET_PLAYERS_BY_NAME,
        GET_PLAYER_NAME,
        UPDATE_PLAYER_NAME,
        CLEAR_UP_TEAMS;
    }

    private DBUtils() {
    }
}
