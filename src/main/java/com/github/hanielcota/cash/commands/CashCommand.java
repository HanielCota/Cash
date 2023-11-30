package com.github.hanielcota.cash.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.github.hanielcota.cash.domain.EconomyService;
import com.github.hanielcota.cash.infra.MySQLEconomyRepository;
import com.github.hanielcota.cash.view.CashView;
import com.github.hanielcota.cash.view.items.MenuItemFactory;
import com.github.hanielcota.cash.view.menu.CashMenu;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("cash")
@AllArgsConstructor
public class CashCommand extends BaseCommand {

    private final EconomyService economyService;
    private final MenuItemFactory menuItemFactory;
    private final MySQLEconomyRepository economyRepository;

    @Default
    private void onCash(Player player, String[] args) {
        if (args.length == 0) {
            CashMenu cashView = new CashView(economyService, menuItemFactory, economyRepository).createCashView(player);
            cashView.open(player);
            return;
        }
        player.sendMessage("§aSeu cash " + economyService.getBalanceAbbreviated(player));
    }

    @Subcommand("set")
    @CommandCompletion("@players")
    private void onSetCashCommand(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso incorreto! Utilize: /cash set <jogador> <quantidade>");
            return;
        }

        String targetPlayerName = args[0];
        int amount;

        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cA quantidade deve ser um número inteiro válido.");
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            sender.sendMessage("§cO jogador especificado não está online ou não existe.");
            return;
        }

        if (amount < 0) {
            sender.sendMessage("§cO valor não pode ser negativo.");
            return;
        }

        economyService.setBalance(targetPlayer, amount);
        sender.sendMessage("§aO cash de " + targetPlayer.getName() + " foi definido para: " + amount);
    }

    @Subcommand("ver")
    private void onVerCashCommand(Player sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUso incorreto! Utilize: /cash ver <jogador>");
            return;
        }

        String targetPlayerName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            sender.sendMessage("§cO jogador especificado não está online ou não existe.");
            return;
        }

        sender.sendMessage("§aCash de " + targetPlayer.getName() + ": " + economyService.getBalanceAbbreviated(targetPlayer));
    }

    @Subcommand("remove")
    @CommandCompletion("@players")
    private void onRemoveCashCommand(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso incorreto! Utilize: /cash remove <jogador> <quantidade>");
            return;
        }

        String targetPlayerName = args[0];
        int amount;

        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cA quantidade deve ser um número inteiro válido.");
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            sender.sendMessage("§cO jogador especificado não está online ou não existe.");
            return;
        }

        if (amount < 0) {
            sender.sendMessage("§cO valor não pode ser negativo.");
            return;
        }

        economyService.withdraw(targetPlayer, amount);
        sender.sendMessage("§aRemovida a quantia de " + amount + " cash de " + targetPlayer.getName());
    }

    @Subcommand("add")
    @CommandCompletion("@players")
    private void onAddCashCommand(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso incorreto! Utilize: /cash adicionar <jogador> <quantidade>");
            return;
        }

        String targetPlayerName = args[0];
        int amount;

        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cA quantidade deve ser um número inteiro válido.");
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            sender.sendMessage("§cO jogador especificado não está online ou não existe.");
            return;
        }

        if (amount < 0) {
            sender.sendMessage("§cO valor não pode ser negativo.");
            return;
        }

        economyService.deposit(targetPlayer, amount);
        sender.sendMessage("§aAdicionada a quantia de " + amount + " cash para " + targetPlayer.getName());
    }


    @Subcommand("enviar")
    @CommandCompletion("@players")
    private void onSendCashCommand(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso incorreto! Utilize: /cash enviar <jogador> <quantidade>");
            return;
        }

        String targetPlayerName = args[0];
        int amount;

        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cA quantidade deve ser um número inteiro válido.");
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);

        if (targetPlayer == null) {
            sender.sendMessage("§cO jogador especificado não está online ou não existe.");
            return;
        }

        if (amount < 0) {
            sender.sendMessage("§cO valor não pode ser negativo.");
            return;
        }

        if (economyService.getBalance(sender) < amount) {
            sender.sendMessage("§cVocê não tem cash suficiente para enviar.");
            return;
        }

        economyService.withdraw(sender, amount);
        economyService.deposit(targetPlayer, amount);

        sender.sendMessage("§aEnviada a quantia de " + amount + " cash para " + targetPlayer.getName());
        targetPlayer.sendMessage("§aRecebida a quantia de " + amount + " cash de " + sender.getName());
    }
}