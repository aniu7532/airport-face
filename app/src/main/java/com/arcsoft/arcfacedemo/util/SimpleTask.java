package com.arcsoft.arcfacedemo.util;

import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.ThreadUtils;

/**
 * 简单后台任务基类，封装 ThreadUtils 任务的取消、失败与成功回调日志。
 */
public abstract class SimpleTask extends ThreadUtils.Task<String> {

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
