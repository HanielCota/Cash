package com.github.hanielcota.cash.view;

import com.github.hanielcota.cash.domain.EconomyService;
import com.github.hanielcota.cash.domain.PlayerAccount;
import com.github.hanielcota.cash.infra.MySQLEconomyRepository;
import com.github.hanielcota.cash.view.items.MenuItemFactory;
import com.github.hanielcota.cash.view.menu.CashTopPlayersMenu;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
public class CashTopView {

    private final EconomyService economyService;
    private final MenuItemFactory menuItemFactory;
    private final MySQLEconomyRepository economyRepository;
    private final Cache<List<PlayerAccount>> topPlayersCache = new Cache<>(TimeUnit.MINUTES.toMillis(30));

    public CashTopPlayersMenu createTopCashMenu(Player player) {
        CashTopPlayersMenu menu = new CashTopPlayersMenu();

        updateTopPlayers(menu, player);
        return menu;
    }

    private void updateTopPlayers(CashTopPlayersMenu topPlayersMenu, Player player) {
        List<PlayerAccount> topPlayers = topPlayersCache.get(() -> economyRepository.getTopPlayers(5));
        int startSlot = 11;

        ItemStack barrierItem = menuItemFactory.createBarrierItem();

        for (int i = 0; i < 5; i++) {
            topPlayersMenu.setItem(startSlot + i, barrierItem);
        }

        for (int i = 0; i < Math.min(topPlayers.size(), 5); i++) {
            PlayerAccount playerAccount = topPlayers.get(i);
            ItemStack topPlayerItem = menuItemFactory.createTopPlayerItem(playerAccount, playerAccount.getPlayerId(), i + 1);
            topPlayersMenu.setItem(startSlot + i, topPlayerItem);
        }
        topPlayersMenu.setItem(27, menuItemFactory.createBackItem(), click -> {
            new CashView(economyService, menuItemFactory, economyRepository)
                    .createCashView(player)
                    .open(player);
        });
    }


    private static class Cache<V> {
        private final long expirationMillis;
        private long lastUpdateTime;
        private V cachedValue;

        public Cache(long expirationMillis) {
            this.expirationMillis = expirationMillis;
        }

        public V get(CashTopView.CacheLoader<V> loader) {
            if (System.currentTimeMillis() - lastUpdateTime > expirationMillis) {
                cachedValue = loader.load();
                lastUpdateTime = System.currentTimeMillis();
            }
            return cachedValue;
        }
    }

    private interface CacheLoader<V> {
        V load();
    }
}
