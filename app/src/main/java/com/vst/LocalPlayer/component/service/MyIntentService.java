package com.vst.LocalPlayer.component.service;

import android.app.IntentService;
import android.content.*;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.vst.LocalPlayer.UpgradeUtils;
import com.vst.LocalPlayer.model.IMDBApi;
import com.vst.LocalPlayer.model.NFOApi;
import com.vst.LocalPlayer.UUtils;
import com.vst.LocalPlayer.component.provider.MediaStore;
import com.vst.LocalPlayer.component.provider.MediaStoreHelper;
import com.vst.LocalPlayer.widget.WindowLoadingHelper;
import com.vst.dev.common.util.MD5Util;
import com.vst.dev.common.util.Utils;

import java.io.File;
import java.io.FileFilter;

public class MyIntentService extends IntentService {
    private static final String ACTION_SCANNER = "com.vst.LocalPlayer.component.service.action.SCANNER";
    private static final String ACTION_SCANNER_DEVICE_ID = "deviceId";
    private static final String ACTION_SCANNER_DEVICE_PATH = "devicePath";
    private static final String ACTION_UPDATE_VALID = "com.vst.LocalPlayer.component.service.action.UpdateDeviceAndMediaValid";
    private static final String ACTION_ENTRY_INFO = "com.vst.LocalPlayer.component.service.action.EntryNfo";
    private static final String ACTION_ENTRY_INFO_PATH = "path";
    private static final String ACTION_ENTRY_INFO_META_TITLE = "metaTitle";
    private static final String ACTION_ENTRY_INFO_MEDIA_ID = "mediaId";
    private static final String ACTION_UPGRADE = "com.vst.LocalPlayer.component.service.action.UPGRADE";

    public static void startActionScanner(Context context, String devicePath, long deviceId) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_SCANNER);
        intent.putExtra(ACTION_SCANNER_DEVICE_PATH, devicePath);
        intent.putExtra(ACTION_SCANNER_DEVICE_ID, deviceId);
        context.startService(intent);
    }


    public static void startActionUpdateValid(Context context) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_UPDATE_VALID);
        context.startService(intent);
    }


    public static void startActionEntryInfo(Context context, String mediaPath, String metaTitle, long mediaId) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_ENTRY_INFO);
        intent.putExtra(ACTION_ENTRY_INFO_MEDIA_ID, mediaId);
        intent.putExtra(ACTION_ENTRY_INFO_PATH, mediaPath);
        intent.putExtra(ACTION_ENTRY_INFO_META_TITLE, metaTitle);
        context.startService(intent);
    }

    public static void startActionUpgrade(Context context) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_UPGRADE);
        context.startService(intent);
    }


    private static void handleActionUpgrad(Context context) {
        //从sp中获取信息
        Bundle b = UpgradeUtils.getUpgradeInfoFromSp(context);
        if (b == null) {
            b = UpgradeUtils.getUpgradeInfoFromNet(context);
        }
        if (b == null) {
            return;
        }
        File apkFile = UpgradeUtils.getApkFile(context);
        if (apkFile.exists()) {
            if (MD5Util.getFileMD5String(apkFile).equalsIgnoreCase(b.getString(UpgradeUtils.MD5))) {
                //发送更新提示
                Utils.modifyFile(apkFile);
                b.putParcelable(UpgradeUtils.APK_FILE_URI, Uri.fromFile(apkFile));
                UpgradeUtils.sendUpgradeBrodCast(context, b);
                return;
            } else {
                apkFile.delete();
            }
        }
        String url = b.getString(UpgradeUtils.URL);
        String md5 = b.getString(UpgradeUtils.MD5);
        if (UpgradeUtils.saveApkFile(context, url, md5)) {
            //发送更新提示
            Utils.modifyFile(apkFile);
            b.putParcelable(UpgradeUtils.APK_FILE_URI, Uri.fromFile(apkFile));
            UpgradeUtils.sendUpgradeBrodCast(context, b);
            return;
        }
    }

    public MyIntentService() {
        super("IntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SCANNER.equals(action)) {
                final String devicePath = intent.getStringExtra(ACTION_SCANNER_DEVICE_PATH);
                final long deviceId = intent.getLongExtra(ACTION_SCANNER_DEVICE_ID, -1);
                if (deviceId < 0 || devicePath == null || "".equals(devicePath)) {
                    throw new IllegalArgumentException("the device info is null");
                } else {
                    handleActionScanner(getContentResolver(), new File(devicePath), deviceId);
                }
            } else if (ACTION_UPDATE_VALID.equals(action)) {
                handleActionUpdateValid(getContentResolver());
            } else if (ACTION_ENTRY_INFO.equals(action)) {
                String path = intent.getStringExtra(ACTION_ENTRY_INFO_PATH);
                long id = intent.getLongExtra(ACTION_ENTRY_INFO_MEDIA_ID, -1);
                String mataTitle = intent.getStringExtra(ACTION_ENTRY_INFO_META_TITLE);
                handlerActionMediaInfo(getContentResolver(), path, mataTitle, id);
            } else if (ACTION_UPGRADE.equals(action)) {
                handleActionUpgrad(this);
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        WindowLoadingHelper.setLaoding(this, true);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WindowLoadingHelper.setLaoding(this, false);
    }

    private void handlerActionAddMedia(ContentResolver cr, String mediaPath, String devicePath, long deviceId) {
        String title = null;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(mediaPath);
            title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        } catch (Exception e) {
        } finally {
            mmr.release();
        }
        String relativePath = mediaPath.replace(devicePath, "");
        long mediaId = MediaStoreHelper.mediaIsInStore(cr, deviceId, relativePath);
        if (mediaId < 0) {
            Uri uri = MediaStoreHelper.addNewMediaBase(cr, mediaPath, devicePath, deviceId, title);
            if (uri != null) {
                mediaId = ContentUris.parseId(uri);
                startActionEntryInfo(this, mediaPath, title, mediaId);
            }
        } else {
            startActionEntryInfo(this, mediaPath, title, mediaId);
        }
    }


    private void handleActionUpdateValid(ContentResolver cr) {
        Cursor c = getContentResolver().query(com.vst.LocalPlayer.component.provider.MediaStore.MediaDevice.CONTENT_URI, null,
                null, null, null);
        while (c.moveToNext()) {
            String devicePath = c.getString(c.getColumnIndex(com.vst.LocalPlayer.component.provider.MediaStore.MediaDevice.FIELD_DEVICE_PATH));
            String deviceUUID = c.getString(c.getColumnIndex(com.vst.LocalPlayer.component.provider.MediaStore.MediaDevice.FIELD_DEVICE_UUID));
            long deviceId = c.getLong(c.getColumnIndex(com.vst.LocalPlayer.component.provider.MediaStore.MediaDevice._ID));
            boolean valid = MediaStoreHelper.checkDeviceValid(devicePath, deviceUUID);
            MediaStoreHelper.updateMediaDeviceValid(cr, devicePath, deviceUUID, valid);
            MediaStoreHelper.updateMediaValidByDevice(cr, deviceId, valid);
        }
        c.close();
    }

    private void handleActionScanner(ContentResolver cr, File sda, long deviceId) {
        long start = System.currentTimeMillis();
        scannerVideoFiles(cr, sda, sda.getAbsolutePath(), deviceId);
        long end = System.currentTimeMillis();
        Log.e("handleActionScanner", "" + (end - start) / 1000f);
    }


    private void handlerActionMediaInfo(ContentResolver cr, String mediaPath, String metaTitle, long mediaBaseId) {
        long mediaInfoId = -1;
        String sourceID;
        //Log.e("Info", "mediaPath=" + mediaPath + ",mediaBaseId=" + mediaBaseId);
        if (mediaBaseId >= 0) {
            File mediaFile = new File(mediaPath);
            //nfo api
            //本地文件寻找imdbId
            sourceID = NFOApi.getImdbIdFromNFOFile(mediaPath);
            Log.w("Info", "FromNFO imdb=" + sourceID);
            if (sourceID == null) {
//                if (metaTitle != null) {
//                    sourceID = IMDBApi.getImdbIdFromSearch(metaTitle, null);
//                } else {
//                    String name = UUtils.smartMediaName(mediaFile.getName());
//                    sourceID = IMDBApi.getImdbIdFromSearch(name, null);
//                }
            }
            Log.w("Info", "FromIMDB imdb=" + sourceID);
            if (sourceID != null) {
                //exsist  get sourceID
                boolean sourceExist = false;
                Cursor c = cr.query(MediaStore.MediaInfo.CONTENT_URI, null, MediaStore.MediaInfo.FIELD_SOURCE_ID + "=?",
                        new String[]{sourceID}, null);
                if (c.getCount() > 0) {
                    sourceExist = true;
                }
                c.close();
                if (!sourceExist) {
                    String json = IMDBApi.imdbById(sourceID, null);
                    Uri uri = MediaStoreHelper.insertMediaInfo(cr, json, sourceID);
                    mediaInfoId = ContentUris.parseId(uri);
                }
            }

            //douban api
            Log.w("Info", "mediaSourceId=" + sourceID);
            //媒体信息关联到MediaBaseTable
            if (mediaInfoId > -1) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaBase.FIELD_MEDIA_INFO_ID, mediaInfoId);
                cr.update(MediaStore.getContentUri(MediaStore.MediaBase.TABLE_NAME, mediaBaseId), values, null, null);
            }
        }
    }


    /*非递归*/
    /*private void scannerVideoFiles(String path) {
        File rootfile = new File(path);
        LinkedList<String> dirs = new LinkedList<String>();
        LinkedList<String> dirsBuffer = new LinkedList<String>();
        //init
        if (rootfile.isDirectory()) {
            dirs.add(rootfile.getAbsolutePath());
        } else {
            scannerFileToStore(rootfile.getAbsolutePath());
        }
        while (!dirs.isEmpty()) {
            File rootDir = new File(dirs.remove(0));
            File[] files = rootDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) {
                        return true;
                    }
                    String filename = pathname.getName();
                    if (filename.endsWith(".ts") || filename.endsWith(".avi") || filename.endsWith(".mp4")
                            || filename.endsWith(".rmvb") || filename.endsWith(".mkv") || filename.endsWith(".flv")) {
                        return true;
                        //scannerFileToStore(pathname.getAbsolutePath());
                    }
                    return false;
                }
            });
            if (files != null && files.length > 0) {
                int length = files.length;
                for (int i = 0; i < length; i++) {
                    File file = files[i];
                    if (file.isDirectory()) {
                        dirsBuffer.add(file.getAbsolutePath());
                    } else {
                        scannerFileToStore(file.getAbsolutePath());
                    }
                }
            }
            dirs.addAll(dirsBuffer);
            dirsBuffer.clear();
        }
    }*/

    /*递归*/
    private void scannerVideoFiles(ContentResolver cr, File sda, String devicePath, long deviceId) {
        if (!sda.exists()) {
            return;
        }
        File file = sda;
        if (file.isDirectory()) {
            if (UUtils.isBDMV(file)) {
                handlerActionAddMedia(cr, file.getAbsolutePath(), devicePath, deviceId);
            } else {
                File[] files = file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        if (!pathname.isHidden()) {
                            String filename = pathname.getName();
                            if (filename.contains("$REC")) {
                                return false;
                            }
                            if (UUtils.fileIsVideo(pathname)) {
                                return true;
                            }
                            if (pathname.isDirectory()) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        scannerVideoFiles(cr, f, devicePath, deviceId);
                    }
                }
            }
        } else {
            handlerActionAddMedia(cr, file.getAbsolutePath(), devicePath, deviceId);
        }
    }
}

