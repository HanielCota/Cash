package com.github.hanielcota.cash.listeners;

import com.github.hanielcota.cash.domain.EconomyService;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@AllArgsConstructor
public class PlayerJoinEventListener implements Listener {

    private final EconomyService economyService;
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (economyService.getBalance(player) <= 0) {
            double initialBalance = 0;
            economyService.deposit(player, initialBalance);
        }
    }
}
