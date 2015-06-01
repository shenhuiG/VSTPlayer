package com.vst.LocalPlayer.component.activity;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.widget.PopupWindow;
import com.vst.LocalPlayer.widget.WindowLoadingHelper;

public class BaseActivity extends Activity {
    PopupWindow loadingWindow = null;
    ServiceLoadingReceiver receiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (receiver == null) {
            receiver = new ServiceLoadingReceiver();
        }
        registerReceiver(receiver, new IntentFilter(WindowLoadingHelper.ACTION_WINDOW_LOADING));
        boolean isLoading = WindowLoadingHelper.isLoading(this);
        if (isLoading) {
            if (loadingWindow == null) {
                loadingWindow = WindowLoadingHelper.makeLoadingWindow(this);
            }
            show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        if (loadingWindow != null && loadingWindow.isShowing()) {
            loadingWindow.dismiss();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            receiver = null;
        }
    }

    boolean onAttach = false;

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        onAttach = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onAttach = false;
    }


    private void show() {
        if (!onAttach) {
            Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                @Override
                public boolean queueIdle() {
                    WindowLoadingHelper.show(loadingWindow, BaseActivity.this);
                    return false;
                }
            });
        } else {
            WindowLoadingHelper.show(loadingWindow, BaseActivity.this);
        }

    }

    class ServiceLoadingReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WindowLoadingHelper.ACTION_WINDOW_LOADING.equals(action)) {
                boolean isLoading = intent.getBooleanExtra("isLoading", false);
                if (isLoading) {
                    if (loadingWindow == null) {
                        loadingWindow = WindowLoadingHelper.makeLoadingWindow(BaseActivity.this);
                    }
                    show();
                } else {
                    if (loadingWindow != null && loadingWindow.isShowing()) {
                        loadingWindow.dismiss();
                    }
                }
            }
        }
    }
}
