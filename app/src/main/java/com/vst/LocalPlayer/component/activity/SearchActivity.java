package com.vst.LocalPlayer.component.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.FrameLayout;
import com.vst.LocalPlayer.R;

public class SearchActivity extends Activity {
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ctx = getApplication();

    }

    private void initView() {
        FrameLayout layout = new FrameLayout(ctx);
        layout.setBackgroundResource(R.drawable.main_bg);



    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
