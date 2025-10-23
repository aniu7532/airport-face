package com.arcsoft.arcfacedemo.widget.dialog;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.util.WeakHandler;
import com.blankj.utilcode.util.ObjectUtils;
import com.lxj.xpopup.core.CenterPopupView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomPopDialog extends CenterPopupView {

    private ImageView ivIcon;
    private TextView tvMessage;

    private Drawable successIcon;
    private Drawable failureIcon;
    private String successText;
    private String failureText;
    int icon;
    private String message;
    WeakHandler handler = new WeakHandler();

    public CustomPopDialog(Context context, int icon) {
        super(context);
        this.icon = icon;
    }

    public CustomPopDialog(Context context, int icon, String message) {
        super(context);
        this.icon = icon;
        this.message = message;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_custom_toast;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        // 初始化视图
        ivIcon = findViewById(R.id.iv_icon);
        tvMessage = findViewById(R.id.tv_message);

        // 设置默认图标和文字
        successIcon = getContext().getDrawable(R.drawable.ic_success);
        failureIcon = getContext().getDrawable(R.drawable.ic_failure);
        successText = "验证成功";
        failureText = "验证失败";

        if (icon == 1) {
            ivIcon.setImageDrawable(successIcon);
            tvMessage.setText(successText);
        } else {
            ivIcon.setImageDrawable(failureIcon);
            tvMessage.setText(failureText);
        }
        if (ObjectUtils.isNotEmpty(message)) {
            tvMessage.setText(message);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, 1000);
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
