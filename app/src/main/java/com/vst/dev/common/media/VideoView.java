package com.vst.dev.common.media;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import com.vst.LocalPlayer.R;
import com.vst.dev.common.util.Utils;
import org.videolan.libvlc.*;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VideoView extends SurfaceView implements IPlayer, IVideoPlayer {

    private static final String TAG = "VideoView";
    private Uri mUri;
    private Map<String, String> mHeader;
    private int mCurrentSize = IPlayer.SURFACE_BEST_FIT;
    private static final int VLC_SURFACE_SIZE = 1;
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PREPARING = 1;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private int mCurrentState = STATE_IDLE;
    private int mTargetState = STATE_IDLE;
    private SurfaceHolder mSurfaceHolder = null;
    private MediaPlayer mMediaPlayer = null;
    private LibVLC mLibVLC = null;
    private VLCEventHandler<IPlayer> mVlcHandler = null;
    private int mDecodeType = HARD_DECODE;
    private int mVideoWidth = 0;
    private int mVideoHeight = 0;
    private int mSurfaceWidth = 0;
    private int mSurfaceHeight = 0;
    private IPlayer.OnInfoListener mOnInfoListener = null;
    private IPlayer.OnCompletionListener mOnCompletionListener = null;
    private IPlayer.OnPreparedListener mOnPreparedListener = null;
    private IPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = null;
    private IPlayer.OnErrorListener mOnErrorListener = null;
    private int mCurrentBufferPercentage = 0;
    private int mSeekWhenPrepared;
    private Context mContext;
    private VideoView mVideoView;
    private PopupWindow mBufferPop = null;
    private IPlayer.OnTimedTextChangedListener mOnTimedTextChangedListener = null;
    private SubTripe mSubTripe;

    public VideoView(Context context) {
        super(context);
        mContext = context.getApplicationContext();
        mVideoView = this;
        initVideoView();
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context.getApplicationContext();
        initVideoView();
    }

    @SuppressWarnings("deprecation")
    private void initVideoView() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        getHolder().addCallback(mSHCallback);
        getHolder().setFormat(PixelFormat.RGBX_8888);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mVlcHandler = new VLCEventHandler<IPlayer>(this);
        // requestFocus();
    }

    @Override
    public void setVideoPath(Uri uri, Map<String, String> header) {
        if (uri == null) {
            return;
        }
        mUri = uri;
        mHeader = header;
        mSeekWhenPrepared = 0;
        // 解决部分盒子硬解无法播放 modify by 张杰 2014-10-15
        setVisibility(View.INVISIBLE);
        setVisibility(View.VISIBLE);
        requestLayout();
        invalidate();
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            return;
        }
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mContext.sendBroadcast(i);
        releaseOldMPInstance();
        switch (mDecodeType) {
            case SOFT_DECODE:
                openVideoByVLCPlayer();
                break;
            case HARD_DECODE:
            default:
                openVideoByMediaPlayer();
                break;
        }
    }

    private static File findBDMVMediaFile(File bdmv) {
        File streamDir = new File(bdmv, "BDMV/STREAM");
        File[] fs = streamDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".m2ts");
            }
        });
        if (fs != null && fs.length > 0) {
            File target = null;
            long size = 0;
            for (int i = 0; i < fs.length; i++) {
                File f = fs[i];
                if (target == null) {
                    target = f;
                    size = f.length();
                } else {
                    long ss = f.length();
                    if (ss > size) {
                        size = ss;
                        target = f;
                    }
                }
            }
            return target;
        }
        return null;
    }

    private void openVideoByMediaPlayer() {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(mPreparedListener4Android);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            Uri uri = mUri;
            if (uri.getScheme().equals("bluray")) {
                File t = findBDMVMediaFile(new File(uri.getPath()));
                if (t != null) {
                    uri = Uri.fromFile(t);
                }
            }
            mMediaPlayer.setDataSource(mContext, uri, mHeader);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();
            mCurrentState = STATE_PREPARING;
            mHandler.sendEmptyMessageDelayed(TIME_OUT, mTimeOut);
        } catch (Throwable ex) {
            ex.printStackTrace();
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }

    private void openVideoByVLCPlayer() {
        try {
            if (mLibVLC == null) {
                try {
                    mLibVLC = LibVLC.getInstance();
                    mLibVLC.setOnPreparedListener(mPreparedListener4VLC);
                    mLibVLC.init(mContext);
                    mLibVLC.setFontColor(LibVLC.VlcSpuColorEnum.Blue);
                } catch (LibVlcException e) {
                    e.printStackTrace();
                }
            }
            mCurrentBufferPercentage = 0;
            mLibVLC.setOnPreparedListener(mPreparedListener4VLC);
            mLibVLC.prepareAsync();
            mCurrentState = STATE_PREPARING;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @TargetApi(16)
    public AudioTrack[] getAudioTracks() {
        if (mDecodeType == SOFT_DECODE) {
            if (mLibVLC != null) {
                TrackInfo[] trackInfos = mLibVLC.readTracksInfoInternal();
                Log.e("getAudioTracks", "trackInfos=" + trackInfos);
                if (trackInfos != null && trackInfos.length > 0) {
                    ArrayList<AudioTrack> list = new ArrayList<AudioTrack>();
                    for (int i = 0; i < trackInfos.length; i++) {
                        TrackInfo info = trackInfos[i];
                        Log.e("getAudioTracks", "track=" + info.Type + "," + info.Id + "," + info.Language);
                        if (info.Type == TrackInfo.TYPE_AUDIO) {
                            //if (!TextUtils.isEmpty(info.Language)) {
                            AudioTrack subTrack = new AudioTrack();
                            subTrack.trackId = info.Id;
                            subTrack.language = info.Id + "," + info.Language;
                            list.add(subTrack);
                            //}
                        }
                    }
                    if (list.size() > 0) {
                        return list.toArray(new AudioTrack[0]);
                    }
                }
            }
        } else {
            if (mMediaPlayer != null) {
                //版本号高于16
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    MediaPlayer.TrackInfo[] trackInfos = mMediaPlayer.getTrackInfo();
                    if (trackInfos != null && trackInfos.length > 0) {
                        ArrayList<AudioTrack> list = new ArrayList<AudioTrack>();
                        for (int i = 0; i < trackInfos.length; i++) {
                            MediaPlayer.TrackInfo info = trackInfos[i];
                            Log.e(TAG, "TrackInfo: " + info.getTrackType() + ", "
                                    + info.getLanguage() + "," + info.describeContents());
                            if (info.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                                AudioTrack subTrack = new AudioTrack();
                                subTrack.trackId = i;
                                subTrack.language = i + "," + info.getLanguage();
                                list.add(subTrack);
                            }
                        }
                        if (list.size() > 0) {
                            return list.toArray(new AudioTrack[0]);
                        }
                    }
                }
            }
        }
        return null;
    }

    @TargetApi(16)
    public void setAudioTrack(AudioTrack audioTrack) {
        if (mDecodeType == SOFT_DECODE) {
            if (mLibVLC != null) {
                mLibVLC.setAudioTrack(audioTrack.trackId);
            }
        } else {
            if (mMediaPlayer != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mMediaPlayer.selectTrack(audioTrack.trackId);
                }
            }
        }
    }

    public int getAudioTrackId() {
        if (mDecodeType == SOFT_DECODE) {
            if (mLibVLC != null) {
                Log.e("getAudioTracks", "getAudioTrackId=" + mLibVLC.getAudioTrack());
                return mLibVLC.getAudioTrack();
            }
        } else {
            if (mMediaPlayer != null) {
                //mMediaPlayer.getAudioSessionId();
            }
        }
        return -1;
    }

    @Override
    @TargetApi(16)
    public SubTrack[] getInternalSubTitle() {
        if (mDecodeType == HARD_DECODE) {
            if (mMediaPlayer != null) {
                //版本号高于16
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    MediaPlayer.TrackInfo[] trackInfos = mMediaPlayer.getTrackInfo();
                    if (trackInfos != null && trackInfos.length > 0) {
                        for (int i = 0; i < trackInfos.length; i++) {
                            MediaPlayer.TrackInfo info = trackInfos[i];
                            Log.e(TAG, "TrackInfo: " + info.getTrackType() + ", "
                                    + info.getLanguage() + "," + info.describeContents());
                            if (info.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                            }
                            if (info.getTrackType() == MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT) {
                            }
                        }
                    }
                }
            }
        } else {
            if (mLibVLC != null) {
                TrackInfo[] trackInfos = mLibVLC.readTracksInfoInternal();
                if (trackInfos != null && trackInfos.length > 0) {
                    ArrayList<SubTrack> list = new ArrayList<SubTrack>();
                    for (int i = 0; i < trackInfos.length; i++) {
                        TrackInfo info = trackInfos[i];
                        if (info.Type == TrackInfo.TYPE_TEXT) {
                            if (!TextUtils.isEmpty(info.Language)) {
                                SubTrack subTrack = new SubTrack();
                                subTrack.from = SubTrack.SubTrackType.Internal;
                                subTrack.trackId = info.Id;
                                subTrack.language = info.Language;
                                subTrack.name = "internal " + info.Language;
                                list.add(subTrack);
                            }
                        }
                    }
                    if (list.size() > 0) {
                        return list.toArray(new SubTrack[0]);
                    }
                }
            }
        }
        return null;
    }

    boolean openSPIF = false;

    public void setSPIF(boolean open) {
        if (mDecodeType == SOFT_DECODE) {
            if (mLibVLC != null) {
                mLibVLC.setSPDIF(open);
                openSPIF = open;
            }
        } else {
            openSPIF = false;
        }
    }

    public boolean isSPIF() {
        return openSPIF;
    }

    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(VideoView.this);
            }
            mCurrentState = STATE_PLAYBACK_COMPLETED;
            mTargetState = STATE_PLAYBACK_COMPLETED;
        }
    };

    private MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            }
            changeSurfaceSize(mVideoWidth, mVideoHeight, mVideoWidth, mVideoHeight, 1, 1);
        }
    };

    private MediaPlayer.OnPreparedListener mPreparedListener4Android = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            try {
                mCurrentState = STATE_PREPARED;
                mVideoWidth = mp.getVideoWidth();
                mVideoHeight = mp.getVideoHeight();
                mHandler.removeMessages(TIME_OUT);
                if (mSeekWhenPrepared > 0) {
                    seekTo(mSeekWhenPrepared);
                }
                _onPrepared(VideoView.this);
                if (mVideoWidth * mVideoHeight != 0) {
                    getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                    if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                        if (mTargetState == STATE_PLAYING) {
                            start();
                        }
                    }
                } else if (mTargetState == STATE_PLAYING) {
                    start();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }
    };

    private LibVLC.OnPreparedListener mPreparedListener4VLC = new LibVLC.OnPreparedListener() {
        public void onPrepared(LibVLC mp) {
            Log.e(TAG, "LibVLC.OnPreparedListener onPrepared............");
            try {
                if (mLibVLC != null) {
                    mLibVLC.attachSurface(mSurfaceHolder.getSurface(), mVideoView);
                    mSurfaceHolder.setKeepScreenOn(true);
                    EventHandler.getInstance().addHandler(mVlcHandler);
                    setKeepScreenOn(true);
                    mLibVLC.setMediaList();
                    mLibVLC.getMediaList().add(new Media(mLibVLC, mUri.toString()), null);
                    mLibVLC.getMediaList().addParams(mHeader);
                    mLibVLC.playIndex(mLibVLC.getMediaList().size() - 1);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent parent = getParent();
        if (parent != null) {
            ((View) parent).addOnLayoutChangeListener(mLayoutChangeListener);
        }
    }

    private OnLayoutChangeListener mLayoutChangeListener = new OnLayoutChangeListener() {
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                   int oldRight, int oldBottom) {
            if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
                changeScale(mCurrentSize);
            }
        }
    };

    private void _onPrepared(IPlayer player) {
        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared(VideoView.this);
        }
    }

    private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
        public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
            Log.d(TAG, "Error: " + framework_err + "," + impl_err);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mHandler.removeMessages(TIME_OUT);
            if (mOnErrorListener != null) {
                if (mOnErrorListener.onError(VideoView.this, framework_err, impl_err)) {
                    return true;
                }
            }
            return false;
        }
    };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new MediaPlayer.OnBufferingUpdateListener() {
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            mCurrentBufferPercentage = percent;
            if (mOnBufferingUpdateListener != null) {
                mOnBufferingUpdateListener.onBufferingUpdate(VideoView.this, percent);
            }
        }
    };

    @Override
    public void setOnTimedTextChangedListener(OnTimedTextChangedListener listener) {
        mOnTimedTextChangedListener = listener;
    }


    @Override
    public void setOnPreparedListener(IPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    @Override
    public void setOnCompletionListener(IPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    @Override
    public void setOnErrorListener(IPlayer.OnErrorListener l) {
        mOnErrorListener = l;
    }

    @Override
    public void setOnInfoListener(IPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            Log.i(TAG, "**surfaceChanged**");
            if (mDecodeType == HARD_DECODE) {
                mSurfaceWidth = w;
                mSurfaceHeight = h;
                boolean isValidState = (mTargetState == STATE_PLAYING);
                boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
                if (mMediaPlayer != null && isValidState && hasValidSize) {
                    mMediaPlayer.setDisplay(holder);
                    start();
                    if (mSeekWhenPrepared != 0) {
                        seekTo(mSeekWhenPrepared);
                    }
                }
            } else {
                if (mLibVLC != null) {
                    Log.i(TAG, "mLibVLC.attachSurface................");
                    if (mLibVLC.isLoadFinish())
                        mLibVLC.attachSurface(holder.getSurface(), VideoView.this);
                }
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "surfaceCreated");
            mSurfaceHolder = holder;
            openVideo();
            changeScale(mCurrentSize);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "surfaceDestroyed");
            if (!Utils.isAllwinnerPackage(mContext)) {
                mSurfaceHolder = null;
                mCurrentState = STATE_IDLE;
                mTargetState = STATE_IDLE;
                if (mMediaPlayer != null) {
                    mMediaPlayer.reset();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                if (mLibVLC != null) {
                    if (mCurrentState != STATE_PREPARING) {
                        mLibVLC.setMediaList();
                        if (mLibVLC.isLoadFinish()) {
                            mLibVLC.stop();
                            mLibVLC.detachSurface();
                        }
                    }
                    mLibVLC = null;
                }
            }
            if (mBufferPop != null && mBufferPop.isShowing()) {
                mBufferPop.dismiss();
            }
        }
    };

    public long getDuration() {
        long mDuration = -1;
        if (isInPlaybackState()) {
            if (mDecodeType == SOFT_DECODE) {
                if (mLibVLC.isLoadFinish()) {
                    mDuration = mLibVLC.getLength();
                }
            } else {
                mDuration = mMediaPlayer.getDuration();
            }
            if (mDuration > 0) {
                return mDuration;
            }
        }
        mDuration = -1;
        return mDuration;

    }

    public long getPosition() {
        if (isInPlaybackState()) {
            if (mDecodeType == SOFT_DECODE) {
                return mLibVLC.getTime();
            } else {
                return mMediaPlayer.getCurrentPosition();
            }
        }
        return -1;
    }

    @Override
    public void seekTo(int ms) {
        if (isInPlaybackState() && ms >= 0) {
            if (mDecodeType == SOFT_DECODE && mLibVLC != null) {
                mLibVLC.setTime(ms);
            } else {
                if (mMediaPlayer != null) {
                    mMediaPlayer.seekTo(ms);
                }
            }
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = ms;
        }
    }

    public boolean isPlaying() {
        if (mDecodeType == SOFT_DECODE) {
            return isInPlaybackState() && mLibVLC.isPlaying();
        } else {
            return isInPlaybackState() && mMediaPlayer.isPlaying();
        }
    }

    public int getBufferPercentage() {
        if (isInPlaybackState()) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    public boolean isInPlaybackState() {
        boolean inPlayState = mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE
                && mCurrentState != STATE_PREPARING;
        switch (mDecodeType) {
            case SOFT_DECODE:
                return mLibVLC != null && inPlayState;
            case HARD_DECODE:
            default:
                return mMediaPlayer != null && inPlayState;
        }
    }

    MediaPlayer.OnInfoListener mInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            return _onInfo(VideoView.this, what, extra, null);
        }
    };

    private boolean _onInfo(IPlayer player, int what, int extra, Bundle b) {
        if (mOnInfoListener != null) {
            if (mOnInfoListener.onInfo(VideoView.this, what, extra, b)) {
                return true;
            }
        }
        try {
            if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                Log.d(TAG, "_onInfo >> MEDIA_INFO_BUFFERING_START");
                if (mBufferPop == null) {
                    mBufferPop = new PopupWindow(mContext);
                    mBufferPop.setFocusable(false);
                    mBufferPop.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    Drawable source = Utils.getLocalDrawable(mContext, R.drawable.media_buffering);
                    mBufferPop.setWidth(source.getIntrinsicWidth());
                    mBufferPop.setHeight(source.getIntrinsicHeight());
                    ProgressBar bar = new ProgressBar(mContext);
                    bar.setIndeterminate(false);
                    bar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.buffering_rotate));
                    mBufferPop.setContentView(bar);
                }
                if (mBufferPop != null && !mBufferPop.isShowing()) {
                    mBufferPop.showAtLocation(this, Gravity.CENTER, 0, 0);
                }
            } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                Log.d(TAG, "_onInfo >> MEDIA_INFO_BUFFERING_END");
                if (mBufferPop != null && mBufferPop.isShowing()) {
                    mBufferPop.dismiss();
                }
            } else if (what == IPlayer.VLC_INFO_POSITION_CHANGED) {
                if (mBufferPop != null && mBufferPop.isShowing()) {
                    mBufferPop.dismiss();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void setDecodeType(int decodeType) {
        if (mDecodeType != decodeType) {
            openSPIF = false;
            if (mUri != null) {
                mSeekWhenPrepared = (int) getPosition();
            }
            mDecodeType = decodeType;
            if (mUri != null) {
                setVisibility(View.INVISIBLE);
                setVisibility(View.VISIBLE);
                requestLayout();
                invalidate();
            }
        }
    }

    public int getDecodeType() {
        return mDecodeType;
    }

    public void start() {
        mTargetState = STATE_PLAYING;
        if (isInPlaybackState()) {
            if (mDecodeType == HARD_DECODE && mMediaPlayer != null) {
                mMediaPlayer.start();
                changeScale(mCurrentSize);
            } else {
                if (mCurrentState == STATE_PAUSED && mLibVLC != null) {
                    mLibVLC.play();
                }
            }
            mCurrentState = STATE_PLAYING;
        }
    }

    public void pause() {
        if (isInPlaybackState()) {
            if (mDecodeType == SOFT_DECODE) {
                if (mLibVLC.isPlaying()) {
                    mLibVLC.pause();
                    mCurrentState = STATE_PAUSED;
                }
            } else {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mCurrentState = STATE_PAUSED;
                }
            }
        }
        mTargetState = STATE_PAUSED;
    }

    private void releaseOldMPInstance() {
        try {
            mHandler.removeMessages(TIME_OUT);
            mCurrentState = STATE_IDLE;
            // 回收 解码实例
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            if (mLibVLC != null) {
                if (mCurrentState != STATE_PREPARING)
                    if (mLibVLC.isLoadFinish()) {
                        mLibVLC.stopIndex();
                    }
                mLibVLC.getMediaList().clear();
                if (mVlcHandler != null) {
                    EventHandler.getInstance().removeHandler(mVlcHandler);
                }
            }
            if (mBufferPop != null && mBufferPop.isShowing()) {
                mBufferPop.dismiss();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopPlayback() {
        try {
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            mHandler.removeMessages(TIME_OUT);
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
            if (mLibVLC != null) {
                if (mCurrentState != STATE_PREPARING) {
                    if (mLibVLC.isLoadFinish()) {
                        mLibVLC.stop();
                        mLibVLC.detachSurface();
                        mLibVLC.destroy();
                    }
                }
                EventHandler.getInstance().removeHandler(mVlcHandler);
                mLibVLC = null;
            }
            recycel();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void recycel() {
        mBufferingUpdateListener = null;
        mCompletionListener = null;
        mBufferPop = null;
        mVlcHandler = null;
        mUri = null;
        mTimeOutCount = null;
        mSurfaceHolder = null;
        mErrorListener = null;
        mHandler = null;
        mInfoListener = null;
        ViewParent parent = getParent();
        if (parent != null) {
            ((View) parent).removeOnLayoutChangeListener(mLayoutChangeListener);
        }
        mLayoutChangeListener = null;
        mMediaPlayer = null;
        mOnPreparedListener = null;
        mOnTimedTextChangedListener = null;
        mSizeChangedListener = null;
    }

    public void resumeVideo() {
        mHandler.removeMessages(TIME_OUT);
        if (mUri != null) {
            if (mSeekWhenPrepared <= 0) {
                mSeekWhenPrepared = (int) getPosition();
            }
            openVideo();
        }
    }

    public void stopVideo() {
        mSeekWhenPrepared = (int) getPosition();
        mCurrentState = STATE_IDLE;
        releaseOldMPInstance();
    }

    public void resetVideo() {
        mSeekWhenPrepared = 0;
        mCurrentState = STATE_IDLE;
        mUri = null;
        if (mSubTripe != null) {
            mSubTripe.release();
            mSubTripe = null;
        }
        releaseOldMPInstance();
        setVisibility(View.INVISIBLE);
    }

    private class VLCEventHandler<T extends IPlayer> extends Handler {

        private WeakReference<T> mOwner = null;

        private T getOwner() {
            if (mOwner == null) {
                return null;
            }
            return mOwner.get();
        }

        public VLCEventHandler(T owner) {
            mOwner = new WeakReference<T>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                IPlayer player = getOwner();
                if (player == null)
                    return;
                Bundle b = msg.getData();
                int event = b.getInt("event");
                switch (event) {
                    case EventHandler.MediaPlayerPlaying:
                        Log.w(TAG, "VideoPlayerEventHandler MediaPlayerPlaying:" + event);
                        mCurrentState = STATE_PREPARED;
                        _onPrepared(VideoView.this);
                        if (mSeekWhenPrepared > 0) {
                            seekTo(mSeekWhenPrepared);
                        }
                        mCurrentState = STATE_PLAYING;
                        mTargetState = STATE_PLAYING;
                        break;
                    case EventHandler.MediaPlayerPaused:
                        Log.w(TAG, "VideoPlayerEventHandler MediaPlayerPaused:" + event);
                        break;
                    case EventHandler.MediaPlayerStopped:// 地址打开失败会返回此
                        Log.w(TAG, "VideoPlayerEventHandler MediaPlayerStopped:" + event);
                        break;
                    case EventHandler.MediaPlayerEndReached:
                        Log.w(TAG, "VideoPlayerEventHandler MediaPlayerPlaying:" + event);
                        mCurrentState = STATE_PLAYBACK_COMPLETED;
                        mTargetState = STATE_PLAYBACK_COMPLETED;
                        if (mOnCompletionListener != null) {
                            mOnCompletionListener.onCompletion(player);
                        }
                        break;
                    case EventHandler.MediaPlayerVout:
                        Log.w(TAG, "VideoPlayerEventHandler MediaPlayerVout:" + event);
                        break;
                    case EventHandler.MediaPlayerPositionChanged:// 作用于进度条
                        Log.w(TAG, "VideoPlayerEventHandler MediaPlayerPositionChanged:" + event);
                        // don't spam the logs
                        _onInfo(player, VLC_INFO_POSITION_CHANGED, 0, null);
                        break;
                    case EventHandler.MediaPlayerEncounteredError:
                        Log.w(TAG, "VideoPlayerEventHandler MediaPlayerEncounteredError:" + event);
                        mCurrentState = STATE_ERROR;
                        mTargetState = STATE_ERROR;
                        if (mOnErrorListener != null) {
                            mOnErrorListener.onError(player, VLC_ERROR, 0);
                        }
                        break;
                    case EventHandler.MediaPlayerBuffering:
                        Log.w(TAG, "VideoPlayerEventHandler MediaPlayerBuffering:" + event);
                        float buf = b.getFloat("data");
                        if (mOnInfoListener != null) {
                            if (buf < 100.0f) {
                                _onInfo(player, MediaPlayer.MEDIA_INFO_BUFFERING_START, (int) buf, null);
                            } else {
                                _onInfo(player, MediaPlayer.MEDIA_INFO_BUFFERING_END, (int) buf, null);
                            }
                        }
                        break;
                    default:
                        Log.w(TAG, "VideoPlayerEventHandler other:" + event);
                        break;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num,
                               int sar_den) {
        Log.w(TAG, "setSurfaceSize " + width + "," + height + "," + visible_width + "," + visible_height
                + "," + sar_num + "," + sar_den);
        if (width * height == 0 || visible_width * visible_height == 0) {
            return;
        }
        mVideoHeight = height;
        mVideoWidth = width;
        // 具体的显示通过 消息通知的形式 另外处理 避免卡死
        Message msg = mHandler.obtainMessage(VLC_SURFACE_SIZE);
        Bundle data = new Bundle();
        data.putIntArray("params",
                new int[]{width, height, visible_width, visible_height, sar_num, sar_den});
        msg.setData(data);
        mHandler.sendMessage(msg);
    }

    private static final int TIME_OUT = 5000;
    private long mTimeOut = TIME_OUT;
    private HashMap<Uri, Integer> mTimeOutCount = new HashMap<Uri, Integer>();

    @Override
    public void setTimeOut(long timeout) {
        if (timeout < TIME_OUT) {
            mTimeOut = TIME_OUT;
        } else {
            mTimeOut = timeout;
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == VLC_SURFACE_SIZE) {
                Bundle data = msg.getData();
                int[] size = data.getIntArray("params");
                int width = size[0];
                int height = size[1];
                int visible_width = size[2];
                int visible_height = size[3];
                int sar_num = size[4];
                int sar_den = size[5];
                changeSurfaceSize(width, height, visible_width, visible_height, sar_num, sar_den);
            } else if (msg.what == TIME_OUT) {
                Bundle b = new Bundle();
                b.putString("uri", mUri.toString());
                b.putInt("seek", mSeekWhenPrepared);
                int count;
                if (mTimeOutCount.containsKey(mUri)) {
                    count = mTimeOutCount.get(mUri) + 1;
                } else {
                    mTimeOutCount.clear();
                    count = 1;
                }
                mTimeOutCount.put(mUri, count);
                b.putInt("count", count);
                _onInfo(VideoView.this, MEDIA_INFO_TIMEOUT, 0, b);
            }
        }
    };


    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    @Override
    public void changeScale(int scale) {
        mCurrentSize = scale;
        if (mSurfaceHolder != null) {
            Message msg = mHandler.obtainMessage(VLC_SURFACE_SIZE);
            Bundle data = new Bundle();
            data.putIntArray("params",
                    new int[]{mVideoWidth, mVideoHeight, mVideoWidth, mVideoHeight, 1, 1});
            msg.setData(data);
            mHandler.sendMessage(msg);
        }
    }

    // 具体实现解码显示的缩放---------------------------------------------------------------------------------------------
    protected void changeSurfaceSize(int width, int height, int visible_width, int visible_height,
                                     int sar_num, int sar_den) {
        if (mSurfaceHolder == null) {
            Log.e(TAG, "Invalid surface size");
            return;
        }
        ViewGroup parent = (ViewGroup) getParent();
        if (parent == null) {
            return;
        }
        int dw = parent.getWidth();
        int dh = parent.getHeight();
        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (dw > dh && isPortrait || dw < dh && !isPortrait) {
            int d = dw;
            dw = dh;
            dh = d;
        }
        // sanity check
        if (dw * dh == 0 || width * height == 0) {
            Log.e(TAG, "Invalid surface size");
            return;
        }
        double ar, vw;
        double density = (double) sar_num / (double) sar_den;
        if (density == 1.0) {
            vw = visible_width;
            ar = (double) visible_width / (double) visible_height;
        } else {
            vw = visible_width * density;
            ar = vw / visible_height;
        }
        double dar = (double) dw / (double) dh;
        switch (mCurrentSize) {
            case SURFACE_BEST_FIT:
                if (dar < ar)
                    dh = (int) (dw / ar);
                else
                    dw = (int) (dh * ar);
                break;
            case SURFACE_FILL:
                break;
            case SURFACE_16_9:
                ar = 16.0 / 9.0;
                if (dar < ar)
                    dh = (int) (dw / ar);
                else
                    dw = (int) (dh * ar);
                break;
            case SURFACE_4_3:
                ar = 4.0 / 3.0;
                if (dar < ar)
                    dh = (int) (dw / ar);
                else
                    dw = (int) (dh * ar);
                break;
        }
        mSurfaceHolder.setFixedSize(width, height);
        LayoutParams lp = getLayoutParams();
        lp.width = dw * width / visible_width;
        lp.height = dh * height / visible_height;
        System.out.println("lp.width>>" + lp.width + " ,lp.height>>" + lp.height);
        if (lp instanceof FrameLayout.LayoutParams) {
            ((FrameLayout.LayoutParams) lp).gravity = Gravity.CENTER;
        }
        setLayoutParams(lp);
        // invalidate();
    }

    @Override
    public int getScaleSize() {
        return mCurrentSize;
    }

    private SubTrack mSubTrack = null;

    @Override
    public SubTrack getSubTrack() {
        return mSubTrack;
    }

    @Override
    public void setSubTrack(SubTrack subTrack, long offset) {
        System.out.println("videoview  setSubTrack=" + subTrack);
        mSubTrack = subTrack;
        if (subTrack != null) {
            switch (subTrack.from) {
                case Local:
                case Internet:
                    if (mDecodeType == SOFT_DECODE) {
                        if (mLibVLC != null) {
                            mLibVLC.setSpuTrack(-1);
                        }
                    }
                    if (subTrack.path != null) {
                        if (mSubTripe != null) {
                            mSubTripe.release();
                            mSubTripe = null;
                        }
                        mSubTripe = new SubTripe(this, subTrack.path);
                        mSubTripe.setTimeOffset(offset);
                    }
                    break;
                case Internal:
                    if (mDecodeType == SOFT_DECODE) {
                        if (mLibVLC != null) {
                            mLibVLC.setSpuTrack(subTrack.trackId);
                        }
                    }
                    break;
            }
        } else {
            System.out.println("videoview  setSubTrack=null");
            if (mDecodeType == SOFT_DECODE) {
                if (mLibVLC != null) {
                    mLibVLC.setSpuTrack(-1);
                }
            }
            if (mSubTripe != null) {
                mSubTripe.release();
                mSubTripe = null;
            }
        }
    }

    public void setSubtitleOffset(long offset) {
        if (mSubTripe != null) {
            mSubTripe.setTimeOffset(offset);
        }
    }

    void onTimedTextChanged(SubTripe.SubItem subtitle) {
        if (mOnTimedTextChangedListener != null) {
            if (subtitle != null) {
                mOnTimedTextChangedListener.onTimedTextChanger(subtitle.srtBody, subtitle.beginTime,
                        subtitle.endTime);
            } else {
                mOnTimedTextChangedListener.onTimedTextChanger(null, -1, -1);
            }
        }
    }
}
