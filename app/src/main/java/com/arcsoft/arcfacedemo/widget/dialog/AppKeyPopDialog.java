package com.arcsoft.arcfacedemo.widget.dialog;

import com.arcsoft.arcfacedemo.R;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lxj.xpopup.core.CenterPopupView;
import com.lxj.xpopup.util.XPopupUtils;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

public class AppKeyPopDialog extends CenterPopupView {

    public static String Appid = "GmRzWwTgM27MoXy2LbJKwcCcD4c29WnsJrwxRwhdUEoD";
    public static String Sdkkey = "GHQDrynWFYxCZbBrbcTG6zAowgxpdpGvUW9VkxbEGM4C";
    public static String Activecode = "005B-11W5-23Z8-BUUL";

    public AppKeyPopDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_key;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        EditText etAppid = findViewById(R.id.etAppid);
        EditText etSdkkey = findViewById(R.id.etSdkkey);
        EditText etActivecode = findViewById(R.id.etActivecode);
        etAppid.setText(SPUtils.getInstance().getString("Appid", Appid));
        etSdkkey.setText(SPUtils.getInstance().getString("Sdkkey", Sdkkey));
        etActivecode.setText(SPUtils.getInstance().getString("Activecode", Activecode));

        Button btn_cancel = findViewById(R.id.btn_cancel);
        Button btn_confirm = findViewById(R.id.btn_confirm);

        btn_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        btn_confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ObjectUtils.isEmpty(etAppid.getText())) {
                    ToastUtils.showShort("请输入Appid");
                    return;
                }
                if (ObjectUtils.isEmpty(etSdkkey.getText())) {
                    ToastUtils.showShort("请输入Sdkkey");
                    return;
                }
                if (ObjectUtils.isEmpty(etActivecode.getText())) {
                    ToastUtils.showShort("请输入Activecode");
                    return;
                }
                SPUtils.getInstance().put("Appid", etAppid.getText().toString());
                SPUtils.getInstance().put("Sdkkey", etSdkkey.getText().toString());
                SPUtils.getInstance().put("Activecode", etActivecode.getText().toString());
                dismiss();
            }
        });

    }

    // 自适应， 最大高度为Window的0.85
    @Override
    protected int getMaxHeight() {
        return 0;
    }

    @Override
    protected int getMaxWidth() {
        return (int) (XPopupUtils.getAppWidth(getContext()) * 0.8f);
    }
}
