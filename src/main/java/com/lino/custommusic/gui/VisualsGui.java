package com.lino.custommusic.gui;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.MusicBox;
import com.lino.custommusic.model.VisualSettings;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

public class VisualsGui extends GuiWindow {

    public VisualsGui(CustomMusicPlugin plugin, Player player, MusicBox musicBox) {
        super(plugin, player, musicBox, plugin.getConfigManager().getRawMessage("gui.visuals_title"), 5);
    }

    @Override
    public void build() {
        fillBorders(Material.BLACK_STAINED_GLASS_PANE);
        VisualSettings vs = musicBox.getVisualSettings();

        setItem(11, createButton(
                Material.FIREWORK_STAR,
                "&e&lParticle FX: &f" + vs.getParticleMode().name(),
                "&7Ambient note and sparkle particles.",
                "&eClick to cycle particle effect mode."
        ), e -> {
            VisualSettings.ParticleMode[] modes = VisualSettings.ParticleMode.values();
            int next = (vs.getParticleMode().ordinal() + 1) % modes.length;
            vs.setParticleMode(modes[next]);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            build();
        });

        setItem(13, createButton(
                Material.MAGENTA_CONCRETE,
                "&d&lDisco Floor: &f" + vs.getDiscoFloorMode().name(),
                "&7Shimmering client-side dance floor blocks.",
                "&eClick to cycle dance floor animation."
        ), e -> {
            VisualSettings.DiscoFloorMode[] modes = VisualSettings.DiscoFloorMode.values();
            int next = (vs.getDiscoFloorMode().ordinal() + 1) % modes.length;
            vs.setDiscoFloorMode(modes[next]);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            build();
        });

        setItem(15, createButton(
                Material.BEACON,
                "&b&lDisco Ball: &f" + vs.getDiscoBallStyle().name(),
                "&7Rotating ceiling/stand disco orb.",
                "&eClick to cycle disco ball accessory."
        ), e -> {
            VisualSettings.DiscoBallStyle[] styles = VisualSettings.DiscoBallStyle.values();
            int next = (vs.getDiscoBallStyle().ordinal() + 1) % styles.length;
            vs.setDiscoBallStyle(styles[next]);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            build();
        });

        setItem(29, createButton(
                Material.PURPLE_DYE,
                "&a&lBossBar Color: &f" + vs.getBarColor().name(),
                "&7Color of the song progress bar.",
                "&eClick to cycle color."
        ), e -> {
            BarColor[] colors = BarColor.values();
            int next = (vs.getBarColor().ordinal() + 1) % colors.length;
            vs.setBarColor(colors[next]);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            build();
        });

        setItem(33, createButton(
                Material.NAME_TAG,
                "&6&lBossBar Style: &f" + vs.getBarStyle().name(),
                "&7Segment pattern of the progress bar.",
                "&eClick to cycle style."
        ), e -> {
            BarStyle[] styles = BarStyle.values();
            int next = (vs.getBarStyle().ordinal() + 1) % styles.length;
            vs.setBarStyle(styles[next]);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            build();
        });

        setItem(36, createButton(Material.BARRIER, "&c&lBack to Studio"), e -> {
            new ControlGui(plugin, player, musicBox).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        });
    }
}
