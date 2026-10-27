package com.lino.custommusic.api;

import com.lino.custommusic.model.AudioNote;
import java.util.List;
import java.util.Map;

public interface SongTrack {
    String getId();
    String getTitle();
    String getAuthor();
    int getLengthInTicks();
    int getDurationSeconds();
    double getPrice();
    String getPermission();
    Map<Integer, List<AudioNote>> getNotesByTick();
    boolean isCustomSound();
    String getCustomSoundName();
    int getAuditionCount();
    int getPurchaseCount();
    void incrementAuditions();
    void incrementPurchases();
}
