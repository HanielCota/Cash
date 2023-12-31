package com.github.hanielcota.cash.view.items;

import com.github.hanielcota.cash.domain.EconomyService;
import com.github.hanielcota.cash.domain.PlayerAccount;
import com.github.hanielcota.cash.utils.ItemBuilder;
import com.github.hanielcota.cash.utils.NumberFormatter;
import com.github.hanielcota.cash.utils.SkullUrl;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MenuItemFactory {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");


    public ItemStack createGiftItem() {
        return new ItemBuilder(SkullUrl.getSKULL_URL())
                .setName("§aPresente de Cash")
                .setLore(
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
                .setName("§cPresente Expirado")
                .setLore(
                        "§7Você já coletou este presente.",
                        "§7Aguarde alguns minutos para coletar novamente.",
                        "§7Surpresas esperam por você!",
                        "§7Torne seu dia mais especial."
                )
                .build();
    }


    public ItemStack createBarrierItem() {
        return new ItemBuilder(Material.BARRIER)
                .setName("§cNinguém")
                .setLore("§7Ninguém ocupou esta vaga.")
                .build();
    }

    public ItemStack createTopItem() {
        return new ItemBuilder(Material.RAW_GOLD)
                .setName("§aRanking de Cash")
                .setLore(
                        "§7Veja os jogadores mais ricos do servidor.",
                        "§7Descubra quem lidera a economia!",
                        "§7O ranking é atualizado regularmente.",
                        "§7Acumule riquezas para subir nas posições."
                )
                .build();
    }

    public ItemStack createBackItem() {
        return new ItemBuilder(Material.SPECTRAL_ARROW)
                .setName("§cVoltar")
                .setLore("§7Clique para retornar ao menu anterior.")
                .build();
    }

    public ItemStack createTopPlayerItem(PlayerAccount playerAccount, String playerName, int position) {
        if (playerAccount == null || playerAccount.getPlayerId() == null) {
            return new ItemStack(Material.AIR);
        }

        double balance = playerAccount.getBalance();

        return new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§a" + playerName)
                .setLore(
                        "§7Colocação: #" + position,
                        "§7Saldo: §e" + NumberFormatter.formatAbbreviatedThreadSafe(balance))
                .setSkullOwner(playerName)
                .build();
    }

    public ItemStack createTransactionItem(double amount, LocalDateTime transactionTime) {
        String formattedAmount = NumberFormatter.formatAbbreviatedThreadSafe(amount);
        String formattedTransactionTime = transactionTime.format(DATE_TIME_FORMATTER);

        return new ItemBuilder(Material.PAPER)
                .setName("§aTransação de Cash")
                .setLore(
                        "§7Valor: §a" + formattedAmount,
                        "§7Data: " + formattedTransactionTime)
                .build();
    }
    public ItemStack createRedirectTransactionHistoric() {
        return new ItemBuilder(Material.WRITABLE_BOOK)
                .setName("§aHistórico de Transação")
                .setLore(
                        "§7Confira seu histórico de transação de cash.")
                .build();
    }

    public ItemStack createRedirectURLItem() {
        return new ItemBuilder(Material.EMERALD)
                .setName("§aComprar Cash")
                .setLore(
                        "§7Clique aqui para ir à nossa loja.",
                        "",
                        "§7Onde você consegue comprar",
                        "§7Cash para desfrutar de toda",
                        "§7as vantagens proporcionadas por ele.")
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
