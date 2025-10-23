package com.arcsoft.arcfacedemo.widget.dialog;

import com.arcsoft.arcfacedemo.R;
import com.lxj.xpopup.core.CenterPopupView;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class LoadingPopDialog extends CenterPopupView {

    String text;

    public LoadingPopDialog(@NonNull Context context, String text) {
        super(context);
        this.text = text;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.loading2;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText(text);
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
