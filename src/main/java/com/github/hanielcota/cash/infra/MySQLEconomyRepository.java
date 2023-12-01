package com.github.hanielcota.cash.infra;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.hanielcota.cash.domain.EconomyRepository;
import com.github.hanielcota.cash.domain.PlayerAccount;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
public class MySQLEconomyRepository implements EconomyRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MySQLEconomyRepository.class);

    private final HikariDataSource dataSource;
    private final Cache<String, PlayerAccount> cache;
    private final Cache<String, List<PlayerAccount>> topPlayersCache;

    public MySQLEconomyRepository(String jdbcUrl, String username, String password) {
        this.dataSource = createDataSource(jdbcUrl, username, password);
        this.cache =
                Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
        this.topPlayersCache =
                Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
        initializeDatabase();
    }

    @Override
    public PlayerAccount findPlayerAccount(Player player) {
        String playerId = player.getName();
        PlayerAccount cachedAccount = cache.getIfPresent(playerId);

        if (cachedAccount != null) {
            return cachedAccount;
        }

        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT player_id, balance FROM cash WHERE player_id = ?")) {
            statement.setString(1, playerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet != null && resultSet.next()) {
                    PlayerAccount playerAccount = new PlayerAccount(playerId, resultSet.getDouble("balance"));
                    cache.put(playerId, playerAccount);
                    return playerAccount;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error finding player account for player: {}", player.getName(), e);
        }

        return null;
    }

    @Override
    public void savePlayerAccount(PlayerAccount playerAccount) {
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement("REPLACE INTO cash (player_id, balance) VALUES (?, ?)")) {
            statement.setString(1, playerAccount.getPlayerId());
            statement.setDouble(2, playerAccount.getBalance());
            statement.executeUpdate();

            // Update the cache
            cache.put(playerAccount.getPlayerId(), playerAccount);
        } catch (SQLException e) {
            LOGGER.error("Error saving player account for player: {}", playerAccount.getPlayerId(), e);
        }
    }

    @Override
    public List<PlayerAccount> getTopPlayers(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must be a non-negative value.");
        }

        String cacheKey = String.format("topPlayers_%d", limit);
        List<PlayerAccount> topPlayers = topPlayersCache.getIfPresent(cacheKey);

        if (topPlayers == null) {
            topPlayers = retrieveTopPlayersFromDatabase(limit);
            topPlayersCache.put(cacheKey, topPlayers);
        }

        return adjustListSize(topPlayers, limit);
    }

    private List<PlayerAccount> retrieveTopPlayersFromDatabase(int limit) {
        List<PlayerAccount> topPlayers = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT player_id, balance FROM cash ORDER BY balance DESC LIMIT ?")) {
            statement.setInt(1, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String uniqueId = resultSet.getString("player_id");
                    double balance = resultSet.getDouble("balance");
                    PlayerAccount playerAccount = new PlayerAccount(uniqueId, balance);
                    topPlayers.add(playerAccount);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error getting top players from the database", e);
            return Collections.emptyList();
        }

        return topPlayers;
    }

    private List<PlayerAccount> adjustListSize(List<PlayerAccount> list, int limit) {
        return Collections.unmodifiableList(list.subList(0, Math.min(list.size(), limit)));
    }

    private HikariDataSource createDataSource(String jdbcUrl, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }

    private void initializeDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS cash " +
                                "(player_id VARCHAR(36) " +
                                "PRIMARY KEY," +
                                "balance DOUBLE NOT NULL)")) {
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error initializing the database", e);
        }
    }
}
