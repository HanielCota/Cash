package com.github.hanielcota.cash.usecase;

import com.github.hanielcota.cash.domain.EconomyRepository;
import com.github.hanielcota.cash.domain.EconomyService;
import com.github.hanielcota.cash.domain.PlayerAccount;
import com.github.hanielcota.cash.utils.NumberFormatter;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class EconomyServiceImpl implements EconomyService {

    private final EconomyRepository economyRepository;

    @Override
    public double getBalance(Player player) {
        PlayerAccount playerAccount = getOrCreatePlayerAccount(player);
        return playerAccount.getBalance();
    }

    @Override
    public String getBalanceAbbreviated(Player player) {
        PlayerAccount playerAccount = getOrCreatePlayerAccount(player);
        return NumberFormatter.formatAbbreviatedThreadSafe(playerAccount.getBalance());
    }

    @Override
    public void deposit(Player player, double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Deposit amount must be non-negative.");
        }

        PlayerAccount playerAccount = getOrCreatePlayerAccount(player);
        double newBalance = playerAccount.getBalance() + amount;
        playerAccount.setBalance(newBalance);
        economyRepository.savePlayerAccount(playerAccount);
    }

    @Override
    public void withdraw(Player player, double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Withdrawal amount must be non-negative.");
        }

        PlayerAccount playerAccount = getOrCreatePlayerAccount(player);

        if (playerAccount.getBalance() < amount) {
            return;
        }

        double newBalance = playerAccount.getBalance() - amount;
        playerAccount.setBalance(newBalance);
        economyRepository.savePlayerAccount(playerAccount);
    }

    @Override
    public void setBalance(Player player, double amount) {
        PlayerAccount playerAccount = getOrCreatePlayerAccount(player);
        playerAccount.setBalance(amount);
        economyRepository.savePlayerAccount(playerAccount);
    }

    private PlayerAccount getOrCreatePlayerAccount(Player player) {
        PlayerAccount playerAccount = economyRepository.findPlayerAccount(player);

        if (playerAccount == null) {
            playerAccount = new PlayerAccount(player.getName(), 0);
            economyRepository.savePlayerAccount(playerAccount);
        }

        return playerAccount;
    }
}
