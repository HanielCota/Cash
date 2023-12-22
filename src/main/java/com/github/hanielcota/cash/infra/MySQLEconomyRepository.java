package com.github.hanielcota.cash.infra;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.hanielcota.cash.domain.CashTransaction;
import com.github.hanielcota.cash.domain.EconomyRepository;
import com.github.hanielcota.cash.domain.PlayerAccount;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@AllArgsConstructor
@Slf4j
public class MySQLEconomyRepository implements EconomyRepository {

    private final HikariDataSource dataSource;
    private final Cache<String, PlayerAccount> cache;
    private final Cache<String, List<PlayerAccount>> topPlayersCache;

    public MySQLEconomyRepository(String jdbcUrl, String username, String password) {
        this.dataSource = createDataSource(jdbcUrl, username, password);
        this.cache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
        this.topPlayersCache = Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
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
                if (resultSet.next()) {
                    PlayerAccount playerAccount = new PlayerAccount(playerId, resultSet.getDouble("balance"));
                    cache.put(playerId, playerAccount);
                    return playerAccount;
                }
            }
        } catch (SQLException e) {
            log.error("Error finding player account for player: {}", player.getName(), e);
        }

        return null;
    }

    @Override
    public void savePlayerAccount(PlayerAccount playerAccount) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("REPLACE INTO cash (player_id, balance) VALUES (?, ?)")) {
             statement.setString(1, playerAccount.getPlayerId());
             statement.setDouble(2, playerAccount.getBalance());
             statement.executeUpdate();

            // Update the cache
            cache.put(playerAccount.getPlayerId(), playerAccount);

            saveCashTransaction(playerAccount.getPlayerId(), playerAccount.getBalance());

        } catch (SQLException e) {
            log.error("Error saving player account for player: {}", playerAccount.getPlayerId(), e);
        }
    }

    @Override
    public List<PlayerAccount> getTopPlayers(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must be a non-negative value.");
        }

        String cacheKey = String.format("topPlayers_%d", limit);
        List<PlayerAccount> topPlayers = topPlayersCache.getIfPresent(cacheKey);

        if (topPlayers != null) {
            return adjustListSize(topPlayers, limit);
        }

        topPlayers = retrieveTopPlayersFromCache(limit);
        topPlayersCache.put(cacheKey, topPlayers);
        return adjustListSize(topPlayers, limit);
    }

    private List<PlayerAccount> retrieveTopPlayersFromCache(int limit) {
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
            log.error("Error getting top players from the database", e);
        }

        return topPlayers;
    }

    private void saveCashTransaction(String playerId, double amount) {
        try (Connection connection = dataSource.getConnection();
            PreparedStatement statement = connection.prepareStatement("INSERT INTO cash_transaction (player_id, amount) VALUES (?, ?)")) {
            statement.setString(1, playerId);
            statement.setDouble(2, amount);
            statement.executeUpdate();
        } catch (SQLException e) {
            log.error("Error saving cash transaction for player: {}", playerId, e);
        }
    }

    public List<CashTransaction> getLastCashTransactions(String playerId, int limit) {
        List<CashTransaction> transactions = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT amount, transaction_time FROM cash_transaction WHERE player_id = ? ORDER BY transaction_time DESC LIMIT ?")) {
            statement.setString(1, playerId);
            statement.setInt(2, limit);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    double amount = resultSet.getDouble("amount");
                    Timestamp transactionTime = resultSet.getTimestamp("transaction_time");
                    CashTransaction transaction = new CashTransaction(amount, transactionTime.toLocalDateTime());
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            log.error("Error getting last cash transactions for player: {}", playerId, e);
        }

        return transactions;
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
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            // Create 'cash' table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS cash ("
                    + "player_id VARCHAR(36) PRIMARY KEY,"
                    + "balance DOUBLE NOT NULL)");

            // Create 'cash_transaction' table
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS cash_transaction ("
                    + "transaction_id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "player_id VARCHAR(36),"
                    + "amount DOUBLE NOT NULL,"
                    + "transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        } catch (SQLException e) {
            log.error("Error initializing the database", e);
        }
    }

}
