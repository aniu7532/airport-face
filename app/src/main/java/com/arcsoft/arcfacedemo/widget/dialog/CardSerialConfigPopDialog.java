package com.arcsoft.arcfacedemo.widget.dialog;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.ui.activity.LivenessDetectJinActivity;
import com.arcsoft.arcfacedemo.ui.activity.LivenessDetectYuanActivity;
import com.arcsoft.arcfacedemo.ui.activity.LivenessDetectYuanAndJinActivity;
import com.arcsoft.arcfacedemo.ui.activity.LoginActivity;
import com.arcsoft.arcfacedemo.ui.activity.RegisterAndRecognizeActivity;
import com.arcsoft.arcfacedemo.util.CardSerialConfigUtil;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lxj.xpopup.core.CenterPopupView;
import com.lxj.xpopup.util.XPopupUtils;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

/**
 * 读卡器串口配置弹窗，保存串口路径与波特率后重启相关页面使配置生效。
 */
public class CardSerialConfigPopDialog extends CenterPopupView {

    public CardSerialConfigPopDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_card_serial;
    }

    /** 初始化表单、加载当前配置并处理保存逻辑。 */
    @Override
    protected void onCreate() {
        super.onCreate();
        EditText etPath = findViewById(R.id.etSerialPath);
        EditText etBaud = findViewById(R.id.etBaudRate);

        etPath.setText(CardSerialConfigUtil.getCardSerialPath());
        etBaud.setText(String.valueOf(CardSerialConfigUtil.getCardSerialBaudRate()));

        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnConfirm = findViewById(R.id.btn_confirm);

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = etPath.getText() == null ? "" : etPath.getText().toString().trim();
                String baudStr = etBaud.getText() == null ? "" : etBaud.getText().toString().trim();
                if (ObjectUtils.isEmpty(path)) {
                    ToastUtils.showShort("请输入串口路径");
                    return;
                }
                if (ObjectUtils.isEmpty(baudStr)) {
                    ToastUtils.showShort("请输入波特率");
                    return;
                }
                int baud;
                try {
                    baud = Integer.parseInt(baudStr);
                } catch (NumberFormatException e) {
                    ToastUtils.showShort("波特率格式不正确");
                    return;
                }
                if (baud <= 0) {
                    ToastUtils.showShort("波特率必须大于0");
                    return;
                }
                CardSerialConfigUtil.saveConfig(path, baud);
                ToastUtils.showShort("保存成功，正在重启页面以生效");

                // 仿照修改查验模式的逻辑，重启到登录页并关闭相关页面
                if (!(ActivityUtils.getTopActivity() instanceof LoginActivity)) {
                    ActivityUtils.startActivity(LoginActivity.class);

                    ActivityUtils.finishOtherActivities(LivenessDetectJinActivity.class);
                    ActivityUtils.finishOtherActivities(LivenessDetectYuanActivity.class);
                    ActivityUtils.finishOtherActivities(LivenessDetectYuanAndJinActivity.class);
                    ActivityUtils.finishOtherActivities(RegisterAndRecognizeActivity.class);
                }

                dismiss();
            }
        });
    }

    @Override
    protected int getMaxHeight() {
        return 0;
    }

    @Override
    protected int getMaxWidth() {
        return (int) (XPopupUtils.getAppWidth(getContext()) * 0.8f);
    }
}

