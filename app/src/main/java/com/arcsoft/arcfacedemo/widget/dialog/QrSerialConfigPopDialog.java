package com.arcsoft.arcfacedemo.widget.dialog;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.ui.activity.LivenessDetectJinActivity;
import com.arcsoft.arcfacedemo.ui.activity.LivenessDetectYuanActivity;
import com.arcsoft.arcfacedemo.ui.activity.LivenessDetectYuanAndJinActivity;
import com.arcsoft.arcfacedemo.ui.activity.LoginActivity;
import com.arcsoft.arcfacedemo.ui.activity.RegisterAndRecognizeActivity;
import com.arcsoft.arcfacedemo.util.QrSerialConfigUtil;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lxj.xpopup.core.CenterPopupView;
import com.lxj.xpopup.util.XPopupUtils;

public class QrSerialConfigPopDialog extends CenterPopupView {

    public QrSerialConfigPopDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_qr_serial;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        EditText etPath = findViewById(R.id.etQrDevicePath);
        EditText etBaud = findViewById(R.id.etQrBaudRate);
        EditText etDataBits = findViewById(R.id.etQrDataBits);
        EditText etStopBits = findViewById(R.id.etQrStopBits);
        EditText etParity = findViewById(R.id.etQrParity);

        etPath.setText(QrSerialConfigUtil.getDevicePath());
        etBaud.setText(String.valueOf(QrSerialConfigUtil.getBaudRate()));
        etDataBits.setText(String.valueOf(QrSerialConfigUtil.getDataBits()));
        etStopBits.setText(String.valueOf(QrSerialConfigUtil.getStopBits()));
        etParity.setText(String.valueOf(QrSerialConfigUtil.getParity()));

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
                String dataBitsStr = etDataBits.getText() == null ? "" : etDataBits.getText().toString().trim();
                String stopBitsStr = etStopBits.getText() == null ? "" : etStopBits.getText().toString().trim();
                String parityStr = etParity.getText() == null ? "" : etParity.getText().toString().trim();
                if (ObjectUtils.isEmpty(path)) {
                    ToastUtils.showShort("请输入devicePath");
                    return;
                }
                if (ObjectUtils.isEmpty(baudStr) || ObjectUtils.isEmpty(dataBitsStr) || ObjectUtils.isEmpty(stopBitsStr)
                        || ObjectUtils.isEmpty(parityStr)) {
                    ToastUtils.showShort("请完整填写串口参数");
                    return;
                }
                int baud;
                int dataBits;
                int stopBits;
                int parity;
                try {
                    baud = Integer.parseInt(baudStr);
                    dataBits = Integer.parseInt(dataBitsStr);
                    stopBits = Integer.parseInt(stopBitsStr);
                    parity = Integer.parseInt(parityStr);
                } catch (NumberFormatException e) {
                    ToastUtils.showShort("参数格式不正确，请输入数字");
                    return;
                }
                if (baud <= 0 || dataBits <= 0 || stopBits <= 0 || parity < 0) {
                    ToastUtils.showShort("参数范围不正确");
                    return;
                }
                QrSerialConfigUtil.saveConfig(path, baud, dataBits, stopBits, parity);
                ToastUtils.showShort("保存成功，正在重启页面以生效");
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
