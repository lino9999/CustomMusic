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

public class StatsGui extends GuiWindow {

    public StatsGui(CustomMusicPlugin plugin, Player player, MusicBox musicBox) {
        super(plugin, player, musicBox, plugin.getConfigManager().getRawMessage("gui.stats_title"), 5);
    }

    @Override
    public void build() {
        fillBorders(Material.BLACK_STAINED_GLASS_PANE);

        List<SongEntry> songs = new ArrayList<>(plugin.getSongRegistry().getAllSongs());

        List<SongEntry> topPlayed = new ArrayList<>(songs);
        topPlayed.sort(Comparator.comparingInt(SongEntry::getAuditionCount).reversed());

        int[] playedSlots = {10, 11, 12, 19, 20, 21, 28, 29, 30};
        for (int i = 0; i < playedSlots.length && i < topPlayed.size(); i++) {
            SongEntry s = topPlayed.get(i);
            setItem(playedSlots[i], createButton(
                    Material.NOTE_BLOCK,
                    "&b#" + (i + 1) + " " + s.getTitle(),
                    "&7Auditions: &e" + s.getAuditionCount() + " times",
                    "&7Author: &f" + s.getAuthor()
            ), null);
        }

        List<SongEntry> topBought = new ArrayList<>(songs);
        topBought.sort(Comparator.comparingInt(SongEntry::getPurchaseCount).reversed());

        int[] boughtSlots = {14, 15, 16, 23, 24, 25, 32, 33, 34};
        for (int i = 0; i < boughtSlots.length && i < topBought.size(); i++) {
            SongEntry s = topBought.get(i);
            setItem(boughtSlots[i], createButton(
                    Material.GOLD_INGOT,
                    "&6#" + (i + 1) + " " + s.getTitle(),
                    "&7Purchased: &e" + s.getPurchaseCount() + " times",
                    "&7Price: &a" + s.getPrice() + " coins"
            ), null);
        }

        setItem(4, createButton(
                Material.NETHER_STAR,
                "&e&lServer Music Analytics",
                "&7Total Songs Loaded: &f" + songs.size(),
                "&7Total Server Auditions: &f" + songs.stream().mapToInt(SongEntry::getAuditionCount).sum(),
                "&7Total Track Purchases: &f" + songs.stream().mapToInt(SongEntry::getPurchaseCount).sum()
        ), null);

        setItem(36, createButton(Material.BARRIER, "&c&lBack to Studio"), e -> {
            new ControlGui(plugin, player, musicBox).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        });
    }
}
