package com.fren_gor.ultimateAdvancementAPI.database.impl;

import com.fren_gor.ultimateAdvancementAPI.AdvancementMain;
import com.fren_gor.ultimateAdvancementAPI.database.IDatabase;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.IllegalKeyException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotRegisteredException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.google.common.base.Preconditions;
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

/**
 * Class used to establish a connection to a SQLite database.
 */
public class SQLite implements IDatabase {

    private final Logger logger;
    private final Connection connection;

    /**
     * Creates the SQLite connection.
     *
     * @param main The instance of the main class of the API.
     * @param dbFile The SQLite database file. If it doesn't exist, it is created.
     * @throws Exception If anything goes wrong.
     */
    public SQLite(@NotNull AdvancementMain main, @NotNull File dbFile) throws Exception {
        Preconditions.checkNotNull(main, "AdvancementMain is null.");
        Preconditions.checkNotNull(dbFile, "Database file is null.");
        this.logger = main.getLogger();
        if (!dbFile.exists() && !dbFile.createNewFile()) {
            throw new IOException("Cannot create the database file.");
        }
        Class.forName("org.sqlite.JDBC");
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        config.setEncoding(Encoding.UTF8);
        config.setSynchronous(SynchronousMode.FULL);
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile, config.toProperties());
    }

    /**
     * Creates a SQLite in memory database connection.
     *
     * @param logger The plugin {@link Logger}.
     * @throws Exception If anything goes wrong.
     */
    SQLite(@NotNull Logger logger) throws Exception {
        Preconditions.checkNotNull(logger, "Logger is null.");
        this.logger = logger;
        Class.forName("org.sqlite.JDBC");
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true);
        config.setEncoding(Encoding.UTF8);
        config.setSynchronous(SynchronousMode.FULL);
        this.connection = DriverManager.getConnection("jdbc:sqlite::memory:", config.toProperties());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws SQLException {
        try (Statement statement = openConnection().createStatement()) {
            //statement.addBatch("PRAGMA foreign_keys = ON;");
            statement.addBatch("CREATE TABLE IF NOT EXISTS `Teams` (`ID` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT);");
            statement.addBatch("CREATE TABLE IF NOT EXISTS `Players` (`UUID` TEXT NOT NULL PRIMARY KEY, `Name` TEXT NOT NULL, `TeamID` INTEGER NOT NULL, FOREIGN KEY(`TeamID`) REFERENCES `Teams`(`ID`) ON DELETE CASCADE ON UPDATE CASCADE);");
            statement.addBatch("CREATE TABLE IF NOT EXISTS `Advancements` (`Namespace` TEXT NOT NULL, `Key` TEXT NOT NULL, `TeamID` INTEGER NOT NULL, `Progression` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`Namespace`,`Key`,`TeamID`), FOREIGN KEY(`TeamID`) REFERENCES `Teams`(`ID`) ON DELETE CASCADE ON UPDATE CASCADE);");
            statement.addBatch("CREATE TABLE IF NOT EXISTS `Unredeemed` (`Namespace` TEXT NOT NULL, `Key` TEXT NOT NULL, `TeamID` INTEGER NOT NULL, `GiveRewards` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`Namespace`,`Key`,`TeamID`), FOREIGN KEY(`Namespace`, `Key`,`TeamID`) REFERENCES `Advancements`(`Namespace`, `Key`,`TeamID`) ON DELETE CASCADE ON UPDATE CASCADE);");
            statement.executeBatch();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection openConnection() throws SQLException {
        return connection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws SQLException {
        connection.close();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<AdvancementKey, Integer> getTeamAdvancements(int teamId) throws SQLException {
        try (PreparedStatement ps = openConnection().prepareStatement("SELECT `Namespace`,`Key`,`Progression` FROM `Advancements` WHERE `TeamID`=?;")) {
            ps.setInt(1, teamId);
            ResultSet r = ps.executeQuery();
            Map<AdvancementKey, Integer> map = new HashMap<>();
            while (r.next()) {
                String namespace = r.getString(1);
                String key = r.getString(2);
                int progression = r.getInt(3);
                try {
                    map.put(new AdvancementKey(namespace, key), progression);
                } catch (IllegalKeyException e) {
                    logger.warning("Invalid AdvancementKey (" + namespace + ':' + key + ") encountered while reading Advancements table: " + e.getMessage());
                }
            }
            return map;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Entry<TeamProgression, Boolean> loadOrRegisterPlayer(@NotNull UUID uuid, @NotNull String name) throws SQLException {
        int teamId;
        ResultSet r;
        try (PreparedStatement psTeamId = openConnection().prepareStatement("SELECT `TeamID` FROM `Players` WHERE `UUID`=?;")) {
            psTeamId.setString(1, uuid.toString());

            r = psTeamId.executeQuery();
            if (!r.next()) { // Player isn't registered
                try (PreparedStatement psInsert = openConnection().prepareStatement("INSERT INTO `Teams` DEFAULT VALUES RETURNING `ID`;")) {
                    r = psInsert.executeQuery();
                    if (!r.next()) {
                        throw new SQLException("Cannot insert default values into Teams table.");
                    }
                    teamId = r.getInt(1);
                }
                try (PreparedStatement psInsertPl = openConnection().prepareStatement("INSERT INTO `Players` (`UUID`, `Name`, `TeamID`) VALUES (?, ?, ?);")) {
                    psInsertPl.setString(1, uuid.toString());
                    psInsertPl.setString(2, name);
                    psInsertPl.setInt(3, teamId);
                    psInsertPl.execute();
                    return new SimpleEntry<>(new TeamProgression(teamId), true);
                }
            }

            teamId = r.getInt(1);
        }
        List<UUID> list = getTeamMembers(teamId);
        Map<AdvancementKey, Integer> map = getTeamAdvancements(teamId);
        return new SimpleEntry<>(new TeamProgression(map, teamId, list), false);
    }

    /**
     * {@inheritDoc}
     */
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

        try (PreparedStatement psAdv = openConnection().prepareStatement("SELECT `Namespace`, `Key`, `Progression` FROM `Advancements` WHERE `TeamID`=?;")) {
            Map<AdvancementKey, Integer> map = new HashMap<>();
            psAdv.setInt(1, teamID);
            ResultSet r = psAdv.executeQuery();
            while (r.next()) {
                String namespace = r.getString(1);
                String key = r.getString(2);
                int progression = r.getInt(3);
                try {
                    map.put(new AdvancementKey(namespace, key), progression);
                } catch (IllegalKeyException e) {
                    logger.warning("Invalid AdvancementKey (" + namespace + ':' + key + ") encountered while reading Advancements table: " + e.getMessage());
                }
            }
            return new TeamProgression(map, teamID, list);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TeamProgression createNewTeam() throws SQLException {
        try (PreparedStatement psInsert = openConnection().prepareStatement("INSERT INTO `Teams` DEFAULT VALUES RETURNING `ID`;")) {
            ResultSet r = psInsert.executeQuery();
            if (!r.next()) {
                throw new SQLException("Cannot insert default values into Teams table.");
            }
            int teamId = r.getInt(1);
            return new TeamProgression(teamId);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAdvancement(@NotNull AdvancementKey key, int teamId, @Range(from = 0, to = Integer.MAX_VALUE) int progression) throws SQLException {
        if (progression <= 0) {
            try (PreparedStatement ps = openConnection().prepareStatement("DELETE FROM `Advancements` WHERE `Namespace`=? AND `Key`=? AND `TeamID`=?;")) {
                ps.setString(1, key.getNamespace());
                ps.setString(2, key.getKey());
                ps.setInt(3, teamId);
                ps.execute();
            }
        } else {
            try (PreparedStatement ps = openConnection().prepareStatement("INSERT OR REPLACE INTO `Advancements` (`Namespace`, `Key`, `TeamID`, `Progression`) VALUES (?, ?, ?, ?);")) {
                ps.setString(1, key.getNamespace());
                ps.setString(2, key.getKey());
                ps.setInt(3, teamId);
                ps.setInt(4, progression);
                ps.execute();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LinkedList<Entry<AdvancementKey, Boolean>> getUnredeemed(int teamId) throws SQLException {
        try (PreparedStatement ps = openConnection().prepareStatement("SELECT `Namespace`, `Key`, `GiveRewards` FROM `Unredeemed` WHERE `TeamID`=?;")) {
            ps.setInt(1, teamId);
            ResultSet r = ps.executeQuery();
            LinkedList<Entry<AdvancementKey, Boolean>> list = new LinkedList<>();
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsetUnredeemed(@NotNull AdvancementKey key, int teamId) throws SQLException {
        try (PreparedStatement ps = openConnection().prepareStatement("DELETE FROM `Unredeemed` WHERE `Namespace`=? AND `Key`=? AND `TeamID`=?;")) {
            ps.setString(1, key.getNamespace());
            ps.setString(2, key.getKey());
            ps.setInt(3, teamId);
            ps.execute();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsetUnredeemed(@NotNull List<Entry<AdvancementKey, Boolean>> keyList, int teamId) throws SQLException {
        Connection conn = openConnection();
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM `Unredeemed` WHERE `Namespace`=? AND `Key`=? AND `TeamID`=?;")) {
            try {
                conn.setAutoCommit(false); // Make the full transaction atomic

                for (Entry<AdvancementKey, ?> key : keyList) {
                    ps.setString(1, key.getKey().getNamespace());
                    ps.setString(2, key.getKey().getKey());
                    ps.setInt(3, teamId);
                    ps.execute();
                }
                conn.commit();
            } catch (Throwable t) {
                conn.rollback();
                throw t;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterPlayer(@NotNull UUID uuid) throws SQLException {
        try (PreparedStatement stDelete = openConnection().prepareStatement("DELETE FROM `Players` WHERE `UUID`=?;")) {
            stDelete.setString(1, uuid.toString());
            stDelete.execute();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void movePlayer(@NotNull UUID uuid, int newTeamId) throws SQLException {
        try (PreparedStatement stUpdate = openConnection().prepareStatement("UPDATE `Players` SET `TeamID`=? WHERE `UUID`=?;")) {
            stUpdate.setInt(1, newTeamId);
            stUpdate.setString(2, uuid.toString());
            stUpdate.execute();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TeamProgression movePlayerInNewTeam(@NotNull UUID uuid) throws SQLException {
        TeamProgression team = createNewTeam();
        movePlayer(uuid, team.getTeamId());
        return team;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePlayerName(@NotNull UUID uuid, @NotNull String name) throws SQLException {
        try (PreparedStatement ps = openConnection().prepareStatement("UPDATE `Players` SET `Name`=? WHERE `UUID`=?;")) {
            ps.setString(1, name);
            ps.setString(2, uuid.toString());
            ps.execute();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearUpTeams() throws SQLException {
        try (PreparedStatement ps = openConnection().prepareStatement("DELETE FROM `Teams` WHERE `ID` NOT IN (SELECT `TeamID` FROM `Players` GROUP BY `TeamID`);")) {
            ps.execute();
        }
    }
}
