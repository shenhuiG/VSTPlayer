package com.vst.LocalPlayer.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.PopupWindow;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public abstract class MenuBuild {
    protected Context mContext;
    protected CharSequence mTitle;
    protected onMenuListener mOnMenuListener;
    protected ArrayList<MenuItem> mMenuItems = new ArrayList<MenuItem>();


    public MenuBuild(Context context) {
        mContext = context;
    }

    public MenuBuild setTitle(CharSequence cs) {
        mTitle = cs;
        return this;
    }

    public MenuBuild addMenuItem(MenuItem item) {
        mMenuItems.add(item);
        return this;
    }

    public MenuBuild setOnMenuListener(onMenuListener menuListener) {
        this.mOnMenuListener = menuListener;
        return this;
    }


    public abstract PopupWindow create();

    protected abstract View makeMenuView();

    public static class MenuItem {
        public CharSequence cs;
        public Drawable icon;
        public int id;

        public MenuItem(int id, CharSequence cs, Drawable icon) {
            this.id = id;
            this.cs = cs;
            this.icon = icon;
        }

        public MenuItem(CharSequence cs, Drawable icon) {
            this.cs = cs;
            this.icon = icon;
        }

        public MenuItem(int id, CharSequence cs) {
            this.id = id;
            this.cs = cs;
        }
    }

    public interface onMenuListener {
        public void onMenuItemOnClick(MenuItem item);

        public void onMenuItemOnSelection(MenuItem item);

        public void onMenuDismiss();
    }

}
