package com.vst.LocalPlayer.component.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.vst.LocalPlayer.MediaStoreNotifier;
import com.vst.LocalPlayer.R;
import com.vst.LocalPlayer.component.provider.MediaStore;
import com.vst.LocalPlayer.component.provider.MediaStoreHelper;
import com.vst.LocalPlayer.component.service.MyIntentService;
import com.vst.LocalPlayer.model.DeviceInfo;

import java.util.ArrayList;

public class DeviceScreenActivity extends Activity implements MediaStoreNotifier.CallBack {

    private Context mContext = null;
    private ArrayList<DeviceInfo> deviceInfos = new ArrayList<DeviceInfo>();
    private LinearLayout mCenterLayout = null;
    private MediaStoreNotifier notifier;
    private TextView mCountView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplication();
        initView();
        notifier = new MediaStoreNotifier(mContext.getContentResolver(), this);
        notifier.registQueryContentUri(MediaStore.MediaDevice.CONTENT_URI, null,
                MediaStore.MediaDevice.FIELD_VALID + "=?", new String[]{"1"}, null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        notifier.release();
        mContext = null;
    }


    private void initView() {
        FrameLayout root = new FrameLayout(mContext);
        root.setBackgroundResource(R.drawable.main_bg);
        LinearLayout topView = new LinearLayout(mContext);
        topView.setPadding(com.vst.dev.common.util.Utils.getFitSize(mContext, 60), com.vst.dev.common.util.Utils.getFitSize(mContext, 20),
                com.vst.dev.common.util.Utils.getFitSize(mContext, 60), 0);
        topView.setHorizontalGravity(LinearLayout.HORIZONTAL);
        TextView text1 = new TextView(mContext);
        text1.setTextSize(TypedValue.COMPLEX_UNIT_PX, com.vst.dev.common.util.Utils.getFitSize(mContext, 30));
        text1.setText(R.string.device_tip1);
        topView.addView(text1);
        mCountView = new TextView(mContext);
        mCountView.setTextSize(TypedValue.COMPLEX_UNIT_PX, com.vst.dev.common.util.Utils.getFitSize(mContext, 24));
        mCountView.setText(getString(R.string.device_tip2, 0));
        topView.addView(mCountView, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView menuTipView = new TextView(mContext);
        menuTipView.setPadding(0, 0, com.vst.dev.common.util.Utils.getFitSize(mContext, 15), 0);
        menuTipView.setText(com.vst.dev.common.util.Utils.makeImageSpannable(getResources().getString(R.string.device_menu_tip),
                getResources().getDrawable(R.drawable.ic_menu_tip), 0, com.vst.dev.common.util.Utils.getFitSize(mContext, 23),
                com.vst.dev.common.util.Utils.getFitSize(mContext, 23), ImageSpan.ALIGN_BOTTOM));
        topView.addView(menuTipView);
        TextView upTipView = new TextView(mContext);
        upTipView.setText(com.vst.dev.common.util.Utils.makeImageSpannable(getResources().getString(R.string.device_up_tip),
                getResources().getDrawable(R.drawable.ic_up_tip), 0, com.vst.dev.common.util.Utils.getFitSize(mContext, 23),
                com.vst.dev.common.util.Utils.getFitSize(mContext, 23), ImageSpan.ALIGN_BOTTOM));
        topView.addView(upTipView);
        root.addView(topView);
        HorizontalScrollView scrollView = new HorizontalScrollView(mContext);
        scrollView.setHorizontalScrollBarEnabled(false);
        mCenterLayout = new LinearLayout(mContext);
        mCenterLayout.setOrientation(LinearLayout.HORIZONTAL);
        mCenterLayout.setPadding(com.vst.dev.common.util.Utils.getFitSize(mContext, 120), 0, 0, 0);
        scrollView.addView(mCenterLayout);
        root.addView(scrollView, new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER));
        setContentView(root);
    }

    private void updateUI() {
        mCenterLayout.removeAllViews();
        if (deviceInfos != null && deviceInfos.size() > 0) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.rightMargin = com.vst.dev.common.util.Utils.getFitSize(mContext, 15);
            for (int i = 0; i < deviceInfos.size(); i++) {
                DeviceInfo device = deviceInfos.get(i);
                View v = makeDeviceView(device);
                if (v != null) {
                    mCenterLayout.addView(v, lp);
                }
            }
        }
        mCountView.setText(getString(R.string.device_tip2, deviceInfos != null ? deviceInfos.size() : 0));
        mCenterLayout.requestFocus();
    }


    private View makeDeviceView(final DeviceInfo deviceInfo) {
        FrameLayout deviceView = new FrameLayout(mContext);
        deviceView.setFocusable(true);
        deviceView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        deviceView.setBackgroundResource(R.drawable.main_focus_bg);
        deviceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, FileExplorerScreenActivity.class);
                i.putExtra(FileExplorerScreenActivity.PARAMS_DEVICE, deviceInfo);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(i);
            }
        });
        deviceView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    ContentResolver cr = mContext.getContentResolver();
                    MediaStoreHelper.updateMediaDeviceValid(cr, deviceInfo.path, deviceInfo.uuid, false);
                    MediaStoreHelper.updateMediaValidByDevice(cr, deviceInfo.id, false);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                    MyIntentService.startActionScanner(mContext, deviceInfo.path, deviceInfo.id);
                    return true;
                }
                return false;
            }
        });
        ImageView deviceBg = new ImageView(mContext);
        deviceBg.setImageResource(R.drawable.bg_usb);
        deviceBg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        deviceView.addView(deviceBg, com.vst.dev.common.util.Utils.getFitSize(mContext, 240), com.vst.dev.common.util.Utils.getFitSize(mContext, 308));
        TextView mDeviceNumTxtView = new TextView(mContext);
        mDeviceNumTxtView.setText(deviceInfo.name);
        mDeviceNumTxtView.setGravity(Gravity.CENTER_VERTICAL);
        mDeviceNumTxtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, com.vst.dev.common.util.Utils.getFitSize(mContext, 28));
        mDeviceNumTxtView.setPadding(com.vst.dev.common.util.Utils.getFitSize(mContext, 32), 0, 0, 0);
        deviceView.addView(mDeviceNumTxtView, new FrameLayout.LayoutParams(-1, com.vst.dev.common.util.Utils.getFitSize(mContext, 108), Gravity.BOTTOM));
        return deviceView;
    }

    @Override
    public void QueryNotify(Uri uri, Cursor cursor) {
        if (uri.equals(MediaStore.MediaDevice.CONTENT_URI)) {
            deviceInfos.clear();
            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaDevice.FIELD_DEVICE_PATH));
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaDevice._ID));
                String uuid = cursor.getString(cursor.getColumnIndex(MediaStore.MediaDevice.FIELD_DEVICE_UUID));
                deviceInfos.add(new DeviceInfo(id, path, uuid));
            }
            updateUI();
        }
    }

}
