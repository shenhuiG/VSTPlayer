package com.vst.LocalPlayer.component.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import com.vst.LocalPlayer.component.provider.MediaStore;
import com.vst.dev.common.util.Utils;

public class MainScreenActivity extends Activity implements MediaStoreNotifier.CallBack {

    private Context ctx = null;
    private TextView mVideoNumTxtView = null;
    private TextView mDeviceNumTxtView = null;
    private int mVideoCount = 0;
    private int mDeviceCount = 0;
    private MediaStoreNotifier notifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplication();
        setContentView(makeAttachUI());
        updateUI();
        notifier = new MediaStoreNotifier(ctx.getContentResolver(), this);
        notifier.registQueryContentUri(MediaStore.MediaBase.CONTENT_URI, null,
                MediaStore.MediaBase.FIELD_VALID + "=?", new String[]{"1"}, null);
        notifier.registQueryContentUri(MediaStore.MediaDevice.CONTENT_URI, null,
                MediaStore.MediaDevice.FIELD_VALID + "=?", new String[]{"1"}, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        notifier.release();
        notifier = null;
        ctx = null;
    }

    protected View makeAttachUI() {
        FrameLayout layout = new FrameLayout(ctx);
        layout.setBackgroundResource(R.drawable.main_bg);
        layout.setPadding(Utils.getFitSize(ctx, 60),
                Utils.getFitSize(ctx, 20),
                Utils.getFitSize(ctx, 60),
                Utils.getFitSize(ctx, 20));
        TextView titleTxt = new TextView(ctx);
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
        mDeviceNumTxtView.setGravity(Gravity.CENTER_VERTICAL);
        mDeviceNumTxtView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(ctx, 28));
        mDeviceNumTxtView.setPadding(Utils.getFitSize(ctx, 32), 0, 0, 0);
        deviceView.addView(mDeviceNumTxtView, new FrameLayout.LayoutParams(-1, Utils.getFitSize(ctx, 108), Gravity.BOTTOM));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.leftMargin = Utils.getFitSize(ctx, 30);
        center.addView(deviceView, lp);
        layout.addView(center, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        return layout;
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
