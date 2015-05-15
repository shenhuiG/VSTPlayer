package com.vst.LocalPlayer.model;

import com.vst.dev.common.media.SubTrack;

import java.io.File;
import java.io.FilenameFilter;

public class SubtripApi {
    private static File findSrtFile(String mediaPath) {
        File f = new File(mediaPath.replace("file://", ""));
        System.out.println("findSrtFile " + mediaPath);
        if (f.exists()) {
            File parent = f.getParentFile();
            String name = f.getName();
            name = name.substring(0, name.lastIndexOf("."));
            System.out.println("name=" + name);
            //同名的srt
            File srt = new File(parent, name + ".srt");
            if (srt.exists()) {
                System.out.println("srt=" + srt);
                return srt;
            } else {
                //不同名的 类似的
            }
        }
        return null;
    }


    public static SubTrack[] getLocalSubTitle(File f) {
        if (f.exists()) {
            File parent = f.getParentFile();
            File[] files = parent.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String s) {
                    return s.endsWith(".srt");
                }
            });
            if (files != null && files.length > 0) {
                SubTrack[] ss = new SubTrack[files.length];
                for (int i = 0; i < files.length; i++) {
                    File srt = files[i];
                    SubTrack s = new SubTrack();
                    s.name = srt.getName();
                    s.path = srt.getAbsolutePath();
                    s.from = SubTrack.SubTrackType.Local;
                    ss[i] = s;
                }
                return ss;
            }
        }
        return null;
    }
}
