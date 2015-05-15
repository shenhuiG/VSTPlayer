package com.vst.LocalPlayer.component.activity;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import com.vst.LocalPlayer.MediaStoreNotifier;
import com.vst.LocalPlayer.R;
import com.vst.LocalPlayer.Utils;
import com.vst.LocalPlayer.component.provider.MediaStore;
import com.vst.LocalPlayer.model.MediaInfo;
import com.vst.LocalPlayer.widget.ContextMenuImpl;
import com.vst.LocalPlayer.widget.MenuBuild;
import com.vst.LocalPlayer.widget.MenuBuildImpl;
import com.yixia.zi.utils.ImageCache;
import com.yixia.zi.utils.ImageFetcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VideosScreenActivity extends Activity implements MediaStoreNotifier.CallBack, MenuBuild.onMenuListener {

    public static final String TAG = "VideosScreen";
    private Context mContext;
    private MediaStoreNotifier notifier = null;
    private GridView mGridView;
    private TextView mTextView;
    private ArrayAdapter<MediaInfo> mAdapter;
    private ArrayList<MediaInfo> mArray = new ArrayList<MediaInfo>();
    private HashMap<Long, String> mDevicePath = new HashMap<Long, String>();
    private ImageFetcher fetcher;
    private static final int MENU_ID_SEARCH = 0;
    private static final int MENU_ID_ALL = 3;
    private static final int MENU_ID_ADD = 2;
    private static final int MENU_ID_RECORD = 1;
    private static final int MENU_ID_ITEM_DELETE = 4;
    private static final int MENU_ID_ITEM_EDIT = 5;
    private int mMenuItemId = MENU_ID_ALL;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        initView();
        updateUI();
        notifier = new MediaStoreNotifier(mContext.getContentResolver(), this);
        notifier.registQueryContentUri(MediaStore.MediaBase.CONTENT_URI, null,
                MediaStore.MediaBase.FIELD_VALID + "=? AND " + MediaStore.MediaBase.FIELD_HIDE + "=?", new String[]{"1", "0"}, null);
        fetcher = new ImageFetcher(mContext);
        fetcher.setImageCache(new ImageCache(mContext, new ImageCache.ImageCacheParams(".cache")));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOptionMenu != null && mOptionMenu.isShowing()) {
            mOptionMenu.dismiss();
            mOptionMenu = null;
        }
        if (mContextMenu != null && mContextMenu.isShowing()) {
            mContextMenu.dismiss();
            mContextMenu = null;
        }
        mContext = null;
        notifier.release();
        fetcher.setExitTasksEarly(true);
        fetcher = null;
    }

    private PopupWindow mOptionMenu;
    private PopupWindow mContextMenu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("");
        if (this.mOptionMenu == null) {
            this.mOptionMenu = new MenuBuildImpl(this.getApplication())
                    .setTitle("菜单")
                    .addMenuItem(new MenuBuild.MenuItem(MENU_ID_SEARCH, "搜索影片", getResources().getDrawable(R.drawable.icon_menu_search)))
                    .addMenuItem(new MenuBuild.MenuItem(MENU_ID_RECORD, "最近播放", getResources().getDrawable(R.drawable.icon_menu_bofang)))
                    .addMenuItem(new MenuBuild.MenuItem(MENU_ID_ADD, "最近添加", getResources().getDrawable(R.drawable.icon_menu_tianjia)))
                    .addMenuItem(new MenuBuild.MenuItem(MENU_ID_ALL, "全部影片", getResources().getDrawable(R.drawable.icon_menu_all)))
                    .setOnMenuListener(this)
                    .create();
        }
        return super.onCreateOptionsMenu(menu);
    }

    private MediaInfo queryMediaBase(long mediaId) {
        Cursor cursor = mContext.getContentResolver().query(MediaStore.getContentUri(MediaStore.MediaBase.TABLE_NAME, mediaId),
                null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int valid = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_VALID));
                int hide = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_HIDE));
                if (valid == 1 && hide == 0) {
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_NAME));
                    String rPath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_RELATIVE_PATH));
                    long deviceId = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_DEVICE_ID));
                    long _id = cursor.getLong(cursor.getColumnIndex("_id"));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.MediaInfo.FIELD_TITLE));
                    String poster = cursor.getString(cursor.getColumnIndex(MediaStore.MediaInfo.FIELD_POSTER));
                    String devicePath = "";
                    if (mDevicePath.containsKey(deviceId)) {
                        devicePath = mDevicePath.get(deviceId);
                    } else {
                        Cursor deviceC = mContext.getContentResolver().query(MediaStore.getContentUri(
                                MediaStore.MediaDevice.TABLE_NAME, deviceId), null, null, null, null);
                        if (deviceC.moveToFirst()) {
                            devicePath = deviceC.getString(deviceC.getColumnIndex(MediaStore.MediaDevice.FIELD_DEVICE_PATH));
                            mDevicePath.put(deviceId, devicePath);
                        }
                        deviceC.close();
                    }
                    String path = devicePath + rPath;
                    return new MediaInfo(_id, path, name, title, poster, deviceId, devicePath);
                }
            }
            cursor.close();
        }
        return null;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (mOptionMenu.isShowing()) {
            mOptionMenu.dismiss();
        } else {
            mOptionMenu.showAtLocation(mGridView, Gravity.LEFT, 0, 0);
        }
        return false;
    }

    private void initView() {
        LinearLayout root = new LinearLayout(mContext);
        root.setBackgroundResource(R.drawable.main_bg);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(
                com.vst.dev.common.util.Utils.getFitSize(mContext, 80), com.vst.dev.common.util.Utils.getFitSize(mContext, 20),
                com.vst.dev.common.util.Utils.getFitSize(mContext, 80), com.vst.dev.common.util.Utils.getFitSize(mContext, 0));
        LinearLayout top = new LinearLayout(mContext);
        mTextView = new TextView(mContext);
        com.vst.dev.common.util.Utils.applyFace(mTextView);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, com.vst.dev.common.util.Utils.getFitSize(mContext, 26));
        top.addView(mTextView, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView menuTipView = new TextView(mContext);
        com.vst.dev.common.util.Utils.applyFace(menuTipView);
        menuTipView.setText(com.vst.dev.common.util.Utils.makeImageSpannable(getResources().getString(R.string.videos_menu_tip),
                getResources().getDrawable(R.drawable.ic_menu_tip), 0, com.vst.dev.common.util.Utils.getFitSize(mContext, 23),
                com.vst.dev.common.util.Utils.getFitSize(mContext, 23), ImageSpan.ALIGN_BOTTOM));
        menuTipView.setTextSize(TypedValue.COMPLEX_UNIT_PX, com.vst.dev.common.util.Utils.getFitSize(mContext, 20));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.rightMargin = com.vst.dev.common.util.Utils.getFitSize(mContext, 15);
        top.addView(menuTipView, lp);
        TextView okTipView = new TextView(mContext);
        com.vst.dev.common.util.Utils.applyFace(okTipView);
        okTipView.setTextSize(TypedValue.COMPLEX_UNIT_PX, com.vst.dev.common.util.Utils.getFitSize(mContext, 20));
        okTipView.setText(com.vst.dev.common.util.Utils.makeImageSpannable(getResources().getString(R.string.videos_ok_tip),
                getResources().getDrawable(R.drawable.ic_ok_tip), 0, com.vst.dev.common.util.Utils.getFitSize(mContext, 23),
                com.vst.dev.common.util.Utils.getFitSize(mContext, 23), ImageSpan.ALIGN_BOTTOM));
        top.addView(okTipView, new LinearLayout.LayoutParams(-2, -2));
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(-1, -2);
        lp2.bottomMargin = com.vst.dev.common.util.Utils.getFitSize(mContext, 20);
        root.addView(top, lp2);
        mGridView = new GridView(mContext);
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaInfo info = (MediaInfo) parent.getAdapter().getItem(position);
                Uri uri = Utils.getMediaUri(info.path);

                if (uri != null) {
                    Utils.playMediaFile(mContext, uri, info.id, info.deviceId, info.devicePath);
                } else {
                    Toast.makeText(mContext, "该影片不存在 路径：" + "盘:" + info.path, Toast.LENGTH_LONG).show();
                }
            }
        });
        mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final MediaInfo info = (MediaInfo) parent.getAdapter().getItem(position);
                if (mContextMenu != null && mContextMenu.isShowing()) {
                    mContextMenu.dismiss();
                    mContextMenu = null;
                }
                mContextMenu = new ContextMenuImpl(mContext.getApplicationContext())
                        .setTitle("菜单")
                        .addMenuItem(new MenuBuild.MenuItem(MENU_ID_ITEM_DELETE, "删除", getResources().getDrawable(R.drawable.icon_menu_search)))
                        .addMenuItem(new MenuBuild.MenuItem(MENU_ID_ITEM_EDIT, "编辑", getResources().getDrawable(R.drawable.icon_menu_all)))
                        .setOnMenuListener(VideosScreenActivity.this)
                        .create();
                mContextMenu.showAtLocation(mGridView, Gravity.CENTER, 0, 0);
                ImageView poster = (ImageView) mContextMenu.getContentView().findViewWithTag("poster");
                if (poster != null) {
                    if (info.poster != null) {
                        fetcher.loadImage(info.poster, poster, R.drawable.poster_default);
                    } else {
                        poster.setImageResource(R.drawable.poster_default);
                    }
                }
                return true;
            }
        });
        mGridView.setNumColumns(5);
        //mGridView.setColumnWidth(com.vst.dev.common.util.Utils.getFitSize(mContext, 150));
        //mGridView.setStretchMode(GridView.STRETCH_SPACING_UNIFORM);
        mGridView.setHorizontalSpacing(com.vst.dev.common.util.Utils.getFitSize(mContext, 20));
        //mGridView.setVerticalSpacing(com.vst.dev.common.util.Utils.getFitSize(mContext, 20));
        root.addView(mGridView);
        setContentView(root);
    }

    @Override
    public void onMenuItemOnClick(MenuBuild.MenuItem item) {
        mMenuItemId = item.id;
        Cursor cursor;
        switch (mMenuItemId) {
            case MENU_ID_SEARCH:
                //跳转到搜索
                break;
            case MENU_ID_RECORD:
                mArray.clear();
                cursor = mContext.getContentResolver().query(MediaStore.MediaRecord.CONTENT_URI, null, null, null, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        long mediaId = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaRecord.FIELD_MEDIA_ID));
                        MediaInfo mediaBaseModel = queryMediaBase(mediaId);
                        if (mediaBaseModel != null) {
                            mArray.add(mediaBaseModel);
                        }
                        if (mArray.size() >= 10) {
                            break;
                        }
                    }
                    cursor.close();
                }
                updateUI();
                break;
            case MENU_ID_ADD:
                mArray.clear();
                cursor = mContext.getContentResolver().query(MediaStore.MediaBase.CONTENT_URI, null,
                        MediaStore.MediaBase.FIELD_VALID + "=? AND " + MediaStore.MediaBase.FIELD_HIDE
                                + "=?", new String[]{"1", "0"}, MediaStore.MediaBase.FIELD_DATE + " desc");
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_NAME));
                        String rPath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_RELATIVE_PATH));
                        long deviceId = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_DEVICE_ID));
                        long _id = cursor.getLong(cursor.getColumnIndex("_id"));
                        String title = cursor.getString(cursor.getColumnIndex(MediaStore.MediaInfo.FIELD_TITLE));
                        String poster = cursor.getString(cursor.getColumnIndex(MediaStore.MediaInfo.FIELD_POSTER));
                        String devicePath = "";
                        if (mDevicePath.containsKey(deviceId)) {
                            devicePath = mDevicePath.get(deviceId);
                        } else {
                            Cursor deviceC = mContext.getContentResolver().query(MediaStore.getContentUri(
                                    MediaStore.MediaDevice.TABLE_NAME, deviceId), null, null, null, null);
                            if (deviceC.moveToFirst()) {
                                devicePath = deviceC.getString(deviceC.getColumnIndex(MediaStore.MediaDevice.FIELD_DEVICE_PATH));
                                mDevicePath.put(deviceId, devicePath);
                            }
                            deviceC.close();
                        }
                        String path = devicePath + rPath;
                        mArray.add(new MediaInfo(_id, path, name, title, poster, deviceId, devicePath));
                        if (mArray.size() >= 10) {
                            break;
                        }
                    }
                    cursor.close();
                }
                updateUI();
                break;
            case MENU_ID_ALL:
                mArray.clear();
                cursor = mContext.getContentResolver().query(MediaStore.MediaBase.CONTENT_URI, null,
                        MediaStore.MediaBase.FIELD_VALID + "=? AND " + MediaStore.MediaBase.FIELD_HIDE
                                + "=?", new String[]{"1", "0"}, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_NAME));
                        String rPath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_RELATIVE_PATH));
                        long deviceId = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_DEVICE_ID));
                        long _id = cursor.getLong(cursor.getColumnIndex("_id"));
                        String title = cursor.getString(cursor.getColumnIndex(MediaStore.MediaInfo.FIELD_TITLE));
                        String poster = cursor.getString(cursor.getColumnIndex(MediaStore.MediaInfo.FIELD_POSTER));
                        String devicePath = "";
                        if (mDevicePath.containsKey(deviceId)) {
                            devicePath = mDevicePath.get(deviceId);
                        } else {
                            Cursor deviceC = mContext.getContentResolver().query(MediaStore.getContentUri(
                                    MediaStore.MediaDevice.TABLE_NAME, deviceId), null, null, null, null);
                            if (deviceC.moveToFirst()) {
                                devicePath = deviceC.getString(deviceC.getColumnIndex(MediaStore.MediaDevice.FIELD_DEVICE_PATH));
                                mDevicePath.put(deviceId, devicePath);
                            }
                            deviceC.close();
                        }
                        String path = devicePath + rPath;
                        mArray.add(new MediaInfo(_id, path, name, title, poster, deviceId, devicePath));
                    }
                    cursor.close();
                }
                updateUI();
                break;
        }

    }

    @Override
    public void onMenuItemOnSelection(MenuBuild.MenuItem item) {
    }

    @Override
    public void onMenuDismiss() {
    }

    protected void updateUI() {
        if (mGridView != null) {
            if (mAdapter == null) {
                mAdapter = new Adapter(mContext, mArray);
                mGridView.setAdapter(mAdapter);
            } else {
                mAdapter.notifyDataSetChanged();
            }
        }
        if (mTextView != null) {
            mTextView.setText("视频 " + mArray.size() + " 部");
        }
    }

    @Override
    public void QueryNotify(Uri uri, Cursor cursor) {
        if (uri.equals(MediaStore.MediaBase.CONTENT_URI)) {
            if (mMenuItemId != 3) {
                return;
            }
            mArray.clear();
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_NAME));
                String rPath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_RELATIVE_PATH));
                long deviceId = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_DEVICE_ID));
                long _id = cursor.getLong(cursor.getColumnIndex("_id"));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.MediaInfo.FIELD_TITLE));
                String poster = cursor.getString(cursor.getColumnIndex(MediaStore.MediaInfo.FIELD_POSTER));
                String devicePath = "";
                if (mDevicePath.containsKey(deviceId)) {
                    devicePath = mDevicePath.get(deviceId);
                } else {
                    Cursor deviceC = mContext.getContentResolver().query(MediaStore.getContentUri(
                            MediaStore.MediaDevice.TABLE_NAME, deviceId), null, null, null, null);
                    if (deviceC.moveToFirst()) {
                        devicePath = deviceC.getString(deviceC.getColumnIndex(MediaStore.MediaDevice.FIELD_DEVICE_PATH));
                        mDevicePath.put(deviceId, devicePath);
                    }
                    deviceC.close();
                }
                String path = devicePath + rPath;
                mArray.add(new MediaInfo(_id, path, name, title, poster, deviceId, devicePath));
            }
            updateUI();
        }
    }


    private class Adapter extends ArrayAdapter<MediaInfo> {

        public Adapter(Context context, List<MediaInfo> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (null == convertView) {
                holder = new ViewHolder();
                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.VERTICAL);
                FrameLayout imageContainer = new FrameLayout(mContext);
                imageContainer.setBackgroundResource(R.drawable.main_focus_bg);
                ImageView image = new ImageView(mContext);
                image.setScaleType(ImageView.ScaleType.FIT_XY);
                imageContainer.addView(image, com.vst.dev.common.util.Utils.getFitSize(mContext, 180),
                        com.vst.dev.common.util.Utils.getFitSize(mContext, 250));
                TextView text = new TextView(getContext());
                com.vst.dev.common.util.Utils.applyFace(text);
                text.setSingleLine(true);
                text.setPadding(0, 0, 0, com.vst.dev.common.util.Utils.getFitSize(mContext, 15));
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, com.vst.dev.common.util.Utils.getFitSize(mContext, 24));
                text.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                text.setMarqueeRepeatLimit(Integer.MAX_VALUE);
                layout.addView(imageContainer, -2, -2);
                layout.addView(text, -1, -2);
                convertView = layout;
                holder.poster = image;
                holder.name = text;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            MediaInfo info = getItem(position);
            holder.name.setText(null == info.title ? info.name : info.title);
            if (info.poster != null) {
                fetcher.loadImage(info.poster, holder.poster, R.drawable.poster_default);
            } else {
                holder.poster.setImageResource(R.drawable.poster_default);
            }
            return convertView;
        }


        class ViewHolder {
            ImageView poster;
            TextView name;
        }
    }


}
