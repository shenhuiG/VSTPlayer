package com.vst.LocalPlayer.component.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.*;
import android.widget.*;
import com.vst.LocalPlayer.R;
import com.vst.LocalPlayer.model.FileCategory;
import com.vst.LocalPlayer.widget.PlayerMenuView;
import com.vst.LocalPlayer.widget.PlayerSeekController;
import com.vst.LocalPlayer.UUtils;
import com.vst.LocalPlayer.component.provider.MediaStore;
import com.vst.LocalPlayer.model.MediaInfo;
import com.vst.dev.common.media.AudioTrack;
import com.vst.dev.common.media.SubTrack;
import com.vst.LocalPlayer.model.SubtripApi;
import com.vst.dev.common.media.IPlayer;

import net.myvst.v2.extra.media.MediaControlFragment;
import net.myvst.v2.extra.media.controller.MediaControllerManager;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Random;

public class PlayFragment extends MediaControlFragment implements IPlayer.OnCompletionListener,
        IPlayer.OnErrorListener, IPlayer.OnPreparedListener, MediaControllerManager.KeyEventHandler,
        IPlayer.OnInfoListener, PlayerSeekController.ControlCallback, IPlayer.OnTimedTextChangedListener, PlayerMenuView.Control {
    private static final int HANDLE_ERROR = 0x0002;
    private static final int FINAL_PLAY = 0x0001;
    private Context mContext = null;
    private int mSeekWhenPrepared = 0;
    private int mScaleSize = IPlayer.SURFACE_BEST_FIT;
    private int mDecodeType = IPlayer.HARD_DECODE;
    private Uri mMediaUri = null;
    private MediaInfo mMediaInfo;
    private ArrayList<MediaInfo> mAllArray = null;
    private int cycleIndex = -1;
    private Handler mHandler;
    private PlayerSeekController mSeekController;
    private int mCycleMode = IPlayer.NO_CYCLE;

    @Override
    public void setCycleMode(int i) {
        mCycleMode = i;
    }

    @Override
    public int getCycleMode() {
        return mCycleMode;
    }

    @Override
    public void setDecodeType(int i) {
        mDecodeType = i;
        if (mPlayer != null) {
            mPlayer.setDecodeType(mDecodeType);
        }
    }

    @Override
    public int getDecodeType() {
        return mDecodeType;
    }

    @Override
    public AudioTrack[] getAudioTracks() {
        if (mPlayer != null) {
            return mPlayer.getAudioTracks();
        }
        return null;
    }

    @Override
    public int getAudioTrackId() {
        if (mPlayer != null) {
            return mPlayer.getAudioTrackId();
        }
        return -1;
    }

    @Override
    public void setAudioTrack(AudioTrack track) {
        if (mPlayer != null) {
            mPlayer.setAudioTrack(track);
        }
    }

    public SubTrack[] getSubTracks() {
        File file = new File(mMediaUri.getPath());
        SubTrack[] localSubs = SubtripApi.getLocalSubTitle(file);
        if (localSubs != null && localSubs.length == 0) {
            localSubs = null;
        }
        SubTrack[] internalSubs = null;
        if (mPlayer != null) {
            internalSubs = mPlayer.getInternalSubTitle();
            if (internalSubs != null && internalSubs.length == 0) {
                internalSubs = null;
            }
        }
        if (localSubs != null && internalSubs == null) {
            return localSubs;
        } else if (localSubs == null && internalSubs != null) {
            return internalSubs;
        } else if (localSubs != null && internalSubs != null) {
            SubTrack[] tracks = new SubTrack[localSubs.length + internalSubs.length];
            for (int i = 0; i < localSubs.length; i++) {
                tracks[i] = localSubs[i];
            }
            for (int i = 0; i < internalSubs.length; i++) {
                tracks[i + localSubs.length] = internalSubs[i];
            }
            return tracks;
        }
        return null;
    }

    public SubTrack getSubTrack() {
        if (mPlayer != null) {
            SubTrack track = mPlayer.getSubTrack();
            if (track != null) {
                return track;
            }
        }
        return new SubTrack(SubTrack.SubTrackType.NONE);
    }

    public void setSubTrack(SubTrack track) {
        if (mPlayer != null) {
            if (track.from == SubTrack.SubTrackType.NONE) {
                mPlayer.setSubTrack(null, 0);
            } else {
                mPlayer.setSubTrack(track, 0);
            }
        }
    }

    public void setAudioOutSPIF(boolean b) {
        if (mPlayer != null) {
            mPlayer.setSPIF(b);
        }
    }

    public boolean isAudioOutSPIF() {
        if (mPlayer != null) {
            mPlayer.isSPIF();
        }
        return false;
    }

    public boolean isSPIFFuctionValid() {
        return mDecodeType == IPlayer.SOFT_DECODE;
    }

    public static PlayFragment newInstance(Bundle args) {
        PlayFragment fragment = new PlayFragment();
        if (args != null) {
            fragment.setArguments(args);
        }
        return fragment;
    }

    private boolean init(Bundle args) {
        if (args != null) {
            Uri mediaUri = args.getParcelable("uri");
            if (mediaUri != null && !mediaUri.equals(mMediaUri)) {
                mMediaInfo = (MediaInfo) args.getSerializable("mediainfo");
                mSeekWhenPrepared = getPositionFromRecord(mMediaInfo.id);
                mPlayer.resetVideo();
                mHandler.sendMessage(mHandler.obtainMessage(FINAL_PLAY, mediaUri));
            }
            return true;
        }
        return false;
    }


    private int getPositionFromRecord(long mediaID) {
        if (mediaID >= 0) {
            Cursor c = mContext.getContentResolver().query(MediaStore.MediaRecord.CONTENT_URI, null,
                    MediaStore.MediaRecord.FIELD_MEDIA_ID + "=?", new String[]{mediaID + ""}, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                return c.getInt(c.getColumnIndex(MediaStore.MediaRecord.FIELD_POSITION));
            }
            c.close();
        }
        return 0;
    }

    public int[] getVideoSize() {
        if (mPlayer != null) {
            return new int[]{mPlayer.getVideoWidth(), mPlayer.getVideoHeight()};
        }
        return new int[]{0, 0};
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity.getApplicationContext();
        mHandler = HANDLER;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (init(getArguments())) {
            initController();
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onPause() {
        long p = mPlayer.getPosition();
        long d = mPlayer.getDuration();
        if (p > d - 10000) {
            p = d - 10000;
        }
        if (p < 0) {
            p = 0;
        }
        insertRecord(mMediaInfo.id, p, d);
        super.onPause();
    }


    private void insertRecord(long mediaId, long position, long duration) {
        if (mediaId >= 0) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaRecord.FIELD_POSITION, position);
            values.put(MediaStore.MediaRecord.FIELD_DURATION, duration);
            values.put(MediaStore.MediaRecord.FIELD_DATE, System.currentTimeMillis());
            //first update
            int count = mContext.getContentResolver().update(MediaStore.MediaRecord.CONTENT_URI, values,
                    MediaStore.MediaRecord.FIELD_MEDIA_ID + "=?", new String[]{mediaId + ""});
            if (count <= 0) {
                //update failure
                values.put(MediaStore.MediaRecord.FIELD_MEDIA_ID, mediaId);
                values.put(MediaStore.MediaRecord.FIELD_DATE, System.currentTimeMillis());
                Uri uri = mContext.getContentResolver().insert(MediaStore.MediaRecord.CONTENT_URI, values);
                System.out.println("insert success uri=" + uri);
            } else {
                System.out.println("update success");
            }
        }
    }

    private void initController() {
        if (mControllerManager != null) {
            mControllerManager.reset();
            mSeekController = new PlayerSeekController(mContext);
            mSeekController.setControl(this);
            mControllerManager.addController(PlayerSeekController.SEEK_CONTROLLER, mSeekController, null, null);
            PlayerMenuView menuView = new PlayerMenuView(mContext);
            menuView.setControl(this);
            WindowManager.LayoutParams wlp = MediaControllerManager.createDefaultLayoutParams();
            wlp.height = com.vst.dev.common.util.Utils.getFitSize(mContext, 450);
            wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            wlp.gravity = Gravity.TOP;
            mControllerManager.addController(PlayerMenuView.TAG, menuView, null, wlp);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        if (mControllerManager != null) {
            mControllerManager.reset();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mHandler = null;
    }

    private void handleError(String msg) {
        mHandler.sendMessage(mHandler.obtainMessage(HANDLE_ERROR, msg));
    }


    private final Handler HANDLER = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FINAL_PLAY:
                    Uri uri = (Uri) msg.obj;
                    mMediaUri = uri;
                    if (mSeekController != null) {
                        mSeekController.setMediaMeta(mMediaInfo);
                    }
                    if (mPlayer != null && uri != null) {
                        mPlayer.setDecodeType(mDecodeType);
                        mPlayer.setVideoPath(uri, null);
                        mPlayer.start();
                        if (mSeekWhenPrepared > 0) {
                            mPlayer.seekTo(mSeekWhenPrepared);
                            mSeekWhenPrepared = 0;
                        }
                    }
                    break;
                case HANDLE_ERROR:
                    showErrorToast();
                    getActivity().finish();
                    break;
                default:
            }
        }
    };

    private void showErrorToast() {
        Toast t = new Toast(mContext);
        LinearLayout root = new LinearLayout(mContext);
        ImageView i = new ImageView(mContext);
        i.setImageResource(R.drawable.ic_play_error_toast);
        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(i, com.vst.dev.common.util.Utils.getFitSize(mContext, 181), com.vst.dev.common.util.Utils.getFitSize(mContext, 101));
        t.setView(root);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.setDuration(Toast.LENGTH_SHORT);
        t.show();
    }


    @Override
    public long getDuration() {
        if (mPlayer != null) {
            return mPlayer.getDuration();
        }
        return -1;
    }

    public long getPosition() {
        if (mPlayer != null) {
            return mPlayer.getPosition();
        }
        return -1;
    }

    public void seekTo(int pos) {
        if (mPlayer != null) {
            mPlayer.seekTo(pos);
        }
    }

    @Override
    public void mediaPlay() {

    }

    @Override
    public void mediaPause() {

    }

    @Override
    public void onPrepared(IPlayer mp) {
        mPlayer.changeScale(mScaleSize);
        mPlayer.start();
    }

    @Override
    public boolean onError(IPlayer mp, int what, int extra) {
        if (what == IPlayer.VLC_INIT_ERROR) {
            mPlayer.setDecodeType(IPlayer.HARD_DECODE);
        } else {
            handleError("error");
        }
        return true;
    }

    @Override
    public void onCompletion(IPlayer mp) {
        switch (mCycleMode) {
            case IPlayer.NO_CYCLE:
                if (getActivity() != null) {
                    getActivity().finish();
                }
                break;
            case IPlayer.SINGLE_CYCLE:
                mPlayer.resetVideo();
                mHandler.sendMessage(mHandler.obtainMessage(FINAL_PLAY, mMediaUri));
                break;
            case IPlayer.ALL_CYCLE:
            case IPlayer.RANDOM_CYCLE:
                //get all video
                if (mAllArray == null) {
                    mAllArray = getAllVideoList(mMediaInfo.deviceId, mMediaInfo.devicePath);
                    if (mAllArray == null) {
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                        return;
                    }
                }
                //get index
                int size = mAllArray.size();
                if (mCycleMode == IPlayer.RANDOM_CYCLE) {
                    Random rand = new Random(cycleIndex);
                    cycleIndex = rand.nextInt(size);
                } else {
                    if (cycleIndex == -1) {
                        if (mMediaInfo.id >= 0) {
                            for (int i = 0; i < size; i++) {
                                MediaInfo media = mAllArray.get(i);
                                if (media.id == mMediaInfo.id) {
                                    cycleIndex = i;
                                    break;
                                }
                            }
                        }
                    }
                    cycleIndex = (cycleIndex + 1) % size;
                }
                mMediaInfo = mAllArray.get(cycleIndex);
                Uri uri = UUtils.getMediaUri(mMediaInfo.path);
                if (uri != null) {
                    mPlayer.resetVideo();
                    mHandler.sendMessage(mHandler.obtainMessage(FINAL_PLAY, uri));
                }
                break;
            case IPlayer.QUEUE_CYCLE:
                //dir video cycle
                File dir = new File(mMediaUri.getPath()).getParentFile();
                ArrayList<String> list = getQueueVideoList(dir);
                if (list != null && !list.isEmpty()) {
                    int index = list.indexOf(mMediaUri.getPath());
                    index = (index + 1) % list.size();
                    String path = list.get(index);
                    //~~~~~~~~~~~~
                    uri = UUtils.getMediaUri(path);
                    String relativePath = path.replace(mMediaInfo.devicePath, "");
                    Cursor cursor = mContext.getContentResolver().query(MediaStore.MediaBase.CONTENT_URI, null,
                            MediaStore.MediaBase.FIELD_RELATIVE_PATH + "=?", new String[]{relativePath}, null);
                    MediaInfo mediaInfo;
                    if (cursor.moveToFirst()) {
                        String name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_NAME));
                        String title = cursor.getString(cursor.getColumnIndex(MediaStore.MediaInfo.FIELD_TITLE));
                        String poster = cursor.getString(cursor.getColumnIndex(MediaStore.MediaInfo.FIELD_POSTER));
                        long mediaId = cursor.getLong(cursor.getColumnIndex("_id"));
                        mediaInfo = new MediaInfo(mediaId, path, name, title, poster, mMediaInfo.deviceId, mMediaInfo.path);
                    } else {
                        String name = new File(path).getName();
                        mediaInfo = new MediaInfo(-1, path, name, null, null, mMediaInfo.deviceId, mMediaInfo.path);
                    }
                    cursor.close();
                    if (mediaInfo != null) {
                        mMediaInfo = mediaInfo;
                        uri = UUtils.getMediaUri(path);
                        mPlayer.resetVideo();
                        mHandler.sendMessage(mHandler.obtainMessage(FINAL_PLAY, uri));
                    }
                }
                break;

        }
    }

    private ArrayList<MediaInfo> getAllVideoList(long deviceId, String devicePath) {
        ArrayList<MediaInfo> array = null;
        Cursor cursor = mContext.getContentResolver().query(MediaStore.MediaBase.CONTENT_URI, null,
                MediaStore.MediaBase.FIELD_DEVICE_ID + "=? AND " + MediaStore.MediaBase.FIELD_HIDE + "=?", new String[]{deviceId + "", "0"}, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                array = new ArrayList<MediaInfo>();
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_NAME));
                    String relativePath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaBase.FIELD_RELATIVE_PATH));
                    long _id = cursor.getLong(cursor.getColumnIndex("_id"));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.MediaInfo.FIELD_TITLE));
                    String poster = cursor.getString(cursor.getColumnIndex(MediaStore.MediaInfo.FIELD_POSTER));
                    String path = devicePath + relativePath;
                    array.add(new MediaInfo(_id, path, name, title, poster, deviceId, devicePath));
                }
            }
            cursor.close();
        }
        return array;
    }

    private ArrayList<String> getQueueVideoList(File dir) {
        System.out.println(dir);
        File[] files = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                FileCategory fileCategory = UUtils.getFileCategory(file);
                return fileCategory == FileCategory.BDMV || fileCategory == FileCategory.Video;
            }
        });
        if (files != null && files.length > 0) {
            ArrayList<String> list = new ArrayList<String>();
            for (File f : files) {
                list.add(f.getAbsolutePath());
            }
            return list;
        }
        return null;
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }

    public boolean isPause() {
        if (mPlayer != null) {
            return mPlayer.isInPlaybackState() && !mPlayer.isPlaying();
        }
        return false;
    }

    public boolean onHandlerKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        boolean uniqueDown = event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0;
        if (uniqueDown) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                    || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                System.out.println("mControllerManager" + mControllerManager);
                mControllerManager.show(PlayerSeekController.SEEK_CONTROLLER);
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                if (isPlaying()) {
//                    if (mSeekController != null) {
//                        mSeekController.executePause();
//                    }
                } else {
//                    if (mSeekController != null) {
//                        mSeekController.executePlay();
//                    }
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                mControllerManager.show(PlayerMenuView.TAG);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean handlerKeyEvent(KeyEvent event) {
        boolean uniqueDown = event.getRepeatCount() == 0 && event.getAction() == KeyEvent.ACTION_DOWN;
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (uniqueDown) {
                mControllerManager.hide();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onHandlerTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public void setPlayer(IPlayer player) {
        super.setPlayer(player);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnTimedTextChangedListener(this);
    }

    @Override
    public void changArguments(Bundle args) {
        if (init(getArguments())) {
            initController();
        } else {
            getActivity().finish();
        }
    }


    @Override
    public boolean onInfo(IPlayer mp, int what, int extra, Bundle b) {
        if (what == IPlayer.MEDIA_INFO_TIMEOUT) {
            String uri = b.getString("uri");
            int seek = b.getInt("seek");
            int count = b.getInt("count");
            if (count <= 1) {
                if (mPlayer != null) {
                    mPlayer.setDecodeType(mDecodeType);
                    mPlayer.setVideoPath(Uri.parse(uri), null);
                    mPlayer.start();
                    if (seek > 0) {
                        mPlayer.seekTo(seek);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private PopupWindow mSubTitleWindow = null;
    private WindowManager.LayoutParams mSubTitlesLocation = null;

    private PopupWindow getAndMakeSubTitleWindow() {
        if (mSubTitleWindow == null) {
            TextView tv = new TextView(mContext);
            tv.setTextColor(Color.WHITE);
            tv.getPaint().setFakeBoldText(true);
            tv.setTextSize(com.vst.dev.common.util.Utils.getFitSize(mContext, 25));
            tv.setShadowLayer(10, 0, 0, Color.BLACK);
            tv.setGravity(Gravity.CENTER);
            mSubTitleWindow = new PopupWindow(tv);
            mSubTitleWindow.setFocusable(false);
            mSubTitleWindow.setWindowLayoutMode(-1, -2);
            mSubTitleWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return mSubTitleWindow;
    }

    @Override
    public void onTimedTextChanger(String text, long stat, long end) {
        mSubTitleWindow = getAndMakeSubTitleWindow();
        if (text != null) {
            TextView tv = (TextView) mSubTitleWindow.getContentView();
            tv.setText(text);
            if (!mSubTitleWindow.isShowing()) {
                if (mSubTitlesLocation == null) {
                    mSubTitlesLocation = new WindowManager.LayoutParams();
                    mSubTitlesLocation.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    mSubTitlesLocation.y = com.vst.dev.common.util.Utils.getFitSize(mContext, 30);
                }
                if (!isDetached()) {
                    mSubTitleWindow.showAtLocation((View) mPlayer, mSubTitlesLocation.gravity, 0,
                            mSubTitlesLocation.y);
                }
            }
        } else {
            if (mSubTitleWindow != null && mSubTitleWindow.isShowing()) {
                mSubTitleWindow.dismiss();
            }
        }
    }
}
