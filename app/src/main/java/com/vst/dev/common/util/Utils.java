package com.vst.dev.common.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import org.apache.http.Header;
import org.apache.http.NameValuePair;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class Utils {
    public static final String EXTRA = "extra";
    private static final String WIDTH = "RawWidth";
    private static final String HEIGHT = "RawHeight";
    private static final float DESIGN_WIDTH = 1280;
    private static final float DESIGN_HEIGHT = 720;
    private static final String CONFIG_DIR = "config";
    private static final String PIC_DIR = "pic";
    private static final String PIC_CACHE_DIR = "pic_cache";
    private static final String ROOT_NAME = "VST";
    private static final String TEMP_DIR = "temp";
    public static final String START_PIC = "start_pic.png";


    public static long getTotalBytes(String path) {
        StatFs statFs = new StatFs(path);
        long blockSize = statFs.getBlockSize();
        long blockCount = statFs.getBlockCount();
        return blockSize * blockCount;
    }

    public static long getAvailableBytes(String path) {
        StatFs statFs = new StatFs(path);
        long blockSize = statFs.getBlockSize();
        long blockCount = statFs.getAvailableBlocks();
        return blockSize * blockCount;
    }

    public static boolean isAutoBrightness(Context act) {
        boolean automicBrightness = false;
        ContentResolver aContentResolver = act.getContentResolver();
        try {
            automicBrightness = Settings.System.getInt(aContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Exception e) {
        }
        return automicBrightness;
    }

    // 改变亮度
    public static void setScreenLightness(Context ctx, int value) {
        if (isAutoBrightness(ctx)) {
            stopAutoBrightness(ctx);
        }
        Settings.System.putInt(ctx.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                Math.min(Math.max(value, 1), 255));
        if (ctx instanceof Activity) {
            try {
                WindowManager.LayoutParams lp = ((Activity) ctx).getWindow().getAttributes();
                lp.screenBrightness = (Math.min(Math.max(value, 30), 255)) / 255f;
                ((Activity) ctx).getWindow().setAttributes(lp);
            } catch (Exception e) {
            }
        }
    }

    // 获取亮度
    public static int getScreenLightness(Context ctx) {
        return Settings.System.getInt(ctx.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
    }

    // 停止自动亮度调节
    public static void stopAutoBrightness(Context ctx) {
        Settings.System.putInt(ctx.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    // 开启亮度自动调节
    public static void startAutoBrightness(Activity activity) {
        Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    public static CharSequence makeImageSpannable(CharSequence taget, Drawable d, int start,
                                                  int drawableWidth, int drawaleHeight, int imageSpanVerticalAlignment) {
        SpannableStringBuilder builder = new SpannableStringBuilder(taget);
        d.setBounds(0, 0, drawableWidth, drawaleHeight);
        ImageSpan imageSpan = new ImageSpan(d, imageSpanVerticalAlignment);
        builder.setSpan(imageSpan, start, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    public static CharSequence makeImageSpannable(CharSequence taget, Drawable d, int start, int end,
                                                  int drawableWidth, int drawaleHeight, int imageSpanVerticalAlignment) {
        SpannableStringBuilder builder = new SpannableStringBuilder(taget);
        d.setBounds(0, 0, drawableWidth, drawaleHeight);
        ImageSpan imageSpan = new ImageSpan(d, imageSpanVerticalAlignment);
        builder.setSpan(imageSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

    public static Drawable makeCornerRadiusDrawable(int corner, int color, int stokeColor, int stokeWidth) {
        GradientDrawable dd = new GradientDrawable();
        dd.setColor(color);
        dd.setCornerRadius(corner);
        dd.setStroke(stokeWidth, stokeColor);
        return dd;
    }

    @SuppressWarnings("resource")
    public static String stringForTime(long timeMs) {
        StringBuilder mFormatBuilder = new StringBuilder();
        int totalSeconds = (int) (timeMs / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return new Formatter(mFormatBuilder, Locale.getDefault()).format("%02d:%02d:%02d", hours,
                    minutes, seconds).toString();
        } else {
            return new Formatter(mFormatBuilder, Locale.getDefault()).format("%02d:%02d", minutes, seconds)
                    .toString();
        }
    }

    public static boolean isNetConnect(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null) {
                if (info.isConnectedOrConnecting()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void changeScreenBrightness(boolean add, Window window) {
        WindowManager.LayoutParams lp = window.getAttributes();
        if (add) {
            lp.screenBrightness += 0.1f;
            if (lp.screenBrightness > 1.0f) {
                lp.screenBrightness = 1.0f;
            }
        } else {
            lp.screenBrightness -= 0.1f;
            if (lp.screenBrightness < 0.1f) {
                lp.screenBrightness = 0.1f;
            }
        }
        window.setAttributes(lp);
    }

    public static void changeVolume(boolean add, Context ctx) {
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (add) {
            current += 1;
            if (current > max) {
                current = max;
            }
            am.setStreamVolume(AudioManager.STREAM_MUSIC, current, AudioManager.FLAG_PLAY_SOUND
                    | AudioManager.FLAG_SHOW_UI);
        } else {
            current -= 1;
            if (current < 0) {
                current = 0;
            }
            am.setStreamVolume(AudioManager.STREAM_MUSIC, current, AudioManager.FLAG_PLAY_SOUND
                    | AudioManager.FLAG_SHOW_UI);
        }
    }

    public static void changeVolume(int delta, Context ctx) {
        AudioManager am = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        int next = current + delta;
        if (next > max) {
            next = max;
        } else if (next < 0) {
            next = 0;
        }
        am.setStreamVolume(AudioManager.STREAM_MUSIC, next, AudioManager.FLAG_PLAY_SOUND
                | AudioManager.FLAG_SHOW_UI);
    }

    public static void applyFace(TextView text) {
        Typeface tf = getTypeFaTypeface(text.getContext().getApplicationContext());
        if (tf != null) {
            text.setTypeface(tf);
        }
    }

    public static void deleteFile(File file, boolean deleteDirSelf) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File childFile : files) {
                        deleteFile(childFile, true);
                    }
                }
                if (deleteDirSelf) {
                    file.delete();
                }
            }
        }
    }

    public static void saveObject(Object obj, String path) {
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(new FileOutputStream(path));
            os.writeObject(obj);
            os.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(os);
            System.out.println("saveObject");
        }
    }

    public static Object readObject(String path) {
        ObjectInputStream is = null;
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        try {
            is = new ObjectInputStream(new FileInputStream(path));
            return is.readObject();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            closeIO(is);
            System.out.println("readObject");
        }
        return null;

    }

    private static Typeface sTypeface;

    private static Typeface getTypeFaTypeface(Context ctx) {
        if (sTypeface == null) {
            synchronized (Utils.class) {
                if (sTypeface == null) {
                    try {
                        sTypeface = Typeface.createFromAsset(ctx.getApplicationContext().getAssets(),
                                "fonts/flfbls.ttf");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return sTypeface;
    }

    public static void closeIO(Closeable ioStream) {
        if (ioStream != null) {
            try {
                ioStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static SharedPreferences getSharedPreferences(Context context, String extra) {
        String name = context.getPackageName();
        if (!TextUtils.isEmpty(extra)) {
            name = name + "_" + extra;
        }
        return context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
    }

    public static int[] getDisplaySize(Context context) {
        SharedPreferences sp = getSharedPreferences(context, EXTRA);
        int width = sp.getInt(WIDTH, 0);
        int height = sp.getInt(HEIGHT, 0);
        if (width * height == 0) {
            int sdkVer = Build.VERSION.SDK_INT;
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            display.getMetrics(dm);
            width = dm.widthPixels;
            height = dm.heightPixels;
            try {
                if (sdkVer == 13) {
                    Method realHeight = display.getClass().getMethod("getRealHeight");
                    height = (Integer) realHeight.invoke(display);
                    Method realWidth = display.getClass().getMethod("getRealWidth");
                    width = (Integer) realWidth.invoke(display);
                } else if (sdkVer > 13 && sdkVer < 17) {
                    Method rawHeight = display.getClass().getMethod("getRawHeight");
                    height = (Integer) rawHeight.invoke(display);
                    Method rawWidth = display.getClass().getMethod("getRawWidth");
                    width = (Integer) rawWidth.invoke(display);
                } else if (sdkVer >= 17) { // 4.2
                    Method mt = display.getClass().getMethod("getRealSize", Point.class);
                    Point point = new Point();
                    mt.invoke(display, point);
                    width = point.x;
                    height = point.y;
                }
            } catch (Exception e) {
            }
            sp.edit().putInt(WIDTH, width).putInt(HEIGHT, height).commit();
        }
        return new int[]{width, height};
    }

    public static int getFitSize(Context ctx, int px) {
        int[] size = getDisplaySize(ctx);
        float ratio = Math.min(size[0] / DESIGN_WIDTH, size[1] / DESIGN_HEIGHT);
        if (ratio < 0.9) {
            ratio = 0.9f;
        }
        return (int) (ratio * px);
    }

    public static int getDisplayWidth(Context context) {
        return getDisplaySize(context)[0];
    }

    public static int getDisplayHeight(Context context) {
        return getDisplaySize(context)[1];
    }

    public static Drawable getLocalDrawable(Context ctx, int drawableId) {
        Drawable drawable = null;
        int[] size = getDisplaySize(ctx);
        final float ratio = Math.min(size[0] / DESIGN_WIDTH, size[1] / DESIGN_HEIGHT);
        try {
            if (ratio < 0.9f) {
                drawable = ctx.getResources().getDrawableForDensity(drawableId,
                        DisplayMetrics.DENSITY_DEFAULT);
                if (drawable != null) {
                    drawable = zoomDrawable(drawable, ratio / 1.0f);
                }
            } else if (ratio >= 0.9f && ratio <= 1.1f) {
                drawable = ctx.getResources().getDrawableForDensity(drawableId,
                        DisplayMetrics.DENSITY_DEFAULT);
            } else if (ratio > 1.1f && ratio < 1.4f) {
                drawable = ctx.getResources().getDrawableForDensity(drawableId,
                        DisplayMetrics.DENSITY_DEFAULT);
                if (drawable != null) {
                    drawable = zoomDrawable(drawable, ratio / 1.0f);
                }
            } else if (ratio >= 1.4f && ratio <= 1.6f) {
                drawable = ctx.getResources().getDrawableForDensity(drawableId, DisplayMetrics.DENSITY_XHIGH);
            } else if (ratio > 1.6f) {
                drawable = ctx.getResources().getDrawableForDensity(drawableId, DisplayMetrics.DENSITY_XHIGH);
                if (drawable != null) {
                    drawable = zoomDrawable(drawable, ratio / 1.5f);
                }
            }
        } catch (Exception e) {
        }
        // Log.d(EXTRA,
        // "displatSize =" + size[0] + ":" + size[1] + ",ratio" + ratio +
        // " ,drawableSize= "
        // + drawable.getIntrinsicWidth() + "x" +
        // drawable.getIntrinsicHeight());
        return drawable;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public static Drawable zoomDrawable(Drawable drawable, float ratio) {
        Bitmap bmp = null;
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        bmp = drawableToBitmap(drawable);
        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);
        bmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        return new BitmapDrawable(bmp);
    }

    public static String getVesionName(Context context) {
        String versionName = null;
        try {
            String name = context.getApplicationInfo().packageName;
            versionName = context.getPackageManager().getPackageInfo(name, 0).versionName;
        } catch (NameNotFoundException e) {
            Log.e("", e.getMessage());
        }
        return versionName;
    }

    // CPU个数
    public static int getNumCores() {
        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                        return true;
                    }
                    return false;
                }
            });
            return files.length;
        } catch (Exception e) {
            return 1;
        }
    }

    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            String name = context.getApplicationInfo().packageName;
            versionCode = context.getPackageManager().getPackageInfo(name, 0).versionCode;
        } catch (NameNotFoundException e) {
            Log.e("", e.getMessage());
        }
        return versionCode;
    }

    public static boolean downLoafFileFromNet(String path, String url) {
        return downLoafFileFromNet(new File(path), url);
    }

    public static boolean downLoafFileFromNet(File file, String url) {
        boolean sucess = false;
        HttpURLConnection connection = null;
        OutputStream os = null;
        InputStream is = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            int count = -1;
            byte[] buffer = new byte[2048];
            os = new FileOutputStream(file);
            is = connection.getInputStream();
            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            sucess = true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            Utils.closeIO(os);
            Utils.closeIO(is);
        }
        return sucess;
    }

    public static boolean isGZIPInputStream(byte[] data) {
        int headerData = (int) ((data[0] << 8) | data[1] & 0xFF);
        return headerData == 0x1f8b;
    }

    public static String doGetByGZIP(String url) {
        System.out.println("doGetByGZIP url=" + url);
        HttpURLConnection connection = null;
        InputStream is = null;
        OutputStream os = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.connect();
            is = new BufferedInputStream(connection.getInputStream());
            is.mark(2);
            byte[] header = new byte[2];
            int result = is.read(header);
            is.reset();
            if (result != -1 && Utils.isGZIPInputStream(header)) {
                is = new GZIPInputStream(is);
            }
            os = new ByteArrayOutputStream();
            int count = -1;
            byte[] buffer = new byte[1024];
            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            return os.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.closeIO(is);
            Utils.closeIO(os);
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    public static byte[] doGet(String url, Header[] heads, NameValuePair[] params) {
        HttpURLConnection connection = null;
        InputStream is = null;
        ByteArrayOutputStream os = null;
        try {
            if (params != null) {
                StringBuffer sb = new StringBuffer(url);
                if (!sb.toString().endsWith("?")) {
                    sb.append('?');
                }
                for (int i = 0; i < params.length; i++) {
                    if (i > 0) {
                        sb.append("&");
                    }
                    NameValuePair pair = params[i];
                    sb.append(URLEncoder.encode(pair.getName()));
                    sb.append("=");
                    sb.append(URLEncoder.encode(pair.getValue()));
                }
                url = sb.toString();
            }
            System.out.println("doGet >" + url);
            connection = (HttpURLConnection) new URL(url).openConnection();
            //connection.setReadTimeout(3000);
            connection.setRequestMethod("GET");
            if (heads != null) {
                for (int i = 0; i < heads.length; i++) {
                    Header head = heads[i];
                    connection.setRequestProperty(head.getName(), head.getValue());
                }
            }
            //connection.setConnectTimeout(5000);
            is = connection.getInputStream();
            os = new ByteArrayOutputStream();
            int count = -1;
            byte[] buffer = new byte[1024];
            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            return os.toByteArray();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.closeIO(is);
            Utils.closeIO(os);
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new byte[0];
    }

    public static byte[] doPost(String url, Header[] heads, NameValuePair... content) {
        System.out.println("doPost >" + url);
        HttpURLConnection connection = null;
        InputStream is = null;
        ByteArrayOutputStream os = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            if (heads != null) {
                for (int i = 0; i < heads.length; i++) {
                    Header head = heads[i];
                    connection.setRequestProperty(head.getName(), head.getValue());
                }
            }
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(3000);
            connection.setDoOutput(true);// 是否输入参数
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < content.length; i++) {
                if (i > 0) {
                    sb.append("&");
                }
                NameValuePair pair = content[i];
                sb.append(URLEncoder.encode(pair.getName(), "UTF-8"));
                sb.append("=");
                sb.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
            }
            connection.getOutputStream().write(sb.toString().getBytes());
            is = connection.getInputStream();
            os = new ByteArrayOutputStream();
            int count = -1;
            byte[] buffer = new byte[1024];
            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            return os.toByteArray();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.closeIO(is);
            Utils.closeIO(os);
            if (connection != null) {
                connection.disconnect();
            }
        }
        return new byte[0];
    }

    public static void closeExecutorService(ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 获取app的名字
     *
     * @param ctx
     * @return
     */
    public static String getApplicationName(Context ctx) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = ctx.getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        String applicationName = (String) packageManager.getApplicationLabel(applicationInfo);
        if (ctx.getPackageName().equals("com.vst.itv52.v1")) {
            applicationName = "VST全聚合桌面版";
        }
        return applicationName;
    }

    /**
     * 获取umeng渠道号
     *
     * @return
     */
    public static String getUmengChannel(Context context) {
        String umengChannel = null;
        SharedPreferences sp = context.getSharedPreferences("setting", Context.MODE_MULTI_PROCESS);
        umengChannel = sp.getString("VSTChannel", null);
        if (umengChannel == null) {
            try {
                umengChannel = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                        PackageManager.GET_META_DATA).metaData.getString("UMENG_CHANNEL");
                sp.edit().putString("VSTChannel", umengChannel).commit();
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (umengChannel == null) {
            umengChannel = "91vst";
        }
        return umengChannel;
    }

    /**
     * 判断是否是手机网络，如果是手机网络则后台不进行网络请求
     *
     * @param ctx
     * @return
     */
    public static boolean isMobileNet(Context ctx) {
        ConnectivityManager manager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null) {
            return true;
        }
        if (info.getType() == ConnectivityManager.TYPE_WIFI
                || info.getType() == ConnectivityManager.TYPE_ETHERNET) {
            return false;
        }
        // 默认是true，主动关闭后将不对其进行判断
        return true;
    }

    /**
     * 获取图片文件夹，主要放首页图片，不随文件的增加自动删除
     *
     * @param context
     * @return
     */
    public static String getPicDir(Context context) {
        File dir = new File(setupFinalRootDir(context), PIC_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }

    public static String setupFinalRootDir(Context context) {
        File root = null;
        // 优先判断是否有sd card 有可能 sd 卡 目录创建失败
        if (Environment.getExternalStorageState().equals((Environment.MEDIA_MOUNTED))) {
            // String DirName = context.getPackageName();
            File sdCardDir = Environment.getExternalStorageDirectory();
            root = new File(sdCardDir, ROOT_NAME);
            if (!root.exists()) {
                root.mkdirs();
            }
            if (root.exists()) {
                modifyFile(root);
                return root.getAbsolutePath();
            }
        }
        // cache
        root = context.getCacheDir();
        if (!root.exists()) {
            root.mkdirs();
        }
        if (root.exists()) {
            modifyFile(root);
            return root.getAbsolutePath();
        }

        throw new IllegalArgumentException("您的设备不支持缓存目录的创建，会影响你的使用体验！！！");
    }

    public static String getStorageDir(Context context) {
        File root = null;
        // 优先判断是否有sd card 有可能 sd 卡 目录创建失败
        if (Environment.getExternalStorageState().equals((Environment.MEDIA_MOUNTED))) {
            File sdCardDir = Environment.getExternalStorageDirectory();
            root = new File(sdCardDir, ROOT_NAME);
            if (!root.exists()) {
                root.mkdirs();
            }
            if (root.exists()) {
                modifyFile(root);
                return root.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * 获取配置文件的文件夹，
     *
     * @param context
     * @return
     */
    public static String getConfigDir(Context context) {
        File dir = new File(setupFinalRootDir(context), CONFIG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }

    /**
     * 获取图片硬盘缓存文件夹，主要放缓存的图片，随文件的增加自动清理空间
     *
     * @param context
     * @return
     */
    public static String getPicCacheDir(Context context) {
        File dir = new File(setupFinalRootDir(context), PIC_CACHE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }

    /**
     * 获取临时文件夹，放临时文件，可以随程序退出清理掉文件
     *
     * @param context
     * @return
     */
    public static String getTempDir(Context context) {
        File dir = new File(setupFinalRootDir(context), TEMP_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }

    /**
     * 修改文件权限
     *
     * @param file
     */
    public static void modifyFile(File file) {
        modifyFile(file.getAbsolutePath());
    }

    /**
     * 修改文件权限
     *
     * @param file
     */
    public static void modifyFile(String file) {
        Process process = null;
        try {
            String command = "chmod -R 777 " + file;
            Runtime runtime = Runtime.getRuntime();
            process = runtime.exec(command);
            process.waitFor();
        } catch (Exception e) {

        }
    }

    public static void clearViewData(ViewGroup viewgroup) {
        if (viewgroup == null) {
            return;
        }
        for (int i = 0; i < viewgroup.getChildCount(); i++) {
            View child = viewgroup.getChildAt(i);
            child.setBackgroundResource(0);
            child.clearAnimation();
            child.clearFocus();
            child.setOnKeyListener(null);
            child.setOnFocusChangeListener(null);
            child.setFocusable(false);
            child.setClickable(false);
            child.setEnabled(false);
            if (child instanceof ViewGroup) {
                clearViewData((ViewGroup) child);
            } else {
                if (child instanceof ImageView) {
                    ((ImageView) child).setImageResource(0);
                }
                child = null;
            }
        }
    }

    public static boolean isAllwinnerPackage(Context ctx) {
        if (ctx.getPackageName().equals("com.vst.vod.allwinner")) {
            return true;
        }
        return false;
    }
}
