package com.arcsoft.arcfacedemo.util;

import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.ThreadUtils;

public abstract class SmallTask extends ThreadUtils.Task<String> {
    @Override
    public void onCancel() {
        ALog.d("ThreadUtils" + ", onCancel: " + Thread.currentThread());
    }

    @Override
    public void onFail(Throwable t) {
        ALog.e("ThreadUtils" + ", onFail: " + t.getMessage());
    }

    @Override
    public void onSuccess(String result) {
    }
}
