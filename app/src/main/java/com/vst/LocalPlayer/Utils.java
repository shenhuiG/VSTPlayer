package com.vst.LocalPlayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StatFs;
import android.util.Log;

import com.vst.LocalPlayer.component.activity.PlayerActivity;
import com.vst.LocalPlayer.model.FileCategory;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Utils {

    private static final String TAG = "Utils";

//    public static void getDeviceName(Context ctx) {
//        System.out.println("CCCCCCCCCCCCCCCCCCCCCCC");
////        UsbManager manager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
////        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
////        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
////        String strDeviceNameList = "";
////        while (deviceIterator.hasNext()) {
////            UsbDevice device = deviceIterator.next();
////            System.out.println("Name: " + device.getDeviceName() + "\n"
////                    + "VID: " + device.getVendorId()
////                    + "       PID: " + device.getProductId());
////        }
//
//        try {
//            //获得外接USB输入设备的信息
//            Process p = Runtime.getRuntime().exec("cat /proc/bus/input/devices");
//            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
//            String line = null;
//            while ((line = in.readLine()) != null) {
//                String deviceInfo = line.trim();
//                //对获取的每行的设备信息进行过滤，获得自己想要的。
//                System.out.println(deviceInfo);
//            }
//
//        } catch (Exception e) {
//            // TODO: handle exception
//            e.printStackTrace();
//        }
//    }


    /**
     * 截取720P 标记之前，后面的全部丢掉
     *
     * @param fileName
     * @return
     */
    public static String smartMediaName(String fileName) {
        String reg = "WinG|ENG|aAf|3D|720P|720p|1080P|1080p|X264|DTS|BluRay|Bluray|HSBS|x264|CHD|H-SBS|" +
                "Wiki|WiKi|ML|RemuX|CnSCG|HDChina|Sample|sample|AVC|MA|5.1|AC3|AAC|rip|265|" +
                "HDTV|DL|DHD|HD|HEVC|DiCH|dich|dhd|hdtv|Pix|BAWLS|hv|NG";
        //reduce ext
        String result = fileName.substring(0, fileName.lastIndexOf("."));
        //reduce other word like 720P,DTS,X264..
        result = result.replaceAll(reg, "");
        //reduce last .
        String endReg = "[0-9]|[|]|.| |-";
        String endX = "0123456789.-[] ";
        Pattern pattern = Pattern.compile(endReg, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(result);
        if (matcher.matches()) {
            return result;
        }
        if (result.length() > 2) {
            String endChar = result.substring(result.length() - 1, result.length());
            while (endX.contains(endChar)) {
                result = result.substring(0, result.length() - 1);
                if (result.length() > 2) {
                    endChar = result.substring(result.length() - 1, result.length());
                } else {
                    break;
                }
            }
        }
        return result;
    }


    public static Uri getMediaUri(String mediaPath) {
        File f = new File(mediaPath);
        if (f.exists()) {
            FileCategory category = getFileCategory(f);
            if (category == FileCategory.BDMV) {
                return Uri.parse("bluray://" + f.getAbsolutePath());
            } else if (category == FileCategory.Video) {
                return Uri.fromFile(f);
            }
        }
        return null;
    }

    public static void playMediaFile(Context ctx, Uri uri, long id, long deviceId, String devicePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "video/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(ctx.getPackageName());
        intent.setClass(ctx, PlayerActivity.class);
        if (id >= 0) {
            Bundle args = new Bundle();
            args.putLong("_id", id);
            args.putLong("deviceId", deviceId);
            args.putString("devicePath", devicePath);
            intent.putExtras(args);
        }
        ctx.startActivity(intent);
    }

    public static void playAudioFile(Context ctx, File mediaFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(mediaFile), "audio/*");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(intent);
    }


    public static boolean isBDMV(File dir) {
        if (dir.isDirectory()) {
            File bdmvDir = new File(dir, "BDMV");
            File certificateDir = new File(dir, "CERTIFICATE");
            if (bdmvDir.exists() && certificateDir.exists()) {
                return true;
            }
        }
        return false;
    }


    public static FileCategory getFileCategory(File file) {
        if (file.exists()) {
            if (file.isDirectory()) {
                if (isBDMV(file)) {
                    return FileCategory.BDMV;
                } else {
                    return FileCategory.Dir;
                }
            }
            String fileName = file.getName();
            int i = fileName.lastIndexOf(".");
            String ext = fileName.substring(i + 1);
            if ("apk".equalsIgnoreCase(ext)) {
                return FileCategory.Apk;
            }
            if ("txt".equalsIgnoreCase(ext) || "doc".equalsIgnoreCase(ext) || "pdf".equalsIgnoreCase(ext) || "xls".equalsIgnoreCase(ext)) {
                return FileCategory.Doc;
            }
            if ("jpg".equalsIgnoreCase(ext) || "png".equalsIgnoreCase(ext) || "jepg".equalsIgnoreCase(ext)) {
                return FileCategory.Picture;
            }
            if ("rar".equals(ext) || "zip".equalsIgnoreCase(ext) || "iso".equalsIgnoreCase(ext)) {
                return FileCategory.Zip;
            }
            if ("mp3".equals(ext) || "wma".equalsIgnoreCase(ext) || "ogg".equalsIgnoreCase(ext) || "acc".equalsIgnoreCase(ext)
                    || "wav".equalsIgnoreCase(ext)) {
                return FileCategory.Music;
            }
            if ("mp4".equalsIgnoreCase(ext) || "mkv".equalsIgnoreCase(ext) || "rmvb".equalsIgnoreCase(ext) || "avi".equalsIgnoreCase(ext)
                    || "flv".equalsIgnoreCase(ext) || "ts".equalsIgnoreCase(ext) || "rmvb".equalsIgnoreCase(ext)
                    || "vob".equalsIgnoreCase(ext) || "webm".equalsIgnoreCase(ext) || "wmv".equalsIgnoreCase(ext)
                    || "arm".equalsIgnoreCase(ext) || "ra".equalsIgnoreCase(ext)
                    || "wac".equalsIgnoreCase(ext)
                    || "m2ts".equalsIgnoreCase(ext)
                    ) {
                return FileCategory.Video;
            }
            return FileCategory.Other;
        }
        return null;
    }

    public static int getFileCategoryIcon(FileCategory category) {
        if (category != null) {
            switch (category) {
                case Music:
                    return R.drawable.ic_disk_music;
                case Video:
                case BDMV:
                    return R.drawable.ic_disk_video;
                case Picture:
                    return R.drawable.ic_disk_picture;
                case Doc:
                    return R.drawable.ic_disk_txt;
                case Zip:
                    return R.drawable.ic_disk_rar;
                case Apk:
                    return R.drawable.ic_disk_apk;
                case Dir:
                    return R.drawable.ic_disk_folder;
                default:
                    return R.drawable.ic_disk_unknown;
            }
        }
        return 0;
    }

    public static int getFileCategoryIcon(File file) {
        FileCategory category = getFileCategory(file);
        if (category != null) {
            switch (category) {
                case Music:
                    return R.drawable.ic_disk_music;
                case Video:
                case BDMV:
                    return R.drawable.ic_disk_video;
                case Picture:
                    return R.drawable.ic_disk_picture;
                case Doc:
                    return R.drawable.ic_disk_txt;
                case Zip:
                    return R.drawable.ic_disk_rar;
                case Apk:
                    return R.drawable.ic_disk_apk;
                case Dir:
                    return R.drawable.ic_disk_folder;
                default:
                    return R.drawable.ic_disk_unknown;
            }
        }
        return 0;
    }


    public static boolean fileIsVideo(File file) {
        FileCategory category = getFileCategory(file);
        return category == FileCategory.BDMV || category == FileCategory.Video;
    }


    public static String fileSizeFormat(long size) {
        float mbSize = size / 1000f / 1000f;
        if (mbSize < 1000f) {
            return String.format("%d MB ", Math.round(mbSize));
        } else {
            float gSize = mbSize / 1000f;
            if (gSize < 1000f) {
                return String.format("%d G ", Math.round(gSize));
            } else {
                float TSize = gSize / 1000f;
                return String.format("%d T ", Math.round(TSize));
            }

        }
    }


    public static String writeDeviceUUID(String devicePath) {
        File root = new File(devicePath);
        if (root.isDirectory()) {
            String uuid = UUID.randomUUID().toString();
            File uFile = new File(devicePath, ".uuid");
            FileWriter writer = null;
            try {
                writer = new FileWriter(uFile);
                writer.write(uuid);
                Log.d(TAG, "writeDeviceUUID uuid:" + uuid);
                return uuid;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }


    public static String readDeviceUUID(String devicePath) {
        String uuid = null;
        File root = new File(devicePath);
        if (root.isDirectory()) {
            File uFile = new File(devicePath, ".uuid");
            FileReader reader = null;
            try {
                char[] buffer = new char[36];
                reader = new FileReader(uFile);
                reader.read(buffer, 0, buffer.length);
                uuid = new String(buffer);
                Log.d(TAG, "readDeviceUUID uuid:" + uuid);
                return uuid;
            } catch (IOException e) {
                e.printStackTrace();
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
        return uuid;
    }


    public static long getDeviceLaseTime(String devicePath) {
        File root = new File(devicePath);
        if (root.isDirectory()) {
            //1
            long time1 = root.lastModified();
            Log.d(TAG, "getDeviceLaseTime time1:" + new Date(time1));
            return time1;
            /*long time2 = 0;
            File[] files = root.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return !filename.equals(".uuid");
                }
            });
            if (files != null && files.length > 0) {
                for (File f : files) {
                    time2 = Math.max(time2, f.lastModified());
                }
            }
            Log.d(TAG, "getDeviceLaseTime time2:" + new Date(time2));*/
        }
        return 0;
    }

}
