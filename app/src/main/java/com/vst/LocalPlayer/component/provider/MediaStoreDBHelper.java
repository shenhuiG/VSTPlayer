package com.vst.LocalPlayer.component.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MediaStoreDBHelper extends SQLiteOpenHelper {

    private static MediaStoreDBHelper mInstance;

    MediaStoreDBHelper(Context context) {
        super(context, MediaStore.NAME, null, MediaStore.VERSION);
    }

    static MediaStoreDBHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MediaStoreDBHelper(context);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DEVICE_TABLE_V1);
        db.execSQL(CREATE_MEDIA_BASE_TABLE_V1);
        db.execSQL(CREATE_MEDIA_INFO_TABLE_V1);
        db.execSQL(CREATE_MEDIA_RECORD_TABLE_V1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    static final String CREATE_DEVICE_TABLE_V1 = "CREATE TABLE "
            + MediaStore.MediaDevice.TABLE_NAME
            + "("
            + MediaStore.MediaDevice._ID + " integer primary key autoincrement not null,"
            + MediaStore.MediaDevice.FIELD_DEVICE_UUID + " text not null ,"
            + MediaStore.MediaDevice.FIELD_DEVICE_PATH + " text not null,"
            + MediaStore.MediaDevice.FIELD_LAST_MODIFY_TIME + " long not null,"
            + MediaStore.MediaDevice.FIELD_VALID + " boolean not null"
            + ")";


    static final String CREATE_MEDIA_BASE_TABLE_V1 = "CREATE TABLE "
            + MediaStore.MediaBase.TABLE_NAME
            + "("
            + MediaStore.MediaBase._ID + " integer primary key autoincrement not null,"
            + MediaStore.MediaBase.FIELD_NAME + " text not null,"
            + MediaStore.MediaBase.FIELD_META_TITLE + " text ,"
            + MediaStore.MediaBase.FIELD_FILE_SIZE + " long not null,"
            + MediaStore.MediaBase.FIELD_DEVICE_ID + " long not null,"
            + MediaStore.MediaBase.FIELD_MEDIA_INFO_ID + " long ,"
            + MediaStore.MediaBase.FIELD_VALID + " boolean not null,"
            + MediaStore.MediaBase.FIELD_RELATIVE_PATH + " text not null,"
            + MediaStore.MediaBase.FIELD_DATE + " long not null ,"
            + MediaStore.MediaBase.FIELD_HIDE + " boolean not null default 0 "
            + ")";

    static final String CREATE_MEDIA_RECORD_TABLE_V1 = "CREATE TABLE "
            + MediaStore.MediaRecord.TABLE_NAME
            + "("
            + MediaStore.MediaRecord._ID + " integer primary key autoincrement not null,"
            + MediaStore.MediaRecord.FIELD_MEDIA_ID + " long not null,"
            + MediaStore.MediaRecord.FIELD_POSITION + " long not null,"
            + MediaStore.MediaRecord.FIELD_DURATION + " long not null,"
            + MediaStore.MediaRecord.FIELD_DATE + " long not null"
            + ")";


    static final String CREATE_MEDIA_INFO_TABLE_V1 = "CREATE TABLE "
            + MediaStore.MediaInfo.TABLE_NAME
            + "("
            + MediaStore.MediaInfo.FIELD_ID + " integer primary key autoincrement not null, "
            + MediaStore.MediaInfo.FIELD_TITLE + " text ,"
            + MediaStore.MediaInfo.FIELD_YEAR + " text ,"
            + MediaStore.MediaInfo.FIELD_DIRECTOR + " text ,"
            + MediaStore.MediaInfo.FIELD_WRITER + " text ,"
            + MediaStore.MediaInfo.FIELD_ACTORS + " text ,"
            + MediaStore.MediaInfo.FIELD_PLOT + " text ,"
            + MediaStore.MediaInfo.FIELD_GENRE + " text ,"
            + MediaStore.MediaInfo.FIELD_LANGUAGE + " text ,"
            + MediaStore.MediaInfo.FIELD_TYPE + " text ,"
            + MediaStore.MediaInfo.FIELD_COUNTRY + " text ,"
            + MediaStore.MediaInfo.FIELD_POSTER + " text ,"
            + MediaStore.MediaInfo.FIELD_SOURCE_ID + " text ,"
            + MediaStore.MediaInfo.FIELD_SOURCE + " text"
            + ")";
}
