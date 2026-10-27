package com.lino.custommusic.listener;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.gui.GuiWindow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryClickListener implements Listener {
    private final CustomMusicPlugin plugin;

    public InventoryClickListener(CustomMusicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getInventory().getHolder() instanceof GuiWindow window) {
            window.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            plugin.getGuiManager().unregisterWindow(player);
        }
    }
}
