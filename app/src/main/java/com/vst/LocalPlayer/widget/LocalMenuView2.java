package com.vst.LocalPlayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.vst.LocalPlayer.R;
import com.vst.dev.common.media.AudioTrack;
import com.vst.dev.common.media.IPlayer;
import com.vst.dev.common.media.SubTrack;
import com.vst.dev.common.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class LocalMenuView2 extends LinearLayout {

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


    private static final int GROUP_CYCLE = 1;
    private static final int GROUP_SUBTITLE = 2;
    private static final int GROUP_DECODE = 3;
    private static final int GROUP_AUDIO = 4;
    private static final int GROUP_AUDIOOUT = 5;
    private HashMap<Integer, ArrayList<Integer>> checkedIdMap = new HashMap<Integer, ArrayList<Integer>>();
    public static final String LOOPER_ALL = "全部循环";
    public static final String LOOPER_SINGLE = "单个循环";
    public static final String LOOPER_QUEUE = "顺序循环";
    public static final String LOOPER_OFF = "单个播放";
    public static final String LOOPER_RANDOM = "随机循环";
    public static final String TAG = "menu";
    private Context mContext;
    private Control mControl;

    public LocalMenuView2(Context context) {
        super(context);
        mContext = context.getApplicationContext();
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackgroundColor(0xa0000000);
    }

    private void addCheckViewId(int group, int checkeId) {
        if (checkedIdMap.containsKey(group)) {
            ArrayList<Integer> idList = checkedIdMap.get(group);
            idList.add(checkeId);
            checkedIdMap.put(group, idList);
        } else {
            ArrayList<Integer> idList = new ArrayList<Integer>();
            idList.add(checkeId);
            checkedIdMap.put(group, idList);
        }
    }

    public void setControl(Control control) {
        mControl = control;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        makePerferenceView();
        requestFocus();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private View makeCheckableItemView(final String content, final Object obj, final int group) {
        final RadioButton rbt = new RadioButton(mContext);
        rbt.setSingleLine(true);
        rbt.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        rbt.setMarqueeRepeatLimit(Integer.MAX_VALUE);
        rbt.setBackgroundResource(R.drawable.icon_item_bg_l);
        rbt.setButtonRightDrawable(getResources().getDrawable(R.drawable.icon_checked_sel));
        rbt.setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
        rbt.setGravity(Gravity.CENTER_VERTICAL);
        rbt.setText(content);
        rbt.setPadding(0, Utils.getFitSize(mContext, 20), Utils.getFitSize(mContext, 40), Utils.getFitSize(mContext, 20));
        if (obj != null) {
            rbt.setTag(obj);
        }
        rbt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }
        });
        return rbt;
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


    private void makePerferenceView() {
        removeAllViews();
        View cycelModView = makePerferenceCycleModView();
        if (cycelModView != null) {
            addView(cycelModView);
        }
        View decodeView = makePerferenceDecodeView();
        if (decodeView != null) {
            addView(decodeView);
        }
        View audioView = makePerferenceAudioView();
        if (audioView != null) {
            addView(audioView);
        }
        View audioOutView = makePerferenceAudioOutView();
        if (audioOutView != null) {
            addView(audioOutView);
        }
        View subTitleView = makePerferenceSubtitleView();
        if (subTitleView != null) {
            addView(subTitleView);
        }
    }


    private View makePerferenceCycleModView() {
        if (mControl != null) {
            int cycle = mControl.getCycleMode();
            RadioGroup layout = new RadioGroup(mContext);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            HashMap<Integer, String> m = new HashMap<Integer, String>();
            m.put(IPlayer.NO_CYCLE, LOOPER_OFF);
            m.put(IPlayer.SINGLE_CYCLE, LOOPER_SINGLE);
            m.put(IPlayer.QUEUE_CYCLE, LOOPER_QUEUE);
            m.put(IPlayer.ALL_CYCLE, LOOPER_ALL);
            m.put(IPlayer.RANDOM_CYCLE, LOOPER_RANDOM);
            Iterator<Integer> keyIterator = m.keySet().iterator();
            while (keyIterator.hasNext()) {
                int key = keyIterator.next();
                String value = m.get(key);
                View v = makeCheckableItemView(value, key, GROUP_CYCLE);
                layout.addView(v, Utils.getFitSize(mContext, 220), -2);
            }
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

    private View makePerferenceDecodeView() {
        if (mControl != null) {
            int decode = mControl.getDecodeType();
            RadioGroup layout = new RadioGroup(mContext);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            HashMap<Integer, String> m = new HashMap<Integer, String>();
            m.put(IPlayer.HARD_DECODE, "硬解码");
            m.put(IPlayer.SOFT_DECODE, "软解码");
            Iterator<Integer> keyIterator = m.keySet().iterator();
            while (keyIterator.hasNext()) {
                int key = keyIterator.next();
                String value = m.get(key);
                View v = makeCheckableItemView(value, key, GROUP_DECODE);
                layout.addView(v, Utils.getFitSize(mContext, 220), -2);
            }
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

    private View makePerferenceAudioView() {
        if (mControl != null) {
            AudioTrack[] tracks = mControl.getAudioTracks();
            if (tracks != null && tracks.length > 0) {
                int id = mControl.getAudioTrackId();
                RadioGroup layout = new RadioGroup(mContext);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                for (int i = 0; i < tracks.length; i++) {
                    AudioTrack track = tracks[i];
                    layout.addView(makeCheckableItemView(track.language, track, GROUP_AUDIO), Utils.getFitSize(mContext, 220), -2);
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


    private View makePerferenceAudioOutView() {
        if (mControl != null) {
            if (mControl.isSPIFFuctionValid()) {
                boolean spif = mControl.isAudioOutSPIF();
                RadioGroup layout = new RadioGroup(mContext);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                HashMap<Boolean, String> m = new HashMap<Boolean, String>();
                m.put(true, "开启功放");
                m.put(false, "关闭功放");
                Iterator<Boolean> keyIterator = m.keySet().iterator();
                while (keyIterator.hasNext()) {
                    boolean key = keyIterator.next();
                    String value = m.get(key);
                    View v = makeCheckableItemView(value, key, GROUP_AUDIOOUT);
                    layout.addView(v, Utils.getFitSize(mContext, 220), -2);
                }
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

    private View makePerferenceSubtitleView() {
        if (mControl != null) {
            SubTrack[] tracks = mControl.getSubTracks();
            if (tracks != null && tracks.length > 0) {
                SubTrack track = mControl.getSubTrack();
                RadioGroup layout = new RadioGroup(mContext);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.addView(makeCheckableItemView("wu", new SubTrack(SubTrack.SubTrackType.NONE), GROUP_SUBTITLE), Utils.getFitSize(mContext, 220), -2);
                for (int i = 0; i < tracks.length; i++) {
                    SubTrack _track = tracks[i];
                    layout.addView(makeCheckableItemView(_track.name + "", _track, GROUP_SUBTITLE), Utils.getFitSize(mContext, 220), -2);
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

    //radioButton 分组

    private void onItemChecked(int group, int viewId, Object obj) {
        switch (group) {
            case GROUP_AUDIO:
                break;
            case GROUP_AUDIOOUT:
                break;
            case GROUP_DECODE:
                break;
            case GROUP_SUBTITLE:
                break;
            case GROUP_CYCLE:
                break;
        }
    }
}
