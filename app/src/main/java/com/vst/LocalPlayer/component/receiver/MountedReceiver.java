package com.vst.LocalPlayer.component.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.vst.LocalPlayer.UUtils;
import com.vst.LocalPlayer.component.provider.MediaStore;
import com.vst.LocalPlayer.component.provider.MediaStoreHelper;
import com.vst.LocalPlayer.component.service.MyIntentService;

import java.io.File;

public class MountedReceiver extends BroadcastReceiver {
    public MountedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            String path = new File(intent.getData().getPath()).getAbsolutePath();
            Log.d(getClass().getSimpleName(), "ACTION_MEDIA_MOUNTED  path=" + path);
            String uuid;
            boolean isNewDevice = true;
            ContentResolver cr = context.getContentResolver();
            if ((uuid = UUtils.readDeviceUUID(path)) != null) {
                isNewDevice = !MediaStoreHelper.deviceInStore(cr, uuid);
            }
            if (isNewDevice) {
                //new device
                //Scanner
                uuid = UUtils.writeDeviceUUID(path);
                Uri uri = MediaStoreHelper.addNewMediaDevice(cr, uuid, path);
                Log.d(getClass().getSimpleName(), "ACTION_MEDIA_MOUNTED new device path=" + path + " ,id=" + ContentUris.parseId(uri));
                MyIntentService.startActionScanner(context, path, ContentUris.parseId(uri));
            } else {
                //old device update
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaDevice.FIELD_DEVICE_PATH, path);
                cr.update(MediaStore.MediaDevice.CONTENT_URI, values, MediaStore.MediaDevice.FIELD_DEVICE_UUID + "=?", new String[]{uuid});
                MyIntentService.startActionUpdateValid(context);
            }
        } else if (Intent.ACTION_MEDIA_REMOVED.equals(action)) {
            Uri uri = intent.getData();
            String path = new File(uri.getPath()).getAbsolutePath();
            Log.d(getClass().getSimpleName(), "ACTION_MEDIA_REMOVED  path=" + path);
            MyIntentService.startActionUpdateValid(context);
        }
    }
}
