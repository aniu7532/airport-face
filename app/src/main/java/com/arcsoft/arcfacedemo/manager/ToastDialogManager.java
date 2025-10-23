package com.arcsoft.arcfacedemo.manager;

import com.arcsoft.arcfacedemo.util.CustomToastDialog;
import com.arcsoft.arcfacedemo.util.WeakHandler;

import android.content.Context;

public class ToastDialogManager {

    private static CustomToastDialog dialog;

    /**
     * 显示成功弹窗
     */
    public static void showSuccess(Context context) {
        dismiss(); // 关闭之前的弹窗
        dialog = new CustomToastDialog(context);
        dialog.showSuccess();
        new WeakHandler().postDelayed(() -> ToastDialogManager.dismiss(), 1000); // 1秒后关闭
    }

    /**
     * 显示失败弹窗
     */
    public static void showFailure(Context context) {
        dismiss(); // 关闭之前的弹窗
        dialog = new CustomToastDialog(context);
        dialog.showFailure();
        new WeakHandler().postDelayed(() -> ToastDialogManager.dismiss(), 1000); // 1秒后关闭
    }

    /**
     * 关闭弹窗
     */
    public static void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }

    /**
     * 显示自定义弹窗
     *
     * @param context
     * @param icon
     * @param message
     */
    public static void showCustomDialog(Context context, int icon, String message) {
        dismiss(); // 关闭之前的弹窗
        dialog = new CustomToastDialog(context);
        dialog.showCustom(icon, message);
        new WeakHandler().postDelayed(() -> ToastDialogManager.dismiss(), 1000); // 1秒后关闭
    }
}
