package com.lino.custommusic.portable;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.MusicBox;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CompanionTracker {
    private static final NamespacedKey COMPANION_KEY = new NamespacedKey("custommusic", "companion");
    private final CustomMusicPlugin plugin;
    private final Map<UUID, MusicBox> carriedBoxes = new ConcurrentHashMap<>();
    private final Map<UUID, ArmorStand> visualStands = new ConcurrentHashMap<>();

    public CompanionTracker(CustomMusicPlugin plugin) {
        this.plugin = plugin;
    }

    public void setCompanion(Player player, MusicBox box) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        removeCompanion(player);

        carriedBoxes.put(uuid, box);
        box.setPortableCarrier(player);

        Location loc = calculateOffset(player);
        player.getWorld().spawn(loc, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setSmall(true);
            stand.setInvulnerable(true);
            stand.setCollidable(false);
            stand.getPersistentDataContainer().set(COMPANION_KEY, PersistentDataType.BYTE, (byte) 1);
            stand.getEquipment().setHelmet(new ItemStack(Material.NOTE_BLOCK));
            visualStands.put(uuid, stand);
        });
    }

    public void removeCompanion(Player player) {
        if (player == null) return;
        UUID uuid = player.getUniqueId();
        MusicBox box = carriedBoxes.remove(uuid);
        if (box != null) {
            box.setPortableCarrier(null);
        }
        ArmorStand stand = visualStands.remove(uuid);
        if (stand != null && stand.isValid()) {
            stand.remove();
        }
    }

    public boolean isCarrying(Player player) {
        return player != null && carriedBoxes.containsKey(player.getUniqueId());
    }

    public MusicBox getCompanionBox(Player player) {
        return player != null ? carriedBoxes.get(player.getUniqueId()) : null;
    }

    public void tick() {
        carriedBoxes.forEach((uuid, box) -> {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                ArmorStand stand = visualStands.remove(uuid);
                if (stand != null && stand.isValid()) {
                    stand.remove();
                }
                carriedBoxes.remove(uuid);
                return;
            }

            ArmorStand stand = visualStands.get(uuid);
            Location targetLoc = calculateOffset(player);
            if (stand == null || !stand.isValid() || !stand.getWorld().equals(player.getWorld())) {
                setCompanion(player, box);
            } else {
                stand.teleport(targetLoc);
            }
            box.setLocation(player.getLocation());
        });
    }

    public void clearAll() {
        visualStands.values().forEach(stand -> {
            if (stand != null && stand.isValid()) {
                stand.remove();
            }
        });
        visualStands.clear();
        carriedBoxes.clear();
    }

    private Location calculateOffset(Player player) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().setY(0).normalize();
        Vector side = new Vector(-dir.getZ(), 0, dir.getX()).multiply(0.65);
        Vector back = dir.clone().multiply(-0.4);

        Location target = eye.clone().add(side).add(back);
        target.setY(player.getLocation().getY() + 0.9);
        target.setYaw(player.getLocation().getYaw());
        return target;
    }
}
