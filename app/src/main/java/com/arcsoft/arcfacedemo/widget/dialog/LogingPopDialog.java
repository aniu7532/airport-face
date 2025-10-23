package com.arcsoft.arcfacedemo.widget.dialog;

import com.arcsoft.arcfacedemo.R;
import com.lxj.xpopup.core.CenterPopupView;

import android.content.Context;

import androidx.annotation.NonNull;

public class LogingPopDialog extends CenterPopupView {

    public LogingPopDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_loging;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
    }

    // 自适应， 最大高度为Window的0.85
    @Override
    protected int getMaxHeight() {
        return 0;
    }

    @Override
    protected int getMaxWidth() {
        return 0;
    }
}
