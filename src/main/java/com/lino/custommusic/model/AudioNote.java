package com.lino.custommusic.model;

import org.bukkit.Sound;

public record AudioNote(int tick, Sound sound, float pitch, float volume) {
}
