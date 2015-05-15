package com.vst.LocalPlayer.component.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.vst.dev.common.media.VideoView;

import net.myvst.v2.extra.media.MediaControlFragment;
import net.myvst.v2.extra.media.controller.MediaControllerManager;

public class PlayerActivity extends FragmentActivity {
    private static final String NORMAL_TAG = "normal";
    private VideoView player;
    private FragmentManager mFM;
    private MediaControlFragment mMediaControl;
    private boolean mIsWillExit = false;
    private Handler mHanlder = new Handler();
    private MediaControllerManager mControllerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mControllerManager = new MediaControllerManager(this);
        mFM = getSupportFragmentManager();
        player = new VideoView(this);
        FrameLayout layout = new FrameLayout(this);
        layout.setId(android.R.id.widget_frame);
        layout.addView(player, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        setContentView(layout, new ViewGroup.LayoutParams(-1, -1));
        initIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initIntent(intent);
    }


    @Override
    public void onBackPressed() {
        mediaExit();
    }

    private void mediaExit() {
        if (mIsWillExit) {
            long time = System.currentTimeMillis();
            super.onBackPressed();
            System.out.println("onBackPressed " + (System.currentTimeMillis() - time));
        } else {
            Toast.makeText(this.getApplicationContext(), "再按一次退出视频播放", Toast.LENGTH_LONG).show();
            mIsWillExit = true;
            mHanlder.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsWillExit = false;
                }
            }, 2000);
        }
    }

    private void initIntent(Intent i) {
        Uri uri = i.getData();
        Bundle args = new Bundle();
        args.putParcelable("uri", uri);
        if (i.getExtras() != null) {
            args.putAll(i.getExtras());
        }
        addNormalControl(args);
    }

    private void addNormalControl(Bundle args) {
        mMediaControl = (PlayFragment) mFM.findFragmentByTag(NORMAL_TAG);
        if (mMediaControl == null) {
            player.resetVideo();
            mMediaControl = PlayFragment.newInstance(args);
            mMediaControl.setPlayer(player);
            mMediaControl.attachMediaControllerManager(mControllerManager);
            mFM.beginTransaction().replace(android.R.id.widget_frame, mMediaControl, NORMAL_TAG).commit();
        } else {
            mMediaControl.changArguments(args);
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean isNumKey = keyCode == KeyEvent.KEYCODE_0 || keyCode == KeyEvent.KEYCODE_1
                || keyCode == KeyEvent.KEYCODE_2 || keyCode == KeyEvent.KEYCODE_3
                || keyCode == KeyEvent.KEYCODE_4 || keyCode == KeyEvent.KEYCODE_5
                || keyCode == KeyEvent.KEYCODE_6 || keyCode == KeyEvent.KEYCODE_7
                || keyCode == KeyEvent.KEYCODE_8 || keyCode == KeyEvent.KEYCODE_9;
        boolean isMediaSupportedKey = keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER
                || keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN
                || keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || isNumKey;
        if (isMediaSupportedKey) {
            if (mMediaControl != null) {
                if (mMediaControl.onHandlerKeyEvent(event)) {
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mMediaControl != null) {
            if (mMediaControl.onHandlerTouchEvent(ev)) {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHanlder.removeCallbacksAndMessages(null);
        player.stopPlayback();
        mFM = null;
        mFM = null;
        mControllerManager = null;
    }

}
