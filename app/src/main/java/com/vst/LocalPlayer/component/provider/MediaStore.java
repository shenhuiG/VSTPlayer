package com.vst.LocalPlayer.component.provider;


import android.net.Uri;
import android.provider.BaseColumns;

public final class MediaStore {
    public static final String NAME = "MediaStore";
    public static final int VERSION = 1;

    public static final String AUTHORITY = "com.vst.localplayer.mediaStore";

    private static final String CONTENT_AUTHORITY_SLASH = "content://" + AUTHORITY + "/";


    public static final Uri getContentUri(String tableName) {
        return Uri.parse(CONTENT_AUTHORITY_SLASH + tableName);
    }

    public static final Uri getContentUri(String tableName, long rowId) {
        return Uri.parse(CONTENT_AUTHORITY_SLASH + tableName + "/" + rowId);
    }

    public interface MediaDevice extends BaseColumns {
        public static final String TABLE_NAME = "MediaDevice";
        public static final String FIELD_DEVICE_UUID = "UUID";
        public static final String FIELD_DEVICE_PATH = "path";
        public static final String FIELD_LAST_MODIFY_TIME = "lastTime";
        public static final String FIELD_VALID = "isValid";
        public static final Uri CONTENT_URI = getContentUri(TABLE_NAME);

    }

    public interface MediaBase extends BaseColumns {
        public static final String TABLE_NAME = "MediaBase";
        public static final String FIELD_NAME = "name";
        public static final String FIELD_META_TITLE = "metaTitle";
        public static final String FIELD_RELATIVE_PATH = "relativePath";
        public static final String FIELD_FILE_SIZE = "fileSize";
        public static final String FIELD_DEVICE_ID = "deviceId";
        public static final String FIELD_VALID = "valid";
        public static final String FIELD_DATE = "date";
        public static final String FIELD_MEDIA_INFO_ID = "mediaInfoId";
        public static final String FIELD_HIDE = "hide";
        public static final Uri CONTENT_URI = getContentUri(TABLE_NAME);
    }


    public interface MediaInfo {
        public static final String TABLE_NAME = "MediaInfo";
        public static final String FIELD_ID = "mediaInfo_id";
        public static final String FIELD_TITLE = "title";           //media  Name
        public static final String FIELD_YEAR = "year";
        public static final String FIELD_DIRECTOR = "director";
        public static final String FIELD_WRITER = "writer";
        public static final String FIELD_ACTORS = "actors";
        public static final String FIELD_GENRE = "genre";
        public static final String FIELD_PLOT = "plot";
        public static final String FIELD_LANGUAGE = "language";
        public static final String FIELD_COUNTRY = "country";
        public static final String FIELD_TYPE = "type";
        public static final String FIELD_POSTER = "poster";
        public static final String FIELD_SOURCE_ID = "sourceId";    // tt1233 ,12345,
        public static final String FIELD_SOURCE = "source";          //imdb,douban,nfo
        public static final Uri CONTENT_URI = getContentUri(TABLE_NAME);
    }

    public interface MediaRecord extends BaseColumns {
        public static final String TABLE_NAME = "MediaRecord";
        public static final String FIELD_ID = "mediaRecord_id";
        public static final String FIELD_MEDIA_ID = "mediaId";
        public static final String FIELD_POSITION = "position";
        public static final String FIELD_DURATION = "duration";
        public static final String FIELD_DATE = "date";
        public static final Uri CONTENT_URI = getContentUri(TABLE_NAME);
    }


}
