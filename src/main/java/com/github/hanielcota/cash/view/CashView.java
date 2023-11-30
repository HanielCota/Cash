package com.github.hanielcota.cash.view;

import com.github.hanielcota.cash.domain.EconomyService;
import com.github.hanielcota.cash.infra.MySQLEconomyRepository;
import com.github.hanielcota.cash.utils.ClickMessage;
import com.github.hanielcota.cash.view.cache.GiftCooldownCache;
import com.github.hanielcota.cash.view.items.MenuItemFactory;
import com.github.hanielcota.cash.view.menu.CashMenu;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
public class CashView {

    private final EconomyService economyService;
    private final MenuItemFactory menuItemFactory;
    private final MySQLEconomyRepository economyRepository;
    private final GiftCooldownCache giftCooldownCache = GiftCooldownCache.getInstance();

    public CashMenu createCashView(Player player) {
        CashMenu playerCashMenu = new CashMenu();
        addCommonItems(playerCashMenu, player);
        return playerCashMenu;
    }

    private void addCommonItems(CashMenu playerCashMenu, Player player) {
        playerCashMenu.setItem(22, menuItemFactory.createInformationItem());
        playerCashMenu.setItem(4, menuItemFactory.createSkullItem(player, economyService));

        playerCashMenu.setItem(21, menuItemFactory.createRedirectURLItem(), click -> redirectURL(player));

        playerCashMenu.setItem(19, menuItemFactory.createTopItem(), click -> {
            new CashTopView(economyService, menuItemFactory, economyRepository)
                    .createTopCashMenu(player)
                    .open(player);
        });

        if (!canCollectGift(player)) {
            playerCashMenu.setItem(24, menuItemFactory.createGrayGiftItem());
            return;
        }

        playerCashMenu.setItem(24, menuItemFactory.createGiftItem(), click -> collectGift(player, playerCashMenu));
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
                "",
                "§aVocê coletou o presente com sucesso.",
                "§aEm 30 minutos, você poderá coletar novamente.",
                "");
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 10f, 10f);
        playerCashMenu.setItem(24, menuItemFactory.createGrayGiftItem());
        giftCooldownCache.updateLastCollectedTime(player.getName());
        economyService.deposit(player, 30);
    }

    private void redirectURL(Player player) {
        player.sendMessage("");

        new ClickMessage("§aClique ")
                .then("§a§lAQUI")
                .click(ClickEvent.Action.OPEN_URL, "http://localhost")
                .then("§a para comprar cash.")
                .send(player);

        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 10f, 10f);
    }

}
