package com.github.hanielcota.cash;

import co.aikar.commands.PaperCommandManager;
import com.github.hanielcota.cash.commands.CashCommand;
import com.github.hanielcota.cash.domain.EconomyService;
import com.github.hanielcota.cash.infra.MySQLEconomyRepository;
import com.github.hanielcota.cash.listeners.PlayerJoinEventListener;
import com.github.hanielcota.cash.usecase.EconomyServiceImpl;
import com.github.hanielcota.cash.utils.external.FastInvManager;
import com.github.hanielcota.cash.view.MenuItemFactory;
import com.github.hanielcota.cash.view.menu.CashMenu;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class CashPlugin extends JavaPlugin {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    private final EconomyService economyService;
    private final CashMenu cashMenu;
    private final MenuItemFactory menuItemFactory;
    private final MySQLEconomyRepository economyRepository;

    public CashPlugin() {
        getLogger().info("Initializing CashPlugin...");
        saveDefaultConfig();

        FileConfiguration config = getConfig();
        jdbcUrl = config.getString("database.jdbcUrl");
        username = config.getString("database.username");
        password = config.getString("database.password");

        validateConfig();

        economyRepository = new MySQLEconomyRepository(jdbcUrl, username, password);
        economyService = new EconomyServiceImpl(economyRepository);
        cashMenu = new CashMenu();
        menuItemFactory = new MenuItemFactory();
    }

    @Override
    public void onEnable() {
        getLogger().info("Enabling CashPlugin...");
        registerEvents();
        registerCommands();
        getLogger().info("CashPlugin has been enabled!");
    }

    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerJoinEventListener(economyService), this);

        FastInvManager.register(this);
    }

    private void validateConfig() {
        if (jdbcUrl == null || username == null || password == null) {
            getLogger().severe("Invalid configuration. Please check your config.yml file.");
            getServer().getPluginManager().disablePlugin(this);
            throw new IllegalStateException("Invalid configuration");
        }
    }

    private void registerCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new CashCommand(economyService, menuItemFactory, economyRepository));
    }
}
