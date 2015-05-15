package com.vst.dev.common.media;

import java.util.Map;

import android.net.Uri;
import android.os.Bundle;

public interface IPlayer {

    public static final int SURFACE_BEST_FIT = 0;
    public static final int SURFACE_16_9 = 2;
    public static final int SURFACE_4_3 = 3;
    public static final int SURFACE_FILL = 1;

    public static final int NO_CYCLE = 0;
    public static final int SINGLE_CYCLE = 1;
    public static final int ALL_CYCLE = 2;
    public static final int RANDOM_CYCLE = 3;
    public static final int QUEUE_CYCLE = 4;

    public static final int DEFINITION_LD = 0;// 流畅
    public static final int DEFINITION_SD = 1;// 标清
    public static final int DEFINITION_HD = 2;// 高清
    public static final int DEFINITION_FULLHD = 3;// 超清
    public static final int DEFINITION_BLUE = 4;// 蓝光
    public static final int DEFINITION_1080P = 5;// 原画

    public static final int HARD_DECODE = 100;
    public static final int SOFT_DECODE = 101;
    public static final int INTELLIGENT_DECODE = 102;
    public static final int VLC_INIT_ERROR = 1000;
    public static final int VLC_ERROR = 1001;
    public static final int VLC_INFO_POSITION_CHANGED = 1004;
    public static final int MEDIA_INFO_TIMEOUT = 0xffff;


    public void start();

    public void pause();

    public int getVideoWidth();

    public int getVideoHeight();

    public void seekTo(int pos);

    public long getPosition();

    public long getDuration();

    public boolean isPlaying();

    public boolean isInPlaybackState();

    public boolean isSPIF();

    public void setSPIF(boolean open);

    public void changeScale(int scale);

    public int getScaleSize();

    public void setDecodeType(int decodeType);

    public int getDecodeType();

    public void setTimeOut(long timeout);

    public void setVideoPath(Uri uri, Map<String, String> headers);

    public void stopPlayback();

    public int getBufferPercentage();

    public void setOnErrorListener(OnErrorListener listener);

    public void setOnInfoListener(OnInfoListener listener);

    public void setOnCompletionListener(OnCompletionListener listener);

    public void setOnPreparedListener(OnPreparedListener listener);

    public void setSubtitleOffset(long offset);

    public void setSubTrack(SubTrack subTrack, long offset);

    public SubTrack getSubTrack();

    public void setAudioTrack(AudioTrack audioTrack);

    public SubTrack[] getInternalSubTitle();

    public AudioTrack[] getAudioTracks();

    public int getAudioTrackId();

    public void setOnTimedTextChangedListener(OnTimedTextChangedListener listener);

    // public void suspend();

    // public void resume();

    public void resetVideo();

    public interface OnErrorListener {
        boolean onError(IPlayer mp, int what, int extra);
    }

    public interface OnInfoListener {
        boolean onInfo(IPlayer mp, int what, int extra, Bundle extraData);
    }

    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(IPlayer mp, int percent);
    }

    public interface OnCompletionListener {
        void onCompletion(IPlayer mp);
    }

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(IPlayer mp, int width, int height);
    }

    public interface OnPreparedListener {
        void onPrepared(IPlayer mp);
    }

    public interface OnSeekCompleteListener {
        void onSeekComplete(IPlayer mp);
    }

    public interface OnTimedTextChangedListener {
        void onTimedTextChanger(String text, long stat, long end);
    }
}
