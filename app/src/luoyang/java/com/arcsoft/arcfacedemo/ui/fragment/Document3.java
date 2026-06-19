package com.arcsoft.arcfacedemo.ui.fragment;

import android.view.View;

import com.arcsoft.arcfacedemo.R;

/**
 * 洛阳渠道无临时通行证，仅占位以满足编译与历史记录入口。
 */
public class Document3 extends AbstractDocument3 {

    @Override
    protected int getLayoutResId() {
        return R.layout.document3;
    }

    @Override
    protected void bindViews(View view) {
        card_img = view.findViewById(R.id.card_img);
        or_code = view.findViewById(R.id.or_code);
        statusOverlay = view.findViewById(R.id.statusOverlay);
        statusText = view.findViewById(R.id.statusText);
    }

    @Override
    protected void bindCardContent() {
        // 洛阳渠道不支持临时证展示
    }
}
