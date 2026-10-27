package com.lino.custommusic.visual;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.MusicBox;
import com.lino.custommusic.model.VisualSettings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.concurrent.ThreadLocalRandom;

public class DiscoBallEffect {
    private static final NamespacedKey ENTITY_TAG = new NamespacedKey("custommusic", "discoball");
    private final CustomMusicPlugin plugin;
    private final MusicBox musicBox;
    private ArmorStand stand;
    private float currentYaw = 0.0f;

    public DiscoBallEffect(CustomMusicPlugin plugin, MusicBox musicBox) {
        this.plugin = plugin;
        this.musicBox = musicBox;
    }

    public void tick() {
        if (musicBox.isPortable()) {
            remove();
            return;
        }

        VisualSettings.DiscoBallStyle style = musicBox.getVisualSettings().getDiscoBallStyle();
        if (style == VisualSettings.DiscoBallStyle.OFF) {
            remove();
            return;
        }

        Location boxLoc = musicBox.getLocation();
        if (boxLoc == null || boxLoc.getWorld() == null) {
            remove();
            return;
        }

        Location targetLoc = boxLoc.clone().add(0.5, 1.25, 0.5);
        if (stand == null || !stand.isValid()) {
            spawn(targetLoc, style);
        }

        if (stand != null) {
            currentYaw = (currentYaw + 12.0f) % 360.0f;
            targetLoc.setYaw(currentYaw);
            stand.teleport(targetLoc);

            Location particleLoc = targetLoc.clone().add(0, 1.7, 0);
            stand.getWorld().spawnParticle(
                    Particle.END_ROD,
                    particleLoc.getX(),
                    particleLoc.getY(),
                    particleLoc.getZ(),
                    1,
                    0.15,
                    0.15,
                    0.15,
                    0.01
            );
        }
    }

    private void spawn(Location loc, VisualSettings.DiscoBallStyle style) {
        remove();
        loc.getWorld().spawn(loc, ArmorStand.class, entity -> {
            entity.setVisible(false);
            entity.setGravity(false);
            entity.setMarker(true);
            entity.setSmall(true);
            entity.setInvulnerable(true);
            entity.setCollidable(false);
            entity.getPersistentDataContainer().set(ENTITY_TAG, PersistentDataType.BYTE, (byte) 1);

            Material helmetMat = switch (style) {
                case CRYSTAL -> Material.AMETHYST_CLUSTER;
                case RETRO -> Material.JUKEBOX;
                case EMERALD -> Material.EMERALD_BLOCK;
                case NEON -> Material.SEA_LANTERN;
                case AMETHYST -> Material.AMETHYST_BLOCK;
                default -> Material.GLOWSTONE;
            };

            entity.getEquipment().setHelmet(new ItemStack(helmetMat));
            this.stand = entity;
        });
    }

    public void remove() {
        if (stand != null && stand.isValid()) {
            stand.remove();
            stand = null;
        }
    }
}
