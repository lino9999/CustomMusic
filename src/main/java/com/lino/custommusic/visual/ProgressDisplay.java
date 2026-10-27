package com.lino.custommusic.visual;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.MusicBox;
import com.lino.custommusic.model.VisualSettings;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ProgressDisplay {
    private final CustomMusicPlugin plugin;
    private final MusicBox musicBox;
    private BossBar bossBar;

    public ProgressDisplay(CustomMusicPlugin plugin, MusicBox musicBox) {
        this.plugin = plugin;
        this.musicBox = musicBox;
    }

    public void update(String songTitle, int currentSeconds, int totalSeconds) {
        if (totalSeconds <= 0) {
            totalSeconds = 1;
        }
        double progress = Math.max(0.0, Math.min(1.0, (double) currentSeconds / totalSeconds));
        String timeStr = formatTime(currentSeconds);
        String totalStr = formatTime(totalSeconds);

        if (plugin.getConfigManager().isBossBarEnabled()) {
            VisualSettings visual = musicBox.getVisualSettings();
            if (bossBar == null) {
                bossBar = Bukkit.createBossBar(
                        "",
                        visual.getBarColor(),
                        visual.getBarStyle()
                );
                bossBar.setVisible(true);
            } else {
                bossBar.setColor(visual.getBarColor());
                bossBar.setStyle(visual.getBarStyle());
            }

            String format = plugin.getConfigManager().getBossBarFormat()
                    .replace("%song%", songTitle)
                    .replace("%time%", timeStr)
                    .replace("%total%", totalStr);

            bossBar.setTitle(plugin.getConfigManager().format(format));
            bossBar.setProgress(progress);

            Collection<? extends Player> targets = getAudience();
            bossBar.getPlayers().stream()
                    .filter(p -> !targets.contains(p))
                    .toList()
                    .forEach(bossBar::removePlayer);

            targets.forEach(p -> {
                if (!bossBar.getPlayers().contains(p)) {
                    bossBar.addPlayer(p);
                }
            });
        }

        if (plugin.getConfigManager().isActionBarEnabled()) {
            String actionFormat = plugin.getConfigManager().getActionBarFormat()
                    .replace("%song%", songTitle)
                    .replace("%time%", timeStr)
                    .replace("%total%", totalStr);
            String formatted = plugin.getConfigManager().format(actionFormat);

            getAudience().forEach(p -> p.spigot().sendMessage(
                    ChatMessageType.ACTION_BAR,
                    TextComponent.fromLegacyText(formatted)
            ));
        }
    }

    public void clear() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }

    private Collection<? extends Player> getAudience() {
        if (musicBox.isHeadphones() && musicBox.getOwnerUuid() != null) {
            Player owner = Bukkit.getPlayer(musicBox.getOwnerUuid());
            if (owner != null && owner.isOnline()) {
                return java.util.Collections.singletonList(owner);
            }
            return java.util.Collections.emptyList();
        }

        Location loc = musicBox.getLocation();
        if (loc == null || loc.getWorld() == null) {
            return java.util.Collections.emptyList();
        }

        double radius = Math.max(16.0, musicBox.getVolume() * 0.4);
        double maxDistSq = radius * radius;
        java.util.List<Player> audience = new java.util.ArrayList<>();
        for (Player p : loc.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(loc) <= maxDistSq) {
                audience.add(p);
            }
        }
        return audience;
    }

    private static String formatTime(int totalSecs) {
        int minutes = totalSecs / 60;
        int seconds = totalSecs % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
