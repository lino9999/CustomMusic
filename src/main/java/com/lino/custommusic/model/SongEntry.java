package com.lino.custommusic.model;

import com.lino.custommusic.api.SongTrack;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SongEntry implements SongTrack {
    private final String id;
    private final String title;
    private final String author;
    private final int lengthInTicks;
    private final int durationSeconds;
    private final double price;
    private final String permission;
    private final Map<Integer, List<AudioNote>> notesByTick;
    private final boolean customSound;
    private final String customSoundName;
    private int auditionCount;
    private int purchaseCount;

    public SongEntry(String id, String title, String author, int lengthInTicks, int durationSeconds, double price, String permission, Map<Integer, List<AudioNote>> notesByTick) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.lengthInTicks = lengthInTicks;
        this.durationSeconds = durationSeconds;
        this.price = price;
        this.permission = permission;
        this.notesByTick = notesByTick != null ? notesByTick : Collections.emptyMap();
        this.customSound = false;
        this.customSoundName = null;
        this.auditionCount = 0;
        this.purchaseCount = 0;
    }

    public SongEntry(String id, String title, String author, int durationSeconds, double price, String permission, String customSoundName) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.lengthInTicks = durationSeconds * 20;
        this.durationSeconds = durationSeconds;
        this.price = price;
        this.permission = permission;
        this.notesByTick = Collections.emptyMap();
        this.customSound = true;
        this.customSoundName = customSoundName;
        this.auditionCount = 0;
        this.purchaseCount = 0;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getAuthor() {
        return author;
    }

    @Override
    public int getLengthInTicks() {
        return lengthInTicks;
    }

    @Override
    public int getDurationSeconds() {
        return durationSeconds;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public Map<Integer, List<AudioNote>> getNotesByTick() {
        return notesByTick;
    }

    @Override
    public boolean isCustomSound() {
        return customSound;
    }

    @Override
    public String getCustomSoundName() {
        return customSoundName;
    }

    @Override
    public int getAuditionCount() {
        return auditionCount;
    }

    @Override
    public int getPurchaseCount() {
        return purchaseCount;
    }

    @Override
    public void incrementAuditions() {
        this.auditionCount++;
    }

    @Override
    public void incrementPurchases() {
        this.purchaseCount++;
    }

    public void setAuditionCount(int auditionCount) {
        this.auditionCount = auditionCount;
    }

    public void setPurchaseCount(int purchaseCount) {
        this.purchaseCount = purchaseCount;
    }
}
