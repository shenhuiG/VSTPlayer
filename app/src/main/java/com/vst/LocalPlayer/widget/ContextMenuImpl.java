package com.vst.LocalPlayer.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.vst.LocalPlayer.R;
import com.vst.dev.common.util.Utils;

import java.lang.ref.WeakReference;

public class ContextMenuImpl extends MenuBuild {
    private WeakReference<PopupWindow> mRef;

    public ContextMenuImpl(Context context) {
        super(context);
    }

    @Override
    public PopupWindow create() {
        PopupWindow popupWindow = new PopupWindow(mContext);
        View view = makeMenuView();
        if (view != null) {
            popupWindow.setContentView(view);
        }
        popupWindow.setWindowLayoutMode(-1, -1);
        popupWindow.setFocusable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0xE5000000));
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mOnMenuListener != null) {
                    mOnMenuListener.onMenuDismiss();
                }
            }
        });
        mRef = new WeakReference<PopupWindow>(popupWindow);
        return popupWindow;
    }

    @Override
    protected View makeMenuView() {
        if (mMenuItems.size() > 0) {
            LinearLayout root = new LinearLayout(mContext);
            root.setOrientation(LinearLayout.HORIZONTAL);
            root.setGravity(Gravity.CENTER);
            ImageView p = new ImageView(mContext);
            p.setTag("poster");
            root.addView(p, Utils.getFitSize(mContext, 400), Utils.getFitSize(mContext, 300));
            LinearLayout layout = new LinearLayout(mContext);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            for (int i = 0; i < mMenuItems.size(); i++) {
                MenuItem item = mMenuItems.get(i);
                layout.addView(makeSelectionItem(item), Utils.getFitSize(mContext, 300), -2);
            }
            root.addView(layout, -2, -2);
            return root;
        }
        return null;
    }


    private View makeSelectionItem(final MenuItem menuItem) {
        final LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        layout.setBackgroundResource(R.drawable.icon_item_bg_l);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRef != null) {
                    PopupWindow pop = mRef.get();
                    if (pop != null && pop.isShowing()) {
                        pop.dismiss();
                    }
                }
                if (mOnMenuListener != null) {
                    mOnMenuListener.onMenuItemOnClick(menuItem);
                }
            }
        });
        layout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View child = layout.getChildAt(i);
                    child.setSelected(hasFocus);
                }
                if (hasFocus && mOnMenuListener != null) {
                    mOnMenuListener.onMenuItemOnSelection(menuItem);

                }
            }
        });
        layout.setPadding(60, 30, 60, 30);
        ImageView icon = new ImageView(mContext);
        icon.setImageDrawable(menuItem.icon);
        layout.addView(icon, new LinearLayout.LayoutParams(-2, -2));
        TextView contentView = new TextView(mContext);
        contentView.setText(menuItem.cs);
        contentView.setGravity(Gravity.CENTER_VERTICAL);
        contentView.setTextColor(Color.WHITE);
        contentView.setPadding(30, 0, 0, 0);
        contentView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 25);
        layout.addView(contentView, new LinearLayout.LayoutParams(0, -2, 1.0F));
        layout.setFocusable(true);
        return layout;
    }

}
