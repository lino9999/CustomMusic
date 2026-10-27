package com.lino.custommusic.model;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.audio.PlaybackEngine;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MusicBox {
    private final UUID boxId;
    private final UUID ownerUuid;
    private Location location;
    private float yaw;
    private int volume = 60;
    private boolean locked = false;
    private boolean headphones = false;
    private int speedStep = 0;
    private boolean autoPlay = false;
    private boolean poweredOff = false;
    private boolean inUse = false;

    private SongEntry currentSong;
    private final List<SongEntry> playlist = new ArrayList<>();
    private final List<DiscRecord> insertedDiscs = new ArrayList<>();
    private final VisualSettings visualSettings = new VisualSettings();

    private PlaybackEngine engine;
    private Player portableCarrier;

    public MusicBox(UUID boxId, UUID ownerUuid, Location location, boolean autoPlay) {
        this.boxId = boxId != null ? boxId : UUID.randomUUID();
        this.ownerUuid = ownerUuid;
        this.location = location;
        this.autoPlay = autoPlay;
    }

    public void initEngine(CustomMusicPlugin plugin) {
        if (this.engine == null) {
            this.engine = new PlaybackEngine(plugin, this);
        }
    }

    public UUID getBoxId() {
        return boxId;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
    }

    public void adjustVolume(int delta) {
        setVolume(this.volume + delta);
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isHeadphones() {
        return headphones;
    }

    public void setHeadphones(boolean headphones) {
        this.headphones = headphones;
    }

    public int getSpeedStep() {
        return speedStep;
    }

    public void cycleSpeed() {
        speedStep++;
        if (speedStep > 1) {
            speedStep = -1;
        }
    }

    public float getPlaybackSpeedMultiplier() {
        return switch (speedStep) {
            case -1 -> 0.75f;
            case 1 -> 1.25f;
            default -> 1.0f;
        };
    }

    public boolean isAutoPlay() {
        return autoPlay;
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public boolean isPoweredOff() {
        return poweredOff;
    }

    public void setPoweredOff(boolean poweredOff) {
        this.poweredOff = poweredOff;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public SongEntry getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(SongEntry currentSong) {
        this.currentSong = currentSong;
    }

    public List<SongEntry> getPlaylist() {
        return playlist;
    }

    public void addToPlaylist(SongEntry song) {
        if (song != null && !playlist.contains(song)) {
            playlist.add(song);
        }
    }

    public void removeFromPlaylist(SongEntry song) {
        playlist.remove(song);
    }

    public boolean hasMorePlaylistTracks() {
        return !playlist.isEmpty();
    }

    public void playNextTrack(CustomMusicPlugin plugin) {
        if (playlist.isEmpty()) {
            return;
        }
        int nextIndex = 0;
        if (currentSong != null) {
            int idx = playlist.indexOf(currentSong);
            if (idx >= 0 && idx + 1 < playlist.size()) {
                nextIndex = idx + 1;
            }
        }
        currentSong = playlist.get(nextIndex);
        startPlayback(plugin);
    }

    public void playPreviousTrack(CustomMusicPlugin plugin) {
        if (playlist.isEmpty()) {
            return;
        }
        int prevIndex = playlist.size() - 1;
        if (currentSong != null) {
            int idx = playlist.indexOf(currentSong);
            if (idx > 0) {
                prevIndex = idx - 1;
            }
        }
        currentSong = playlist.get(prevIndex);
        startPlayback(plugin);
    }

    public List<DiscRecord> getInsertedDiscs() {
        return insertedDiscs;
    }

    public void insertDisc(DiscRecord disc) {
        if (disc != null && !insertedDiscs.contains(disc)) {
            insertedDiscs.add(disc);
        }
    }

    public void ejectDisc(DiscRecord disc) {
        insertedDiscs.remove(disc);
    }

    public VisualSettings getVisualSettings() {
        return visualSettings;
    }

    public void startPlayback(CustomMusicPlugin plugin) {
        initEngine(plugin);
        engine.start();
    }

    public void pausePlayback() {
        if (engine != null) {
            engine.pause();
        }
    }

    public void resumePlayback() {
        if (engine != null) {
            engine.resume();
        }
    }

    public void stopPlayback() {
        if (engine != null) {
            engine.stop();
        }
    }

    public boolean isPlaying() {
        return engine != null && engine.isPlaying();
    }

    public boolean isPaused() {
        return engine != null && engine.isPaused();
    }

    public PlaybackEngine getEngine() {
        return engine;
    }

    public Player getPortableCarrier() {
        return portableCarrier;
    }

    public void setPortableCarrier(Player portableCarrier) {
        if (this.engine != null) {
            this.engine.getFloorEffect().reset();
            this.engine.getBallEffect().remove();
        }
        this.portableCarrier = portableCarrier;
    }

    public boolean isPortable() {
        return portableCarrier != null;
    }
}
