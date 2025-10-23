package com.sz.zysx.autoupdate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * project name: AutoUpdate
 * author: Jason-Liang
 * create time: 2019/10/15 17:04
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    String mAction = null;
    Context mContext = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mAction = intent.getAction();
        Log.i(TAG, "mAction: " + mAction);
    }
}
