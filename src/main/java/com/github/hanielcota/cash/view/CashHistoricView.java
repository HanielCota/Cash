package com.github.hanielcota.cash.view;

import com.github.hanielcota.cash.domain.CashTransaction;
import com.github.hanielcota.cash.domain.EconomyService;
import com.github.hanielcota.cash.infra.MySQLEconomyRepository;
import com.github.hanielcota.cash.view.items.MenuItemFactory;
import com.github.hanielcota.cash.view.menu.CashHistoricMenu;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@AllArgsConstructor
public class CashHistoricView {

    private final EconomyService economyService;
    private final MenuItemFactory menuItemFactory;
    private final MySQLEconomyRepository economyRepository;

    public CashHistoricMenu createHistoricMenu(Player player) {
        CashHistoricMenu menu = new CashHistoricMenu();
        populateMenuWithTransactions(menu, player.getName());
        setBackItem(menu, player);
        fillEmptySlotsWithBarrier(menu);
        return menu;
    }

    private void populateMenuWithTransactions(CashHistoricMenu menu, String playerId) {
        List<CashTransaction> transactions = economyRepository.getLastCashTransactions(playerId, 7);

        int startSlot = 10;
        int endSlot = 16;

        for (int i = 0; i < transactions.size(); i++) {
            CashTransaction transaction = transactions.get(i);
            double amount = transaction.getAmount();
            LocalDateTime transactionTime = transaction.getTransactionTime();

            int slot = startSlot + i;
            if (slot <= endSlot) {
                menu.setItem(slot, menuItemFactory.createTransactionItem(amount, transactionTime));
            }
        }
    }

    private void setBackItem(CashHistoricMenu menu, Player player) {
        final int backItemSlot = 27;
        menu.setItem(backItemSlot, menuItemFactory.createBackItem(), click -> openCashView(player));
    }

    private void openCashView(Player player) {
        new CashView(economyService, menuItemFactory, economyRepository)
                .createCashView(player)
                .open(player);
    }

    private void fillEmptySlotsWithBarrier(CashHistoricMenu menu) {
        IntStream.rangeClosed(10, 16)
                .filter(i -> menu.getInventory().getItem(i) == null)
                .forEach(i -> menu.setItem(i, menuItemFactory.createBarrierItem()));
    }
}
