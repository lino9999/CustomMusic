package com.lino.custommusic.audio;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.SongEntry;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SongRegistry {
    private final CustomMusicPlugin plugin;
    private final Map<String, SongEntry> songs = new ConcurrentHashMap<>();
    private final File songsFolder;

    public SongRegistry(CustomMusicPlugin plugin) {
        this.plugin = plugin;
        this.songsFolder = new File(plugin.getDataFolder(), "songs");
    }

    public synchronized void loadAll() {
        songs.clear();
        if (!songsFolder.exists()) {
            songsFolder.mkdirs();
        }

        File[] files = songsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".nbs"));
        if (files == null || files.length == 0) {
            exportDefaultSongs();
            files = songsFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".nbs"));
        }
        if (files != null) {
            for (File file : files) {
                String id = file.getName().replace(".nbs", "").toLowerCase();
                double defaultPrice = plugin.getConfig().getDouble("economy.default_price", 100.0);
                SongEntry entry = NbsParser.parse(file, id, defaultPrice, "");
                if (entry != null) {
                    plugin.getDatabaseService().loadSongStats(entry);
                    songs.put(id, entry);
                }
            }
        }

        ConfigurationSection oggSection = plugin.getConfig().getConfigurationSection("custom_ogg_tracks");
        if (oggSection != null) {
            for (String key : oggSection.getKeys(false)) {
                String sound = oggSection.getString(key + ".sound", "");
                int length = oggSection.getInt(key + ".length", 120);
                String title = oggSection.getString(key + ".title", key);
                double price = oggSection.getDouble(key + ".price", 150.0);
                String perm = oggSection.getString(key + ".permission", "");

                SongEntry oggEntry = new SongEntry(key.toLowerCase(), title, "Custom", length, price, perm, sound);
                plugin.getDatabaseService().loadSongStats(oggEntry);
                songs.put(key.toLowerCase(), oggEntry);
            }
        }

        plugin.getLogger().info("Successfully indexed " + songs.size() + " audio tracks.");
    }

    public SongEntry getSong(String id) {
        return id != null ? songs.get(id.toLowerCase()) : null;
    }

    public Collection<SongEntry> getAllSongs() {
        return Collections.unmodifiableCollection(songs.values());
    }

    public synchronized void registerSong(SongEntry song) {
        if (song != null) {
            songs.put(song.getId().toLowerCase(), song);
        }
    }

    public synchronized void unregisterSong(String id) {
        if (id != null) {
            songs.remove(id.toLowerCase());
        }
    }

    public boolean isFileInSongsFolder(String filename) {
        File f = new File(songsFolder, filename.endsWith(".nbs") ? filename : filename + ".nbs");
        return f.exists();
    }

    private void exportDefaultSongs() {
        String[] defaults = {
                "Africa.nbs", "Bad_Piggies_Theme.nbs", "Billie_Jean.nbs", "Call_Me_Maybe.nbs",
                "Dance_Monkey.nbs", "Drop_Down.nbs", "Faded.nbs", "FNAF.nbs",
                "Gravity_Falls_Theme.nbs", "Interstellar.nbs", "Megalovania.nbs", "Monster.nbs",
                "Moonlight.nbs", "Never_Gonna_Give_You_Up.nbs", "PPAP.nbs", "Revenge.nbs",
                "Rude.nbs", "Russian_Anthem.nbs", "Soviet_Anthem.nbs", "Sponge_Bob_Main_Theme.nbs",
                "Steves_Lava_Chicken.nbs", "SubterraneanHell.nbs", "Tokyo_Teddy_Bear.nbs", "What_is_Love.nbs"
        };

        for (String songName : defaults) {
            try {
                plugin.saveResource("songs/" + songName, false);
            } catch (Exception ignored) {}
        }
    }
}
