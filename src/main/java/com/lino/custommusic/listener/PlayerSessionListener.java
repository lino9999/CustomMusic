package com.lino.custommusic.listener;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.MusicBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class PlayerSessionListener implements Listener {
    private final CustomMusicPlugin plugin;

    public PlayerSessionListener(CustomMusicPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getGuiManager().unregisterWindow(player);
        plugin.getDatabaseService().invalidatePlayerCache(player.getUniqueId());

        if (plugin.getCompanionManager().isCarrying(player)) {
            MusicBox box = plugin.getCompanionManager().getCompanionBox(player);
            if (box != null) {
                ItemStack item = plugin.getConfigManager().createBoxItem(box.isAutoPlay());
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                if (!leftover.isEmpty()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), item);
                }
            }
            plugin.getCompanionManager().removeCompanion(player);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (plugin.getCompanionManager().isCarrying(player)) {
            MusicBox box = plugin.getCompanionManager().getCompanionBox(player);
            if (box != null) {
                ItemStack item = plugin.getConfigManager().createBoxItem(box.isAutoPlay());
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            plugin.getCompanionManager().removeCompanion(player);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (plugin.getCompanionManager().isCarrying(player)) {
            plugin.getCompanionManager().tick();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getDatabaseService().hasPlayerUnlockedSong(player.getUniqueId(), "");
        });

        for (MusicBox box : plugin.getBoxManager().getAllBoxes()) {
            if (box.isAutoPlay() && box.getLocation() != null
                    && box.getLocation().getWorld() != null
                    && box.getLocation().getWorld().equals(player.getWorld())
                    && !box.isPlaying()) {
                box.startPlayback(plugin);
            }
        }
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (plugin.getCompanionManager().isCarrying(player)) {
            plugin.getCompanionManager().tick();
        }
    }
}
