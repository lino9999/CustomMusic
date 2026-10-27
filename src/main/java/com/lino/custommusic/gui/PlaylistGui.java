package com.lino.custommusic.gui;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.MusicBox;
import com.lino.custommusic.model.SongEntry;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlaylistGui extends GuiWindow {
    private final int page;

    public PlaylistGui(CustomMusicPlugin plugin, Player player, MusicBox musicBox, int page) {
        super(plugin, player, musicBox, plugin.getConfigManager().getRawMessage("gui.playlist_title"), 6);
        this.page = page;
    }

    @Override
    public void build() {
        fillBorders(Material.BLACK_STAINED_GLASS_PANE);

        List<SongEntry> playlist = musicBox.getPlaylist();
        int pageSize = 28;
        int maxPages = Math.max(1, (int) Math.ceil((double) playlist.size() / pageSize));
        int safePage = Math.max(0, Math.min(page, maxPages - 1));

        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int startIndex = safePage * pageSize;
        for (int i = 0; i < slots.length; i++) {
            int slot = slots[i];
            int songIndex = startIndex + i;
            if (songIndex >= playlist.size()) {
                setItem(slot, null, null);
                continue;
            }

            SongEntry song = playlist.get(songIndex);
            boolean isCurrent = musicBox.getCurrentSong() != null && musicBox.getCurrentSong().getId().equals(song.getId());

            Material mat = isCurrent ? Material.NOTE_BLOCK : Material.MUSIC_DISC_MELLOHI;
            String name = (isCurrent ? "&a▶ &f" : "&b♬ &f") + song.getTitle();

            List<String> lore = new ArrayList<>();
            lore.add("&7Author: &f" + song.getAuthor());
            lore.add("&7Duration: &e" + String.format("%02d:%02d", song.getDurationSeconds() / 60, song.getDurationSeconds() % 60));
            lore.add(" ");
            lore.add("&aLeft-Click to play now.");
            lore.add("&cRight-Click to remove from playlist.");

            setItem(slot, createButton(mat, name, lore), e -> {
                if (e.isRightClick()) {
                    musicBox.removeFromPlaylist(song);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
                } else {
                    musicBox.setCurrentSong(song);
                    musicBox.startPlayback(plugin);
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                }
                build();
            });
        }

        setItem(45, createButton(Material.BARRIER, "&c&lBack to Studio"), e -> {
            new ControlGui(plugin, player, musicBox).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        });

        if (safePage > 0) {
            setItem(48, createButton(Material.ARROW, "&e&lPrevious Page", "&7Go to page " + safePage), e -> {
                new PlaylistGui(plugin, player, musicBox, safePage - 1).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            });
        }

        setItem(49, createButton(
                Material.BOOK,
                "&b&lActive Playlist",
                "&7Total queued songs: &e" + playlist.size(),
                "&7Page &e" + (safePage + 1) + " &7of &e" + maxPages
        ), null);

        if (safePage < maxPages - 1) {
            setItem(50, createButton(Material.SPECTRAL_ARROW, "&e&lNext Page", "&7Go to page " + (safePage + 2)), e -> {
                new PlaylistGui(plugin, player, musicBox, safePage + 1).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            });
        }
    }
}
