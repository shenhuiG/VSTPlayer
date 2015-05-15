package com.vst.LocalPlayer;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;

public class MediaStoreNotifier {

    public interface CallBack {
        void QueryNotify(Uri uri, Cursor cursor);
    }

    private AsyncQueryHandler mAsyncQueryHandler;
    private ContentResolver mCR = null;
    private CallBack mCallback = null;
    private static final Handler mHandler = new Handler(Looper.getMainLooper());
    private ArrayList<QueryArg> mArgs = new ArrayList<QueryArg>();
    private static final long DELAY = 500;

    public MediaStoreNotifier(ContentResolver cr, CallBack callback) {
        if (cr == null || callback == null) {
            throw new IllegalArgumentException("the params in not null!");
        }
        mCR = cr;
        mCallback = callback;
        mAsyncQueryHandler = new AsyncQueryHandler(mCR) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                super.onQueryComplete(token, cookie, cursor);
                if (!mArgs.isEmpty() && token < mArgs.size()) {
                    QueryArg arg = mArgs.get(token);
                    if (mCallback != null) {
                        mCallback.QueryNotify(arg.uri, cursor);
                    }
                    if (!cursor.isClosed()) {
                        cursor.close();
                    }
                }
            }
        };
    }

    public void registQueryContentUri(Uri uri, String[] projection, String selection, String[] selectionArgs,
                                      String orderBy) {
        final int token = mArgs.size();
        final QueryArg arg = new QueryArg();
        arg.uri = uri;
        arg.projection = projection;
        arg.selection = selection;
        arg.selectionArgs = selectionArgs;
        arg.orderBy = orderBy;
        arg.runnable = new Runnable() {
            @Override
            public void run() {
                mAsyncQueryHandler.startQuery(token, null, arg.uri,
                        arg.projection, arg.selection, arg.selectionArgs, arg.orderBy);
            }
        };
        arg.contentObserver = new ContentObserver(mHandler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
                mHandler.removeCallbacks(arg.runnable);
                mHandler.postDelayed(arg.runnable, DELAY);
            }
        };
        mCR.registerContentObserver(uri, true, arg.contentObserver);
        mArgs.add(token, arg);
        mHandler.post(arg.runnable);
    }

    public void unregistQueryContentUri(Uri uri) {
        if (!mArgs.isEmpty()) {
            final ArrayList<QueryArg> argsCopy = (ArrayList<QueryArg>) mArgs.clone();
            for (QueryArg arg : argsCopy) {
                if (arg.uri.equals(uri)) {
                    mArgs.remove(arg);
                    mCR.unregisterContentObserver(arg.contentObserver);
                }
            }
        }
    }

    public void release() {
        mHandler.removeCallbacksAndMessages(null);
        mAsyncQueryHandler.removeCallbacksAndMessages(null);
        if (!mArgs.isEmpty()) {
            for (QueryArg arg : mArgs) {
                mCR.unregisterContentObserver(arg.contentObserver);
            }
        }
        mArgs.clear();
        mCR = null;
        mCallback = null;
        mAsyncQueryHandler = null;
    }

    protected static final class QueryArg {
        public Uri uri;
        public String[] projection;
        public String selection;
        public String[] selectionArgs;
        public String orderBy;
        public ContentObserver contentObserver;
        public Runnable runnable;
        public Object cookie;
    }
}
