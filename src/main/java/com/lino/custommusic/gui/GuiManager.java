package com.lino.custommusic.gui;

import com.lino.custommusic.CustomMusicPlugin;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GuiManager {
    private final CustomMusicPlugin plugin;
    private final Map<UUID, GuiWindow> activeWindows = new ConcurrentHashMap<>();

    public GuiManager(CustomMusicPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerWindow(Player player, GuiWindow window) {
        if (player != null && window != null) {
            activeWindows.put(player.getUniqueId(), window);
        }
    }

    public void unregisterWindow(Player player) {
        if (player != null) {
            GuiWindow w = activeWindows.remove(player.getUniqueId());
            if (w != null && w.getMusicBox() != null) {
                w.getMusicBox().setInUse(false);
            }
        }
    }

    public GuiWindow getActiveWindow(Player player) {
        return player != null ? activeWindows.get(player.getUniqueId()) : null;
    }

    public void closeAll() {
        activeWindows.forEach((uuid, window) -> {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.closeInventory();
            }
        });
        activeWindows.clear();
    }
}
