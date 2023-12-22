package com.github.hanielcota.cash.infra;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.hanielcota.cash.domain.EconomyRepository;
import com.github.hanielcota.cash.domain.PlayerAccount;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class InMemoryEconomyRepository implements EconomyRepository {
    private final Cache<String, PlayerAccount> cache =
            Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    @Override
    public PlayerAccount findPlayerAccount(Player player) {
        String playerId = player.getName();
        PlayerAccount cachedAccount = cache.getIfPresent(playerId);

        return cachedAccount != null ? cachedAccount : getDefaultPlayerAccount(playerId);
    }

    @Override
    public void savePlayerAccount(PlayerAccount playerAccount) {
        String playerUUID = playerAccount.getPlayerId();
        cache.put(playerUUID, playerAccount);
    }

    @Override
    public List<PlayerAccount> getTopPlayers(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("O limite deve ser um valor não negativo");
        }

        return cache.asMap().values().stream().limit(limit).toList();
    }


    private PlayerAccount getDefaultPlayerAccount(String playerId) {
        return new PlayerAccount(playerId, 0.0);
    }
}
