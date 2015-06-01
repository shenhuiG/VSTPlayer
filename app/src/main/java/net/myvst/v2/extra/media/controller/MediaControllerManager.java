package net.myvst.v2.extra.media.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Iterator;

public class MediaControllerManager extends FrameLayout {
    private String TAG = "MediaControllerManager";

    public interface KeyEventHandler {
        boolean handlerKeyEvent(KeyEvent event);
    }

    private class ControllHandler {
        public View mControlView;
        public ViewGroup.LayoutParams mViewLayoutParams;
        public WindowManager.LayoutParams mWindowLayoutParams;
        public String mControlTag;

        public ControllHandler(String controlTag, View controlView,
                               android.view.ViewGroup.LayoutParams viewLayoutParams,
                               android.view.WindowManager.LayoutParams windowLayoutParams) {
            super();
            mControlTag = controlTag;
            mControlView = controlView;
            if (viewLayoutParams == null) {
                viewLayoutParams = new ViewGroup.LayoutParams(-1, -2);
            }
            if (windowLayoutParams == null) {
                windowLayoutParams = createDefaultLayoutParams();
            }
            mViewLayoutParams = viewLayoutParams;
            mWindowLayoutParams = windowLayoutParams;
        }
    }

    private Context mContext = null;
    private WindowManager mWindowManager = null;
    private HashMap<String, ControllHandler> mControllHandlerMap = new HashMap<String, ControllHandler>();

    private String mControllerId = null;
    private boolean mShowing = false;
    private static final int DEFAULT_TIME_OUT_MS = 10000;
    private static final int FADE_OUT = 1;
    private int mTimeOut = DEFAULT_TIME_OUT_MS;
    private boolean mSupportBack = true;
    private KeyEventHandler mKeyEventHandler = null;
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == FADE_OUT) {
                hide();
            }
        }
    };


    public MediaControllerManager(Context context) {
        super(context);
        mContext = context;
        initFloatingWindow();
    }

    public void setKeyEventHandler(KeyEventHandler keyEventHandler) {
        mKeyEventHandler = keyEventHandler;
    }

    private void initFloatingWindow() {
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
    }

    public static WindowManager.LayoutParams createDefaultLayoutParams() {
        WindowManager.LayoutParams p = new WindowManager.LayoutParams();
        p.gravity = Gravity.CENTER;
        p.height = LayoutParams.MATCH_PARENT;
        p.width = LayoutParams.MATCH_PARENT;
        p.format = PixelFormat.TRANSLUCENT;
        p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        p.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH;
        p.token = null;
        p.windowAnimations = 0;
        return p;
    }

    public void addController(String controllerId, View contentView, ViewGroup.LayoutParams viewParams,
                              WindowManager.LayoutParams windowParams) {
        ControllHandler controllHandler = new ControllHandler(controllerId, contentView, viewParams, windowParams);
        mControllHandlerMap.put(controllerId, controllHandler);
        if (contentView instanceof IInverseControl) {
            ((IInverseControl) contentView).addControllerManager(this, controllerId);
        }
    }

    public void show(String controllerId) {
        show(controllerId, DEFAULT_TIME_OUT_MS, true);
    }

    public void show(String controllerId, int timeout) {
        show(controllerId, timeout, true);
    }

    public boolean isShowing() {
        return mShowing;
    }

    public void show(String controllerId, int timeout, boolean supportBack) {
        try {
            if (!isSupport(controllerId)) {
                return;
            }
            if (!controllerId.equals(mControllerId)) {
                hide();
            }
            if (!mShowing) {
                try {
                    mControllerId = controllerId;
                    ControllHandler controllHandler = mControllHandlerMap.get(controllerId);
                    if (getChildCount() > 0) {
                        removeAllViewsInLayout();
                    }
                    addView(controllHandler.mControlView, controllHandler.mViewLayoutParams);
                    if (getParent() == null) {
                        mWindowManager.addView(this, controllHandler.mWindowLayoutParams);
                    }
                    mShowing = true;
                    requestFocus();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if (timeout > 0) {
                mHandler.removeMessages(FADE_OUT);
                mHandler.sendMessageDelayed(mHandler.obtainMessage(FADE_OUT), timeout);
            } else {
                mHandler.removeMessages(FADE_OUT);
            }
            mSupportBack = supportBack;
            mTimeOut = timeout;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public boolean isSupport(String controllerId) {
        return mControllHandlerMap.containsKey(controllerId);
    }

    public View getControllerView(String controllerId) {
        if (isSupport(controllerId)) {
            ControllHandler controllHandler = mControllHandlerMap.get(controllerId);
            return controllHandler.mControlView;
        }
        return null;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        show(mControllerId, mTimeOut, mSupportBack);
        if (super.dispatchKeyEvent(event)) {
            return true;
        } else {
            try {
                int keyCode = event.getKeyCode();
                final boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN;
                if (mSupportBack && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (uniqueDown) {
                        hide();
                    }
                    return true;
                }
                if (mKeyEventHandler != null) {
                    if (mKeyEventHandler.handlerKeyEvent(event)) {
                        return true;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        show(mControllerId, mTimeOut, mSupportBack);
        requestFocus();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        show(mControllerId, mTimeOut, mSupportBack);
        requestFocus();
        return super.dispatchTrackballEvent(event);
    }

    public void hide() {
        if (mControllerId != null) {
            hide(mControllerId);
        }
    }

    public void hide(String controlId) {
        Log.i(TAG, "hideControl =" + controlId);
        if (mShowing && controlId != null && controlId.equals(mControllerId)) {
            try {
                if (getParent() != null) {
                    mWindowManager.removeView(this);
                    removeAllViews();
                }
            } catch (Throwable ex) {
                Log.w("MediaController", "already removed");
            }
            mHandler.removeMessages(FADE_OUT);
            mShowing = false;
            mControllerId = null;
            mSupportBack = true;
        }
    }

    public void reset() {
        hide();
        Iterator<ControllHandler> i = mControllHandlerMap.values().iterator();
        while (i.hasNext()) {
            ControllHandler h = i.next();
            h.mControlView = null;
            h = null;
        }
        mControllHandlerMap.clear();
    }
}
