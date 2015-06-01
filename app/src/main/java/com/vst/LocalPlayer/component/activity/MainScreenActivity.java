package com.vst.LocalPlayer.component.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vst.LocalPlayer.MediaStoreNotifier;
import com.vst.LocalPlayer.R;
import com.vst.LocalPlayer.UpgradeUtils;
import com.vst.LocalPlayer.component.provider.MediaStore;
import com.vst.LocalPlayer.component.service.MyIntentService;
import com.vst.LocalPlayer.widget.UpgradDialog;
import com.vst.LocalPlayer.widget.WindowLoadingHelper;
import com.vst.dev.common.util.Utils;

import java.util.List;

public class MainScreenActivity extends BaseActivity implements MediaStoreNotifier.CallBack {

    private Context ctx = null;
    private TextView mVideoNumTxtView = null;
    private TextView mDeviceNumTxtView = null;
    private int mVideoCount = 0;
    private int mDeviceCount = 0;
    private MediaStoreNotifier notifier;
    private UpgradDialog mUpdateDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = this;
        WindowLoadingHelper.setLaoding(this, checkServiceIsRunning(this, MyIntentService.class.getName()));
        initView();
        notifier = new MediaStoreNotifier(ctx.getContentResolver(), this);
        notifier.registQueryContentUri(MediaStore.MediaBase.CONTENT_URI, null,
                MediaStore.MediaBase.FIELD_VALID + "=?", new String[]{"1"}, null);
        notifier.registQueryContentUri(MediaStore.MediaDevice.CONTENT_URI, null,
                MediaStore.MediaDevice.FIELD_VALID + "=?", new String[]{"1"}, null);
        registerReceiver(receiver, new IntentFilter(UpgradeUtils.ACTION));
        MyIntentService.startActionUpgrade(getApplication());
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UpgradeUtils.ACTION.equals(action)) {
                Bundle b = intent.getExtras();
                showUpgradeDialog(b);
            }
        }
    };

    private static boolean checkServiceIsRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        try {
            List<ActivityManager.RunningServiceInfo> serviceList
                    = activityManager.getRunningServices(30);
            if (serviceList != null && serviceList.size() > 0) {
                for (int i = 0; i < serviceList.size(); i++) {
                    if (serviceList.get(i).service.getClassName().equals(className) == true) {
                        isRunning = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
        }
        return isRunning;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        notifier.release();
        notifier = null;
        ctx = null;
    }

    private void showUpgradeDialog(Bundle b) {
        if (mUpdateDialog == null) {
            mUpdateDialog = new UpgradDialog(ctx);
        }
        mUpdateDialog.setBundler(b);
        mUpdateDialog.show();
    }

    private void initView() {
        FrameLayout layout = new FrameLayout(ctx);
        layout.setBackgroundResource(R.drawable.main_bg);
        TextView titleTxt = new TextView(ctx);
        titleTxt.setPadding(Utils.getFitSize(ctx, 60),
                Utils.getFitSize(ctx, 20), 0, 0);
        Utils.applyFace(titleTxt);
        titleTxt.setText(R.string.vst_player);
        titleTxt.setTextColor(Color.WHITE);
        titleTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(ctx, 30));
        layout.addView(titleTxt);
        LinearLayout center = new LinearLayout(ctx);
        center.setOrientation(LinearLayout.HORIZONTAL);
        FrameLayout videoView = new FrameLayout(ctx);
        videoView.setFocusable(true);
        videoView.setBackgroundResource(R.drawable.main_focus_bg);
        videoView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        ImageView videoBg = new ImageView(ctx);
        videoBg.setScaleType(ScaleType.FIT_CENTER);
        videoBg.setImageResource(R.drawable.bg_shipin);
        videoView.addView(videoBg, Utils.getFitSize(ctx, 360), Utils.getFitSize(ctx, 308));
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDeviceCount > 0) {
                    Intent i = new Intent(ctx, VideosScreenActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(i);
                }
            }
        });
        mVideoNumTxtView = new TextView(ctx);
        mVideoNumTxtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(ctx, 28));
        mVideoNumTxtView.setGravity(Gravity.CENTER_VERTICAL);
        mVideoNumTxtView.setPadding(Utils.getFitSize(ctx, 32), 0, 0, 0);
        mVideoNumTxtView.setText(getString(R.string.videoFomart, mVideoCount));
        Utils.applyFace(mVideoNumTxtView);
        videoView.addView(mVideoNumTxtView, new FrameLayout.LayoutParams(-1, Utils.getFitSize(ctx, 108), Gravity.BOTTOM));
        center.addView(videoView, new LinearLayout.LayoutParams(-2, -2));
        FrameLayout deviceView = new FrameLayout(ctx);
        deviceView.setFocusable(true);
        deviceView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        deviceView.setBackgroundResource(R.drawable.main_focus_bg);
        deviceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoCount > 0) {
                    Intent i = new Intent(ctx, DeviceScreenActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ctx.startActivity(i);
                }
            }
        });
        ImageView deviceBg = new ImageView(ctx);
        deviceBg.setBackgroundColor(getResources().getColor(R.color.yellow_1));
        deviceBg.setImageResource(R.drawable.bg_folder);
        deviceBg.setScaleType(ScaleType.FIT_CENTER);
        deviceView.addView(deviceBg, Utils.getFitSize(ctx, 240), Utils.getFitSize(ctx, 308));
        mDeviceNumTxtView = new TextView(ctx);
        Utils.applyFace(mDeviceNumTxtView);
        mDeviceNumTxtView.setGravity(Gravity.CENTER_VERTICAL);
        mDeviceNumTxtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(ctx, 28));
        mDeviceNumTxtView.setPadding(Utils.getFitSize(ctx, 32), 0, 0, 0);
        mDeviceNumTxtView.setText(getString(R.string.deviceFomart, mDeviceCount));
        deviceView.addView(mDeviceNumTxtView, new FrameLayout.LayoutParams(-1, Utils.getFitSize(ctx, 108), Gravity.BOTTOM));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.leftMargin = Utils.getFitSize(ctx, 30);
        center.addView(deviceView, lp);
        layout.addView(center, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        setContentView(layout);
    }

    private void updateUI() {
        if (mDeviceNumTxtView != null) {
            mDeviceNumTxtView.setText(getString(R.string.deviceFomart, mDeviceCount));
        }
        if (mVideoNumTxtView != null) {
            mVideoNumTxtView.setText(getString(R.string.videoFomart, mVideoCount));
        }
    }

    @Override
    public void QueryNotify(Uri uri, Cursor cursor) {
        if (uri.equals(MediaStore.MediaDevice.CONTENT_URI)) {
            mDeviceCount = cursor.getCount();
        }
        if (uri.equals(MediaStore.MediaBase.CONTENT_URI)) {
            mVideoCount = cursor.getCount();
        }
        updateUI();
    }
}
