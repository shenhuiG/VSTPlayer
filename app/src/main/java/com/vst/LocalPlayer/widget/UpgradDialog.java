package com.vst.LocalPlayer.widget;

import java.io.File;

import android.util.TypedValue;
import android.widget.*;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import com.vst.LocalPlayer.R;
import com.vst.LocalPlayer.UpgradeUtils;
import com.vst.dev.common.util.MD5Util;
import com.vst.dev.common.util.Utils;

public class UpgradDialog extends Dialog {
    private Context mContext;
    private TextView mTitleView;
    private LinearLayout mContentView;
    private boolean backKeyEnable = true;
    private Bundle mBundler;
    private Button mUpgradeButton;

    public UpgradDialog(Context context) {
        super(context, R.style.upgradDialog);
        mContext = context;
        init();
    }

    @Override
    public void onBackPressed() {
        if (backKeyEnable) {
            super.onBackPressed();
        }
    }

    private void init() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundResource(R.drawable.update_bg_720);
        LinearLayout topLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams lp0 = new LinearLayout.LayoutParams(-2, -2);
        lp0.gravity = Gravity.CENTER_HORIZONTAL;
        lp0.topMargin = Utils.getFitSize(mContext, 120);
        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(R.drawable.logo_720);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        topLayout.addView(imageView, Utils.getFitSize(mContext, 263), Utils.getFitSize(mContext, 29));
        topLayout.setOrientation(LinearLayout.HORIZONTAL);
        topLayout.setGravity(Gravity.CENTER_VERTICAL);
        mTitleView = new TextView(mContext);
        Utils.applyFace(mTitleView);
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 30));
        mTitleView.setPadding(Utils.getFitSize(mContext, 30), 0, 0, 0);
        topLayout.addView(mTitleView, -2, -2);
        layout.addView(topLayout, lp0);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(Utils.getFitSize(mContext, 650), Utils.getFitSize(mContext, 300));
        lp2.leftMargin = Utils.getFitSize(mContext, 177);
        lp2.topMargin = Utils.getFitSize(mContext, 60);
        lp2.bottomMargin = Utils.getFitSize(mContext, 45);
        ScrollView scrollView = new ScrollView(mContext);
        mContentView = new LinearLayout(mContext);
        mContentView.setOrientation(LinearLayout.VERTICAL);
        //scrollView.setBackgroundColor(0xffffffff);
        scrollView.addView(mContentView, -1, -1);
        layout.addView(scrollView, lp2);
        LinearLayout oprationLayout = new LinearLayout(mContext);
        oprationLayout.setOrientation(LinearLayout.HORIZONTAL);
        mUpgradeButton = new Button(mContext);
        mUpgradeButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40);
        mUpgradeButton.setTextColor(Color.WHITE);
        mUpgradeButton.setBackgroundResource(R.drawable.update_button_bg);
        mUpgradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBundler != null) {
                    Uri akpUri = mBundler.getParcelable(UpgradeUtils.APK_FILE_URI);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(akpUri, "application/vnd.android.package-archive");
                    mContext.startActivity(intent);
                }
                dismiss();
            }
        });
        mUpgradeButton.setText(R.string.upgrad_now);
        Button mButton2 = new Button(mContext);
        mButton2.setTextColor(Color.WHITE);
        mButton2.setText(R.string.upgrad_later);
        mButton2.setTextSize(TypedValue.COMPLEX_UNIT_PX, 40);
        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mButton2.setBackgroundResource(R.drawable.update_button_bg);
        oprationLayout.addView(mUpgradeButton, Utils.getFitSize(mContext, 266), Utils.getFitSize(mContext, 93));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Utils.getFitSize(mContext, 266), Utils.getFitSize(mContext, 93));
        lp.leftMargin = Utils.getFitSize(mContext, 60);
        oprationLayout.addView(mButton2, lp);
        Utils.applyFace(mUpgradeButton);
        Utils.applyFace(mButton2);
        LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(-2, -2);
        lp3.leftMargin = Utils.getFitSize(mContext, 177);
        layout.addView(oprationLayout, lp3);
        setContentView(layout, new ViewGroup.LayoutParams(-1, -1));
    }

    public Bundle getBundler() {
        return mBundler;
    }

    public void setBundler(Bundle upgradBundler) {
        mBundler = upgradBundler;
        setupTitleView();
        setupContentView();
    }


    @Override
    public void show() {
        mHandler.removeCallbacks(dimissRun);
        mHandler.postDelayed(dimissRun, 30000);
        if (mUpgradeButton != null) {
            mUpgradeButton.requestFocus();
        }
        super.show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mHandler.removeCallbacks(dimissRun);
    }

    private void setupTitleView() {
        if (mTitleView != null && mBundler != null) {
            String version = mBundler.getString(UpgradeUtils.VERSION);
            mTitleView.setText(makeTitleSequence(version));
        } else {
            mTitleView.setText(null);
        }
    }

    private CharSequence makeTitleSequence(String vsrsion) {
        String ss = "打扰了，现在可更新为" + vsrsion + "版本";
        return ss;
    }


    private void setupContentView() {
        if (mContentView != null && mBundler != null) {
            mContentView.removeAllViews();
            TextView tv = new TextView(mContext);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 20));
            Utils.applyFace(tv);
            tv.setPadding(0, 0, 0, Utils.getFitSize(mContext, 10));
            tv.setText("更新内容:");
            mContentView.addView(tv);
            String in = mBundler.getString(UpgradeUtils.INSTRUCTION);
            TextView t = new TextView(mContext);
            t.setText(in);
            t.setLineSpacing(0f, 1.5f);
            t.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 20));
            Utils.applyFace(t);
            mContentView.addView(t);
        }
    }

    private Runnable dimissRun = new Runnable() {
        @Override
        public void run() {
            //dismiss();
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    break;
            }
        }
    };

}
