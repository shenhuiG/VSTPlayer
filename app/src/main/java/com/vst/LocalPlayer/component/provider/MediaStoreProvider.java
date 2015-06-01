package com.vst.LocalPlayer.component.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MediaStoreProvider extends ContentProvider {

    private MediaStoreDBHelper mDBHelper;
    private SQLiteDatabase mDataBase;

    private static final int MEDIA_BASE_ITEM = 1;
    private static final int MEDIA_BASE_ITEM_ID = 2;
    private static final int MEDIA_DEVICE_ITEM = 3;
    private static final int MEDIA_DEVICE_ITEM_ID = 4;
    private static final int MEDIA_INFO_ITEM = 5;
    private static final int MEDIA_INFO_ITEM_ID = 6;
    private static final int MEDIA_RECORD_ITEM = 7;
    private static final int MEDIA_RECORD_ITEM_ID = 8;
    private static final UriMatcher sMatcher;

    static {
        sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sMatcher.addURI(MediaStore.AUTHORITY, MediaStore.MediaBase.TABLE_NAME, MEDIA_BASE_ITEM);
        sMatcher.addURI(MediaStore.AUTHORITY, MediaStore.MediaBase.TABLE_NAME + "/#", MEDIA_BASE_ITEM_ID);
        sMatcher.addURI(MediaStore.AUTHORITY, MediaStore.MediaDevice.TABLE_NAME, MEDIA_DEVICE_ITEM);
        sMatcher.addURI(MediaStore.AUTHORITY, MediaStore.MediaDevice.TABLE_NAME + "/#", MEDIA_DEVICE_ITEM_ID);
        sMatcher.addURI(MediaStore.AUTHORITY, MediaStore.MediaInfo.TABLE_NAME, MEDIA_INFO_ITEM);
        sMatcher.addURI(MediaStore.AUTHORITY, MediaStore.MediaInfo.TABLE_NAME + "/#", MEDIA_INFO_ITEM_ID);
        sMatcher.addURI(MediaStore.AUTHORITY, MediaStore.MediaRecord.TABLE_NAME, MEDIA_RECORD_ITEM);
        sMatcher.addURI(MediaStore.AUTHORITY, MediaStore.MediaRecord.TABLE_NAME + "/#", MEDIA_RECORD_ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        mDBHelper = MediaStoreDBHelper.getInstance(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        mDataBase = mDBHelper.getWritableDatabase();
        int count = 0;
        long rowId;
        try {
            rowId = ContentUris.parseId(uri);
        } catch (Exception e) {
            rowId = -1;
        }
        switch (sMatcher.match(uri)) {
            case MEDIA_BASE_ITEM:
            case MEDIA_BASE_ITEM_ID:
                if (rowId >= 0) {
                    selection = MediaStore.MediaBase.TABLE_NAME + "." + MediaStore.MediaBase._ID + "=?";
                    selectionArgs = new String[]{Long.toString(rowId)};
                }
                count = mDataBase.delete(MediaStore.MediaBase.TABLE_NAME, selection, selectionArgs);
                break;
            case MEDIA_DEVICE_ITEM:
            case MEDIA_DEVICE_ITEM_ID:
                if (rowId >= 0) {
                    selection = MediaStore.MediaDevice.TABLE_NAME + "." + MediaStore.MediaDevice._ID + "=?";
                    selectionArgs = new String[]{Long.toString(rowId)};
                }
                count = mDataBase.delete(MediaStore.MediaDevice.TABLE_NAME, selection, selectionArgs);
                break;
            case MEDIA_INFO_ITEM:
                break;
            case MEDIA_INFO_ITEM_ID:
                break;
            case MEDIA_RECORD_ITEM:
                break;
            case MEDIA_RECORD_ITEM_ID:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);

        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        mDataBase = mDBHelper.getWritableDatabase();
        long rowId;
        switch (sMatcher.match(uri)) {
            case MEDIA_BASE_ITEM:
                rowId = mDataBase.insert(MediaStore.MediaBase.TABLE_NAME, null, values);
                break;
            case MEDIA_DEVICE_ITEM:
                rowId = mDataBase.insert(MediaStore.MediaDevice.TABLE_NAME, null, values);
                break;
            case MEDIA_INFO_ITEM:
                rowId = mDataBase.insert(MediaStore.MediaInfo.TABLE_NAME, null, values);
                break;
            case MEDIA_RECORD_ITEM:
                rowId = mDataBase.insert(MediaStore.MediaRecord.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);

        }
        if (rowId >= 0) {
            Uri noteUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(uri, null);
            return noteUri;
        }
        return null;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        mDataBase = mDBHelper.getWritableDatabase();
        Cursor cursor;
        long rowId;
        try {
            rowId = ContentUris.parseId(uri);
        } catch (Exception e) {
            rowId = -1;
        }
        switch (sMatcher.match(uri)) {
            case MEDIA_BASE_ITEM:
            case MEDIA_BASE_ITEM_ID:
                if (rowId >= 0) {
                    selection = MediaStore.MediaBase.TABLE_NAME + "." + MediaStore.MediaBase._ID + "=?";
                    selectionArgs = new String[]{Long.toString(rowId)};
                }
                String columnName = "*";
                if (projection != null && projection.length > 0) {
                    StringBuffer sb = new StringBuffer();
                    for (String column : projection) {
                        sb.append(column);
                        sb.append(" ");
                    }
                }
                String rawQuerySQL = "select " + columnName + " from " + MediaStore.MediaBase.TABLE_NAME
                        + " LEFT JOIN " + MediaStore.MediaInfo.TABLE_NAME
                        + " ON " + MediaStore.MediaBase.TABLE_NAME + "." + MediaStore.MediaBase.FIELD_MEDIA_INFO_ID
                        + " = " + MediaStore.MediaInfo.TABLE_NAME + "." + MediaStore.MediaInfo.FIELD_ID
                        + (selection == null ? " " : " where " + selection)
                        + (sortOrder == null ? " " : "ORDER BY " + sortOrder);
                cursor = mDataBase.rawQuery(rawQuerySQL, selectionArgs);
                break;
            case MEDIA_DEVICE_ITEM:
            case MEDIA_DEVICE_ITEM_ID:
                if (rowId >= 0) {
                    selection = MediaStore.MediaDevice._ID + "=?";
                    selectionArgs = new String[]{Long.toString(rowId)};
                }
                cursor = mDataBase.query(MediaStore.MediaDevice.TABLE_NAME, projection, selection,
                        selectionArgs, MediaStore.MediaDevice._ID, null, sortOrder);
                break;
            case MEDIA_INFO_ITEM:
            case MEDIA_INFO_ITEM_ID:
                if (rowId >= 0) {
                    selection = MediaStore.MediaInfo.FIELD_ID + "=?";
                    selectionArgs = new String[]{Long.toString(rowId)};
                }
                cursor = mDataBase.query(MediaStore.MediaInfo.TABLE_NAME, projection, selection,
                        selectionArgs, MediaStore.MediaInfo.FIELD_ID, null, sortOrder);
                break;
            case MEDIA_RECORD_ITEM:
                cursor = mDataBase.query(MediaStore.MediaRecord.TABLE_NAME, projection, selection,
                        selectionArgs, MediaStore.MediaRecord.FIELD_MEDIA_ID, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        mDataBase = mDBHelper.getWritableDatabase();
        int count = 0;
        long rowId;
        try {
            rowId = ContentUris.parseId(uri);
        } catch (Exception e) {
            rowId = -1;
        }
        switch (sMatcher.match(uri)) {
            case MEDIA_BASE_ITEM:
            case MEDIA_BASE_ITEM_ID:
                if (rowId >= 0) {
                    selection = MediaStore.MediaBase._ID + "=?";
                    selectionArgs = new String[]{Long.toString(rowId)};
                }
                count = mDataBase.update(MediaStore.MediaBase.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MEDIA_DEVICE_ITEM:
            case MEDIA_DEVICE_ITEM_ID:
                if (rowId >= 0) {
                    selection = MediaStore.MediaDevice._ID + "=?";
                    selectionArgs = new String[]{Long.toString(rowId)};
                }
                count = mDataBase.update(MediaStore.MediaDevice.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MEDIA_INFO_ITEM:
            case MEDIA_INFO_ITEM_ID:
                if (rowId >= 0) {
                    selection = MediaStore.MediaInfo.FIELD_ID + "=?";
                    selectionArgs = new String[]{Long.toString(rowId)};
                }
                count = mDataBase.update(MediaStore.MediaInfo.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MEDIA_RECORD_ITEM:
            case MEDIA_RECORD_ITEM_ID:
                if (rowId >= 0) {
                    selection = MediaStore.MediaRecord._ID + "=?";
                    selectionArgs = new String[]{Long.toString(rowId)};
                }
                count = mDataBase.update(MediaStore.MediaRecord.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }
}
