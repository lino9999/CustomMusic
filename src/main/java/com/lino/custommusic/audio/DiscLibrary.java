package com.lino.custommusic.audio;

import com.lino.custommusic.model.DiscRecord;

import java.util.Arrays;
import java.util.List;

public final class DiscLibrary {
    private DiscLibrary() {}

    public static List<DiscRecord> getAllDiscs() {
        return Arrays.asList(DiscRecord.values());
    }

    public static DiscRecord getDiscByName(String name) {
        if (name == null) return null;
        for (DiscRecord record : DiscRecord.values()) {
            if (record.name().equalsIgnoreCase(name) || record.getDisplayName().equalsIgnoreCase(name)) {
                return record;
            }
        }
        return null;
    }
}
