package com.lino.custommusic.hook;

import com.lino.custommusic.CustomMusicPlugin;
import com.lino.custommusic.model.MusicBox;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlaceholderHook extends PlaceholderExpansion {
    private final CustomMusicPlugin plugin;

    public PlaceholderHook(CustomMusicPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "custommusic";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Lino";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equalsIgnoreCase("radio_song")) {
            return plugin.getRadioStation().getCurrentTrack() != null
                    ? plugin.getRadioStation().getCurrentTrack().getTitle()
                    : "None";
        }

        if (player == null) {
            return "";
        }

        MusicBox companion = plugin.getCompanionManager().getCompanionBox(player);
        if (companion == null) {
            return switch (params.toLowerCase()) {
                case "is_playing" -> "false";
                case "current_song" -> "None";
                case "volume" -> "0";
                default -> "";
            };
        }

        return switch (params.toLowerCase()) {
            case "is_playing" -> String.valueOf(companion.isPlaying());
            case "current_song" -> companion.getCurrentSong() != null ? companion.getCurrentSong().getTitle() : "None";
            case "volume" -> String.valueOf(companion.getVolume());
            default -> "";
        };
    }
}
