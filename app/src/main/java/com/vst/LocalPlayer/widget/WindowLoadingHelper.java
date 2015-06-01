package com.vst.LocalPlayer.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import com.vst.LocalPlayer.R;
import com.vst.dev.common.util.Utils;

public class WindowLoadingHelper {

    public static final String ACTION_WINDOW_LOADING = "window.action.loading";

    public static PopupWindow makeLoadingWindow(Activity activity) {
        FrameLayout view = new FrameLayout(activity);
        int size = Utils.getFitSize(activity, 52);
        ProgressBar bar = new ProgressBar(activity);
        bar.setIndeterminate(false);
        bar.setIndeterminateDrawable(activity.getResources().getDrawable(R.drawable.window_loading));
        view.addView(bar, new FrameLayout.LayoutParams(size, size, Gravity.CENTER));
        ImageView imageView = new ImageView(activity);
        imageView.setImageResource(R.drawable.ic_service_usb);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        view.addView(imageView, new FrameLayout.LayoutParams(size, size, Gravity.CENTER));
        PopupWindow popupWindow = new PopupWindow(view);
        popupWindow.setFocusable(false);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setWindowLayoutMode(-2, -2);
        return popupWindow;
    }

    public static void show(final PopupWindow window, final Activity activity) {
        View acho = activity.getWindow().getDecorView();
        window.showAtLocation(acho, Gravity.RIGHT | Gravity.BOTTOM, Utils.getFitSize(activity, 25), Utils.getFitSize(activity, 27));
    }


    public static boolean isLoading(Context context) {
        SharedPreferences sp = context.getSharedPreferences("status", Context.MODE_MULTI_PROCESS);
        return sp.getBoolean("isLoading", false);
    }

    public static void setLaoding(Context context, boolean loading) {
        boolean isLoading = isLoading(context);
        if (isLoading != loading) {
            SharedPreferences sp = context.getSharedPreferences("status", Context.MODE_MULTI_PROCESS);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("isLoading", loading);
            editor.commit();
            Intent intent = new Intent(ACTION_WINDOW_LOADING);
            intent.putExtra("isLoading", loading);
            context.sendBroadcast(intent);
        }
    }


}
