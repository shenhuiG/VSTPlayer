package com.vst.LocalPlayer.model;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by shenh on 2015/3/31.
 */
public class NFOApi {

    public static File getNfoInfo(String mediaPath) {
        //find nfo file
        File mediaFile = new File(mediaPath);
        File parent = mediaFile.getParentFile();
        String name = mediaFile.getName();
        //same name
        String nfoName = name.substring(0, name.lastIndexOf(".")) + ".nfo";
        File nfoFile = new File(parent, nfoName);
        if (!nfoFile.exists()) {
            File[] files = parent.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.endsWith(".nfo");
                }
            });
            if (files != null && files.length > 0) {
                nfoFile = files[0];
            }
        }
        if (nfoFile.exists()) {
            return nfoFile;
        }
        return null;
    }

    public static String getImdbIdFromNFOFile(String mediaPath) {
        File nfoFile = getNfoInfo(mediaPath);
        if (nfoFile != null && nfoFile.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(nfoFile)));
                String buff;
                while ((buff = reader.readLine()) != null) {
                    buff = buff.toLowerCase();
                    if ((buff.contains("imdb link") || buff.contains("imdb url")) && buff.contains(":")) {
                        System.out.println(buff);
                        String imdbInfo = buff.substring(buff.lastIndexOf(":"));
                        if (imdbInfo.contains("tt")) {
                            imdbInfo = imdbInfo.substring(imdbInfo.indexOf("tt"));
                            imdbInfo = imdbInfo.replaceAll("[^0-9a-z]", "");
                            return imdbInfo;
                        }
                    }
                }
            } catch (IOException e) {

            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
    }


    private static void parseNFOFile(File nfoFile) throws IOException {
        System.out.println(nfoFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(nfoFile)));
        String buff;
        while ((buff = reader.readLine()) != null) {
            if (!TextUtils.isEmpty(buff)) {
                buff = buff.replaceAll("[^0-9a-zA-Z:@#%-|]", " ");
                if (!TextUtils.isEmpty(buff)) {
                    System.out.println(buff);
                }
            }
        }
    }

}
