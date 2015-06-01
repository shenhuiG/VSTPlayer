package com.vst.LocalPlayer.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vst.LocalPlayer.R;
import com.vst.LocalPlayer.model.MediaInfo;
import com.vst.dev.common.media.IPlayer;
import com.vst.dev.common.util.Utils;

import net.myvst.v2.extra.media.controller.IInverseControl;
import net.myvst.v2.extra.media.controller.MediaControllerManager;
import net.myvst.v2.extra.media.controller.SeekView;
import net.myvst.v2.extra.media.controller.SeekView.OnSeekChangedListener;
import net.myvst.v2.extra.media.controller.TextDrawable;

public class PlayerSeekController extends FrameLayout implements IInverseControl {

    public interface ControlCallback {

        public long getPosition();

        public long getDuration();

        public void seekTo(int pos);

        public void mediaPlay();

        public void mediaPause();

        public boolean isPlaying();

        public boolean isPause();

        public int[] getVideoSize();

        public int getCycleMode();
    }

    private MediaControllerManager mControllerManager;
    private TextView m720pView;
    private ImageView mCycelModView;
    private TextView mExtView;
    private TextView mTitleView;

    private SeekView mSeekView;
    private TextView mTxtDuration;
    private FrameLayout mStateView;
    private String mControlId;
    private MediaInfo mMediaInfo;
    public static final String SEEK_CONTROLLER = "seek";
    private ControlCallback mControl;
    private Context mContext;
    private boolean mDragging = false;
    private boolean mAttach = false;
    private static final int SET_PROCESS = 1;
    private static final int PROGRESS_INCREMENT = 20000;
    private TextDrawable mPositionDrawable = null;
    private View pauseView = null;
    private ImageView seekBView = null;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SET_PROCESS) {
                setProgress();
            }
        }
    };

    public PlayerSeekController(Context context) {
        super(context);
        mContext = context.getApplicationContext();
        initView();
    }

    public void setMediaMeta(MediaInfo mediaMeta) {
        mMediaInfo = mediaMeta;
        if (mTitleView != null) {
            if (mMediaInfo != null) {
                String text = mMediaInfo.title != null ? mMediaInfo.title : mMediaInfo.name;
                mTitleView.setText(text.replace("-", "- "));
            } else {
                mTitleView.setText(null);
            }
        }
        if (mExtView != null) {
            if (mMediaInfo != null) {
                String ext = mMediaInfo.name.substring(mMediaInfo.name.lastIndexOf(".") + 1);
                mExtView.setText(ext.toUpperCase());
            } else {
                mExtView.setText(null);
            }
        }
    }


    public void setControl(ControlCallback control) {
        mControl = control;
    }

    private void update720pView() {
        if (m720pView != null && mControl != null) {
            int[] size = mControl.getVideoSize();
            if (size[0] * size[1] == 0) {
                m720pView.setText(null);
            } else {
                if (size[0] < 1080 * 0.9) {
                    m720pView.setText("标清");
                }
                if (size[0] > 1080 * 0.9) {
                    m720pView.setText("720P");
                }
                if (size[0] > 1280 * 0.9) {
                    m720pView.setText("1080P");
                }
            }
        }
    }

    private void updateCycelModeView() {
        if (mCycelModView != null && mControl != null) {
            int mod = mControl.getCycleMode();
            switch (mod) {
                case IPlayer.ALL_CYCLE:
                    mCycelModView.setImageResource(R.drawable.ic_loop_all);
                    break;
                case IPlayer.SINGLE_CYCLE:
                    mCycelModView.setImageResource(R.drawable.ic_loop_single);
                    break;
                case IPlayer.NO_CYCLE:
                    mCycelModView.setImageResource(R.drawable.ic_loop_no);
                    break;
                case IPlayer.QUEUE_CYCLE:
                    mCycelModView.setImageResource(R.drawable.ic_loop_queue);
                    break;
                case IPlayer.RANDOM_CYCLE:
                    mCycelModView.setImageResource(R.drawable.ic_loop_random);
                    break;
            }
        }
    }

    private View makeMetaInfoView() {
        LinearLayout layout = new LinearLayout(mContext);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(Utils.getFitSize(mContext, 60), Utils.getFitSize(mContext, 25),
                Utils.getFitSize(mContext, 60), 0);
        mTitleView = new TextView(mContext);
        mTitleView.setSingleLine(true);
        mTitleView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mTitleView.setMarqueeRepeatLimit(Integer.MAX_VALUE);
        Utils.applyFace(mTitleView);
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 30));
        layout.addView(mTitleView, new LinearLayout.LayoutParams(0, -2, 1.0f));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, Utils.getFitSize(mContext, 36));
        lp.rightMargin = Utils.getFitSize(mContext, 6);
        FrameLayout fl = new FrameLayout(mContext);
        fl.setPadding(Utils.getFitSize(mContext, 16), 0, Utils.getFitSize(mContext, 16), 0);
        fl.setBackgroundResource(R.drawable.bg_format);
        mCycelModView = new ImageView(mContext);
        mCycelModView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        //mCycelModView.setBackgroundColor(0xff00ffff);
        mCycelModView.setImageResource(R.drawable.ic_loop_all);
        fl.addView(mCycelModView, new FrameLayout.LayoutParams(39, 31, Gravity.CENTER));
        layout.addView(fl, lp);
        m720pView = new TextView(mContext);
        m720pView.setGravity(Gravity.CENTER);
        Utils.applyFace(m720pView);
        m720pView.setPadding(Utils.getFitSize(mContext, 6), 0, Utils.getFitSize(mContext, 6), 0);
        m720pView.setBackgroundResource(R.drawable.bg_format);
        m720pView.setText("720p");
        layout.addView(m720pView, lp);
        mExtView = new TextView(mContext);
        mExtView.setPadding(Utils.getFitSize(mContext, 6), 0, Utils.getFitSize(mContext, 6), 0);
        mExtView.setBackgroundResource(R.drawable.bg_format);
        mExtView.setGravity(Gravity.CENTER);
        Utils.applyFace(mExtView);
        mExtView.setText("MKV");
        layout.addView(mExtView, lp);
        return layout;
    }

    @SuppressLint("NewApi")
    private void initView() {
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundResource(R.drawable.seek_controller_bg);
        View metaView = makeMetaInfoView();
        layout.addView(metaView, -1, -2);
        mSeekView = new SeekView(mContext);
        mSeekView.setProgressGravity(SeekView.PROGRESS_BOTTOM);
        mSeekView.setPadding(Utils.getFitSize(mContext, 20), 0, Utils.getFitSize(mContext, 20), 0);
        layout.addView(mSeekView, -1, -2);
        mTxtDuration = new TextView(mContext);
        Utils.applyFace(mTxtDuration);
        mTxtDuration.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 30));
        mTxtDuration.setGravity(Gravity.RIGHT);
        mTxtDuration.setPadding(0, Utils.getFitSize(mContext, 10), Utils.getFitSize(mContext, 60), 0);
        mTxtDuration.setTextColor(Color.WHITE);
        layout.addView(mTxtDuration, -1, -2);
        addView(layout, -1, -2);
        mStateView = new FrameLayout(mContext);
        addView(mStateView, new LayoutParams(-1, -1, Gravity.CENTER));
        initControllerView(this);
    }

    private void initControllerView(View v) {
        mSeekView.setKeyProgressIncrement(PROGRESS_INCREMENT);
        mSeekView.setOnSeekChangedListener(mOnSeekChangedListener);
        mPositionDrawable = new TextDrawable(getContext());
        mPositionDrawable.setText(Utils.stringForTime(0));
        mPositionDrawable.setBackDrawable(Utils.getLocalDrawable(getContext(), R.drawable.time_drawable_bg));
        mPositionDrawable.setTextSize(25);
        mSeekView.setThumb(mPositionDrawable);
    }

    private OnSeekChangedListener mOnSeekChangedListener = new SeekView.OnSeekChangedListener() {

        @Override
        public void onSeekChanged(SeekView bar, int progress, int startprogress, boolean increase) {

            if (mControl != null) {
                executeSeek(increase);
            }
            if (progress == startprogress) {
                mDragging = true;
            } else {
                if (mControl != null) {
                    if (progress >= mControl.getDuration()) {
                        progress = (int) (mControl.getDuration() - 1000);
                    }
                    mControl.seekTo(progress);
                    executePlay();
                }
                mDragging = false;
            }
        }

        @Override
        public void onProgressChanged(SeekView bar, int progress, boolean fromuser) {
            mPositionDrawable.setText(Utils.stringForTime(progress));
        }

        @Override
        public void onShowSeekBarView(boolean increase) {
            mHandler.removeCallbacksAndMessages(null);
            showSeekBarView(increase);
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttach = false;
        mHandler.removeMessages(SET_PROCESS);
        if (mControl != null) {
            mControl.mediaPlay();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttach = true;
        updateView();

    }

    private GestureDetector mGestureDetector = null;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    public void updateView() {
        if (mControl != null && mAttach) {
            if (mTxtDuration != null) {
                mTxtDuration.setText(Utils.stringForTime(mControl.getDuration()));
            }
            if (mSeekView != null) {
                setProgress();
            }
            updateCycelModeView();
            update720pView();
        }
    }

    private void setProgress() {
        if (mControl != null && !mDragging) {
            long position = mControl.getPosition();
            long duration = mControl.getDuration();
            Log.d(PlayerSeekController.class.getSimpleName(), "position=" + position + ",duration=" + duration);
            if (mSeekView != null) {
                mSeekView.setMax((int) duration);
                mSeekView.setProgress((int) position);
                mSeekView.setKeyProgressIncrement(PROGRESS_INCREMENT);
            }
        }
        mHandler.sendEmptyMessageDelayed(SET_PROCESS, 1000);
    }

    public void executePlay() {
        try {
            if (mStateView != null) {
                mStateView.removeAllViews();
            }
            mControllerManager.show(mControlId);
            if (mControl != null) {
                mControl.mediaPlay();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void executePause() {
        try {
            if (pauseView == null) {
                FrameLayout layout = new FrameLayout(mContext);
                TextView pauseIcon = new TextView(mContext);
                CharSequence c = Utils
                        .makeImageSpannable("*", getResources().getDrawable(R.drawable.ic_pause_1), 0,
                                Utils.getFitSize(mContext, 78), Utils.getFitSize(mContext, 78),
                                ImageSpan.ALIGN_BOTTOM);
                if (c != null) {
                    pauseIcon.setText(c);
                }
                pauseIcon.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        executePlay();
                    }
                });
                LayoutParams lp2 = new LayoutParams(-2, -2);
                lp2.gravity = Gravity.LEFT | Gravity.BOTTOM;
                lp2.leftMargin = Utils.getFitSize(mContext, 65);
                lp2.bottomMargin = Utils.getFitSize(mContext, 50);
                layout.addView(pauseIcon, lp2);
                pauseView = layout;
            }
            if (pauseView.getParent() == null) {
                mStateView.removeAllViews();
                mStateView.addView(pauseView, new LayoutParams(-1, -1, Gravity.CENTER));
            }
            if (mControl != null) {
                mControl.mediaPause();
            }
            mControllerManager.show(mControlId, -1);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void executeSeek(boolean increase) {
        showSeekBarView(increase);
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessageDelayed(SET_PROCESS, 2000);
    }

    private void showSeekBarView(boolean increase) {
        if (seekBView == null) {
            seekBView = new ImageView(mContext);
            seekBView.setLayoutParams(new LayoutParams(Utils.getFitSize(mContext, 144),
                    Utils.getFitSize(mContext, 145), Gravity.CENTER));
        }
        if (increase) {
            //seekBView.setImageResource(R.drawable.ic_seekforward);
        } else {
            //seekBView.setImageResource(R.drawable.ic_seekbackward);
        }
        if (seekBView.getParent() == null) {
            mStateView.removeAllViews();
            mStateView.addView(seekBView);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event == null) {
            return true;
        }
        try {
            boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN;
            int keyCode = event.getKeyCode();
            if (uniqueDown) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER
                        || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                    if (mControl != null) {
                        if (mControl.isPlaying()) {
                            executePause();
                        } else {
                            executePlay();
                        }
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
                    if (mControl != null) {
                        long p = (int) mControl.getPosition();
                        if (p > 15000) {
                            mControl.seekTo((int) p - 15000);
                        }
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
                    if (mControl != null) {
                        long p = mControl.getPosition();
                        long duration = mControl.getDuration();
                        if (p > 0 && p < duration - 15000) {
                            mControl.seekTo((int) p + 15000);
                        }
                    }
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    /*if (adView != null && pauseView != null && pauseView.getParent() == mStateView) {
                        if (adView.getVisibility() == View.VISIBLE) {
                            adView.setVisibility(View.INVISIBLE);
                        } else {
                            adView.setVisibility(View.VISIBLE);
                        }
                    }*/
                    return true;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    public void addControllerManager(MediaControllerManager controllerManager, String id) {
        mControllerManager = controllerManager;
        mControlId = id;
    }
}
