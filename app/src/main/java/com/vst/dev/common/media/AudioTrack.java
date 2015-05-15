package com.vst.dev.common.media;

public class AudioTrack {
    public int trackId;
    public String language;

    @Override
    public String toString() {
        return language + "," + trackId;
    }
}
