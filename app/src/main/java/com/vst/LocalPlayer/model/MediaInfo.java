package com.vst.LocalPlayer.model;


import java.io.Serializable;

public class MediaInfo implements Serializable {
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
    public boolean equals(Object o) {
        if (o instanceof MediaInfo) {
            MediaInfo m = (MediaInfo) o;
            return m.id == id && m.name.equals(name) && m.path.equals(path);
        }
        return super.equals(o);
    }

    @Override
    public String toString() {
        return path + "," + name + ",";
    }
}
