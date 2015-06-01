package com.vst.LocalPlayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import com.vst.dev.common.util.MD5Util;
import com.vst.dev.common.util.Utils;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UpgradeUtils {
    public static final String ACTION = "hahahhd.asasa.uuu";
    public static final String APKNAME = "apkName";
    public static final String MODEL = "model";
    public static final String VERSION_CODE = "versionCode";
    public static final String VERSION = "version";
    public static final String MD5 = "md5";
    public static final String INSTRUCTION = "instruction";
    public static final String URL = "url";
    public static final String APK_FILE_URI = "uri";

    public static final String UPGRADE_APK_NAME = "player.apk";

    public static Bundle getUpgradeInfoFromSp(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences("upgradInfo", Context.MODE_MULTI_PROCESS);
        int _versionCode = sp.getInt(VERSION_CODE, 0);
        int versionCode = Utils.getVersionCode(ctx);
        System.out.println(_versionCode + " ~~~~ " + versionCode);
        if (_versionCode > versionCode) {
            Bundle bundle = new Bundle();
            bundle.putInt(VERSION_CODE, _versionCode);
            String version = sp.getString(VERSION, null);
            bundle.putString(VERSION, version);
            String md5 = sp.getString(MD5, null);
            bundle.putString(MD5, md5);
            String instruction = sp.getString(INSTRUCTION, null);
            bundle.putString(INSTRUCTION, instruction);
            String url = sp.getString(URL, null);
            bundle.putString(URL, url);
            return bundle;
        } else {
            //delete file
            getApkFile(ctx).delete();
            sp.edit().clear().commit();
        }
        return null;
    }


    public static Bundle getUpgradeInfoFromNet(Context ctx) {
        int versionCode = Utils.getVersionCode(ctx);
        String channel = "91vst";
        int apkType = 1;
        try {
            channel = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(),
                    PackageManager.GET_META_DATA).metaData.getString("channel");
            apkType = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(),
                    PackageManager.GET_META_DATA).metaData.getInt("APPTYPE");
        } catch (Exception e) {
        }
        String requestUrl = "http://api.vstplay.com/api3.0/update.action?" +
                "1=1&uuid=42796C576D6544471BAAE6&channel=" +
                channel + "&apkType=" + apkType;
        String json = new String(Utils.doGet(requestUrl, null, null));
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONObject root = new JSONObject(json);
                JSONObject data = root.getJSONObject("data");
                int _versionCode = data.getInt("upversion");
                if (_versionCode > versionCode) {
                    Bundle bundle = new Bundle();
                    bundle.putInt(VERSION_CODE, _versionCode);
                    String version = data.getString("version");
                    bundle.putString(VERSION, version);
                    String md5 = data.getString("md5");
                    bundle.putString(MD5, md5);
                    String instruction = data.getString("instruction");
                    bundle.putString(INSTRUCTION, instruction);
                    String url = data.getString("url");
                    bundle.putString(URL, url);
                    saveUpgradeInfoToSp(ctx, bundle);
                    return bundle;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static void saveUpgradeInfoToSp(Context ctx, Bundle bundle) {
        SharedPreferences sp = ctx.getSharedPreferences("upgradInfo", Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = sp.edit();
        int versionCode = bundle.getInt(VERSION_CODE);
        editor.putInt(VERSION_CODE, versionCode);
        String version = bundle.getString("version");
        editor.putString(VERSION, version);
        String md5 = bundle.getString("md5");
        editor.putString(MD5, md5);
        String instruction = bundle.getString("instruction");
        editor.putString(INSTRUCTION, instruction);
        String url = bundle.getString("url");
        editor.putString(URL, url);
        editor.commit();
    }


    public static File getApkFile(Context ctx) {
        File file = ctx.getFileStreamPath(UPGRADE_APK_NAME);
        System.out.println(file.toString());
        return file;
    }

    public static void sendUpgradeBrodCast(Context ctx, Bundle b) {
        Intent i = new Intent(ACTION);
        i.putExtras(b);
        ctx.sendBroadcast(i);
    }

    public static boolean saveApkFile(Context ctx, String url, String md5) {
        File apk = getApkFile(ctx);
        HttpURLConnection connection = null;
        InputStream is = null;
        FileOutputStream os = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            is = connection.getInputStream();
            os = new FileOutputStream(apk);
            int count;
            byte[] buffer = new byte[1024];
            while ((count = is.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
            return MD5Util.getFileMD5String(apk).equalsIgnoreCase(md5);
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
        return false;
    }

}
