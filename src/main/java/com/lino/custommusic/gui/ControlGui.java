package com.lino.custommusic.gui;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.MusicBox;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class ControlGui extends GuiWindow {

    public ControlGui(CustomMusicPlugin plugin, Player player, MusicBox musicBox) {
        super(plugin, player, musicBox, plugin.getConfigManager().getRawMessage("gui.main_title"), 6);
    }

    @Override
    public void build() {
        fillBorders(Material.BLACK_STAINED_GLASS_PANE);

        boolean radioActive = plugin.getRadioStation().isConnected(musicBox);
        setItem(10, createButton(
                radioActive ? Material.REDSTONE_TORCH : Material.LEVER,
                radioActive ? "&a&lRadio Station &7(ON)" : "&c&lRadio Station &7(OFF)",
                "&7Listen to continuous 24/7 server music.",
                "&eClick to toggle radio connection."
        ), e -> {
            if (radioActive) {
                plugin.getRadioStation().disconnect(musicBox);
            } else {
                musicBox.stopPlayback();
                plugin.getRadioStation().connect(musicBox);
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            build();
        });

        setItem(12, createButton(
                Material.CHEST,
                "&6&lSong Catalog",
                "&7Explore, preview, and purchase songs.",
                "&eClick to open catalog store."
        ), e -> {
            new CatalogGui(plugin, player, musicBox, 0).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        });

        setItem(14, createButton(
                Material.BOOK,
                "&b&lActive Playlist",
                "&7Queue size: &f" + musicBox.getPlaylist().size(),
                "&eClick to view and edit playlist."
        ), e -> {
            new PlaylistGui(plugin, player, musicBox, 0).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        });

        setItem(16, createButton(
                Material.JUKEBOX,
                "&d&lJukebox Discs",
                "&7Access your vanilla Minecraft music discs.",
                "&eClick to open discs cabinet."
        ), e -> {
            new DiscsGui(plugin, player, musicBox).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        });

        setItem(20, createButton(
                Material.GLOWSTONE_DUST,
                "&e&lVisual Customizer",
                "&7Configure floor lightshow, disco ball, and particles.",
                "&eClick to open settings."
        ), e -> {
            new VisualsGui(plugin, player, musicBox).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        });

        setItem(24, createButton(
                Material.EMERALD,
                "&a&lMusic Leaderboards",
                "&7View the most played and bought tracks.",
                "&eClick to open statistics."
        ), e -> {
            new StatsGui(plugin, player, musicBox).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        });

        setItem(28, createButton(
                Material.ARROW,
                "&f&lPrevious Song",
                "&7Switch to previous song in playlist."
        ), e -> {
            musicBox.playPreviousTrack(plugin);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            build();
        });

        boolean playing = musicBox.isPlaying();
        setItem(30, createButton(
                playing ? Material.RED_DYE : Material.LIME_DYE,
                playing ? "&c&lPause Music" : "&a&lPlay Music",
                playing ? "&7Currently playing: &f" + (musicBox.getCurrentSong() != null ? musicBox.getCurrentSong().getTitle() : "None")
                        : "&7Click to start or resume playback."
        ), e -> {
            if (playing) {
                musicBox.pausePlayback();
            } else {
                if (musicBox.isPaused()) {
                    musicBox.resumePlayback();
                } else if (musicBox.getCurrentSong() != null) {
                    musicBox.startPlayback(plugin);
                } else if (!musicBox.getPlaylist().isEmpty()) {
                    musicBox.playNextTrack(plugin);
                }
            }
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            build();
        });

        setItem(32, createButton(
                Material.SPECTRAL_ARROW,
                "&f&lNext Song",
                "&7Skip to next song in playlist."
        ), e -> {
            musicBox.playNextTrack(plugin);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            build();
        });

        setItem(38, createButton(
                Material.MINECART,
                "&c&lVolume -",
                "&7Decrease volume by 5%."
        ), e -> {
            musicBox.adjustVolume(-plugin.getConfigManager().getVolumeStep());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
            build();
        });

        setItem(40, createButton(
                Material.NOTE_BLOCK,
                "&e&lVolume: &f" + musicBox.getVolume() + "%",
                "&7Adjust loudness of the box."
        ), null);

        setItem(42, createButton(
                Material.HOPPER_MINECART,
                "&a&lVolume +",
                "&7Increase volume by 5%."
        ), e -> {
            musicBox.adjustVolume(plugin.getConfigManager().getVolumeStep());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            build();
        });

        setItem(46, createButton(
                musicBox.isHeadphones() ? Material.IRON_HELMET : Material.CHAINMAIL_HELMET,
                musicBox.isHeadphones() ? "&a&lHeadphones: &fPrivate" : "&7&lHeadphones: &fPublic Speaker",
                "&7Private mode limits sound only to you.",
                "&eClick to toggle."
        ), e -> {
            musicBox.setHeadphones(!musicBox.isHeadphones());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            build();
        });

        boolean carries = plugin.getCompanionManager().isCarrying(player);
        setItem(48, createButton(
                carries ? Material.ENDER_CHEST : Material.CHEST_MINECART,
                carries ? "&a&lCompanion Mode &7(Active)" : "&e&lCompanion Mode &7(Carry Player)",
                "&7Let the music box follow you in the air.",
                "&eClick to toggle companion carry."
        ), e -> {
            if (carries) {
                plugin.getCompanionManager().removeCompanion(player);
                player.sendMessage(plugin.getConfigManager().getMessage("box.portable_docked"));
            } else {
                plugin.getCompanionManager().setCompanion(player, musicBox);
                player.sendMessage(plugin.getConfigManager().getMessage("box.portable_activated"));
            }
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
            player.closeInventory();
        });

        setItem(50, createButton(
                musicBox.isLocked() ? Material.IRON_DOOR : Material.OAK_DOOR,
                musicBox.isLocked() ? "&c&lProtected &7(Locked)" : "&a&lOpen Access &7(Unlocked)",
                "&7Prevent other players from modifying your box.",
                "&eClick to toggle lock."
        ), e -> {
            musicBox.setLocked(!musicBox.isLocked());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            build();
        });

        setItem(52, createButton(
                Material.FEATHER,
                "&b&lPlayback Speed: &f" + musicBox.getPlaybackSpeedMultiplier() + "x",
                "&7Adjust playback tempo.",
                "&eClick to cycle speed (0.75x, 1.0x, 1.25x)."
        ), e -> {
            musicBox.cycleSpeed();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            build();
        });
    }
}
