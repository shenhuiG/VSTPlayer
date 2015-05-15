package net.myvst.v2.extra.media.controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class SeekView extends View {

    public interface OnSeekChangedListener {

        void onSeekChanged(SeekView bar, int progress, int startProgress, boolean increase);

        void onProgressChanged(SeekView bar, int progress, boolean fromuser);

        void onShowSeekBarView(boolean increase);

    }

    private boolean isDraging = false;
    private Drawable mThumb = null;
    private int mKeyProgressIncrement = 1;
    private Drawable mBackProgressDrawable;
    private Drawable mProgressDrawable;
    private Drawable mChangedDrawable;
    private int mMax;
    private int mProgress = 50;
    private int mChangedStartProgress = -1;
    private int progressGravity = PROGRESS_CENTER;
    public static final int PROGRESS_TOP = 0;
    public static final int PROGRESS_CENTER = 1;
    public static final int PROGRESS_BOTTOM = 2;
    public static final int PROGRESS_MIN_SIZE = 6;
    public static final int PROGRESS_MAX_SIZE = 48;
    private int mProgressHeight = PROGRESS_MIN_SIZE;
    private OnSeekChangedListener mOnSeekChangedListener = null;
    private int mThumbOffset = 0;

    public void setOnSeekChangedListener(OnSeekChangedListener listener) {
        mOnSeekChangedListener = listener;
    }

    public SeekView(Context context) {
        super(context);
        init();
    }

    public SeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setProgressGravity(int progressGravity) {
        this.progressGravity = progressGravity;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Drawable d = mBackProgressDrawable;
        int dw = 0;
        int dh = 0;
        if (d != null) {
            updateDrawableState(d);
            int width = d.getIntrinsicWidth();
            int height = d.getIntrinsicHeight();
            dw = width;
            dh = Math.max(mProgressHeight, Math.min(PROGRESS_MAX_SIZE, height));
        }
        d = mThumb;
        if (d != null) {
            updateDrawableState(d);
            int width = d.getIntrinsicWidth();
            int height = d.getIntrinsicHeight();
            dw = Math.max(dw, width);
            dh = Math.max(height, dh);
        }

        dw += getPaddingLeft() + getPaddingRight();
        dh += getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSizeAndState(dw, widthMeasureSpec, 0),
                resolveSizeAndState(dh, heightMeasureSpec, 0));
    }

    private void updateDrawableState(Drawable drawable) {
        int[] state = getDrawableState();
        if (drawable != null && drawable.isStateful()) {
            drawable.setState(state);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawProgress(canvas);
        drawThumb(canvas);
    }

    private void drawThumb(Canvas canvas) {
        if (mThumb != null) {
            int width = Math.max(mProgressHeight, mThumb.getIntrinsicWidth());
            int height = Math.max(mProgressHeight, mThumb.getIntrinsicHeight());
            Rect bounds = getProgressBound();
            int right = getCoordinateX(bounds.left, bounds.right, mProgress);
            bounds.top = (getHeight() - height) / 2;
            bounds.bottom = bounds.top + height;
            bounds.left = right - width / 2;
            bounds.right = bounds.left + width;
            mThumb.setBounds(bounds);
            mThumb.draw(canvas);
        }
    }

    private Rect getProgressBound() {
        Rect bounds = new Rect();
        bounds.left = getPaddingLeft() + mThumbOffset;
        bounds.right = getWidth() - getPaddingRight() - mThumbOffset;
        switch (progressGravity) {
        case PROGRESS_BOTTOM:
            bounds.bottom = getHeight() - getPaddingBottom();
            bounds.top = bounds.bottom - mProgressHeight;
            break;
        case PROGRESS_TOP:
            bounds.top = getPaddingTop();
            bounds.bottom = bounds.top + mProgressHeight;
            break;
        case PROGRESS_CENTER:
            bounds.top = getPaddingTop()
                    + ((getHeight() - getPaddingTop() - getPaddingBottom() - mProgressHeight) / 2);
            bounds.bottom = bounds.top + mProgressHeight;
            break;
        }
        return bounds;
    }

    private void drawProgress(Canvas canvas) {
        Rect bounds = getProgressBound();
        if (mBackProgressDrawable != null) {
            drawProgressDrawable(canvas, bounds, 0, mMax, mBackProgressDrawable);
        }
        if (mProgressDrawable != null) {
            drawProgressDrawable(canvas, bounds, 0, mProgress, mProgressDrawable);
        }
        if (mChangedDrawable != null && mChangedStartProgress >= 0) {
            drawProgressDrawable(canvas, bounds, mChangedStartProgress, mProgress, mChangedDrawable);
        }
    }

    private void drawProgressDrawable(Canvas canvas, Rect bounds, int start, int end, Drawable d) {
        Rect drawableBounds = new Rect(bounds);
        final int left = drawableBounds.left;
        final int right = drawableBounds.right;
        int startX = getCoordinateX(left, right, start);
        int endX = getCoordinateX(left, right, end);
        drawableBounds.right = Math.max(startX, endX);
        drawableBounds.left = Math.min(startX, endX);
        d.setBounds(drawableBounds);
        d.draw(canvas);
    }

    private int getCoordinateX(int xStart, int xEnd, int progress) {
        float ratio = (float) progress / (float) mMax;
        if (ratio <= 0) {
            return xStart;
        } else if (ratio > 0 && ratio < 1) {
            int width = (int) (ratio * (xEnd - xStart));
            return xStart + width;
        } else {
            return xEnd;
        }
    }

    private void init() {
        setFocusable(true);
        mMax = 100;
        mBackProgressDrawable = new ColorDrawable(0xFF595858);
        mProgressDrawable = new ColorDrawable(0xFF006af8);
        mChangedDrawable = new ColorDrawable(0xFF06e500);
    }

    /**
     * 修改背景色
     * 
     * @param backDrawable
     * @param progressDrawable
     * @param changedDrawable
     */
    public void setDrawable(Drawable backDrawable, Drawable progressDrawable, Drawable changedDrawable) {
        mBackProgressDrawable = backDrawable;
        mProgressDrawable = progressDrawable;
        mChangedDrawable = changedDrawable;
    }

    /**
     * 修改进度条的显示高度
     * 
     * @param height
     */
    public void setProgressMinHeight(int height) {
        mProgressHeight = height;
    }

    public void setThumb(Drawable thumb) {
        if (mThumb != thumb) {
            mThumb = thumb;
            mThumbOffset = mThumb.getIntrinsicWidth() / 2;
            invalidate();
        }
    }

    public void setProgress(int progress) {
        setProgress(progress, false);
    }

    synchronized void setProgress(int progress, boolean fromUser) {
        if (progress < 0) {
            progress = 0;
        }
        if (progress > mMax) {
            progress = mMax;
        }
        if (progress != mProgress) {
            mProgress = progress;
            invalidate();
            if (mOnSeekChangedListener != null) {
                mOnSeekChangedListener.onProgressChanged(this, mProgress, fromUser);
            }
        }
    }

    public void setKeyProgressIncrement(int keyProgressIncrement) {
        if (keyProgressIncrement < 1) {
            keyProgressIncrement = 1;
        }
        if (keyProgressIncrement > Math.max(mMax, 15) / 15) {
            keyProgressIncrement = Math.max(mMax, 15) / 15;
        }
        if (mKeyProgressIncrement != keyProgressIncrement) {
            mKeyProgressIncrement = keyProgressIncrement;
        }
    }

    public void setProgressDrawable(Drawable bg, Drawable changed, Drawable progress) {
        if (bg != null) {
            mBackProgressDrawable = bg;
        }
        if (changed != null) {
            mChangedDrawable = changed;
        }
        if (progress != null) {
            mProgressDrawable = progress;
        }
    }

    private int mRatio = 1;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        final int progress = getProgress();
        int keyProgressIncrement = mKeyProgressIncrement;
        mRatio++;
        // Log.i("zip", "mRatio="+mRatio);
        if (mRatio / 5 > 0) {
            keyProgressIncrement = keyProgressIncrement * Math.min(12, mRatio / 5);
        }
        if (keyProgressIncrement < 1) {
            keyProgressIncrement = 1;
        }
        if (keyProgressIncrement > Math.max(mMax, 15) / 15) {
            keyProgressIncrement = Math.max(mMax, 15) / 15;
        }
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_LEFT:
            if (progress <= 0) {
                break;
            }
            if (!isDraging) {
                if (mOnSeekChangedListener != null) {
                    mOnSeekChangedListener.onShowSeekBarView(false);
                    mChangedStartProgress = progress;
                }
                isDraging = true;
            }
            setProgress(progress - keyProgressIncrement, true);
            return true;
        case KeyEvent.KEYCODE_DPAD_RIGHT:
            if (progress >= getMax()) {
                break;
            }
            if (!isDraging) {
                if (mOnSeekChangedListener != null) {
                    mOnSeekChangedListener.onShowSeekBarView(true);
                    mChangedStartProgress = progress;
                }
                isDraging = true;
            }
            setProgress(progress + keyProgressIncrement, true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (isDraging) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mRatio = 1;
                if (isDraging) {
                    if (mOnSeekChangedListener != null) {
                        mOnSeekChangedListener.onSeekChanged(this, getProgress(), mChangedStartProgress,
                                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
                    }
                    isDraging = false;
                    mChangedStartProgress = -1;
                    postInvalidate();
                }
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    public int getMax() {
        return mMax;
    }

    public void setMax(int max) {
        if (max != mMax) {
            mMax = max;
            setKeyProgressIncrement(mMax / 100);
        }
    }

    public int getProgress() {
        return mProgress;
    }

    public int getExtraProgress() {
        return mKeyProgressIncrement;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            setPressed(true);
            if (mThumb != null) {
                invalidate(mThumb.getBounds());
            }
            onStartTrackingTouch();
            trackTouchEvent(event);
            break;
        case MotionEvent.ACTION_MOVE:
            if (isDraging) {
                trackTouchEvent(event);
            } else {
                onStartTrackingTouch();
                trackTouchEvent(event);
            }
            break;

        case MotionEvent.ACTION_UP:
            if (isDraging) {
                trackTouchEvent(event);
                onStopTrackingTouch();
                setPressed(false);
            } else {
                onStartTrackingTouch();
                trackTouchEvent(event);
                onStopTrackingTouch();
            }
            invalidate();
            break;

        case MotionEvent.ACTION_CANCEL:
            if (isDraging) {
                onStopTrackingTouch();
                setPressed(false);
            }
            invalidate(); // see above explanation
            break;
        }
        return true;
    }

    void onStartTrackingTouch() {
        if (!isDraging) {
            if (mOnSeekChangedListener != null) {
                mOnSeekChangedListener.onSeekChanged(this, getProgress(), getProgress(), true);
            }
            mChangedStartProgress = getProgress();
            isDraging = true;
        }
    }

    void onStopTrackingTouch() {
        if (isDraging) {
            if (mOnSeekChangedListener != null) {
                mOnSeekChangedListener.onSeekChanged(this, getProgress(), mChangedStartProgress, true);
            }
            mChangedStartProgress = -1;
            isDraging = false;
        }
    }

    private void trackTouchEvent(MotionEvent event) {
        final int width = getWidth();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        final int available = width - paddingLeft - paddingRight - mThumbOffset * 2;
        int x = (int) event.getX();
        float scale;
        float progress = 0;

        if (x < paddingLeft + mThumbOffset) {
            scale = 0.0f;
        } else if (x > width - paddingRight - mThumbOffset) {
            scale = 1.0f;
        } else {
            scale = (float) (x - paddingLeft - mThumbOffset) / (float) available;
        }
        final int max = getMax();
        progress += scale * max;
        setProgress((int) progress, true);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mThumb != null && mThumb.isStateful()) {
            mThumb.setState(getDrawableState());
        }
    }

}
