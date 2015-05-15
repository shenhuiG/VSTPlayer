package com.vst.LocalPlayer.model;

import java.io.Serializable;

public class DeviceInfo implements Serializable {
    public String path;
    public long id;
    public String name;
    public String uuid;

    public DeviceInfo(long id, String path, String uuid) {
        this.id = id;
        this.path = path;
        this.uuid = uuid;
        name = path.substring(path.lastIndexOf("/") + 1);
    }
}