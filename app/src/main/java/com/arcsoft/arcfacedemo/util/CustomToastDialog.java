package com.arcsoft.arcfacedemo.util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcsoft.arcfacedemo.R;

public class CustomToastDialog extends Dialog {

    private ImageView ivIcon;
    private TextView tvMessage;

    private Drawable successIcon;
    private Drawable failureIcon;
    private String successText;
    private String failureText;

    public CustomToastDialog(Context context) {
        super(context);
        // 设置弹窗样式
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题栏
        setContentView(R.layout.dialog_custom_toast);
        setCancelable(false); // 点击外部不可取消

        // 初始化视图
        ivIcon = findViewById(R.id.iv_icon);
        tvMessage = findViewById(R.id.tv_message);

        // 设置默认图标和文字
        successIcon = context.getDrawable(R.drawable.ic_success);
        failureIcon = context.getDrawable(R.drawable.ic_failure);
        successText = "验证成功";
        failureText = "验证失败";
    }

    /**
     * 显示成功弹窗
     */
    public void showSuccess() {
        ivIcon.setImageDrawable(successIcon);
        tvMessage.setText(successText);
        show();
    }

    /**
     * 显示失败弹窗
     */
    public void showFailure() {
        ivIcon.setImageDrawable(failureIcon);
        tvMessage.setText(failureText);
        show();
    }

    public void showCustom(int icon, String message) {
        if (icon == 1) {
            ivIcon.setImageDrawable(successIcon);
        } else {
            ivIcon.setImageDrawable(failureIcon);
        }
        tvMessage.setText(message);
        show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置弹窗背景为透明
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }
}