package com.github.hanielcota.cash.utils.external;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class FastInvManager {

    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);

    private FastInvManager() {
        throw new UnsupportedOperationException();
    }

    public static void register(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin");

        if (REGISTERED.getAndSet(true)) {
            throw new IllegalStateException("FastInv is already registered by plugin: " + plugin.getName());
        }

        Bukkit.getPluginManager().registerEvents(new InventoryListener(plugin), plugin);
    }

    public static void closeAll() {
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.getOpenInventory().getTopInventory().getHolder() instanceof FastInv)
                .forEach(Player::closeInventory);
    }

    public static final class InventoryListener implements Listener {

        private final Plugin plugin;

        public InventoryListener(Plugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (event.getClickedInventory() == null || !(event.getClickedInventory().getHolder() instanceof FastInv inventory)) {
                return;
            }

            boolean wasCancelled = event.isCancelled();
            event.setCancelled(true);

            inventory.handleClick(event);

            if (!wasCancelled && !event.isCancelled()) {
                event.setCancelled(false);
            }
        }



        @EventHandler
        public void onInventoryOpen(InventoryOpenEvent e) {
            if (!(e.getInventory().getHolder() instanceof FastInv inv)) {
                return;
            }

            inv.handleOpen(e);
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (!(e.getInventory().getHolder() instanceof FastInv inv)) {
                return;
            }

            if (inv.handleClose(e)) {
                Bukkit.getScheduler().runTask(this.plugin, () -> inv.open((Player) e.getPlayer()));
            }
        }

        @EventHandler
        public void onPluginDisable(PluginDisableEvent e) {
            if (e.getPlugin() != this.plugin) {
                return;
            }

            closeAll();
            REGISTERED.set(false);
        }
    }
}
