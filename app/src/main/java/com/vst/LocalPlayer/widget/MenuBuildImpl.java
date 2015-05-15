package com.vst.LocalPlayer.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.vst.LocalPlayer.R;
import com.vst.dev.common.util.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class MenuBuildImpl extends MenuBuild {
    private WeakReference<PopupWindow> mRef;

    public MenuBuildImpl(Context context) {
        super(context);
    }

    public PopupWindow create() {
        View view = makeMenuView();
        if (view != null) {
            PopupWindow popupWindow = new PopupWindow(mContext);
            popupWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.menu_bg));
            popupWindow.setContentView(view);
            popupWindow.setWindowLayoutMode(-1, -1);
            popupWindow.setFocusable(true);
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
        return null;
    }

    protected View makeMenuView() {
        if (mMenuItems.size() > 0) {
            LinearLayout root = new LinearLayout(mContext);
            root.setOrientation(LinearLayout.VERTICAL);
            root.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout layout = new LinearLayout(mContext);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setGravity(Gravity.CENTER);
            layout.setPadding(Utils.getFitSize(mContext, 40), 0, 0, 0);
            if (mTitle != null) {
                TextView title = new TextView(mContext);
                title.setText(mTitle);
                title.getPaint().setFakeBoldText(true);
                title.setGravity(Gravity.CENTER_VERTICAL);
                title.setTextColor(Color.WHITE);
                title.setPadding(Utils.getFitSize(mContext, 108), 0, 0, Utils.getFitSize(mContext, 8));
                title.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 30));
                root.addView(title);
                View v = new View(mContext);
                v.setBackgroundColor(0xff999999);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Utils.getFitSize(mContext, 180), 1);
                lp.bottomMargin = Utils.getFitSize(mContext, 60);
                root.addView(v, lp);
            }
            for (int i = 0; i < mMenuItems.size(); i++) {
                MenuItem item = mMenuItems.get(i);
                layout.addView(makeSelectionItem(item), -1, -2);
            }
            root.addView(layout, Utils.getFitSize(mContext, 280), -2);
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
        layout.setPadding(Utils.getFitSize(mContext, 40), Utils.getFitSize(mContext, 26),
                Utils.getFitSize(mContext, 40), Utils.getFitSize(mContext, 26));
        ImageView icon = new ImageView(mContext);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setImageDrawable(menuItem.icon);
        layout.addView(icon, Utils.getFitSize(mContext, 26), Utils.getFitSize(mContext, 26));
        TextView contentView = new TextView(mContext);
        contentView.setText(menuItem.cs);
        contentView.setGravity(Gravity.CENTER_VERTICAL);
        contentView.setTextColor(mContext.getResources().getColorStateList(R.color.videos_menu_text));
        contentView.setPadding(Utils.getFitSize(mContext, 20), 0, 0, 0);
        contentView.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 24));
        contentView.getPaint().setFakeBoldText(true);
        layout.addView(contentView, new LinearLayout.LayoutParams(0, -2, 1.0F));
        layout.setFocusable(true);
        return layout;
    }
}
