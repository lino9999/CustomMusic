package com.lino.custommusic.gui;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.MusicBox;
import com.lino.custommusic.model.SongEntry;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CatalogGui extends GuiWindow {
    public enum FilterMode {
        ALL,
        PURCHASED,
        MOST_PLAYED,
        MOST_PURCHASED
    }

    private final int page;
    private FilterMode filterMode = FilterMode.ALL;

    public CatalogGui(CustomMusicPlugin plugin, Player player, MusicBox musicBox, int page) {
        super(plugin, player, musicBox, plugin.getConfigManager().getRawMessage("gui.catalog_title"), 6);
        this.page = page;
    }

    public CatalogGui(CustomMusicPlugin plugin, Player player, MusicBox musicBox, int page, FilterMode filterMode) {
        super(plugin, player, musicBox, plugin.getConfigManager().getRawMessage("gui.catalog_title"), 6);
        this.page = page;
        this.filterMode = filterMode;
    }

    @Override
    public void build() {
        fillBorders(Material.BLACK_STAINED_GLASS_PANE);

        List<SongEntry> songs = getFilteredSongs();
        int pageSize = 28;
        int maxPages = Math.max(1, (int) Math.ceil((double) songs.size() / pageSize));
        int safePage = Math.max(0, Math.min(page, maxPages - 1));

        int[] songSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int startIndex = safePage * pageSize;
        for (int i = 0; i < songSlots.length; i++) {
            int slot = songSlots[i];
            int songIndex = startIndex + i;
            if (songIndex >= songs.size()) {
                setItem(slot, null, null);
                continue;
            }

            SongEntry song = songs.get(songIndex);
            boolean unlocked = song.getPrice() <= 0.0 || plugin.getDatabaseService().hasPlayerUnlockedSong(player.getUniqueId(), song.getId()) || player.hasPermission("custommusic.admin");
            boolean inPlaylist = musicBox.getPlaylist().contains(song);

            Material iconMat = inPlaylist ? Material.MUSIC_DISC_CAT : (unlocked ? Material.MUSIC_DISC_13 : Material.DISC_FRAGMENT_5);
            String title = (unlocked ? "&a♫ &f" : "&c🔒 &7") + song.getTitle();

            List<String> lore = new ArrayList<>();
            lore.add("&7Author: &f" + song.getAuthor());
            lore.add("&7Duration: &e" + formatDuration(song.getDurationSeconds()));
            lore.add("&7Plays: &b" + song.getAuditionCount());

            if (!unlocked) {
                lore.add("&7Price: &6" + song.getPrice() + " coins");
                lore.add("&aClick to unlock track!");
            } else {
                lore.add(inPlaylist ? "&a✔ Currently in Box Playlist" : "&7Not in active playlist");
                lore.add("&eClick to toggle in playlist.");
                lore.add("&bShift-Click to play now.");
            }

            setItem(slot, createButton(iconMat, title, lore), e -> {
                if (!unlocked) {
                    handlePurchase(song);
                } else {
                    if (e.isShiftClick()) {
                        musicBox.setCurrentSong(song);
                        musicBox.startPlayback(plugin);
                        player.sendMessage(plugin.getConfigManager().format("&aNow playing: &f" + song.getTitle()));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                    } else {
                        if (inPlaylist) {
                            musicBox.removeFromPlaylist(song);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
                        } else {
                            musicBox.addToPlaylist(song);
                            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                        }
                    }
                    build();
                }
            });
        }

        setItem(45, createButton(Material.BARRIER, "&c&lBack to Studio"), e -> {
            new ControlGui(plugin, player, musicBox).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        });

        if (safePage > 0) {
            setItem(48, createButton(Material.ARROW, "&e&lPrevious Page", "&7Go to page " + safePage), e -> {
                new CatalogGui(plugin, player, musicBox, safePage - 1, filterMode).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            });
        }

        setItem(49, createButton(
                Material.COMPASS,
                "&6&lFilter: &f" + filterMode.name(),
                "&7Page &e" + (safePage + 1) + " &7of &e" + maxPages,
                "&eClick to cycle filter (ALL, PURCHASED, MOST_PLAYED, MOST_PURCHASED)."
        ), e -> {
            FilterMode next = switch (filterMode) {
                case ALL -> FilterMode.PURCHASED;
                case PURCHASED -> FilterMode.MOST_PLAYED;
                case MOST_PLAYED -> FilterMode.MOST_PURCHASED;
                case MOST_PURCHASED -> FilterMode.ALL;
            };
            new CatalogGui(plugin, player, musicBox, 0, next).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
        });

        if (safePage < maxPages - 1) {
            setItem(50, createButton(Material.SPECTRAL_ARROW, "&e&lNext Page", "&7Go to page " + (safePage + 2)), e -> {
                new CatalogGui(plugin, player, musicBox, safePage + 1, filterMode).open();
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            });
        }
    }

    private void handlePurchase(SongEntry song) {
        if (!plugin.getEconomyHook().isAvailable()) {
            unlockAndNotify(song);
            return;
        }

        double price = song.getPrice();
        if (price > 0 && !plugin.getEconomyHook().has(player, price)) {
            player.sendMessage(plugin.getConfigManager().getMessage("audio.insufficient_funds"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (price > 0) {
            plugin.getEconomyHook().withdraw(player, price);
        }

        unlockAndNotify(song);
    }

    private void unlockAndNotify(SongEntry song) {
        plugin.getDatabaseService().unlockSongAsync(player.getUniqueId(), song.getId());
        song.incrementPurchases();
        plugin.getDatabaseService().updateSongStatsAsync(song);

        String msg = plugin.getConfigManager().getMessage("audio.track_purchased")
                .replace("%song%", song.getTitle())
                .replace("%price%", String.valueOf(song.getPrice()));
        player.sendMessage(msg);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);
        build();
    }

    private List<SongEntry> getFilteredSongs() {
        List<SongEntry> list = new ArrayList<>(plugin.getSongRegistry().getAllSongs());
        return switch (filterMode) {
            case PURCHASED -> list.stream()
                    .filter(s -> s.getPrice() <= 0 || plugin.getDatabaseService().hasPlayerUnlockedSong(player.getUniqueId(), s.getId()))
                    .toList();
            case MOST_PLAYED -> {
                list.sort(Comparator.comparingInt(SongEntry::getAuditionCount).reversed());
                yield list;
            }
            case MOST_PURCHASED -> {
                list.sort(Comparator.comparingInt(SongEntry::getPurchaseCount).reversed());
                yield list;
            }
            default -> list;
        };
    }

    private String formatDuration(int secs) {
        return String.format("%02d:%02d", secs / 60, secs % 60);
    }
}
