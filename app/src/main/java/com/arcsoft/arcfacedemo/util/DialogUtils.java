package com.arcsoft.arcfacedemo.util;

import android.content.Context;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.ConfirmPopupView;
import com.lxj.xpopup.impl.InputConfirmPopupView;
import com.lxj.xpopup.interfaces.OnCancelListener;
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;
import com.lxj.xpopup.util.XPopupUtils;

public class DialogUtils {
    static ConfirmPopupView dialog;

    public interface ConfirmListener {
        void onConfirm();
    }

    public interface CancelListener {
        void onCancel();
    }

    public interface OnInputListener {
        void onConfirm(String text);
    }

    public static ConfirmPopupView startUploadDialog(Context context, ConfirmListener confirmListener) {
        return startConfirmDialog(context, "提交确认", "确请仔细核对信息填写是否正确后，再提交数据！", "取消", "确认", confirmListener, null);
    }

    public static ConfirmPopupView startConfirmDialog(Context context, String title, String content,
            ConfirmListener confirmListener) {
        return startConfirmDialog(context, title, content, "取消", "确认", confirmListener, null);
    }

    public static ConfirmPopupView startConfirmDialog(Context context, String title, String content,
            ConfirmListener confirmListener, CancelListener cancelListener) {
        return startConfirmDialog(context, title, content, "取消", "确认", confirmListener, cancelListener);
    }

    public static ConfirmPopupView startConfirmDialog(Context context, String title, String content,
            String cancelBtnText, String confirmBtnText, ConfirmListener confirmListener) {
        return startConfirmDialog(context, title, content, cancelBtnText, confirmBtnText, confirmListener, null);
    }

    public static ConfirmPopupView startConfirmDialog(Context context, String title, String content,
            String cancelBtnText, String confirmBtnText, ConfirmListener confirmListener,
            CancelListener cancelListener) {
        if (dialog != null && dialog.isShow()) {
            dialog.dismiss();
        }
        dialog = new XPopup.Builder(context).maxWidth((int) (XPopupUtils.getAppWidth(context) * 0.8f))
                .isDestroyOnDismiss(true).animationDuration(50)
                .asConfirm(title, content, cancelBtnText, confirmBtnText, new OnConfirmListener() {
                    @Override
                    public void onConfirm() {
                        if (confirmListener != null) {
                            confirmListener.onConfirm();
                        }
                    }
                }, new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        if (cancelListener != null) {
                            cancelListener.onCancel();
                        }
                    }
                }, false);
        dialog.show();
        return dialog;
    }

    public static ConfirmPopupView startForceDialog(Context context, String title, String content, String cancelBtnText,
            String confirmBtnText, ConfirmListener confirmListener, CancelListener cancelListener) {
        if (dialog != null && dialog.isShow()) {
            dialog.dismiss();
        }
        dialog = new XPopup.Builder(context).maxWidth((int) (XPopupUtils.getAppWidth(context) * 0.8f))
                .isDestroyOnDismiss(true).dismissOnTouchOutside(false).dismissOnBackPressed(false)
                .asConfirm(title, content, cancelBtnText, confirmBtnText, new OnConfirmListener() {
                    @Override
                    public void onConfirm() {
                        if (confirmListener != null) {
                            confirmListener.onConfirm();
                        }
                    }
                }, new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        if (cancelListener != null) {
                            cancelListener.onCancel();
                        }
                    }
                }, false);
        dialog.show();
        return dialog;
    }

    public static InputConfirmPopupView startInputConfirm(Context context, String title, String content, String input,
            String hint, OnInputListener inputListener, CancelListener cancelListener) {
        InputConfirmPopupView dialog = new XPopup.Builder(context).hasStatusBarShadow(false)
                .maxWidth((int) (XPopupUtils.getAppWidth(context) * 0.8f)).isDestroyOnDismiss(true)
                .autoOpenSoftInput(true).autoFocusEditText(true) // 是否让弹窗内的EditText自动获取焦点，默认是true
                .moveUpToKeyboard(true) // 是否移动到软键盘上面，默认为true
                .asInputConfirm(title, content, input, hint, new OnInputConfirmListener() {
                    @Override
                    public void onConfirm(String text) {
                        if (inputListener != null) {
                            inputListener.onConfirm(text);
                        }
                    }
                }, new OnCancelListener() {
                    @Override
                    public void onCancel() {
                        if (cancelListener != null) {
                            cancelListener.onCancel();
                        }
                    }
                }, 0);
        dialog.show();
        return dialog;
    }

}
