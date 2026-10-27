package com.lino.custommusic.audio;

import com.lino.custommusic.model.AudioNote;
import com.lino.custommusic.model.SongEntry;
import org.bukkit.Sound;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NbsParser {
    private NbsParser() {}

    public static SongEntry parse(File file, String id, double price, String permission) {
        if (file == null || !file.exists()) {
            return null;
        }

        try (DataInputStream in = new DataInputStream(new FileInputStream(file))) {
            short songLength = readShort(in);
            byte nbsVersion = 0;
            byte defaultInstruments = 10;

            if (songLength == 0) {
                nbsVersion = in.readByte();
                defaultInstruments = in.readByte();
                if (nbsVersion >= 3) {
                    songLength = readShort(in);
                }
            }

            short songLayers = readShort(in);
            String title = readString(in);
            String author = readString(in);
            readString(in);
            readString(in);

            short tempoRaw = readShort(in);
            float tempo = tempoRaw / 100.0f;
            if (tempo <= 0.0f) {
                tempo = 20.0f;
            }

            in.readBoolean();
            in.readByte();
            in.readByte();
            readInt(in);
            readInt(in);
            readInt(in);
            readInt(in);
            readInt(in);
            readString(in);

            if (nbsVersion >= 4) {
                in.readByte();
                in.readByte();
                readShort(in);
            }

            Map<Integer, List<AudioNote>> notesByTick = new HashMap<>();
            short currentTick = -1;
            short tickJumps;

            while ((tickJumps = readShort(in)) != 0) {
                currentTick += tickJumps;
                short currentLayer = -1;
                short layerJumps;

                while ((layerJumps = readShort(in)) != 0) {
                    currentLayer += layerJumps;
                    byte instrument = in.readByte();
                    byte key = in.readByte();
                    float noteVolume = 1.0f;

                    if (nbsVersion >= 4) {
                        byte velocity = in.readByte();
                        in.readByte();
                        readShort(in);
                        noteVolume = Math.max(0.0f, Math.min(1.0f, velocity / 100.0f));
                    }

                    int noteVal = key - 33;
                    if (noteVal < 0) {
                        noteVal = 0;
                    } else if (noteVal > 24) {
                        noteVal = 24;
                    }

                    float pitch = (float) Math.pow(2.0, (noteVal - 12) / 12.0);
                    Sound sound = SoundMapper.getInstrumentSound(instrument);

                    notesByTick.computeIfAbsent((int) currentTick, k -> new ArrayList<>())
                            .add(new AudioNote(currentTick, sound, pitch, noteVolume));
                }
            }

            int finalDurationTicks = Math.max((int) songLength, (int) currentTick);
            int durationSeconds = (int) Math.ceil(finalDurationTicks / (double) tempo);
            if (durationSeconds <= 0) {
                durationSeconds = 1;
            }

            String displayTitle = (title != null && !title.isBlank()) ? title : file.getName().replace(".nbs", "").replace("_", " ");
            String displayAuthor = (author != null && !author.isBlank()) ? author : "Unknown";

            return new SongEntry(
                    id,
                    displayTitle,
                    displayAuthor,
                    finalDurationTicks,
                    durationSeconds,
                    tempo,
                    price,
                    permission,
                    notesByTick
            );
        } catch (IOException e) {
            return null;
        }
    }

    private static short readShort(DataInputStream in) throws IOException {
        int byte1 = in.readUnsignedByte();
        int byte2 = in.readUnsignedByte();
        return (short) (byte1 | (byte2 << 8));
    }

    private static int readInt(DataInputStream in) throws IOException {
        int byte1 = in.readUnsignedByte();
        int byte2 = in.readUnsignedByte();
        int byte3 = in.readUnsignedByte();
        int byte4 = in.readUnsignedByte();
        return byte1 | (byte2 << 8) | (byte3 << 16) | (byte4 << 24);
    }

    private static String readString(DataInputStream in) throws IOException {
        int length = readInt(in);
        if (length <= 0) {
            return "";
        }
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8).replace('\r', ' ').trim();
    }
}
