package com.fren_gor.ultimateAdvancementAPI.database.impl;

import com.fren_gor.ultimateAdvancementAPI.database.IDatabase;
import com.fren_gor.ultimateAdvancementAPI.database.TeamProgression;
import com.fren_gor.ultimateAdvancementAPI.exceptions.IllegalKeyException;
import com.fren_gor.ultimateAdvancementAPI.exceptions.UserNotRegisteredException;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import net.byteflux.libby.Library;
import net.byteflux.libby.LibraryManager;
import net.byteflux.libby.classloader.IsolatedClassLoader;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
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
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Class used to establish a connection to a MySQL database.
 */
public class MySQL implements IDatabase {

    private final Logger logger;
    private final IsolatedClassLoader classLoader;
    private final DataSource dataSource;
    private final Method close;

    /**
     * Creates the MySQL connection.
     *
     * @param username The username.
     * @param password The password.
     * @param databaseName The name of the database.
     * @param host The MySQL host.
     * @param port The MySQL port. Must be greater than zero.
     * @param poolSize The pool size. Must be greater than zero.
     * @param connectionTimeout The connection timeout. Must be greater or equal to 250.
     * @param logger The plugin {@link Logger}.
     * @param manager The {@link LibraryManager}.
     * @throws Exception If anything goes wrong.
     */
    public MySQL(@NotNull String username, @NotNull String password, @NotNull String databaseName, @NotNull String host, @Range(from = 1, to = Integer.MAX_VALUE) int port, @Range(from = 1, to = Integer.MAX_VALUE) int poolSize, @Range(from = 250, to = Long.MAX_VALUE) long connectionTimeout, @NotNull Logger logger, @NotNull LibraryManager manager) throws Exception {
        Validate.notNull(username, "Username is null.");
        Validate.notNull(password, "Password is null.");
        Validate.notNull(databaseName, "Database name is null.");
        Validate.notNull(host, "Host is null.");
        Validate.isTrue(port > 0, "Port must be greater than zero.");
        Validate.isTrue(poolSize > 0, "Pool size must be greater than zero.");
        Validate.isTrue(connectionTimeout >= 250, "Connection timeout must be greater or equals to 250.");
        Validate.notNull(logger, "Logger is null.");
        Validate.notNull(manager, "LibraryManager is null.");

        classLoader = new IsolatedClassLoader();
        classLoader.addPath(manager.downloadLibrary(Library.builder().groupId("org.slf4j").artifactId("slf4j-api").version("1.7.31").checksum("zahiqmloOsBy7ir9C5PQcM6rKR71uFfTGLJMFZWza4o=").build()));
        classLoader.addPath(manager.downloadLibrary(Library.builder().groupId("org.slf4j").artifactId("slf4j-simple").version("1.7.31").checksum("AHwNOZ4Tefv4YkfDCkLRDWGAlBRo9Ky+mu4Uu2Qqwdw=").build()));
        classLoader.addPath(manager.downloadLibrary(Library.builder().groupId("com.zaxxer").artifactId("HikariCP").version("4.0.3").checksum("fAJK7/HBBjV210RTUT+d5kR9jmJNF/jifzCi6XaIxsk=").build()));

        Class<?> hikariConfig = classLoader.loadClass("com.zaxxer.hikari.HikariConfig");
        Class<?> hikariDataSource = classLoader.loadClass("com.zaxxer.hikari.HikariDataSource");

        close = hikariDataSource.getDeclaredMethod("close");

        Properties props = new Properties();
        props.put("jdbcUrl", "jdbc:mysql://" + host + ":" + port + '/' + databaseName);
        props.put("driverClassName", "com.mysql.jdbc.Driver");
        props.put("username", username);
        props.put("password", password);
        props.put("minimumIdle", poolSize);
        props.put("maximumPoolSize", poolSize);
        props.put("connectionTimeout", connectionTimeout);
        props.put("poolName", "UltimateAdvancementAPI");
        props.put("dataSource.useSSL", false);
        props.put("dataSource.cachePrepStmts", true);
        props.put("dataSource.prepStmtCacheSize", 250);
        props.put("dataSource.prepStmtCacheSqlLimit", 2048);
        props.put("dataSource.useServerPrepStmts", true);
        props.put("dataSource.useLocalSessionState", true);
        props.put("dataSource.rewriteBatchedStatements", true);
        props.put("dataSource.cacheResultSetMetadata", true);
        props.put("dataSource.cacheServerConfiguration", true);
        props.put("dataSource.maintainTimeStats", false);

        Object config = hikariConfig.getConstructor(Properties.class).newInstance(props);
        this.dataSource = (DataSource) hikariDataSource.getConstructor(hikariConfig).newInstance(config);
        // Test connection
        // noinspection EmptyTryBlock - disable intellij warning
        try (Connection ignored = openConnection()) {
        } catch (SQLException e) {
            throw new SQLException("An exception occurred while testing the established connection.", e);
        }
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws SQLException {
        try (Connection conn = openConnection(); Statement statement = conn.createStatement()) {
            statement.addBatch("CREATE TABLE IF NOT EXISTS `Teams` (`ID` INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT) DEFAULT CHARSET = utf8mb4;");
            statement.addBatch("CREATE TABLE IF NOT EXISTS `Players` (`UUID` VARCHAR(36) NOT NULL, `Name` VARCHAR(16) NOT NULL, `TeamID` INTEGER NOT NULL, PRIMARY KEY(`UUID`), FOREIGN KEY(`TeamID`) REFERENCES `Teams`(`ID`) ON DELETE CASCADE ON UPDATE CASCADE) DEFAULT CHARSET = utf8mb4;");
            statement.addBatch("CREATE TABLE IF NOT EXISTS `Advancements` (`Namespace` VARCHAR(127) NOT NULL, `Key` VARCHAR(127) NOT NULL, `TeamID` INTEGER NOT NULL, `Criteria` INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(`Namespace`,`Key`,`TeamID`), FOREIGN KEY(`TeamID`) REFERENCES `Teams`(`ID`) ON DELETE CASCADE ON UPDATE CASCADE) DEFAULT CHARSET = utf8mb4;");
            statement.addBatch("CREATE TABLE IF NOT EXISTS `Unredeemed` (`Namespace` VARCHAR(127) NOT NULL, `Key` VARCHAR(127) NOT NULL, `TeamID` INTEGER NOT NULL, `GiveRewards` INTEGER NOT NULL, PRIMARY KEY(`Namespace`,`Key`,`TeamID`), FOREIGN KEY(`Namespace`, `Key`, `TeamID`) REFERENCES `Advancements`(`Namespace`, `Key`, `TeamID`) ON DELETE CASCADE ON UPDATE CASCADE) DEFAULT CHARSET = utf8mb4;");
            statement.executeBatch();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Connection openConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws SQLException {
        try {
            close.invoke(dataSource);
        } catch (ReflectiveOperationException e) {
            throw new SQLException("Cannot close HikariDataSource.", e);
        } finally {
            try {
                classLoader.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTeamId(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException {
        try (Connection conn = openConnection(); PreparedStatement ps = conn.prepareStatement("SELECT `TeamID` FROM `Players` WHERE `UUID`=?;")) {
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
        try (Connection conn = openConnection()) {
            return getTeamMembers(conn, teamId);
        }
    }

    private List<UUID> getTeamMembers(Connection connection, int teamId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT `UUID` FROM `Players` WHERE `TeamID`=?;")) {
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
        try (Connection conn = openConnection()) {
            return getTeamAdvancements(conn, teamId);
        }
    }

    private Map<AdvancementKey, Integer> getTeamAdvancements(Connection connection, int teamId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT `Namespace`,`Key`,`Criteria` FROM `Advancements` WHERE `TeamID`=?;")) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Entry<TeamProgression, Boolean> loadOrRegisterPlayer(@NotNull UUID uuid, @NotNull String name) throws SQLException {
        int teamId;
        ResultSet r;
        try (Connection conn = openConnection()) {
            try (PreparedStatement psTeamId = conn.prepareStatement("SELECT `TeamID` FROM `Players` WHERE `UUID`=?;")) {
                psTeamId.setString(1, uuid.toString());

                r = psTeamId.executeQuery();
                if (!r.next()) { // Player isn't registered
                    try (PreparedStatement psInsert = conn.prepareStatement("INSERT INTO `Teams` () VALUES ();", Statement.RETURN_GENERATED_KEYS); PreparedStatement psInsertPl = conn.prepareStatement("INSERT INTO `Players` (`UUID`, `Name`, `TeamID`) VALUES (?, ?, ?);")) {
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
            List<UUID> list = getTeamMembers(conn, teamId);
            Map<AdvancementKey, Integer> map = getTeamAdvancements(conn, teamId);
            return new SimpleEntry<>(new TeamProgression(map, teamId, list), false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TeamProgression loadUUID(@NotNull UUID uuid) throws SQLException, UserNotRegisteredException {
        int teamID = Integer.MIN_VALUE;
        List<UUID> list = new LinkedList<>();
        try (Connection conn = openConnection()) {
            try (PreparedStatement psTeamId = conn.prepareStatement("SELECT `UUID`, `TeamID` FROM `Players` WHERE `TeamID`=(SELECT `TeamID` FROM `Players` WHERE `UUID`=? LIMIT 1);")) {
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

            try (PreparedStatement psAdv = conn.prepareStatement("SELECT `Namespace`, `Key`, `Criteria` FROM `Advancements` WHERE `TeamID`=?;")) {
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateAdvancement(@NotNull AdvancementKey key, int teamId, @Range(from = 0, to = Integer.MAX_VALUE) int criteria) throws SQLException {
        try (Connection conn = openConnection()) {
            if (criteria <= 0) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM `Advancements` WHERE `Namespace`=? AND `Key`=? AND `TeamID`=?;")) {
                    ps.setString(1, key.getNamespace());
                    ps.setString(2, key.getKey());
                    ps.setInt(3, teamId);
                    ps.execute();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO `Advancements` (`Namespace`, `Key`, `TeamID`, `Criteria`) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE `Criteria`=VALUES(`Criteria`);")) {
                    ps.setString(1, key.getNamespace());
                    ps.setString(2, key.getKey());
                    ps.setInt(3, teamId);
                    ps.setInt(4, criteria);
                    ps.execute();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Entry<AdvancementKey, Boolean>> getUnredeemed(int teamId) throws SQLException {
        try (Connection conn = openConnection(); PreparedStatement ps = conn.prepareStatement("SELECT `Namespace`, `Key`, `GiveRewards` FROM `Unredeemed` WHERE `TeamID`=?;")) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUnredeemed(@NotNull AdvancementKey key, boolean giveRewards, int teamId) throws SQLException {
        try (Connection conn = openConnection(); PreparedStatement ps = conn.prepareStatement("INSERT IGNORE INTO `Unredeemed` (`Namespace`, `Key`, `TeamID`, `GiveRewards`) VALUES (?, ?, ?, ?);")) {
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
        try (Connection conn = openConnection(); PreparedStatement ps = conn.prepareStatement("SELECT Count(*) FROM `Unredeemed` WHERE `Namespace`=? AND `Key`=? AND `TeamID`=?;")) {
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
        try (Connection conn = openConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM `Unredeemed` WHERE `Namespace`=? AND `Key`=? AND `TeamID`=?;")) {
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
        try (Connection conn = openConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM `Unredeemed` WHERE `Namespace`=? AND `Key`=? AND `TeamID`=?;")) {
            for (Entry<AdvancementKey, ?> key : keyList) {
                ps.setString(1, key.getKey().getNamespace());
                ps.setString(2, key.getKey().getKey());
                ps.setInt(3, teamId);
                ps.execute();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterPlayer(@NotNull UUID uuid) throws SQLException {
        try (Connection conn = openConnection(); PreparedStatement stDelete = conn.prepareStatement("DELETE FROM `Players` WHERE `UUID`=?;")) {
            stDelete.setString(1, uuid.toString());
            stDelete.execute();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void movePlayer(@NotNull UUID uuid, int newTeamId) throws SQLException {
        try (Connection conn = openConnection()) {
            movePlayer(conn, uuid, newTeamId);
        }
    }

    private void movePlayer(Connection connection, @NotNull UUID uuid, int newTeamId) throws SQLException {
        try (PreparedStatement stUpdate = connection.prepareStatement("UPDATE `Players` SET `TeamID`=? WHERE `UUID`=?;")) {
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
        try (Connection conn = openConnection(); PreparedStatement psInsert = conn.prepareStatement("INSERT INTO `Teams` () VALUES ();")) {
            psInsert.executeUpdate();
            ResultSet r = psInsert.getGeneratedKeys();
            if (!r.next()) {
                throw new SQLException("Cannot insert default values into Teams table.");
            }
            int teamId = r.getInt(1);
            movePlayer(conn, uuid, teamId);
            return new TeamProgression(teamId, uuid);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UUID> getPlayersByName(@NotNull String name) throws SQLException {
        try (Connection conn = openConnection(); PreparedStatement ps = conn.prepareStatement("SELECT `UUID` FROM `Players` WHERE `Name`=?;")) {
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
        try (Connection conn = openConnection(); PreparedStatement ps = conn.prepareStatement("SELECT `Name` FROM `Players` WHERE `UUID`=? LIMIT 1;")) {
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
        try (Connection conn = openConnection(); PreparedStatement ps = conn.prepareStatement("UPDATE `Players` SET `Name`=? WHERE `UUID`=?;")) {
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
        try (Connection conn = openConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM `Teams` WHERE `ID` NOT IN (SELECT `TeamID` FROM `Players` GROUP BY `TeamID`);")) {
            ps.execute();
        }
    }
}
