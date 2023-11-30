package com.github.hanielcota.cash.view;

import com.github.hanielcota.cash.domain.EconomyService;
import com.github.hanielcota.cash.domain.PlayerAccount;
import com.github.hanielcota.cash.utils.ItemBuilder;
import com.github.hanielcota.cash.utils.NumberFormatter;
import com.github.hanielcota.cash.utils.SkullUrl;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class MenuItemFactory {

    public ItemStack createGiftItem() {
        return new ItemBuilder(SkullUrl.getSKULL_URL())
                .setName("§aPresente de Cash")
                .setLore(
                        "",
                        "§7Ao ficar online no servidor,",
                        "§7por 30 minutos, você recebe",
                        "§7um presente misterioso que pode ser coletado.",
                        "§7Um pequeno mimo por jogar em nosso servidor.",
                        "",
                        "§aClique para coletar o seu presente.")
                .build();
    }

    public ItemStack createGrayGiftItem() {
        return new ItemBuilder(SkullUrl.getSKULL_GIFT_OPEN_GRAY())
                .setName("§cOps!")
                .setLore("§7Você já coletou o presente hoje.")
                .build();
    }

    public ItemStack createBarrierItem() {
        return new ItemBuilder(Material.BARRIER)
                .setName("§cNinguém")
                .setLore("§7Ninguém ocupou esta vaga.")
                .build();
    }

    public ItemStack createTopPlayerItem(PlayerAccount playerAccount, Player player, int position) {
        if (playerAccount == null || playerAccount.getPlayerId() == null) {
            return new ItemStack(Material.AIR);
        }

        double balance = playerAccount.getBalance();

        return new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§a" + player.getName())
                .setLore(
                        "§7Colocação: #" + position,
                        "§7Saldo: §e" + NumberFormatter.formatAbbreviatedThreadSafe(balance))
                .setSkullOwner(player.getName())
                .build();
    }

    public ItemStack createInformationItem() {
        return new ItemBuilder(Material.GLOBE_BANNER_PATTERN)
                .setName("§aInformações")
                .setLore(
                        "§7O Top Cash atualiza a cada 30 minutos,",
                        "§7junto com ele, o sistema",
                        "§7de bonificação do servidor em relação ao cash.",
                        "",
                        "§7Então, não fique de fora e fique atento ao tempo.")
                .addItemFlag(ItemFlag.HIDE_ITEM_SPECIFICS)
                .build();
    }

    public ItemStack createSkullItem(Player player, EconomyService economyService) {
        return new ItemBuilder(org.bukkit.Material.PLAYER_HEAD)
                .setSkullOwner(player.getName())
                .setName("§aSeu cash")
                .setLore("§7Atualmente, você possui: " + economyService.getBalanceAbbreviated(player))
                .build();
    }
}
