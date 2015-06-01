package com.vst.LocalPlayer.component.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.vst.LocalPlayer.UUtils;

import org.json.JSONObject;

import java.io.File;

public class MediaStoreHelper {

    static final String TAG = "MediaStoreHelper";

    public static void updateMediaDeviceValid(ContentResolver cr, String path, String uuid, boolean valid) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaDevice.FIELD_VALID, valid);
        String where = "";
        String[] selectionArgs = null;
        if (path != null) {
            where = MediaStore.MediaDevice.FIELD_DEVICE_PATH + "=?";
            selectionArgs = new String[]{path};
        }
        if (uuid != null) {
            if ("".equals(where)) {
                where = MediaStore.MediaDevice.FIELD_DEVICE_UUID + "=?";
                selectionArgs = new String[]{uuid};
            } else {
                where = where + " AND " + MediaStore.MediaDevice.FIELD_DEVICE_UUID + "=?";
                selectionArgs = new String[]{path, uuid};
            }
        }
        if (selectionArgs != null) {
            int i = cr.update(MediaStore.MediaDevice.CONTENT_URI, values, where, selectionArgs);
        }
    }


    public static boolean deviceInStore(ContentResolver cr, String uuid) {
        boolean inStore = false;
        Cursor c = cr.query(MediaStore.MediaDevice.CONTENT_URI, null, MediaStore.MediaDevice.FIELD_DEVICE_UUID + "=?",
                new String[]{uuid}, null);
        if (c != null) {
            if (c.getCount() > 0) {
                inStore = true;
            }
            c.close();
        }
        return inStore;
    }


    public static long mediaIsInStore(ContentResolver cr, long deviceId, String relativePath) {
        long _id = -1;
        Cursor cursor = cr.query(MediaStore.MediaBase.CONTENT_URI, null, MediaStore.MediaBase.FIELD_DEVICE_ID + "=? and "
                + MediaStore.MediaBase.FIELD_RELATIVE_PATH + "=?", new String[]{deviceId + "", relativePath}, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                _id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaBase._ID));
            }
            cursor.close();
        }
        return _id;
    }

    public static Uri addNewMediaDevice(ContentResolver cr, String uuid, String path) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaDevice.FIELD_DEVICE_UUID, uuid);
        values.put(MediaStore.MediaDevice.FIELD_DEVICE_PATH, path);
        values.put(MediaStore.MediaDevice.FIELD_LAST_MODIFY_TIME, UUtils.getDeviceLaseTime(path));
        values.put(MediaStore.MediaDevice.FIELD_VALID, 1);
        Uri uri = cr.insert(MediaStore.MediaDevice.CONTENT_URI, values);
        return uri;
    }

    public static Uri insertMediaInfo(ContentResolver cr, String json, String sourceID) {
        try {
            JSONObject root = new JSONObject(json);
            String Response = root.getString("Response");
            if ("True".equals(Response)) {
                String title = root.getString("Title");
                String year = root.getString("Year");
                String director = root.getString("Director");
                String writer = root.getString("Writer");
                String actors = root.getString("Actors");
                String genre = root.getString("Genre");
                String plot = root.getString("Plot");
                String language = root.getString("Language");
                String country = root.getString("Country");
                String type = root.getString("Type");
                String poster = root.getString("Poster");
                String sourceId = root.getString("imdbID");
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaInfo.FIELD_SOURCE, "imdb");
                values.put(MediaStore.MediaInfo.FIELD_SOURCE_ID, sourceId);
                values.put(MediaStore.MediaInfo.FIELD_TITLE, title);
                values.put(MediaStore.MediaInfo.FIELD_YEAR, year);
                values.put(MediaStore.MediaInfo.FIELD_DIRECTOR, director);
                values.put(MediaStore.MediaInfo.FIELD_WRITER, writer);
                values.put(MediaStore.MediaInfo.FIELD_ACTORS, actors);
                values.put(MediaStore.MediaInfo.FIELD_GENRE, genre);
                values.put(MediaStore.MediaInfo.FIELD_PLOT, "N/A".equals(plot) ? null : plot);
                values.put(MediaStore.MediaInfo.FIELD_POSTER, "N/A".equals(poster) ? null : poster);
                values.put(MediaStore.MediaInfo.FIELD_TYPE, type);
                values.put(MediaStore.MediaInfo.FIELD_LANGUAGE, language);
                values.put(MediaStore.MediaInfo.FIELD_COUNTRY, country);
                return cr.insert(MediaStore.MediaInfo.CONTENT_URI, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Uri addNewMediaBase(ContentResolver cr, String mediaPath, String devicePath, long deviceId
            , String metaTitle) {
        Uri uri = null;
        File mediaFile = new File(mediaPath);
        if (mediaFile.exists()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaBase.FIELD_NAME, mediaFile.getName());
            values.put(MediaStore.MediaBase.FIELD_FILE_SIZE, mediaFile.getTotalSpace());
            values.put(MediaStore.MediaBase.FIELD_RELATIVE_PATH, mediaPath.replace(devicePath, ""));
            values.put(MediaStore.MediaBase.FIELD_DEVICE_ID, deviceId);
            values.put(MediaStore.MediaBase.FIELD_META_TITLE, metaTitle);
            values.put(MediaStore.MediaBase.FIELD_DATE, System.currentTimeMillis());
            values.put(MediaStore.MediaBase.FIELD_VALID, 1);
            uri = cr.insert(MediaStore.MediaBase.CONTENT_URI, values);
        }
        return uri;
    }

    public static boolean checkDeviceValid(String path, String uuid) {
        String deviceUUID = UUtils.readDeviceUUID(path);
        return uuid.equals(deviceUUID);
    }


    public static int updateMediaValidByDevice(ContentResolver cr, long deviceId, boolean valid) {
        int count;
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaBase.FIELD_VALID, valid);
        String where = MediaStore.MediaBase.FIELD_DEVICE_ID + "=?";
        String[] selectionArgs = new String[]{Long.toString(deviceId)};
        count = cr.update(MediaStore.MediaBase.CONTENT_URI, values, where, selectionArgs);
        return count;
    }


}
