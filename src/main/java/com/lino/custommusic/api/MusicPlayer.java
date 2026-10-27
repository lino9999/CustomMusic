package com.lino.custommusic.api;

public interface MusicPlayer {
    void play();
    void pause();
    void stop();
    boolean isPlaying();
    boolean isPaused();
    void nextTrack();
    void previousTrack();
    int getVolume();
    void setVolume(int volume);
    void setPlaybackSpeed(float speed);
    float getPlaybackSpeed();
}
