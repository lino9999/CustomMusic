package com.lino.custommusic.audio;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.AudioNote;
import com.lino.custommusic.model.MusicBox;
import com.lino.custommusic.model.SongEntry;
import com.lino.custommusic.visual.DiscoBallEffect;
import com.lino.custommusic.visual.DiscoFloorEffect;
import com.lino.custommusic.visual.NoteParticleEffect;
import com.lino.custommusic.visual.ProgressDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class PlaybackEngine {
    private final CustomMusicPlugin plugin;
    private final MusicBox musicBox;
    private final ProgressDisplay progressDisplay;
    private final NoteParticleEffect particleEffect;
    private final DiscoFloorEffect floorEffect;
    private final DiscoBallEffect ballEffect;

    private BukkitTask playbackTask;
    private int currentTick = 0;
    private int totalTicks = 0;
    private double tickAccumulator = 0.0;
    private boolean paused = false;

    public PlaybackEngine(CustomMusicPlugin plugin, MusicBox musicBox) {
        this.plugin = plugin;
        this.musicBox = musicBox;
        this.progressDisplay = new ProgressDisplay(plugin, musicBox);
        this.particleEffect = new NoteParticleEffect(musicBox);
        this.floorEffect = new DiscoFloorEffect(plugin, musicBox);
        this.ballEffect = new DiscoBallEffect(plugin, musicBox);
    }

    public void start() {
        stop();
        SongEntry song = musicBox.getCurrentSong();
        if (song == null) {
            return;
        }

        currentTick = 0;
        totalTicks = song.getLengthInTicks();
        tickAccumulator = 0.0;
        paused = false;

        song.incrementAuditions();
        plugin.getDatabaseService().updateSongStatsAsync(song);

        playbackTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public void stop() {
        if (playbackTask != null) {
            playbackTask.cancel();
            playbackTask = null;
        }
        currentTick = 0;
        tickAccumulator = 0.0;
        paused = false;

        SongEntry song = musicBox.getCurrentSong();
        if (song != null && song.isCustomSound()) {
            stopCustomSound(song.getCustomSoundName());
        }

        progressDisplay.clear();
        floorEffect.reset();
        ballEffect.remove();
    }

    public boolean isPlaying() {
        return playbackTask != null && !paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public DiscoFloorEffect getFloorEffect() {
        return floorEffect;
    }

    public DiscoBallEffect getBallEffect() {
        return ballEffect;
    }

    private void tick() {
        if (paused) {
            return;
        }

        if (!musicBox.isPortable()) {
            Location loc = musicBox.getLocation();
            if (loc == null || loc.getWorld() == null || !loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                return;
            }
        }

        SongEntry song = musicBox.getCurrentSong();
        if (song == null) {
            stop();
            return;
        }

        if (song.isCustomSound()) {
            tickCustomSound(song);
        } else {
            tickNbs(song);
        }

        particleEffect.tick();
        floorEffect.tick();
        ballEffect.tick();
    }

    private void tickNbs(SongEntry song) {
        float tempo = song.getTempo();
        float speedMultiplier = musicBox.getPlaybackSpeedMultiplier();
        tickAccumulator += (tempo / 20.0) * speedMultiplier;

        float masterVol = (musicBox.getVolume() / 100.0f) * plugin.getConfigManager().getVolumeMultiplier();

        while (tickAccumulator >= 1.0) {
            List<AudioNote> notes = song.getNotesByTick().get(currentTick);
            if (notes != null && !notes.isEmpty()) {
                for (AudioNote note : notes) {
                    playNote(note, masterVol);
                }
            }

            currentTick++;
            tickAccumulator -= 1.0;

            if (currentTick >= totalTicks) {
                handleSongEnd();
                return;
            }
        }

        int currentSecs = (int) (currentTick / tempo);
        int totalSecs = song.getDurationSeconds();
        progressDisplay.update(song.getTitle(), currentSecs, totalSecs);
    }

    private void tickCustomSound(SongEntry song) {
        if (currentTick == 0) {
            playCustomSound(song.getCustomSoundName());
        }

        int currentSecs = currentTick / 20;
        int totalSecs = song.getDurationSeconds();
        progressDisplay.update(song.getTitle(), currentSecs, totalSecs);

        currentTick++;
        if (currentTick >= totalTicks) {
            handleSongEnd();
        }
    }

    private void handleSongEnd() {
        stop();
        if (musicBox.isAutoPlay() || musicBox.hasMorePlaylistTracks()) {
            musicBox.playNextTrack(plugin);
        } else {
            musicBox.setCurrentSong(null);
        }
    }

    private void playNote(AudioNote note, float masterVol) {
        float noteVol = note.volume() * masterVol * 2.0f;
        float pitch = note.pitch();

        if (musicBox.isHeadphones()) {
            Player target = musicBox.getPortableCarrier();
            if (target == null && musicBox.getOwnerUuid() != null) {
                target = Bukkit.getPlayer(musicBox.getOwnerUuid());
            }
            if (target != null && target.isOnline()) {
                target.playSound(target.getLocation(), note.sound(), SoundCategory.RECORDS, noteVol, pitch);
            }
            return;
        }

        Location loc = musicBox.getLocation();
        if (loc != null && loc.getWorld() != null) {
            loc.getWorld().playSound(loc, note.sound(), SoundCategory.RECORDS, noteVol, pitch);
        }
    }

    private void playCustomSound(String soundName) {
        float masterVol = (musicBox.getVolume() / 100.0f) * plugin.getConfigManager().getVolumeMultiplier();
        float pitch = musicBox.getPlaybackSpeedMultiplier();

        org.bukkit.Sound bukkitSound = null;
        try {
            bukkitSound = org.bukkit.Sound.valueOf(soundName);
        } catch (IllegalArgumentException ignored) {}

        if (musicBox.isHeadphones()) {
            Player target = musicBox.getPortableCarrier();
            if (target == null && musicBox.getOwnerUuid() != null) {
                target = Bukkit.getPlayer(musicBox.getOwnerUuid());
            }
            if (target != null && target.isOnline()) {
                if (bukkitSound != null) {
                    target.playSound(target.getLocation(), bukkitSound, SoundCategory.RECORDS, masterVol, pitch);
                } else {
                    target.playSound(target.getLocation(), soundName, SoundCategory.RECORDS, masterVol, pitch);
                }
            }
            return;
        }

        Location loc = musicBox.getLocation();
        if (loc != null && loc.getWorld() != null) {
            if (bukkitSound != null) {
                loc.getWorld().playSound(loc, bukkitSound, SoundCategory.RECORDS, masterVol, pitch);
            } else {
                loc.getWorld().playSound(loc, soundName, SoundCategory.RECORDS, masterVol, pitch);
            }
        }
    }

    private void stopCustomSound(String soundName) {
        org.bukkit.Sound bukkitSound = null;
        try {
            bukkitSound = org.bukkit.Sound.valueOf(soundName);
        } catch (IllegalArgumentException ignored) {}

        if (musicBox.isHeadphones()) {
            Player target = musicBox.getPortableCarrier();
            if (target == null && musicBox.getOwnerUuid() != null) {
                target = Bukkit.getPlayer(musicBox.getOwnerUuid());
            }
            if (target != null && target.isOnline()) {
                if (bukkitSound != null) {
                    target.stopSound(bukkitSound, SoundCategory.RECORDS);
                } else {
                    target.stopSound(soundName, SoundCategory.RECORDS);
                }
            }
            return;
        }

        Location loc = musicBox.getLocation();
        if (loc != null && loc.getWorld() != null) {
            double maxDistSq = 32.0 * 32.0;
            for (Player p : loc.getWorld().getPlayers()) {
                if (p.getLocation().distanceSquared(loc) <= maxDistSq) {
                    if (bukkitSound != null) {
                        p.stopSound(bukkitSound, SoundCategory.RECORDS);
                    } else {
                        p.stopSound(soundName, SoundCategory.RECORDS);
                    }
                }
            }
        }
    }
}
