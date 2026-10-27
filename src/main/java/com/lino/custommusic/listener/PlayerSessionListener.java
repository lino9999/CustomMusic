package com.lino.custommusic.listener;

import com.lino.custommusic.CustomMusicPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerSessionListener implements Listener {
    private final CustomMusicPlugin plugin;

    public PlayerSessionListener(CustomMusicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getGuiManager().unregisterWindow(player);
        plugin.getCompanionManager().removeCompanion(player);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (plugin.getCompanionManager().isCarrying(player)) {
            plugin.getCompanionManager().tick();
        }
    }
}
