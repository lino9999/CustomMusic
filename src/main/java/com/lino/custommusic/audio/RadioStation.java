package com.lino.custommusic.audio;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.AudioNote;
import com.lino.custommusic.model.MusicBox;
import com.lino.custommusic.model.SongEntry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RadioStation {
    private final CustomMusicPlugin plugin;
    private final Set<MusicBox> connectedBoxes = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final List<SongEntry> stationQueue = new ArrayList<>();

    private BukkitTask broadcastTask;
    private int currentQueueIndex = 0;
    private SongEntry currentTrack;
    private int broadcastTick = 0;
    private double radioAccumulator = 0.0;
    private int pauseCounter = 0;
    private boolean radioAuditionCounted = false;

    public RadioStation(CustomMusicPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (!plugin.getConfigManager().isRadioEnabled()) {
            return;
        }

        refreshQueue();
        if (stationQueue.isEmpty()) {
            return;
        }

        currentTrack = stationQueue.get(0);
        broadcastTick = 0;

        broadcastTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void stop() {
        if (broadcastTask != null) {
            broadcastTask.cancel();
            broadcastTask = null;
        }
        connectedBoxes.clear();
    }

    public void connect(MusicBox box) {
        if (box == null) return;
        connectedBoxes.add(box);
    }

    public void disconnect(MusicBox box) {
        if (box == null) return;
        connectedBoxes.remove(box);
    }

    public boolean isConnected(MusicBox box) {
        return box != null && connectedBoxes.contains(box);
    }

    public SongEntry getCurrentTrack() {
        return currentTrack;
    }

    public int getBroadcastSeconds() {
        if (currentTrack == null) return 0;
        return (int) (broadcastTick / currentTrack.getTempo());
    }

    public boolean isInIntermission() {
        return pauseCounter > 0;
    }

    public void refreshQueue() {
        stationQueue.clear();
        for (SongEntry song : plugin.getSongRegistry().getAllSongs()) {
            if (!song.isCustomSound()) {
                stationQueue.add(song);
            }
        }
        Collections.shuffle(stationQueue);
    }

    private void tick() {
        if (connectedBoxes.isEmpty() || currentTrack == null) {
            return;
        }

        if (pauseCounter > 0) {
            pauseCounter--;
            if (pauseCounter == 0) {
                advanceTrack();
            }
            return;
        }

        if (!radioAuditionCounted && currentTrack.getLengthInTicks() > 0 && broadcastTick >= currentTrack.getLengthInTicks() * 0.2) {
            radioAuditionCounted = true;
            currentTrack.incrementAuditions();
            plugin.getDatabaseService().updateSongStatsAsync(currentTrack);
        }

        float tempo = currentTrack.getTempo();
        radioAccumulator += (tempo / 20.0);

        while (radioAccumulator >= 1.0) {
            List<AudioNote> notes = currentTrack.getNotesByTick().get(broadcastTick);
            if (notes != null && !notes.isEmpty()) {
                for (MusicBox box : connectedBoxes) {
                    if (box.isPoweredOff()) continue;

                    float masterVol = (box.getVolume() / 100.0f) * plugin.getConfigManager().getVolumeMultiplier();
                    for (AudioNote note : notes) {
                        playNoteToBox(box, note, masterVol);
                    }
                }
            }

            broadcastTick++;
            radioAccumulator -= 1.0;

            if (broadcastTick >= currentTrack.getLengthInTicks()) {
                int pauseSecs = plugin.getConfigManager().getRadioPauseSeconds();
                if (pauseSecs > 0) {
                    pauseCounter = pauseSecs * 20;
                } else {
                    advanceTrack();
                }
                return;
            }
        }
    }

    private void advanceTrack() {
        if (stationQueue.isEmpty()) {
            refreshQueue();
            if (stationQueue.isEmpty()) return;
        }

        currentQueueIndex = (currentQueueIndex + 1) % stationQueue.size();
        currentTrack = stationQueue.get(currentQueueIndex);
        broadcastTick = 0;
        radioAccumulator = 0.0;
        radioAuditionCounted = false;
    }

    private void playNoteToBox(MusicBox box, AudioNote note, float masterVol) {
        float noteVol = note.volume() * masterVol * 2.0f;
        float pitch = note.pitch();

        if (box.isHeadphones() && box.getOwnerUuid() != null) {
            Player owner = Bukkit.getPlayer(box.getOwnerUuid());
            if (owner != null && owner.isOnline()) {
                owner.playSound(owner.getLocation(), note.sound(), SoundCategory.RECORDS, noteVol, pitch);
            }
            return;
        }

        Location loc = box.getLocation();
        if (loc != null && loc.getWorld() != null) {
            if (!box.isPortable() && !loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                return;
            }
            loc.getWorld().playSound(loc, note.sound(), SoundCategory.RECORDS, noteVol, pitch);
        }
    }
}
