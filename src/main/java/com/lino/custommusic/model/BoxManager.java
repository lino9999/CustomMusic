package com.lino.custommusic.model;

import com.lino.custommusic.CustomMusicPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BoxManager {
    private final CustomMusicPlugin plugin;
    private final Map<UUID, MusicBox> boxesById = new ConcurrentHashMap<>();
    private final Map<BlockPosition, MusicBox> boxesByLocation = new ConcurrentHashMap<>();

    public record BlockPosition(String world, int x, int y, int z) {
        public static BlockPosition of(Location loc) {
            return new BlockPosition(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }

    public BoxManager(CustomMusicPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadBoxes() {
        boxesById.clear();
        boxesByLocation.clear();

        List<MusicBox> loaded = plugin.getDatabaseService().loadAllBoxes();
        for (MusicBox box : loaded) {
            registerBox(box);
            if (box.isAutoPlay()) {
                box.startPlayback(plugin);
            }
        }
    }

    public void registerBox(MusicBox box) {
        if (box == null || box.getLocation() == null) return;
        boxesById.put(box.getBoxId(), box);
        boxesByLocation.put(BlockPosition.of(box.getLocation()), box);
    }

    public void unregisterBox(MusicBox box) {
        if (box == null) return;
        box.stopPlayback();
        if (plugin.getRadioStation() != null) {
            plugin.getRadioStation().disconnect(box);
        }
        boxesById.remove(box.getBoxId());
        if (box.getLocation() != null) {
            boxesByLocation.remove(BlockPosition.of(box.getLocation()));
        }
        plugin.getDatabaseService().deleteBox(box.getBoxId());
    }

    public MusicBox getBox(UUID id) {
        return id != null ? boxesById.get(id) : null;
    }

    public MusicBox getBoxAt(Location location) {
        if (location == null || location.getWorld() == null) return null;
        return boxesByLocation.get(BlockPosition.of(location));
    }

    public Collection<MusicBox> getAllBoxes() {
        return Collections.unmodifiableCollection(boxesById.values());
    }

    public int getPlayerBoxCount(UUID ownerUuid) {
        if (ownerUuid == null) return 0;
        int count = 0;
        for (MusicBox box : boxesById.values()) {
            if (ownerUuid.equals(box.getOwnerUuid())) {
                count++;
            }
        }
        return count;
    }

    public int getPlayerBoxLimit(Player player) {
        if (player.hasPermission("custommusic.bypass.limit")) {
            return Integer.MAX_VALUE;
        }

        int max = 1;
        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String perm = pai.getPermission().toLowerCase();
            if (perm.startsWith("custommusic.limit.")) {
                try {
                    int val = Integer.parseInt(perm.substring("custommusic.limit.".length()));
                    if (val > max) {
                        max = val;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return max;
    }

    public void stopAll() {
        for (MusicBox box : boxesById.values()) {
            box.stopPlayback();
            box.setPoweredOff(true);
        }
    }

    public void saveAll() {
        for (MusicBox box : boxesById.values()) {
            plugin.getDatabaseService().saveBox(box);
        }
    }
}
