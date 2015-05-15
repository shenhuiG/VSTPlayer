package com.vst.LocalPlayer.model;


public class MediaInfo {
    public long id = -1;
    public String name;
    public String title;
    public String path;
    public long deviceId;
    public String devicePath;
    public String poster;

    public MediaInfo() {
    }

    public MediaInfo(long id, String path, String name, String title, String poster, long deviceId, String devicePath) {
        this.path = path;
        this.name = name;
        this.devicePath = devicePath;
        this.title = title;
        this.poster = poster;
        this.id = id;
        this.deviceId = deviceId;
    }

    @Override
    public String toString() {
        return path + "," + name + ",";
    }
}
