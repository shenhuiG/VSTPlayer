package com.vst.LocalPlayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.vst.LocalPlayer.R;
import com.vst.dev.common.media.AudioTrack;
import com.vst.dev.common.media.IPlayer;
import com.vst.dev.common.media.SubTrack;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Stack;

public class LocalMenuView extends LinearLayout {

    public interface Control {

        public void setCycleMode(int i);

        public int getCycleMode();

        public void setDecodeType(int i);

        public int getDecodeType();

        public void setAudioOutSPIF(boolean b);

        public boolean isAudioOutSPIF();

        public boolean isSPIFFuctionValid();

        public AudioTrack[] getAudioTracks();

        public int getAudioTrackId();

        public void setAudioTrack(AudioTrack track);

        public SubTrack[] getSubTracks();

        public SubTrack getSubTrack();

        public void setSubTrack(SubTrack track);
    }


    public static final String THRD_SETTING = "3D设定";
    public static final String THRD_ITEM_2D = "2D 播放";
    public static final String THRD_ITEM_3DLF = "3D左右格式";
    public static final String THRD_ITEM_3DUD = "3D上下格式";
    public static final String THRD_ITEM_3D = "3D 播放";
    public static final String LOOPER_SETTING = "循环设定";
    public static final String LOOPER_ALL = "全部循环";
    public static final String LOOPER_SINGLE = "单个循环";
    public static final String LOOPER_QUEUE = "顺序循环";
    public static final String LOOPER_OFF = "单个播放";
    public static final String LOOPER_RANDOM = "随机循环";
    public static final String AUDIO_SETTING = "音频设定";

    public static final String SUBTRIP_SETTING = "字幕设定";
    public static final String DECODE_SETTING = "解码设定";

    public static final String AUDIOOUT_SETTING = "音频输出";

    public static final String MENU_SETTING = "菜单";
    private Stack<String> mPathStack = new Stack();

    public static final String TAG = "menu";
    private Context mContext;
    private TextView titleView;
    private ViewFlipper mFlipper;
    private Control mControl;

    private HashMap<String, WeakReference<View>> mViews = new HashMap<String, WeakReference<View>>();

    public LocalMenuView(Context context) {
        super(context);
        mContext = context.getApplicationContext();
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_VERTICAL);
        initView();
    }

    public void setControl(Control control) {
        mControl = control;
    }

    private void initView() {
        titleView = new TextView(mContext);
        titleView.setText(MENU_SETTING);
        addView(titleView);
        mFlipper = new ViewFlipper(mContext);
        addView(mFlipper, new LayoutParams(460, 700));
    }


    private View getView(String tag) {
        View view = null;
        WeakReference<View> ref = mViews.get(tag);
        if (ref != null && (view = ref.get()) != null) {
            return view;
        }
        if (MENU_SETTING.equals(tag)) {
            view = makeMenuSettingView();
        } else if (THRD_SETTING.equals(tag)) {
            view = make3DSettingView();
        } else if (LOOPER_SETTING.equals(tag)) {
            view = makeLooperSettingView();
        } else if (AUDIO_SETTING.equals(tag)) {
            view = makeAudioSettingView();
        } else if (SUBTRIP_SETTING.equals(tag)) {
            view = makeSUBSettingView();
        } else if (DECODE_SETTING.equals(tag)) {
            view = makeDecodeSettingView();
        } else if (AUDIOOUT_SETTING.equals(tag)) {
            view = makeAudioOutSettingView();
        }

//        if (view != null) {
//            mViews.put(tag, new WeakReference<View>(view));
//        }
        return view;
    }


    private void toNextView(String nextViewTag, Object obj) {
        if (setupFlipper(nextViewTag)) {
            //updateViewData(tag, obj);
            mFlipper.setInAnimation(mContext, R.anim.translate_right_in);
            mFlipper.setOutAnimation(mContext, R.anim.translate_left_out);
            mFlipper.showNext();
            mFlipper.requestFocus();
            mPathStack.push(nextViewTag);
        }
    }

    private void toPreviousView() {
        if (mPathStack.size() > 1) {
            mPathStack.pop();
            String tag = mPathStack.peek();
            if (setupFlipper(tag)) {
                //updateViewData(tag, obj);
                mFlipper.setInAnimation(mContext, R.anim.translate_left_in);
                mFlipper.setOutAnimation(mContext, R.anim.translate_right_out);
                mFlipper.showPrevious();
                mFlipper.requestFocus();
            }
        }
    }

    private boolean setupFlipper(String tag) {
        View nextView = getView(tag);
        if (nextView != null) {
            if (nextView.getParent() != null) {
                mFlipper.removeAllViews();
            }
            if (mFlipper.getChildCount() > 1) {
                mFlipper.removeViewAt(0);
            }
            mFlipper.addView(nextView, mFlipper.getChildCount());
            return true;
        }
        return false;
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        toNextView(MENU_SETTING, null);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFlipper.removeAllViews();
        mPathStack.clear();
    }

    private View makeMenuSettingView() {
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(makeSelectionItem(THRD_SETTING, getResources().getDrawable(R.drawable.icon_3d_setting)));
        layout.addView(makeSelectionItem(LOOPER_SETTING, getResources().getDrawable(R.drawable.icon_looper_setting)));
        layout.addView(makeSelectionItem(AUDIO_SETTING, getResources().getDrawable(R.drawable.icon_audio_setting)));
        layout.addView(makeSelectionItem(SUBTRIP_SETTING, getResources().getDrawable(R.drawable.icon_subtripe_setting)));
        layout.addView(makeSelectionItem(DECODE_SETTING, getResources().getDrawable(R.drawable.icon_subtripe_setting)));
        layout.addView(makeSelectionItem(AUDIOOUT_SETTING, getResources().getDrawable(R.drawable.icon_subtripe_setting)));
        return layout;
    }


    private View make3DSettingView() {
        RadioGroup layout = new RadioGroup(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(makeCheckableItemView(THRD_ITEM_2D, 1), -1, -2);
        layout.addView(makeCheckableItemView(THRD_ITEM_3DLF, 2), -1, -2);
        layout.addView(makeCheckableItemView(THRD_ITEM_3DUD, 3), -1, -2);
        layout.addView(makeCheckableItemView(THRD_ITEM_3D, 4), -1, -2);
        layout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View child = group.findViewById(checkedId);
                int mod = (Integer) child.getTag();
                System.out.println(mod + "");
            }
        });
        return layout;
    }


    private View makeLooperSettingView() {
        if (mControl != null) {
            int cycle = mControl.getCycleMode();
            RadioGroup layout = new RadioGroup(mContext);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(makeCheckableItemView(LOOPER_OFF, IPlayer.NO_CYCLE), -1, -2);
            layout.addView(makeCheckableItemView(LOOPER_SINGLE, IPlayer.SINGLE_CYCLE), -1, -2);
            layout.addView(makeCheckableItemView(LOOPER_QUEUE, IPlayer.QUEUE_CYCLE), -1, -2);
            layout.addView(makeCheckableItemView(LOOPER_ALL, IPlayer.ALL_CYCLE), -1, -2);
            layout.addView(makeCheckableItemView(LOOPER_RANDOM, IPlayer.RANDOM_CYCLE), -1, -2);
            layout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    View child = group.findViewById(checkedId);
                    int mod = (Integer) child.getTag();
                    mControl.setCycleMode(mod);
                }
            });
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                int mod = (Integer) child.getTag();
                if (mod == cycle) {
                    layout.check(child.getId());
                    break;
                }
            }
            return layout;
        }
        return null;
    }

    private View makeSUBSettingView() {
        if (mControl != null) {
            SubTrack[] tracks = mControl.getSubTracks();
            if (tracks != null && tracks.length > 0) {
                SubTrack track = mControl.getSubTrack();
                RadioGroup layout = new RadioGroup(mContext);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(makeCheckableItemView("wu", new SubTrack(SubTrack.SubTrackType.NONE)), -1, -2);
                for (int i = 0; i < tracks.length; i++) {
                    SubTrack _track = tracks[i];
                    layout.addView(makeCheckableItemView(_track.name + "", _track), -1, -2);
                }
                layout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        View child = group.findViewById(checkedId);
                        SubTrack _track = (SubTrack) child.getTag();
                        mControl.setSubTrack(_track);
                    }
                });
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View child = layout.getChildAt(i);
                    SubTrack _track = (SubTrack) child.getTag();
                    if (track.equals(_track)) {
                        layout.check(child.getId());
                        break;
                    }
                }
                return layout;
            }
        }
        return null;
    }

    private View makeAudioOutSettingView() {
        if (mControl != null) {
            if (mControl.isSPIFFuctionValid()) {
                boolean spif = mControl.isAudioOutSPIF();
                RadioGroup layout = new RadioGroup(mContext);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(makeCheckableItemView("开启功放", true), -1, -2);
                layout.addView(makeCheckableItemView("关闭功放", false), -1, -2);
                layout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        View child = group.findViewById(checkedId);
                        boolean _spif = (Boolean) child.getTag();
                        mControl.setAudioOutSPIF(_spif);
                    }
                });
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View child = layout.getChildAt(i);
                    boolean _spif = (Boolean) child.getTag();
                    if (_spif == spif) {
                        layout.check(child.getId());
                        break;
                    }
                }
                return layout;
            }
        }
        return null;
    }


    private View makeDecodeSettingView() {
        if (mControl != null) {
            int decode = mControl.getDecodeType();
            RadioGroup layout = new RadioGroup(mContext);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(makeCheckableItemView("硬解码", IPlayer.HARD_DECODE), -1, -2);
            layout.addView(makeCheckableItemView("软解码", IPlayer.SOFT_DECODE), -1, -2);
            layout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    View child = group.findViewById(checkedId);
                    int mod = (Integer) child.getTag();
                    mControl.setDecodeType(mod);
                }
            });
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                int mod = (Integer) child.getTag();
                if (mod == decode) {
                    layout.check(child.getId());
                    break;
                }
            }
            return layout;
        }
        return null;
    }

    private View makeAudioSettingView() {
        if (mControl != null) {
            AudioTrack[] tracks = mControl.getAudioTracks();
            if (tracks != null && tracks.length > 0) {
                int id = mControl.getAudioTrackId();
                RadioGroup layout = new RadioGroup(mContext);
                layout.setOrientation(LinearLayout.VERTICAL);
                for (int i = 0; i < tracks.length; i++) {
                    AudioTrack track = tracks[i];
                    layout.addView(makeCheckableItemView(track.language, track), -1, -2);
                }
                layout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        View child = group.findViewById(checkedId);
                        AudioTrack track = (AudioTrack) child.getTag();
                        mControl.setAudioTrack(track);
                    }
                });
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View child = layout.getChildAt(i);
                    AudioTrack track = (AudioTrack) child.getTag();
                    if (id == track.trackId) {
                        layout.check(child.getId());
                        break;
                    }
                }
                return layout;
            }
        }
        return null;
    }

    private View makeSelectionItem(final String content, Drawable left) {
        final LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        layout.setBackgroundResource(R.drawable.icon_item_bg_l);
        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toNextView(content, null);
                System.out.println(content);
            }
        });
        layout.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                for (int i = 0; i < layout.getChildCount(); i++) {
                    View child = layout.getChildAt(i);
                    child.setSelected(hasFocus);
                }
            }
        });
        layout.setPadding(60, 30, 60, 30);
        ImageView icon = new ImageView(mContext);
        icon.setImageDrawable(left);
        layout.addView(icon, new LinearLayout.LayoutParams(-2, -2));
        TextView contentView = new TextView(mContext);
        contentView.setText(content);
        contentView.setGravity(Gravity.CENTER_VERTICAL);
        contentView.setTextColor(Color.WHITE);
        contentView.setPadding(30, 0, 0, 0);
        contentView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 25);
        layout.addView(contentView, new LinearLayout.LayoutParams(0, -2, 1.0F));
        ImageView arrow = new ImageView(mContext);
        arrow.setImageResource(R.drawable.ic_arrow_focus);
        layout.addView(arrow, new LinearLayout.LayoutParams(-2, -2));
        layout.setFocusable(true);
        return layout;
    }


    private View makeCheckableItemView(final String content, Object obj) {
        final RadioButton rbt = new RadioButton(mContext);
        rbt.setBackgroundResource(R.drawable.icon_item_bg_l);
        rbt.setButtonRightDrawable(getResources().getDrawable(R.drawable.icon_checked_sel));
        rbt.setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
        rbt.setGravity(Gravity.CENTER_VERTICAL);
        rbt.setText(content);
        rbt.setPadding(0, 30, 60, 30);
        if (obj != null) {
            rbt.setTag(obj);
        }
        return rbt;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0) {
            if (mPathStack.size() > 1) {
                toPreviousView();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    class RadioButton extends android.widget.RadioButton {

        private Drawable mButtonDrawable;

        public RadioButton(Context context) {
            super(context);
        }

        @Override
        protected boolean verifyDrawable(Drawable who) {
            return super.verifyDrawable(who) || who == mButtonDrawable;
        }

        @Override
        public void jumpDrawablesToCurrentState() {
            super.jumpDrawablesToCurrentState();
            if (mButtonDrawable != null) mButtonDrawable.jumpToCurrentState();
        }

        public void setButtonRightDrawable(Drawable d) {
            if (d != null) {
                if (mButtonDrawable != null) {
                    mButtonDrawable.setCallback(null);
                    unscheduleDrawable(mButtonDrawable);
                }
                d.setCallback(this);
                d.setState(getDrawableState());
                d.setVisible(getVisibility() == VISIBLE, false);
                mButtonDrawable = d;
                mButtonDrawable.setState(null);
                setMinHeight(mButtonDrawable.getIntrinsicHeight());
            }
            refreshDrawableState();
        }

        @Override
        protected void drawableStateChanged() {
            super.drawableStateChanged();
            if (mButtonDrawable != null) {
                int[] myDrawableState = getDrawableState();
                mButtonDrawable.setState(myDrawableState);
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            final Drawable buttonDrawable = mButtonDrawable;
            if (buttonDrawable != null) {
                final int verticalGravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;
                final int drawableHeight = buttonDrawable.getIntrinsicHeight();
                final int drawableWidth = buttonDrawable.getIntrinsicWidth();

                int top = 0;
                switch (verticalGravity) {
                    case Gravity.BOTTOM:
                        top = getHeight() - drawableHeight;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        top = (getHeight() - drawableHeight) / 2;
                        break;
                }
                int bottom = top + drawableHeight;
                int left = getWidth() - drawableWidth - getPaddingRight();
                int right = getWidth() - getPaddingRight();
                buttonDrawable.setBounds(left, top, right, bottom);
                buttonDrawable.draw(canvas);
            }
        }

        @Override
        public int getCompoundPaddingLeft() {
            int padding = super.getCompoundPaddingLeft();
            final Drawable buttonDrawable = mButtonDrawable;
            if (buttonDrawable != null) {
                padding += buttonDrawable.getIntrinsicWidth();
            }
            return padding;
        }

        @Override
        public int getCompoundPaddingRight() {
            int padding = super.getCompoundPaddingRight();
            final Drawable buttonDrawable = mButtonDrawable;
            if (buttonDrawable != null) {
                padding += buttonDrawable.getIntrinsicWidth();
            }
            return padding;
        }
    }


    class State {

        String tag;
        int focusdIndex;
        int foucusId;
        int checkedId;
        int checkedIndex;
    }

}
