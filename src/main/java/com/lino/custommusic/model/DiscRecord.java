package com.lino.custommusic.model;

import org.bukkit.Material;
import org.bukkit.Sound;

public enum DiscRecord {
    THIRTEEN("13", Material.MUSIC_DISC_13, Sound.MUSIC_DISC_13, 178),
    CAT("Cat", Material.MUSIC_DISC_CAT, Sound.MUSIC_DISC_CAT, 185),
    BLOCKS("Blocks", Material.MUSIC_DISC_BLOCKS, Sound.MUSIC_DISC_BLOCKS, 345),
    CHIRP("Chirp", Material.MUSIC_DISC_CHIRP, Sound.MUSIC_DISC_CHIRP, 185),
    FAR("Far", Material.MUSIC_DISC_FAR, Sound.MUSIC_DISC_FAR, 174),
    MALL("Mall", Material.MUSIC_DISC_MALL, Sound.MUSIC_DISC_MALL, 197),
    MELLOHI("Mellohi", Material.MUSIC_DISC_MELLOHI, Sound.MUSIC_DISC_MELLOHI, 96),
    STAL("Stal", Material.MUSIC_DISC_STAL, Sound.MUSIC_DISC_STAL, 150),
    STRAD("Strad", Material.MUSIC_DISC_STRAD, Sound.MUSIC_DISC_STRAD, 188),
    WARD("Ward", Material.MUSIC_DISC_WARD, Sound.MUSIC_DISC_WARD, 251),
    ELEVEN("11", Material.MUSIC_DISC_11, Sound.MUSIC_DISC_11, 71),
    WAIT("Wait", Material.MUSIC_DISC_WAIT, Sound.MUSIC_DISC_WAIT, 238),
    OTHERSIDE("Otherside", Material.MUSIC_DISC_OTHERSIDE, Sound.MUSIC_DISC_OTHERSIDE, 195),
    PIGSTEP("Pigstep", Material.MUSIC_DISC_PIGSTEP, Sound.MUSIC_DISC_PIGSTEP, 148),
    FIVE("5", Material.MUSIC_DISC_5, Sound.MUSIC_DISC_5, 178),
    RELIC("Relic", Material.MUSIC_DISC_RELIC, Sound.MUSIC_DISC_RELIC, 218),
    PRECIPICE("Precipice", Material.MUSIC_DISC_PRECIPICE, Sound.MUSIC_DISC_PRECIPICE, 299),
    CREATOR("Creator", Material.MUSIC_DISC_CREATOR, Sound.MUSIC_DISC_CREATOR, 176),
    CREATOR_MUSIC_BOX("Creator (Music Box)", Material.MUSIC_DISC_CREATOR_MUSIC_BOX, Sound.MUSIC_DISC_CREATOR_MUSIC_BOX, 73);

    private final String displayName;
    private final Material material;
    private final Sound sound;
    private final int durationSeconds;

    DiscRecord(String displayName, Material material, Sound sound, int durationSeconds) {
        this.displayName = displayName;
        this.material = material;
        this.sound = sound;
        this.durationSeconds = durationSeconds;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getMaterial() {
        return material;
    }

    public Sound getSound() {
        return sound;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public static DiscRecord fromMaterial(Material material) {
        for (DiscRecord disc : values()) {
            if (disc.material == material) {
                return disc;
            }
        }
        return null;
    }
}
