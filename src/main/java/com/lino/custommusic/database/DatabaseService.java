package com.lino.custommusic.database;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.DiscRecord;
import com.lino.custommusic.model.MusicBox;
import com.lino.custommusic.model.SongEntry;
import com.lino.custommusic.model.VisualSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseService {
    private final CustomMusicPlugin plugin;
    private final File dbFile;
    private Connection connection;

    public DatabaseService(CustomMusicPlugin plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), "music_data.db");
    }

    public synchronized void initialize() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS music_boxes (
                        box_id VARCHAR(36) PRIMARY KEY,
                        owner_uuid VARCHAR(36),
                        world VARCHAR(64),
                        x DOUBLE,
                        y DOUBLE,
                        z DOUBLE,
                        yaw FLOAT,
                        volume INT,
                        locked INT,
                        headphones INT,
                        speed INT,
                        auto_play INT,
                        playlist_ids TEXT,
                        inserted_discs TEXT,
                        particle_mode VARCHAR(32),
                        disco_floor_mode VARCHAR(32),
                        disco_ball_style VARCHAR(32),
                        bar_color VARCHAR(32),
                        bar_style VARCHAR(32)
                    );
                """);
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS song_stats (
                        song_id VARCHAR(64) PRIMARY KEY,
                        auditions INT,
                        purchases INT
                    );
                """);
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS player_unlocks (
                        player_uuid VARCHAR(36),
                        song_id VARCHAR(64),
                        PRIMARY KEY (player_uuid, song_id)
                    );
                """);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Database init failed: " + e.getMessage());
        }
    }

    public synchronized void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }

    public synchronized List<MusicBox> loadAllBoxes() {
        List<MusicBox> result = new ArrayList<>();
        if (connection == null) return result;

        String query = "SELECT * FROM music_boxes;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                UUID boxId = UUID.fromString(rs.getString("box_id"));
                String ownerStr = rs.getString("owner_uuid");
                UUID ownerUuid = (ownerStr != null && !ownerStr.isBlank()) ? UUID.fromString(ownerStr) : null;
                String worldName = rs.getString("world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;

                Location loc = new Location(world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"));
                loc.setYaw(rs.getFloat("yaw"));

                boolean autoPlay = rs.getInt("auto_play") == 1;
                MusicBox box = new MusicBox(boxId, ownerUuid, loc, autoPlay);
                box.setVolume(rs.getInt("volume"));
                box.setLocked(rs.getInt("locked") == 1);
                box.setHeadphones(rs.getInt("headphones") == 1);

                String playlistData = rs.getString("playlist_ids");
                if (playlistData != null && !playlistData.isBlank()) {
                    for (String sid : playlistData.split(",")) {
                        SongEntry song = plugin.getSongRegistry().getSong(sid.trim());
                        if (song != null) {
                            box.addToPlaylist(song);
                        }
                    }
                }

                String discsData = rs.getString("inserted_discs");
                if (discsData != null && !discsData.isBlank()) {
                    for (String discName : discsData.split(",")) {
                        DiscRecord disc = DiscRecord.valueOf(discName.trim());
                        box.insertDisc(disc);
                    }
                }

                VisualSettings vs = box.getVisualSettings();
                try {
                    vs.setParticleMode(VisualSettings.ParticleMode.valueOf(rs.getString("particle_mode")));
                    vs.setDiscoFloorMode(VisualSettings.DiscoFloorMode.valueOf(rs.getString("disco_floor_mode")));
                    vs.setDiscoBallStyle(VisualSettings.DiscoBallStyle.valueOf(rs.getString("disco_ball_style")));
                    vs.setBarColor(BarColor.valueOf(rs.getString("bar_color")));
                    vs.setBarStyle(BarStyle.valueOf(rs.getString("bar_style")));
                } catch (Exception ignored) {}

                result.add(box);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error loading music boxes: " + e.getMessage());
        }
        return result;
    }

    public synchronized void saveBox(MusicBox box) {
        if (connection == null || box == null || box.getLocation() == null) return;

        String upsert = """
            INSERT INTO music_boxes (
                box_id, owner_uuid, world, x, y, z, yaw, volume, locked, headphones, speed,
                auto_play, playlist_ids, inserted_discs, particle_mode, disco_floor_mode,
                disco_ball_style, bar_color, bar_style
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(box_id) DO UPDATE SET
                world = excluded.world,
                x = excluded.x,
                y = excluded.y,
                z = excluded.z,
                yaw = excluded.yaw,
                volume = excluded.volume,
                locked = excluded.locked,
                headphones = excluded.headphones,
                speed = excluded.speed,
                auto_play = excluded.auto_play,
                playlist_ids = excluded.playlist_ids,
                inserted_discs = excluded.inserted_discs,
                particle_mode = excluded.particle_mode,
                disco_floor_mode = excluded.disco_floor_mode,
                disco_ball_style = excluded.disco_ball_style,
                bar_color = excluded.bar_color,
                bar_style = excluded.bar_style;
        """;

        try (PreparedStatement ps = connection.prepareStatement(upsert)) {
            ps.setString(1, box.getBoxId().toString());
            ps.setString(2, box.getOwnerUuid() != null ? box.getOwnerUuid().toString() : "");
            ps.setString(3, box.getLocation().getWorld().getName());
            ps.setDouble(4, box.getLocation().getX());
            ps.setDouble(5, box.getLocation().getY());
            ps.setDouble(6, box.getLocation().getZ());
            ps.setFloat(7, box.getLocation().getYaw());
            ps.setInt(8, box.getVolume());
            ps.setInt(9, box.isLocked() ? 1 : 0);
            ps.setInt(10, box.isHeadphones() ? 1 : 0);
            ps.setInt(11, box.getSpeedStep());
            ps.setInt(12, box.isAutoPlay() ? 1 : 0);

            List<String> ids = box.getPlaylist().stream().map(SongEntry::getId).toList();
            ps.setString(13, String.join(",", ids));

            List<String> discs = box.getInsertedDiscs().stream().map(DiscRecord::name).toList();
            ps.setString(14, String.join(",", discs));

            VisualSettings vs = box.getVisualSettings();
            ps.setString(15, vs.getParticleMode().name());
            ps.setString(16, vs.getDiscoFloorMode().name());
            ps.setString(17, vs.getDiscoBallStyle().name());
            ps.setString(18, vs.getBarColor().name());
            ps.setString(19, vs.getBarStyle().name());

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error saving music box: " + e.getMessage());
        }
    }

    public synchronized void deleteBox(UUID boxId) {
        if (connection == null || boxId == null) return;
        String query = "DELETE FROM music_boxes WHERE box_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, boxId.toString());
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public void updateSongStatsAsync(SongEntry song) {
        if (song == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (DatabaseService.this) {
                if (connection == null) return;
                String upsert = """
                    INSERT INTO song_stats (song_id, auditions, purchases)
                    VALUES (?, ?, ?)
                    ON CONFLICT(song_id) DO UPDATE SET
                        auditions = excluded.auditions,
                        purchases = excluded.purchases;
                """;
                try (PreparedStatement ps = connection.prepareStatement(upsert)) {
                    ps.setString(1, song.getId());
                    ps.setInt(2, song.getAuditionCount());
                    ps.setInt(3, song.getPurchaseCount());
                    ps.executeUpdate();
                } catch (SQLException ignored) {}
            }
        });
    }

    public synchronized void loadSongStats(SongEntry song) {
        if (connection == null || song == null) return;
        String query = "SELECT auditions, purchases FROM song_stats WHERE song_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, song.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    song.setAuditionCount(rs.getInt("auditions"));
                    song.setPurchaseCount(rs.getInt("purchases"));
                }
            }
        } catch (SQLException ignored) {}
    }

    public synchronized boolean hasPlayerUnlockedSong(UUID playerUuid, String songId) {
        if (connection == null || playerUuid == null || songId == null) return false;
        String query = "SELECT 1 FROM player_unlocks WHERE player_uuid = ? AND song_id = ?;";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, playerUuid.toString());
            ps.setString(2, songId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ignored) {
            return false;
        }
    }

    public void unlockSongAsync(UUID playerUuid, String songId) {
        if (playerUuid == null || songId == null) return;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            synchronized (DatabaseService.this) {
                if (connection == null) return;
                String query = "INSERT OR IGNORE INTO player_unlocks (player_uuid, song_id) VALUES (?, ?);";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, playerUuid.toString());
                    ps.setString(2, songId);
                    ps.executeUpdate();
                } catch (SQLException ignored) {}
            }
        });
    }
}
