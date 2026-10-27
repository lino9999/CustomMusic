package com.lino.custommusic.visual;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.MusicBox;
import com.lino.custommusic.model.VisualSettings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DiscoFloorEffect {
    private final CustomMusicPlugin plugin;
    private final MusicBox musicBox;
    private final List<Material> palette;
    private final List<Location> activeFloorBlocks = new ArrayList<>();
    private int step = 0;

    public DiscoFloorEffect(CustomMusicPlugin plugin, MusicBox musicBox) {
        this.plugin = plugin;
        this.musicBox = musicBox;
        this.palette = plugin.getConfigManager().getDiscoFloorPalette();
    }

    public void tick() {
        if (musicBox.isPortable() || !plugin.getConfigManager().isDiscoFloorEnabled()) {
            reset();
            return;
        }

        VisualSettings.DiscoFloorMode mode = musicBox.getVisualSettings().getDiscoFloorMode();
        if (mode == VisualSettings.DiscoFloorMode.OFF || palette.isEmpty()) {
            reset();
            return;
        }

        Location center = musicBox.getLocation();
        if (center == null || center.getWorld() == null) {
            reset();
            return;
        }

        List<Location> floorBlocks = getFloorLocations(center);
        if (!activeFloorBlocks.equals(floorBlocks)) {
            reset();
            activeFloorBlocks.addAll(floorBlocks);
        }

        List<Player> players = new ArrayList<>();
        double maxDistSq = 24.0 * 24.0;
        for (Player p : center.getWorld().getPlayers()) {
            if (p.getLocation().distanceSquared(center) <= maxDistSq) {
                players.add(p);
            }
        }

        step++;
        for (int i = 0; i < floorBlocks.size(); i++) {
            Location loc = floorBlocks.get(i);
            Material mat = switch (mode) {
                case RANDOM -> palette.get(ThreadLocalRandom.current().nextInt(palette.size()));
                case RAINBOW -> palette.get((step + i) % palette.size());
                case IN_TURN -> palette.get(step % palette.size());
                case RADIAL -> {
                    int dist = (int) Math.round(loc.distance(center.clone().add(0, -1, 0)));
                    yield palette.get((step + dist) % palette.size());
                }
                case SHUFFLE -> palette.get((i * 3 + step) % palette.size());
                default -> palette.get(0);
            };

            BlockData data = mat.createBlockData();
            for (Player p : players) {
                p.sendBlockChange(loc, data);
            }
        }
    }

    public void reset() {
        if (activeFloorBlocks.isEmpty()) {
            return;
        }

        for (Location loc : activeFloorBlocks) {
            if (loc.getWorld() == null) continue;
            BlockData realData = loc.getBlock().getBlockData();
            for (Player p : loc.getWorld().getPlayers()) {
                p.sendBlockChange(loc, realData);
            }
        }
        activeFloorBlocks.clear();
    }

    private List<Location> getFloorLocations(Location center) {
        List<Location> list = new ArrayList<>();
        int baseY = center.getBlockY() - 1;
        int cx = center.getBlockX();
        int cz = center.getBlockZ();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                Location loc = new Location(center.getWorld(), cx + dx, baseY, cz + dz);
                if (loc.getBlock().getType().isSolid()) {
                    list.add(loc);
                }
            }
        }
        return list;
    }
}
