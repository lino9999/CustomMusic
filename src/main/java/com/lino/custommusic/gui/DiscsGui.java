package com.lino.custommusic.gui;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.DiscRecord;
import com.lino.custommusic.model.MusicBox;
import com.lino.custommusic.model.SongEntry;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class DiscsGui extends GuiWindow {

    public DiscsGui(CustomMusicPlugin plugin, Player player, MusicBox musicBox) {
        super(plugin, player, musicBox, plugin.getConfigManager().getRawMessage("gui.discs_title"), 5);
    }

    @Override
    public void build() {
        fillBorders(Material.BLACK_STAINED_GLASS_PANE);

        DiscRecord[] discs = DiscRecord.values();
        int[] discSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32
        };

        for (int i = 0; i < discSlots.length && i < discs.length; i++) {
            DiscRecord disc = discs[i];
            int slot = discSlots[i];

            setItem(slot, createButton(
                    disc.getMaterial(),
                    "&d&lDisc: &f" + disc.getDisplayName(),
                    "&7Duration: &e" + String.format("%02d:%02d", disc.getDurationSeconds() / 60, disc.getDurationSeconds() % 60),
                    "&eClick to play this record on the box."
            ), e -> {
                SongEntry virtualEntry = new SongEntry(
                        "disc_" + disc.name().toLowerCase(),
                        disc.getDisplayName(),
                        "Minecraft",
                        disc.getDurationSeconds(),
                        0.0,
                        "",
                        disc.getSound().name()
                );
                musicBox.setCurrentSong(virtualEntry);
                musicBox.startPlayback(plugin);
                player.sendMessage(plugin.getConfigManager().format("&aNow spinning disc: &d" + disc.getDisplayName()));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
                new ControlGui(plugin, player, musicBox).open();
            });
        }

        setItem(36, createButton(Material.BARRIER, "&c&lBack to Studio"), e -> {
            new ControlGui(plugin, player, musicBox).open();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        });
    }
}
