package com.vst.LocalPlayer;


import android.content.Context;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LuaUtils {


    public static String getRawString(Context ctx, int rawId) {
        WebView w ;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(ctx.getResources().openRawResource(rawId), "UTF-8"));
            StringBuffer sb = new StringBuffer();
            String buff;
            while ((buff = reader.readLine()) != null) {
                sb.append(buff);
            }
            return sb.toString();
        } catch (Exception e) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
