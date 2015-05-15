package net.myvst.v2.extra.media;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.vst.dev.common.media.IPlayer;
import com.vst.dev.common.media.VideoView;

import net.myvst.v2.extra.media.controller.MediaControllerManager;

public abstract class MediaControlFragment extends Fragment {

    protected IPlayer mPlayer;
    protected InitCallback mCallback;
    protected MediaControllerManager mControllerManager;

    public IPlayer getPlayer() {
        return mPlayer;
    }

    public void setPlayer(IPlayer player) {
        mPlayer = player;
    }

    public InitCallback getCallback() {
        return mCallback;
    }

    public void setCallback(InitCallback mCallback) {
        this.mCallback = mCallback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        if (mPlayer != null) {
            ((VideoView) mPlayer).resumeVideo();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mPlayer != null) {
            ((VideoView) mPlayer).stopVideo();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public abstract void changArguments(Bundle args);

    public void attachMediaControllerManager(MediaControllerManager manager) {
        mControllerManager = manager;
    }

    ;

    public abstract boolean onHandlerKeyEvent(KeyEvent event);

    public abstract boolean onHandlerTouchEvent(MotionEvent event);

    public interface InitCallback {
        boolean initMediaLibray();
    }
}
