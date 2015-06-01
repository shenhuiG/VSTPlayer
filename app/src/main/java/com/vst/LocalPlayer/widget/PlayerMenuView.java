package com.vst.LocalPlayer.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.vst.LocalPlayer.R;
import com.vst.dev.common.media.AudioTrack;
import com.vst.dev.common.media.IPlayer;
import com.vst.dev.common.media.SubTrack;
import com.vst.dev.common.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class PlayerMenuView extends FrameLayout {

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
    private LinearLayout mRootView;

    public PlayerMenuView(Context context) {
        super(context);
        mContext = context.getApplicationContext();
        initView();
    }

    private void initView() {
        mRootView = new LinearLayout(mContext);
        mRootView.setOrientation(LinearLayout.HORIZONTAL);
        mRootView.setGravity(Gravity.CENTER_HORIZONTAL);
        mRootView.setBackgroundColor(0xf0000000);
        addView(mRootView, -1, Utils.getFitSize(mContext, 470));
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
        makePreferenceView();
        requestFocus();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private View makeCheckableItemView(final String content, final Object obj, final int group) {
        final RadioButton rbt = new RadioButton(mContext);
        Utils.applyFace(rbt);
        rbt.setSingleLine(true);
        rbt.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        rbt.setMarqueeRepeatLimit(Integer.MAX_VALUE);
        rbt.setBackgroundResource(R.drawable.icon_item_bg_l);
        rbt.setButtonDrawable(new ColorDrawable(Color.TRANSPARENT));
        rbt.setGravity(Gravity.CENTER);
        rbt.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 24));
        rbt.setTextColor(getResources().getColorStateList(R.color.checked_text));
        rbt.setText(content);
        rbt.setPadding(Utils.getFitSize(mContext, 20), Utils.getFitSize(mContext, 25), Utils.getFitSize(mContext, 20), Utils.getFitSize(mContext, 25));
        if (obj != null) {
            rbt.setTag(obj);
        }
        return rbt;
    }

    private View makeTextView(String title) {
        TextView tv = new TextView(mContext);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, Utils.getFitSize(mContext, 26));
        tv.setTextColor(0xff00c0ff);
        tv.setText(title);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(0, Utils.getFitSize(mContext, 25), 0, Utils.getFitSize(mContext, 25));
        Utils.applyFace(tv);
        return tv;
    }

    private void makePreferenceView() {
        mRootView.removeAllViews();
        ArrayList<View> views = new ArrayList<View>();
        View cycleModView = makePreferenceCycleModView();
        if (cycleModView != null) {
            views.add(cycleModView);
        }
        View decodeView = makePreferenceDecodeView();
        if (decodeView != null) {
            views.add(decodeView);
        }
        View audioView = makePreferenceAudioView();
        if (audioView != null) {
            views.add(audioView);
        }
        View audioOutView = makePreferenceAudioOutView();
        if (audioOutView != null) {
            views.add(audioOutView);
        }
        View subTitleView = makePreferenceSubtitleView();
        if (subTitleView != null) {
            views.add(subTitleView);
        }
        if (!views.isEmpty()) {
            int height = -2;
            mRootView.addView(makeCutView(), 1, -2);
            for (int i = 0; i < views.size(); i++) {
                View v = views.get(i);
                v.measure(-2, -2);
                height = Math.max(height, v.getMeasuredHeight());
                mRootView.addView(v, -2, -2);
                mRootView.addView(makeCutView(), 1, -2);
            }
            mRootView.setLayoutParams(new FrameLayout.LayoutParams(-1, height));
        }
    }


    private View makePreferenceCycleModView() {
        if (mControl != null) {
            int cycle = mControl.getCycleMode();
            RadioGroup layout = new RadioGroup(mContext);
            layout.setOrientation(LinearLayout.VERTICAL);
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
            layout.addView(makeTextView("循环模式"), 0, new LayoutParams(Utils.getFitSize(mContext, 220), -2));
            layout.addView(makeCutView(), 1, new LayoutParams(Utils.getFitSize(mContext, 220), 1));
            return layout;
        }
        return null;
    }

    private View makeCutView() {
        View v = new View(mContext);
        v.setBackgroundColor(Color.GRAY);
        return v;
    }

    private View makePreferenceDecodeView() {
        if (mControl != null) {
            int decode = mControl.getDecodeType();
            RadioGroup layout = new RadioGroup(mContext);
            layout.setOrientation(LinearLayout.VERTICAL);
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
            layout.addView(makeTextView("解码设置"), 0, new LayoutParams(Utils.getFitSize(mContext, 220), -2));
            layout.addView(makeCutView(), 1, new LayoutParams(Utils.getFitSize(mContext, 220), 1));
            return layout;
        }
        return null;
    }

    private View makePreferenceAudioView() {
        if (mControl != null) {
            AudioTrack[] tracks = mControl.getAudioTracks();
            if (tracks != null && tracks.length > 0) {
                int id = mControl.getAudioTrackId();
                RadioGroup layout = new RadioGroup(mContext);
                layout.setOrientation(LinearLayout.VERTICAL);
                for (int i = 0; i < tracks.length && i < 6; i++) {
                    AudioTrack track = tracks[i];
                    layout.addView(makeCheckableItemView("音频" + track.trackId, track, GROUP_AUDIO), Utils.getFitSize(mContext, 220), -2);
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
                layout.addView(makeTextView("音频设置"), 0, new LayoutParams(Utils.getFitSize(mContext, 220), -2));
                layout.addView(makeCutView(), 1, new LayoutParams(Utils.getFitSize(mContext, 220), 1));
                return layout;
            }
        }
        return null;
    }


    private View makePreferenceAudioOutView() {
        if (mControl != null) {
            if (mControl.isSPIFFuctionValid()) {
                boolean spif = mControl.isAudioOutSPIF();
                RadioGroup layout = new RadioGroup(mContext);
                layout.setOrientation(LinearLayout.VERTICAL);
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
                layout.addView(makeTextView("音频输出"), 0, new LayoutParams(Utils.getFitSize(mContext, 220), -2));
                layout.addView(makeCutView(), 1, new LayoutParams(Utils.getFitSize(mContext, 220), 1));
                return layout;
            }
        }
        return null;
    }

    private View makePreferenceSubtitleView() {
        if (mControl != null) {
            SubTrack[] tracks = mControl.getSubTracks();
            if (tracks != null && tracks.length > 0) {
                SubTrack track = mControl.getSubTrack();
                RadioGroup layout = new RadioGroup(mContext);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(makeCheckableItemView("未设置字幕", new SubTrack(SubTrack.SubTrackType.NONE), GROUP_SUBTITLE), Utils.getFitSize(mContext, 220), -2);
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
                layout.addView(makeTextView("字幕选择"), 0, new LayoutParams(Utils.getFitSize(mContext, 220), -2));
                layout.addView(makeCutView(), 1, new LayoutParams(Utils.getFitSize(mContext, 220), 1));
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
