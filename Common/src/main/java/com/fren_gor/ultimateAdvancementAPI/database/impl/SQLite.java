package com.fren_gor.ultimateAdvancementAPI.database.impl;

import com.fren_gor.ultimateAdvancementAPI.database.IDatabase;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.IllegalKeyException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotRegisteredException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteConfig.Encoding;
import org.sqlite.SQLiteConfig.SynchronousMode;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

public class SQLite implements IDatabase {

    private final Logger logger;
    private final Connection connection;

    public SQLite(@NotNull File dbFile, @NotNull Logger logger) throws Exception {
        if (!dbFile.exists() && !dbFile.createNewFile()) {
            throw new IOException("Cannot create the database file.");
        }
        Class.forName("org.sqlite.JDBC");
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        config.setEncoding(Encoding.UTF8);
        config.setSynchronous(SynchronousMode.FULL);
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile, config.toProperties());
        this.logger = logger;
    }

    @Override
    public void setUp() throws SQLException {
        try (Statement statement = openConnection().createStatement()) {
            //statement.addBatch("PRAGMA foreign_keys = ON;");
            statement.addBatch("CREATE TABLE IF NOT EXISTS `Teams` (`ID` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT);");
            statement.addBatch("CREATE TABLE IF NOT EXISTS `Players` (`UUID` TEXT NOT NULL PRIMARY KEY, `Name` TEXT NOT NULL, `TeamID` INTEGER NOT NULL, FOREIGN KEY(`TeamID`) REFERENCES `Teams`(`ID`) ON DELETE CASCADE ON UPDATE CASCADE);");
            statement.addBatch("CREATE TABLE IF NOT EXISTS `Advancements` (`Namespace` TEXT NOT NULL, `Key` TEXT NOT NULL, `TeamID` INTEGER NOT NULL, `Criteria` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`Namespace`,`Key`,`TeamID`), FOREIGN KEY(`TeamID`) REFERENCES `Teams`(`ID`) ON DELETE CASCADE ON UPDATE CASCADE);");
            statement.addBatch("CREATE TABLE IF NOT EXISTS `Unredeemed` (`Namespace` TEXT NOT NULL, `Key` TEXT NOT NULL, `TeamID` INTEGER NOT NULL, `GiveRewards` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`Namespace`,`Key`,`TeamID`), FOREIGN KEY(`Namespace`, `Key`,`TeamID`) REFERENCES `Advancements`(`Namespace`, `Key`,`TeamID`) ON DELETE CASCADE ON UPDATE CASCADE);");
            statement.executeBatch();
        }
    }

    @Override
    public Connection openConnection() throws SQLException {
        return connection;
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    @Override
    public int getTeamId(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException {
        try (PreparedStatement ps = openConnection().prepareStatement("SELECT `TeamID` FROM `Players` WHERE `UUID`=?;")) {
            ps.setString(1, uuid.toString());
            ResultSet r = ps.executeQuery();
            if (r.next()) {
                return r.getInt(1);
            } else {
                throw new UserNotRegisteredException("No user " + uuid + " has been found.");
            }
        }
    }

    @Override
    public List<UUID> getTeamMembers(int teamId) throws SQLException {
        try (PreparedStatement ps = openConnection().prepareStatement("SELECT `UUID` FROM `Players` WHERE `TeamID`=?;")) {
            ps.setInt(1, teamId);
            ResultSet r = ps.executeQuery();
            List<UUID> list = new LinkedList<>();
            while (r.next()) {
                list.add(UUID.fromString(r.getString(1)));
            }
            return list;
        }
    }

    @Override
    public Map<AdvancementKey, Integer> getTeamAdvancements(int teamId) throws SQLException {
        try (PreparedStatement ps = openConnection().prepareStatement("SELECT `Namespace`,`Key`,`Criteria` FROM `Advancements` WHERE `TeamID`=?;")) {
            ps.setInt(1, teamId);
            ResultSet r = ps.executeQuery();
            Map<AdvancementKey, Integer> map = new HashMap<>();
            while (r.next()) {
                String namespace = r.getString(1);
                String key = r.getString(2);
                int criteria = r.getInt(3);
                try {
                    map.put(new AdvancementKey(namespace, key), criteria);
                } catch (IllegalKeyException e) {
                    logger.warning("Invalid AdvancementKey (" + namespace + ':' + key + ") encountered while reading Advancements table: " + e.getMessage());
                }
            }
            return map;
        }
    }

    @Override
    public Entry<TeamProgression, Boolean> loadOrRegisterPlayer(@NotNull UUID uuid, @NotNull String name) throws SQLException {
        int teamId;
        ResultSet r;
        try (PreparedStatement psTeamId = openConnection().prepareStatement("SELECT `TeamID` FROM `Players` WHERE `UUID`=?;")) {
            psTeamId.setString(1, uuid.toString());

            r = psTeamId.executeQuery();
            if (!r.next()) { // Player isn't registered
                try (PreparedStatement psInsert = openConnection().prepareStatement("INSERT INTO `Teams` DEFAULT VALUES;"); PreparedStatement psInsertPl = openConnection().prepareStatement("INSERT INTO `Players` (`UUID`, `Name`, `TeamID`) VALUES (?, ?, ?);")) {
                    psInsert.executeUpdate();
                    r = psInsert.getGeneratedKeys();
                    if (!r.next()) {
                        throw new SQLException("Cannot insert default values into Teams table.");
                    }
                    teamId = r.getInt(1);
                    psInsertPl.setString(1, uuid.toString());
                    psInsertPl.setString(2, name);
                    psInsertPl.setInt(3, teamId);
                    psInsertPl.execute();
                    return new SimpleEntry<>(new TeamProgression(teamId, uuid), true);
                }
            }

            teamId = r.getInt(1);
        }
        List<UUID> list = getTeamMembers(teamId);
        Map<AdvancementKey, Integer> map = getTeamAdvancements(teamId);
        return new SimpleEntry<>(new TeamProgression(map, teamId, list), false);
    }

    @Override
    public TeamProgression loadUUID(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException {
        int teamID = Integer.MIN_VALUE;
        List<UUID> list = new LinkedList<>();
        try (PreparedStatement psTeamId = openConnection().prepareStatement("SELECT `UUID`, `TeamID` FROM `Players` WHERE `TeamID`=(SELECT `TeamID` FROM `Players` WHERE `UUID`=? LIMIT 1);")) {
            psTeamId.setString(1, uuid.toString());
            ResultSet r = psTeamId.executeQuery();
            while (r.next()) {
                list.add(UUID.fromString(r.getString(1)));
                if (teamID == Integer.MIN_VALUE)
                    teamID = r.getInt(2);
            }
        }

        if (teamID == Integer.MIN_VALUE)
            throw new UserNotRegisteredException("No user " + uuid + " has been found.");

        try (PreparedStatement psAdv = openConnection().prepareStatement("SELECT `Namespace`, `Key`, `Criteria` FROM `Advancements` WHERE `TeamID`=?;")) {
            Map<AdvancementKey, Integer> map = new HashMap<>();
            psAdv.setInt(1, teamID);
            ResultSet r = psAdv.executeQuery();
            while (r.next()) {
                String namespace = r.getString(1);
                String key = r.getString(2);
                int criteria = r.getInt(3);
                try {
                    map.put(new AdvancementKey(namespace, key), criteria);
                } catch (IllegalKeyException e) {
                    logger.warning("Invalid AdvancementKey (" + namespace + ':' + key + ") encountered while reading Advancements table: " + e.getMessage());
                }
            }
            return new TeamProgression(map, teamID, list);
        }
    }

    @Override
    public void updateAdvancement(@NotNull AdvancementKey key, int teamId, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) throws SQLException {
        if (criteria <= 0) {
            try (PreparedStatement ps = openConnection().prepareStatement("DELETE FROM `Advancements` WHERE `Namespace`=? AND `Key`=? AND `TeamID`=?;")) {
                ps.setString(1, key.getNamespace());
                ps.setString(2, key.getKey());
                ps.setInt(3, teamId);
                ps.execute();
            }
        } else {
            try (PreparedStatement ps = openConnection().prepareStatement("INSERT OR REPLACE INTO `Advancements` (`Namespace`, `Key`, `TeamID`, `Criteria`) VALUES (?, ?, ?, ?);")) {
                ps.setString(1, key.getNamespace());
                ps.setString(2, key.getKey());
                ps.setInt(3, teamId);
                ps.setInt(4, criteria);
                ps.execute();
            }
        }
    }

    @Override
    public List<Entry<AdvancementKey, Boolean>> getUnredeemed(int teamId) throws SQLException {
        try (PreparedStatement ps = openConnection().prepareStatement("SELECT `Namespace`, `Key`, `GiveRewards` FROM `Unredeemed` WHERE `TeamID`=?;")) {
            ps.setInt(1, teamId);
            ResultSet r = ps.executeQuery();
            List<Entry<AdvancementKey, Boolean>> list = new LinkedList<>();
            while (r.next()) {
                String namespace = r.getString(1);
                String key = r.getString(2);
                boolean giveRewards = r.getInt(3) != 0; // false iff r.getInt(3) == 0
                try {
                    list.add(new SimpleEntry<>(new AdvancementKey(namespace, key), giveRewards));
                } catch (IllegalKeyException e) {
                    logger.warning("Invalid AdvancementKey (" + namespace + ':' + key + ") encountered while reading Unredeemed table: " + e.getMessage());
                }
            }
            return list;
        }
    }

    @Override
    public void setUnredeemed(@NotNull AdvancementKey key, boolean giveRewards, int teamId) throws SQLException {
        try (PreparedStatement ps = openConnection().prepareStatement("INSERT OR IGNORE INTO `Unredeemed` (`Namespace`, `Key`, `TeamID`, `GiveRewards`) VALUES (?, ?, ?, ?);")) {
            ps.setString(1, key.getNamespace());
            ps.setString(2, key.getKey());
            ps.setInt(3, teamId);
            ps.setInt(4, giveRewards ? 1 : 0);
            ps.execute();
        }
    }

    @Override
    public boolean isUnredeemed(@NotNull AdvancementKey key, int teamId) throws SQLException {
        try (PreparedStatement ps = openConnection().prepareStatement("SELECT Count(*) FROM `Unredeemed` WHERE `Namespace`=? AND `Key`=? AND `TeamID`=?;")) {
            ps.setString(1, key.getNamespace());
            ps.setString(2, key.getKey());
            ps.setInt(3, teamId);
            ResultSet r = ps.executeQuery();
            return r.next() && r.getInt(1) > 0;
        }
    }

    @Override
    public void unsetUnredeemed(@NotNull AdvancementKey key, int teamId) throws SQLException {
        try (PreparedStatement ps = openConnection().prepareStatement("DELETE FROM `Unredeemed` WHERE `Namespace`=? AND `Key`=? AND `TeamID`=?;")) {
            ps.setString(1, key.getNamespace());
            ps.setString(2, key.getKey());
            ps.setInt(3, teamId);
            ps.execute();
        }
    }

    @Override
    public void unsetUnredeemed(@NotNull List<Entry<AdvancementKey, Boolean>> keyList, int teamId) throws SQLException {
        try (PreparedStatement ps = openConnection().prepareStatement("DELETE FROM `Unredeemed` WHERE `Namespace`=? AND `Key`=? AND `TeamID`=?;")) {
            for (Entry<AdvancementKey, ?> key : keyList) {
                ps.setString(1, key.getKey().getNamespace());
                ps.setString(2, key.getKey().getKey());
                ps.setInt(3, teamId);
                ps.execute();
            }
        }
    }

    @Override
    public void unregisterPlayer(@NotNull UUID uuid) throws SQLException {
        try (PreparedStatement stDelete = openConnection().prepareStatement("DELETE FROM `Players` WHERE `UUID`=?;")) {
            stDelete.setString(1, uuid.toString());
            stDelete.execute();
        }
    }

    @Override
    public void movePlayer(@NotNull UUID uuid, int newTeamId) throws SQLException {
        try (PreparedStatement stUpdate = openConnection().prepareStatement("UPDATE `Players` SET `TeamID`=? WHERE `UUID`=?;")) {
            stUpdate.setInt(1, newTeamId);
            stUpdate.setString(2, uuid.toString());
            stUpdate.execute();
        }
    }

    @Override
    public TeamProgression movePlayerInNewTeam(@NotNull UUID uuid) throws SQLException {
        try (PreparedStatement psInsert = openConnection().prepareStatement("INSERT INTO `Teams` DEFAULT VALUES;")) {
            psInsert.executeUpdate();
            ResultSet r = psInsert.getGeneratedKeys();
            if (!r.next()) {
                throw new SQLException("Cannot insert default values into Teams table.");
            }
            int teamId = r.getInt(1);
            movePlayer(uuid, teamId);
            return new TeamProgression(teamId, uuid);
        }
    }

    @Override
    public List<UUID> getPlayersByName(@NotNull String name) throws SQLException {
        try (PreparedStatement ps = openConnection().prepareStatement("SELECT `UUID` FROM `Players` WHERE `Name`=?;")) {
            ps.setString(1, name);
            ResultSet r = ps.executeQuery();
            List<UUID> list = new LinkedList<>();
            while (r.next()) {
                list.add(UUID.fromString(r.getString(1)));
            }
            return list;
        }
    }

    @Override
    public String getPlayerName(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException {
        try (PreparedStatement ps = openConnection().prepareStatement("SELECT `Name` FROM `Players` WHERE `UUID`=? LIMIT 1;")) {
            ps.setString(1, uuid.toString());
            ResultSet r = ps.executeQuery();
            if (!r.next()) {
                throw new UserNotRegisteredException("No user " + uuid + " has been found.");
            }
            return r.getString(1);
        }
    }

    @Override
    public void updatePlayerName(@NotNull UUID uuid, @NotNull String name) throws SQLException {
        try (PreparedStatement ps = openConnection().prepareStatement("UPDATE `Players` SET `Name`=? WHERE `UUID`=?;")) {
            ps.setString(1, name);
            ps.setString(2, uuid.toString());
            ps.execute();
        }
    }

    @Override
    public void clearUpTeams() throws SQLException {
        try (PreparedStatement ps = openConnection().prepareStatement("DELETE FROM `Teams` WHERE `ID` NOT IN (SELECT `TeamID` FROM `Players` GROUP BY `TeamID`);")) {
            ps.execute();
        }
    }
}
