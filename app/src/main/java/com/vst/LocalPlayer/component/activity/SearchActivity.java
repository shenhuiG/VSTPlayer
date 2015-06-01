package com.vst.LocalPlayer.component.activity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.vst.LocalPlayer.MediaStoreNotifier;
import com.vst.LocalPlayer.R;
import com.vst.LocalPlayer.UUtils;
import com.vst.LocalPlayer.component.provider.MediaStore;
import com.vst.LocalPlayer.model.MediaInfo;
import com.vst.dev.common.util.Utils;
import com.yixia.zi.utils.ImageCache;
import com.yixia.zi.utils.ImageFetcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class SearchActivity extends BaseActivity implements MediaStoreNotifier.CallBack {

    public static final String TAG = "VideosScreen";
    private Context mContext;
    private MediaStoreNotifier notifier = null;
    private GridView mGridView;
    private TextView mTextView;
    private ArrayAdapter<MediaInfo> mAdapter;
    private HashMap<String, MediaInfo> mAllMap = new HashMap<String, MediaInfo>();
    private ArrayList<MediaInfo> mArray = new ArrayList<MediaInfo>();
    private HashMap<Long, String> mDevicePath = new HashMap<Long, String>();
    private ImageFetcher fetcher;
    private TextView mKeyWordEditView;
    private LinearLayout mKeyPadView;
    private int mKeyPadType = KEYPAD_ABC;
    private String mKeyWord = "";
    private static final int KEYPAD_123 = 0;
    private static final int KEYPAD_ABC = 1;
    private static final String[] KEYWORD_123 = new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private static final String[] KEYWORD_ABC = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        initView();
        notifier = new MediaStoreNotifier(mContext.getContentResolver(), this);
        notifier.registQueryContentUri(MediaStore.MediaBase.CONTENT_URI, null,
                MediaStore.MediaBase.FIELD_VALID + "=? AND " + MediaStore.MediaBase.FIELD_HIDE + "=?", new String[]{"1", "0"}, null);
        fetcher = new ImageFetcher(mContext);
        fetcher.setImageCache(new ImageCache(mContext, new ImageCache.ImageCacheParams(".cache")));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext = null;
        notifier.release();
        fetcher.setExitTasksEarly(true);
        fetcher = null;
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


    private void initView() {
        LinearLayout root = new LinearLayout(mContext);
        root.setBackgroundResource(R.drawable.main_bg);
        root.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout leftLayout = new LinearLayout(mContext);
        leftLayout.setOrientation(LinearLayout.VERTICAL);
        leftLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        leftLayout.setBackgroundColor(0x0cffffff);
        leftLayout.setPadding(Utils.getFitSize(mContext, 20), Utils.getFitSize(mContext, 100), Utils.getFitSize(mContext, 20),
                Utils.getFitSize(mContext, 30));
        mKeyWordEditView = new TextView(mContext);
        mKeyWordEditView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 30));
        mKeyWordEditView.setTextColor(Color.WHITE);
        mKeyWordEditView.setSingleLine(true);
        mKeyWordEditView.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        mKeyWordEditView.setHint("输入关键字");
        mKeyWordEditView.setBackgroundResource(R.drawable.edittext_bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.leftMargin = Utils.getFitSize(mContext, 30);
        lp.rightMargin = Utils.getFitSize(mContext, 30);
        leftLayout.addView(mKeyWordEditView, lp);
        RelativeLayout actionLayout = new RelativeLayout(mContext);
        actionLayout.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout changedKeyPad = new FrameLayout(mContext);
        changedKeyPad.setBackgroundResource(R.drawable.icon_ssssssss_bg_l);
        changedKeyPad.setFocusable(true);
        changedKeyPad.setPadding(Utils.getFitSize(mContext, 18), 0, Utils.getFitSize(mContext, 18), 0);
        final ImageView changedKeyPadImage = new ImageView(mContext);
        changedKeyPad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mKeyPadType == KEYPAD_ABC) {
                    setupPadView(KEYWORD_123);
                    mKeyPadType = KEYPAD_123;
                } else {
                    setupPadView(KEYWORD_ABC);
                    mKeyPadType = KEYPAD_ABC;
                }
                changedKeyPadImage.setImageResource(mKeyPadType == KEYPAD_123 ? R.drawable.search_keypad_abc : R.drawable.search_keypad_123);
            }
        });
        changedKeyPadImage.setScaleType(ImageView.ScaleType.FIT_XY);
        changedKeyPadImage.setImageResource(mKeyPadType == KEYPAD_123 ? R.drawable.search_keypad_abc : R.drawable.search_keypad_123);
        changedKeyPad.addView(changedKeyPadImage,
                new FrameLayout.LayoutParams(Utils.getFitSize(mContext, 49), Utils.getFitSize(mContext, 20), Gravity.CENTER));
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(-2, -1);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        actionLayout.addView(changedKeyPad, rlp);
        FrameLayout deleteKey = new FrameLayout(mContext);
        deleteKey.setPadding(Utils.getFitSize(mContext, 18), 0, Utils.getFitSize(mContext, 18), 0);
        deleteKey.setBackgroundResource(R.drawable.icon_ssssssss_bg_l);
        ImageView deleteKeyImage = new ImageView(mContext);
        deleteKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mKeyWord = "";
                mKeyWordEditView.setText(mKeyWord);
                showResult();
            }
        });
        deleteKey.setFocusable(true);
        deleteKeyImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        deleteKeyImage.setImageResource(R.drawable.search_delete);
        deleteKey.addView(deleteKeyImage, new FrameLayout.LayoutParams(Utils.getFitSize(mContext, 50),
                Utils.getFitSize(mContext, 26), Gravity.CENTER));
        RelativeLayout.LayoutParams rlp2 = new RelativeLayout.LayoutParams(-2, -1);
        rlp2.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        actionLayout.addView(deleteKey, rlp2);
        FrameLayout backKey = new FrameLayout(mContext);
        backKey.setBackgroundResource(R.drawable.icon_ssssssss_bg_l);
        backKey.setPadding(Utils.getFitSize(mContext, 18), 0, Utils.getFitSize(mContext, 18), 0);
        ImageView backKeyImage = new ImageView(mContext);
        backKey.setFocusable(true);
        backKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mKeyWord.length() > 0) {
                    mKeyWord = mKeyWord.substring(0, mKeyWord.length() - 1);
                } else {
                    mKeyWord = "";
                }
                mKeyWordEditView.setText(mKeyWord);
                showResult();
            }
        });
        backKeyImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        backKeyImage.setImageResource(R.drawable.search_back);
        backKey.addView(backKeyImage,
                new FrameLayout.LayoutParams(
                        Utils.getFitSize(mContext, 49),
                        Utils.getFitSize(mContext, 26), Gravity.CENTER));
        RelativeLayout.LayoutParams rlp3 = new RelativeLayout.LayoutParams(-2, -1);
        rlp3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rlp3.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        actionLayout.addView(backKey, rlp3);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(-1, Utils.getFitSize(mContext, 70));
        lp1.leftMargin = Utils.getFitSize(mContext, 26);
        lp1.rightMargin = Utils.getFitSize(mContext, 26);
        lp1.topMargin = Utils.getFitSize(mContext, 10);
        lp1.bottomMargin = Utils.getFitSize(mContext, 0);
        leftLayout.addView(actionLayout, lp1);
        mKeyPadView = new LinearLayout(mContext);
        mKeyPadView.setOrientation(LinearLayout.VERTICAL);
        leftLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        leftLayout.addView(mKeyPadView, -1, -1);
        setupPadView(KEYWORD_ABC);
        LinearLayout rightLayout = new LinearLayout(mContext);
        rightLayout.setOrientation(LinearLayout.VERTICAL);
        rightLayout.setPadding(
                Utils.getFitSize(mContext, 40), Utils.getFitSize(mContext, 25),
                Utils.getFitSize(mContext, 40), Utils.getFitSize(mContext, 0));
        LinearLayout top = new LinearLayout(mContext);
        mTextView = new TextView(mContext);
        mTextView.setText("搜索结果 " + 0 + " 部");
        Utils.applyFace(mTextView);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 26));
        top.addView(mTextView, new LinearLayout.LayoutParams(0, -2, 1f));
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(-1, -2);
        lp2.bottomMargin = Utils.getFitSize(mContext, 20);
        rightLayout.addView(top, lp2);
        mGridView = new GridView(mContext);
        mGridView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mGridView.setSelection(-1);
                }
            }
        });
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter = new Adapter(mContext, mArray);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaInfo info = (MediaInfo) parent.getAdapter().getItem(position);
                Uri uri = UUtils.getMediaUri(info.path);
                if (uri != null) {
                    UUtils.playMediaFile(mContext, uri, info);
                } else {
                    Toast.makeText(mContext, "该影片不存在 路径：" + "盘:" + info.path, Toast.LENGTH_LONG).show();
                }
            }
        });
        mGridView.setNumColumns(4);
        mGridView.setHorizontalSpacing(Utils.getFitSize(mContext, 20));
        rightLayout.addView(mGridView);
        root.addView(leftLayout, Utils.getFitSize(mContext, 370), -1);
        root.addView(rightLayout, -1, -1);
        setContentView(root);
    }

    private void setupPadView(String[] pad) {
        if (mKeyPadView != null) {
            mKeyPadView.removeAllViews();
            LinearLayout rowLayout = null;
            for (int i = 0; i < pad.length; i++) {
                int j = i % 5;
                if (j == 0) {
                    rowLayout = new LinearLayout(mContext);
                    rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                    rowLayout.setGravity(Gravity.CENTER_VERTICAL);
                    rowLayout.addView(makeKeyTeexView(pad[i]), new LinearLayout.LayoutParams(
                            Utils.getFitSize(mContext, 65),
                            Utils.getFitSize(mContext, 65)));
                } else if (j == 4) {
                    if (rowLayout != null) {
                        rowLayout.addView(makeKeyTeexView(pad[i]), new LinearLayout.LayoutParams(
                                Utils.getFitSize(mContext, 60),
                                Utils.getFitSize(mContext, 60)));
                        mKeyPadView.addView(rowLayout);
                        rowLayout = null;
                    }
                } else {
                    if (rowLayout != null) {
                        rowLayout.addView(makeKeyTeexView(pad[i]), new LinearLayout.LayoutParams(
                                Utils.getFitSize(mContext, 60),
                                Utils.getFitSize(mContext, 60)));
                    }
                }
            }
            if (rowLayout != null) {
                mKeyPadView.addView(rowLayout);
                rowLayout = null;
            }
        }
    }

    private TextView makeKeyTeexView(final String s) {
        TextView tv = new TextView(mContext);
        tv.setFocusable(true);
        tv.setBackgroundResource(R.drawable.icon_ssssssss_bg_l);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 24));
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(getResources().getColorStateList(R.color.videos_menu_text));
        tv.setPadding(Utils.getFitSize(mContext, 0),
                Utils.getFitSize(mContext, 0),
                Utils.getFitSize(mContext, 0),
                Utils.getFitSize(mContext, 0));
        tv.setText(s);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(s);
            }
        });
        tv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TextView tv = (TextView) v;
                if (hasFocus) {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 34));
                } else {
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 24));
                }
            }
        });
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView tv = (TextView) v;
                mKeyWord = mKeyWord + tv.getText();
                mKeyWordEditView.setText(mKeyWord);
                showResult();
            }
        });
        return tv;
    }

    // 在全部里面按找關鍵字筛选
    private void showResult() {
        if ("".equals(mKeyWord)) {
            mArray.clear();
        } else {
            mArray.clear();
            mArray.addAll(searchWithKey(mKeyWord));
        }
        mAdapter.notifyDataSetChanged();
        if (mTextView != null) {
            mTextView.setText("搜索结果 " + mArray.size() + " 部");
        }
    }

    private ArrayList<MediaInfo> searchWithKey(String keyWord) {
        ArrayList<MediaInfo> result = new ArrayList<MediaInfo>();
        if (!mAllMap.isEmpty()) {
            Iterator<String> i = mAllMap.keySet().iterator();
            while (i.hasNext()) {
                String key = i.next();
                if (key.startsWith(keyWord)) {
                    result.add(mAllMap.get(key));
                }
            }
        }
        return result;
    }

    private boolean isHasKeyWord(MediaInfo mediaInfo, String keyWord) {
        boolean match = false;
        String reg = "\\*";
        char[] chars = keyWord.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            reg = reg + chars[i] + "\\*";
        }
        System.out.println(reg);
        if (mediaInfo.name != null) {
            String spell = UUtils.converterToFirstSpell(mediaInfo.name.trim()).toUpperCase();
            System.out.println(spell);
            match = spell.contains(keyWord);
        }
        if (!match) {
            if (mediaInfo.title != null) {
                String spell = UUtils.converterToFirstSpell(mediaInfo.title.trim()).toUpperCase();
                System.out.println(spell);
                match = spell.contains(keyWord);
            }
        }
        return match;
    }

    @Override
    public void QueryNotify(Uri uri, Cursor cursor) {
        if (uri.equals(MediaStore.MediaBase.CONTENT_URI)) {
            mAllMap.clear();
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
                MediaInfo mediaInfo = new MediaInfo(_id, path, name, title, poster, deviceId, devicePath);
                String key = "";
                if (mediaInfo.name != null) {
                    key += UUtils.converterToFirstSpell(mediaInfo.name.trim()).toUpperCase();
                }
                if (mediaInfo.title != null) {
                    key += UUtils.converterToFirstSpell(mediaInfo.title.trim()).toUpperCase();
                }
                System.out.println("key=" + key);
                mAllMap.put(key, mediaInfo);
            }
            showResult();
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
                imageContainer.setBackgroundResource(R.drawable.griditem_focus_bg);
                ImageView image = new ImageView(mContext);
                image.setScaleType(ImageView.ScaleType.FIT_XY);
                imageContainer.addView(image, Utils.getFitSize(mContext, 180),
                        Utils.getFitSize(mContext, 250));
                TextView text = new TextView(getContext());
                Utils.applyFace(text);
                text.setSingleLine(true);
                text.setGravity(Gravity.CENTER);
                text.setPadding(Utils.getFitSize(mContext, 8), 0, Utils.getFitSize(mContext, 8), Utils.getFitSize(mContext, 15));
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 24));
                text.setTextColor(getResources().getColorStateList(R.color.videos_menu_text));
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
            String s = null == info.title ? info.name : info.title;
            holder.name.setText(s.replace("-", "- "));
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
