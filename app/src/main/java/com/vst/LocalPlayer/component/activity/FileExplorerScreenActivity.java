package com.vst.LocalPlayer.component.activity;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.vst.LocalPlayer.MediaStoreNotifier;
import com.vst.LocalPlayer.R;
import com.vst.LocalPlayer.Utils;
import com.vst.LocalPlayer.component.provider.MediaStore;
import com.vst.LocalPlayer.component.provider.MediaStoreHelper;
import com.vst.LocalPlayer.component.service.MyIntentService;
import com.vst.LocalPlayer.model.DeviceInfo;
import com.vst.LocalPlayer.model.FileCategory;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class FileExplorerScreenActivity extends Activity implements MediaStoreNotifier.CallBack, AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {

    public static final String PARAMS_DEVICE = "deviceId";
    public static final String TAG = "FileExplorerScreen";
    private DeviceInfo mDevice;
    private Context mContext = null;
    private boolean mDeviceExists = false;
    private ListView mListView = null;
    private TextView explorerDirView = null;
    private String mExplorerRootPath = null;
    private TextView emptyView;
    private MediaStoreNotifier notifier;
    private List<FileItem> childFiles = new ArrayList<FileItem>();
    private FileArrayAdapter mAdapter;
    private int selection = -1;
    private int y = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplication();
        initView();
        Intent i = getIntent();
        mDevice = (DeviceInfo) i.getSerializableExtra(PARAMS_DEVICE);
        if (mDevice != null) {
            notifier = new MediaStoreNotifier(mContext.getContentResolver(), this);
            notifier.registQueryContentUri(MediaStore.MediaDevice.CONTENT_URI, null,
                    MediaStore.MediaDevice._ID + "=? AND " + MediaStore.MediaDevice.FIELD_VALID + "=?",
                    new String[]{mDevice.id + "", "1"}, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext = null;
        notifier.release();
        notifier = null;
    }

    private void initView() {
        LinearLayout layout = new LinearLayout(mContext);
        layout.setBackgroundResource(R.drawable.main_bg);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(com.vst.dev.common.util.Utils.getFitSize(mContext, 70),
                com.vst.dev.common.util.Utils.getFitSize(mContext, 20), com.vst.dev.common.util.Utils.getFitSize(mContext, 80), 0);
        LinearLayout top = new LinearLayout(mContext);
        top.setOrientation(LinearLayout.HORIZONTAL);
        explorerDirView = new TextView(mContext);
        explorerDirView.setTextSize(TypedValue.COMPLEX_UNIT_PX, com.vst.dev.common.util.Utils.getFitSize(mContext, 30));
        explorerDirView.setSingleLine(true);
        explorerDirView.setEllipsize(TextUtils.TruncateAt.START);
        top.addView(explorerDirView, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView okTipView = new TextView(mContext);
        okTipView.setTextSize(TypedValue.COMPLEX_UNIT_PX, com.vst.dev.common.util.Utils.getFitSize(mContext, 20));
        okTipView.setPadding(com.vst.dev.common.util.Utils.getFitSize(mContext, 40), 0, 0, 0);
        okTipView.setTextColor(0xff999999);
        okTipView.setText(com.vst.dev.common.util.Utils.makeImageSpannable(getResources().getString(R.string.explorer_ok_tip),
                getResources().getDrawable(R.drawable.ic_ok_tip), 0, com.vst.dev.common.util.Utils.getFitSize(mContext, 23),
                com.vst.dev.common.util.Utils.getFitSize(mContext, 23), ImageSpan.ALIGN_BOTTOM));
        top.addView(okTipView, -2, -2);
        layout.addView(top, -1, -2);
        emptyView = new TextView(mContext);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setText(R.string.deviceEmpty);
        emptyView.setTextSize(TypedValue.COMPLEX_UNIT_PX, com.vst.dev.common.util.Utils.getFitSize(mContext, 30));
        mListView = new ListView(mContext);
        mListView.setEmptyView(emptyView);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemLongClickListener(this);
        mListView.setSelector(R.drawable.explorer_item_selector_bg);
        mListView.setVerticalScrollBarEnabled(false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
        lp.leftMargin = com.vst.dev.common.util.Utils.getFitSize(mContext, 150);
        lp.rightMargin = com.vst.dev.common.util.Utils.getFitSize(mContext, 150);
        lp.topMargin = com.vst.dev.common.util.Utils.getFitSize(mContext, 50);
        layout.addView(mListView, lp);
        layout.addView(emptyView);
        setContentView(layout);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FileItem fileItem = mAdapter.getItem(position);
        switch (fileItem.mCategory) {
            case Dir:
                selection = 0;
                y = view.getTop();
                updateDate(fileItem.mFile, null);
                updateUI();
                break;
            case Apk:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setDataAndType(Uri.fromFile(fileItem.mFile), "application/vnd.android.package-archive");
                mContext.startActivity(intent);
                break;
            case Music:
                Utils.playAudioFile(mContext, fileItem.mFile);
                break;
            case Video:
            case BDMV:
                String relativePath = fileItem.mFile.getAbsolutePath().replace(mDevice.path, "");
                long mediaId = -1;
                long deviceId = -1;
                Cursor c = mContext.getContentResolver().query(MediaStore.MediaBase.CONTENT_URI, null,
                        MediaStore.MediaBase.FIELD_RELATIVE_PATH + "=?", new String[]{relativePath}, null);
                if (c.moveToFirst()) {
                    mediaId = c.getLong(c.getColumnIndex(MediaStore.MediaBase._ID));
                    deviceId = c.getLong(c.getColumnIndex(MediaStore.MediaBase.FIELD_DEVICE_ID));
                }
                c.close();
                Uri uri = Utils.getMediaUri(fileItem.mFile.getAbsolutePath());
                if (uri != null) {
                    Utils.playMediaFile(mContext, uri, mediaId, deviceId, mDevice.path);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        FileItem fileItem = mAdapter.getItem(position);
        if ((fileItem.mCategory == FileCategory.Video || fileItem.mCategory == FileCategory.BDMV) && !fileItem.isInStore) {
            Uri uri = MediaStoreHelper.addNewMediaBase(mContext.getContentResolver(), fileItem.mFile.getAbsolutePath(), mDevice.path, mDevice.id, null);
            if (uri != null) {
                showToast();
                FileArrayAdapter.ViewHolder holder = (FileArrayAdapter.ViewHolder) view.getTag();
                holder.videoInStoreView.setVisibility(View.INVISIBLE);
                long mediaId = ContentUris.parseId(uri);
                MyIntentService.startActionEntryInfo(mContext, fileItem.mFile.getAbsolutePath(), null, mediaId);
            }
        }
        return true;
    }

    private CharSequence makePathCS() {
        String rPath = mExplorerRootPath.replace(mDevice.path, "");
        String name = mDevice.path.substring(mDevice.path.lastIndexOf("/") + 1);
        String s = name + rPath;
        s.replace("/", " / ");
        return null;
    }

    private void showToast() {
        Toast t = new Toast(mContext);
        LinearLayout root = new LinearLayout(mContext);
        ImageView i = new ImageView(mContext);
        i.setImageResource(R.drawable.ic_opration_add_toast_bg);
        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(i, com.vst.dev.common.util.Utils.getFitSize(mContext, 226), com.vst.dev.common.util.Utils.getFitSize(mContext, 173));
        t.setView(root);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.setDuration(Toast.LENGTH_SHORT);
        t.show();
    }


    protected void updateUI() {
        if (mListView != null) {
            if (mAdapter == null) {
                mAdapter = new FileArrayAdapter(mContext, childFiles);
                mListView.setAdapter(mAdapter);
            } else {
                mAdapter.notifyDataSetChanged();
            }
            if (selection >= 0) {
                mListView.setSelectionFromTop(selection, y);
            }
            if (mDeviceExists) {
                mListView.setEmptyView(null);
            } else {
                mListView.setEmptyView(emptyView);
            }
            mListView.requestFocus();
        }
        if (explorerDirView != null) {
            String rPath = mExplorerRootPath.replace(mDevice.path, "");
            String name = mDevice.path.substring(mDevice.path.lastIndexOf("/") + 1);
            explorerDirView.setText(name + rPath);
        }
    }

    @Override
    public void onBackPressed() {
        if (onBack()) {
            return;
        }
        super.onBackPressed();
    }

    public boolean onBack() {
        if (!mExplorerRootPath.equals(mDevice.path)) {
            updateDate(new File(mExplorerRootPath).getParentFile(), new File(mExplorerRootPath));
            updateUI();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void QueryNotify(Uri uri, Cursor cursor) {
        if (MediaStore.MediaDevice.CONTENT_URI.equals(uri)) {
            if (cursor.moveToFirst()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaDevice.FIELD_DEVICE_PATH));
                File file = new File(path);
                if (file.exists()) {
                    mDeviceExists = true;
                    updateDate(file, null);
                    updateUI();
                }
            } else {
                mDeviceExists = false;
                childFiles.clear();
                updateUI();
            }
        }
    }


    private void updateDate(File file, File child) {
        mExplorerRootPath = file.getAbsolutePath();
        File[] files = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                File f = new File(dir, filename);
                if (!f.isHidden() && !filename.contains("$")) {
                    return true;
                }
                return false;
            }
        });
        childFiles.clear();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                FileItem fileItem = new FileItem(f);
                childFiles.add(fileItem);
            }
            if (child != null && child.exists()) {
                selection = childFiles.indexOf(new FileItem(child));
            }
        }
    }


    private class FileArrayAdapter extends ArrayAdapter<FileItem> {

        public FileArrayAdapter(Context context, List<FileItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FileItem fileItem = getItem(position);
            ViewHolder holder;
            if (convertView == null) {
                LinearLayout layout = new LinearLayout(mContext);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setPadding(
                        com.vst.dev.common.util.Utils.getFitSize(mContext, 40),
                        com.vst.dev.common.util.Utils.getFitSize(mContext, 25),
                        com.vst.dev.common.util.Utils.getFitSize(mContext, 40),
                        com.vst.dev.common.util.Utils.getFitSize(mContext, 25));
                layout.setGravity(Gravity.CENTER_VERTICAL);
                ImageView leftView = new ImageView(mContext);
                leftView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                layout.addView(leftView, com.vst.dev.common.util.Utils.getFitSize(mContext, 142), com.vst.dev.common.util.Utils.getFitSize(mContext, 82));
                LinearLayout centerView = new LinearLayout(mContext);
                centerView.setOrientation(LinearLayout.VERTICAL);
                centerView.setGravity(Gravity.BOTTOM);
                centerView.setPadding(com.vst.dev.common.util.Utils.getFitSize(mContext, 30), 0,
                        com.vst.dev.common.util.Utils.getFitSize(mContext, 30), 0);
                TextView nameView = new TextView(mContext);
                nameView.setGravity(Gravity.CENTER_VERTICAL);
                nameView.setTextSize(TypedValue.COMPLEX_UNIT_PX, com.vst.dev.common.util.Utils.getFitSize(mContext, 24));
                nameView.setSingleLine(true);
                nameView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                nameView.setMarqueeRepeatLimit(Integer.MAX_VALUE);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
                lp.bottomMargin = com.vst.dev.common.util.Utils.getFitSize(mContext, 10);
                centerView.addView(nameView, lp);
                ImageView videoInStoreView = new ImageView(mContext);
                videoInStoreView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                videoInStoreView.setImageResource(R.drawable.ic_disk_tishi);
                centerView.addView(videoInStoreView, com.vst.dev.common.util.Utils.getFitSize(mContext, 110),
                        com.vst.dev.common.util.Utils.getFitSize(mContext, 25));
                layout.addView(centerView, new LinearLayout.LayoutParams(0, -1, 1f));
                ImageView rightView = new ImageView(mContext);
                rightView.setImageResource(R.drawable.ic_disk_folder_open_nor);
                layout.addView(rightView, com.vst.dev.common.util.Utils.getFitSize(mContext, 13),
                        com.vst.dev.common.util.Utils.getFitSize(mContext, 25));
                holder = new ViewHolder();
                holder.fileIconView = leftView;
                holder.fileNameView = nameView;
                holder.folderOpenView = rightView;
                holder.videoInStoreView = videoInStoreView;
                convertView = layout;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if ((fileItem.mCategory == FileCategory.Video || fileItem.mCategory == FileCategory.BDMV) && !fileItem.isInStore) {
                holder.videoInStoreView.setVisibility(View.VISIBLE);
            } else {
                holder.videoInStoreView.setVisibility(View.INVISIBLE);
            }
            if (fileItem.mCategory == FileCategory.Dir) {
                holder.folderOpenView.setVisibility(View.VISIBLE);
            } else {
                holder.folderOpenView.setVisibility(View.INVISIBLE);
            }
            holder.fileIconView.setImageResource(Utils.getFileCategoryIcon(fileItem.mCategory));
            holder.fileNameView.setText(fileItem.mFile.getName());
            return convertView;
        }

        private class ViewHolder {
            ImageView fileIconView;
            TextView fileNameView;
            ImageView videoInStoreView;
            ImageView folderOpenView;
        }
    }


    private class FileItem {
        File mFile;
        FileCategory mCategory;
        boolean isInStore = true;

        FileItem(File file) {
            mFile = file;
            mCategory = Utils.getFileCategory(mFile);
            if (mCategory == FileCategory.Video || mCategory == FileCategory.BDMV) {
                String relativePath = file.getAbsolutePath().replace(mDevice.path, "");
                long mediaId = MediaStoreHelper.mediaIsInStore(mContext.getContentResolver(), mDevice.id, relativePath);
                if (mediaId < 0) {
                    isInStore = false;
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof FileItem) {
                FileItem item = (FileItem) o;
                return item.mFile.getAbsolutePath().equals(mFile.getAbsolutePath());
            }
            return false;
        }
    }
}
