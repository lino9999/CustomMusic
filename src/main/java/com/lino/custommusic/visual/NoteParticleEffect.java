package com.lino.custommusic.visual;

import com.lino.custommusic.model.MusicBox;
import com.lino.custommusic.model.VisualSettings;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

import java.util.concurrent.ThreadLocalRandom;

public class NoteParticleEffect {
    private final MusicBox musicBox;
    private double spiralAngle = 0.0;

    public NoteParticleEffect(MusicBox musicBox) {
        this.musicBox = musicBox;
    }

    public void tick() {
        VisualSettings.ParticleMode mode = musicBox.getVisualSettings().getParticleMode();
        if (mode == VisualSettings.ParticleMode.OFF) {
            return;
        }

        Location loc = musicBox.getLocation();
        if (loc == null || loc.getWorld() == null) {
            return;
        }

        World world = loc.getWorld();
        double x = loc.getX() + 0.5;
        double y = loc.getY() + 1.2;
        double z = loc.getZ() + 0.5;

        switch (mode) {
            case RANDOM, NOTES -> {
                double noteColor = ThreadLocalRandom.current().nextInt(0, 25) / 24.0;
                world.spawnParticle(Particle.NOTE, x, y, z, 0, noteColor, 0.0, 0.0, 1.0);
            }
            case RAINBOW -> {
                double note1 = ThreadLocalRandom.current().nextInt(0, 25) / 24.0;
                double note2 = ThreadLocalRandom.current().nextInt(0, 25) / 24.0;
                world.spawnParticle(Particle.NOTE, x + 0.3, y, z, 0, note1, 0.0, 0.0, 1.0);
                world.spawnParticle(Particle.NOTE, x - 0.3, y, z, 0, note2, 0.0, 0.0, 1.0);
            }
            case SPIRAL -> {
                spiralAngle += 0.3;
                if (spiralAngle > Math.PI * 2) {
                    spiralAngle = 0.0;
                }
                double radius = 0.75;
                double px = x + Math.cos(spiralAngle) * radius;
                double pz = z + Math.sin(spiralAngle) * radius;
                double py = y + (Math.sin(spiralAngle * 2) * 0.25);
                world.spawnParticle(Particle.NOTE, px, py, pz, 0, 0.5, 0.0, 0.0, 1.0);
            }
            case FUNNEL_FIRE -> {
                world.spawnParticle(Particle.FLAME, x, y - 0.2, z, 2, 0.15, 0.2, 0.15, 0.02);
                world.spawnParticle(Particle.SMOKE, x, y + 0.5, z, 1, 0.05, 0.1, 0.05, 0.01);
            }
            case FUNNEL_NOTES -> {
                world.spawnParticle(Particle.NOTE, x, y, z, 1, 0.1, 0.2, 0.1, 0.5);
                world.spawnParticle(Particle.HAPPY_VILLAGER, x, y + 0.4, z, 1, 0.2, 0.1, 0.2, 0.01);
            }
        }
    }
}
