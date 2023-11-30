package com.github.hanielcota.cash.view;

import com.github.hanielcota.cash.domain.EconomyService;
import com.github.hanielcota.cash.domain.PlayerAccount;
import com.github.hanielcota.cash.infra.MySQLEconomyRepository;
import com.github.hanielcota.cash.view.menu.CashMenu;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
public class CashView {

    private final EconomyService economyService;
    private final MenuItemFactory menuItemFactory;
    private final MySQLEconomyRepository economyRepository;
    private final GiftCooldownCache giftCooldownCache = GiftCooldownCache.getInstance();
    private final Cache<List<PlayerAccount>> topPlayersCache = new Cache<>(TimeUnit.MINUTES.toMillis(30));

    public CashMenu createCashView(Player player) {
        CashMenu playerCashMenu = new CashMenu();
        addCommonItems(playerCashMenu, player);
        return playerCashMenu;
    }

    private void addCommonItems(CashMenu playerCashMenu, Player player) {
        playerCashMenu.setItem(3, menuItemFactory.createInformationItem());
        playerCashMenu.setItem(4, menuItemFactory.createSkullItem(player, economyService));

        if (!canCollectGift(player)) {
            playerCashMenu.setItem(5, menuItemFactory.createGrayGiftItem());
            return;
        }

        playerCashMenu.setItem(5, menuItemFactory.createGiftItem(), click -> collectGift(player, playerCashMenu));
        updateTopPlayers(playerCashMenu, player);
    }

    private boolean canCollectGift(Player player) {
        Optional<Long> lastCollectedTimeOpt = giftCooldownCache.getLastCollectedTime(player.getName());

        if (lastCollectedTimeOpt.isEmpty()) {
            return true;
        }

        long lastCollectedTime = lastCollectedTimeOpt.get();
        long elapsedTimeMillis = System.currentTimeMillis() - lastCollectedTime;
        long cooldownMillis = TimeUnit.SECONDS.toMillis(GiftCooldownCache.COOLDOWN_SECONDS);

        return elapsedTimeMillis >= cooldownMillis;
    }

    private void collectGift(Player player, CashMenu playerCashMenu) {
        player.sendMessage(
                "", "§aVocê coletou o presente com sucesso.", "§aEm 30 minutos, você poderá coletar novamente.", "");
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
        playerCashMenu.setItem(5, menuItemFactory.createGrayGiftItem());
        giftCooldownCache.updateLastCollectedTime(player.getName());
    }

    private void updateTopPlayers(CashMenu playerCashMenu, Player player) {
        List<PlayerAccount> topPlayers = topPlayersCache.get(() -> economyRepository.getTopPlayers(5));
        int startSlot = 20;

        ItemStack barrierItem = menuItemFactory.createBarrierItem();

        for (int i = 0; i < 5; i++) {
            playerCashMenu.setItem(startSlot + i, barrierItem);
        }

        for (int i = 0; i < Math.min(topPlayers.size(), 5); i++) {
            PlayerAccount playerAccount = topPlayers.get(i);
            ItemStack topPlayerItem = menuItemFactory.createTopPlayerItem(playerAccount, player, i + 1);

            if (playerAccount != null) {
                playerCashMenu.setItem(startSlot + i, topPlayerItem);
            }
        }
    }

    private static class Cache<V> {
        private final long expirationMillis;
        private long lastUpdateTime;
        private V cachedValue;

        public Cache(long expirationMillis) {
            this.expirationMillis = expirationMillis;
        }

        public V get(CacheLoader<V> loader) {
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
