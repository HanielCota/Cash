package com.github.hanielcota.cash.domain;

import org.bukkit.entity.Player;

import java.util.List;

public interface EconomyRepository {
    PlayerAccount findPlayerAccount(Player player);
    void savePlayerAccount(PlayerAccount playerAccount);

    List<PlayerAccount> getTopPlayers(int limit);
}