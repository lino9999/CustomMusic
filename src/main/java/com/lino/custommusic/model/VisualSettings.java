package com.lino.custommusic.model;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

public class VisualSettings {
    public enum ParticleMode {
        OFF,
        RANDOM,
        RAINBOW,
        SPIRAL,
        NOTES,
        FUNNEL_FIRE,
        FUNNEL_NOTES
    }

    public enum DiscoFloorMode {
        OFF,
        RANDOM,
        RAINBOW,
        IN_TURN,
        RADIAL,
        SHUFFLE
    }

    public enum DiscoBallStyle {
        OFF,
        CRYSTAL,
        RETRO,
        EMERALD,
        NEON,
        AMETHYST
    }

    private ParticleMode particleMode = ParticleMode.RAINBOW;
    private DiscoFloorMode discoFloorMode = DiscoFloorMode.RANDOM;
    private DiscoBallStyle discoBallStyle = DiscoBallStyle.CRYSTAL;
    private BarColor barColor = BarColor.PURPLE;
    private BarStyle barStyle = BarStyle.SOLID;

    public ParticleMode getParticleMode() {
        return particleMode;
    }

    public void setParticleMode(ParticleMode particleMode) {
        this.particleMode = particleMode == null ? ParticleMode.OFF : particleMode;
    }

    public DiscoFloorMode getDiscoFloorMode() {
        return discoFloorMode;
    }

    public void setDiscoFloorMode(DiscoFloorMode discoFloorMode) {
        this.discoFloorMode = discoFloorMode == null ? DiscoFloorMode.OFF : discoFloorMode;
    }

    public DiscoBallStyle getDiscoBallStyle() {
        return discoBallStyle;
    }

    public void setDiscoBallStyle(DiscoBallStyle discoBallStyle) {
        this.discoBallStyle = discoBallStyle == null ? DiscoBallStyle.OFF : discoBallStyle;
    }

    public BarColor getBarColor() {
        return barColor;
    }

    public void setBarColor(BarColor barColor) {
        this.barColor = barColor == null ? BarColor.PURPLE : barColor;
    }

    public BarStyle getBarStyle() {
        return barStyle;
    }

    public void setBarStyle(BarStyle barStyle) {
        this.barStyle = barStyle == null ? BarStyle.SOLID : barStyle;
    }
}
