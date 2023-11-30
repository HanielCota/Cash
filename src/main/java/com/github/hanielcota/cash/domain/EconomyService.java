package com.github.hanielcota.cash.domain;

import org.bukkit.entity.Player;

public interface EconomyService {
    double getBalance(Player player);
    String getBalanceAbbreviated(Player player);
    void deposit(Player player, double amount);
    void withdraw(Player player, double amount);
    void setBalance(Player player, double amount);

}