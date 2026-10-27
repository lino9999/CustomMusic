package com.lino.custommusic.api;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.MusicBox;
import com.lino.custommusic.model.SongEntry;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public final class CustomMusicAPI {
    private static CustomMusicPlugin plugin;

    private CustomMusicAPI() {}

    public static void init(CustomMusicPlugin instance) {
        plugin = instance;
    }

    public static Optional<MusicBox> getBoxAt(Location location) {
        if (plugin == null || location == null) return Optional.empty();
        return Optional.ofNullable(plugin.getBoxManager().getBoxAt(location));
    }

    public static Optional<MusicBox> getBoxById(UUID boxId) {
        if (plugin == null || boxId == null) return Optional.empty();
        return Optional.ofNullable(plugin.getBoxManager().getBox(boxId));
    }

    public static Optional<MusicBox> getCompanionBox(Player player) {
        if (plugin == null || player == null) return Optional.empty();
        return Optional.ofNullable(plugin.getCompanionManager().getCompanionBox(player));
    }

    public static Collection<SongEntry> getAllSongs() {
        return plugin.getSongRegistry().getAllSongs();
    }

    public static Optional<SongEntry> getSong(String songId) {
        return Optional.ofNullable(plugin.getSongRegistry().getSong(songId));
    }

    public static void playSongAt(Location location, String songId, int volume) {
        getSong(songId).ifPresent(song -> {
            MusicBox box = new MusicBox(UUID.randomUUID(), null, location, false);
            box.setVolume(volume);
            box.setCurrentSong(song);
            box.startPlayback(plugin);
        });
    }
}
