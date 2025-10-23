package com.arcsoft.arcfacedemo.receiver;

import com.arcsoft.arcfacedemo.util.WeakHandler;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.IntentUtils;
import com.blankj.utilcode.util.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * project name: AutoUpdate
 * author: Jason-Liang
 * create time: 2019/10/15 17:04
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    String mAction = null;
    Context mContext = null;
    private final WeakHandler countdownHandler = new WeakHandler();

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mAction = intent.getAction();
        ALog.e("mAction: " + mAction);
        // ActivityUtils.startActivity(LoginActivity.class);
        // AppUtils.launchApp(Utils.getApp().getPackageName());
        Intent intent1 = IntentUtils.getLaunchAppIntent("com.arcsoft.arcfacedemo");
        if (intent1 == null) {
            ALog.e("Didn't exist launcher activity.");
            return;
        }
        // intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
        // | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.putExtra("auto", true);
        Utils.getApp().startActivity(intent1);
    }
}
