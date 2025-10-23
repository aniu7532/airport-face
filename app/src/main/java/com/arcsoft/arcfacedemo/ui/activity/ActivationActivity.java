package com.arcsoft.arcfacedemo.ui.activity;

import java.io.File;
import java.util.Properties;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.databinding.ActivityActivationBinding;
import com.arcsoft.arcfacedemo.ui.viewmodel.ActiveViewModel;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.ErrorCodeUtil;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.arcsoft.arcfacedemo.widget.dialog.AppKeyPopDialog;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.model.ActiveDeviceInfo;
import com.blankj.utilcode.util.SPUtils;
import com.google.android.material.snackbar.Snackbar;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

/**
 * 激活界面
 */
public class ActivationActivity extends BaseActivity {

    private static final int DIRECT_SHOW_CHAR_COUNT = 20;

    /**
     * 离线激活所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS_OFFLINE =
            new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE };
    /**
     * 读取本地配置文件激活的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS_ACTIVE_FROM_CONFIG_FILE =
            new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE };
    /**
     * 获取设备信息的所需的权限信息
     */
    private static final String[] NEEDED_PERMISSIONS_GET_DEVICE_INFO =
            new String[] { Manifest.permission.READ_PHONE_STATE };

    private static final int ACTION_REQUEST_ACTIVE_OFFLINE = 1;
    private static final int ACTION_REQUEST_ACTIVE_ONLINE = 2;
    private static final int ACTION_REQUEST_COPY_DEVICE_FINGER = 3;
    private static final int ACTION_REQUEST_ACTIVE_FROM_CONFIG_FILE = 4;

    private ActivityActivationBinding binding;
    private ActiveViewModel activeViewModel;
    private Snackbar snackbar;
    private static String DEFAULT_AUTH_FILE_PATH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_activation);

        initData();
        initViewModel();
        initView();
    }

    private void initView() {
        enableBackIfActionBarExists();
    }

    private void initViewModel() {
        activeViewModel = new ViewModelProvider(getViewModelStore(),
                new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(ActiveViewModel.class);
        activeViewModel.getActiveResult().observe(this, result -> {
            if (snackbar != null) {
                snackbar.dismiss();
                snackbar = null;
            }
            String notice;
            switch (result) {
            case ErrorInfo.MOK:
                SPUtils.getInstance().put("Appid", binding.getAppId());
                SPUtils.getInstance().put("Sdkkey", binding.getSdkKey());
                SPUtils.getInstance().put("Activecode", binding.getActiveKey());
                notice = getString(R.string.active_success);
                break;
            case ErrorInfo.MERR_ASF_ALREADY_ACTIVATED:
                notice = getString(R.string.already_activated);
                break;
            case ErrorInfo.MERR_ASF_ACTIVEKEY_ACTIVEKEY_ACTIVATED:
                notice = getString(R.string.active_key_activated);
                break;
            default:
                notice = getString(R.string.active_failed, result, ErrorCodeUtil.arcFaceErrorCodeToFieldName(result));
                break;
            }
            showLongSnackBar(binding.getRoot(), notice);
            // ConfigUtil.commitAppId(getApplicationContext(), binding.getAppId());
            // ConfigUtil.commitSdkKey(getApplicationContext(), binding.getSdkKey());
            // ConfigUtil.commitActiveKey(getApplicationContext(), binding.getActiveKey());

            // ConfigUtil.commitAppId(getApplicationContext(), "HaGKp5ySJwy3a5nA18VEn9Gh7qZVkbSe8TfGKBNG7JEw");
            // ConfigUtil.commitSdkKey(getApplicationContext(), "7aNcrgANFo1Zr5sVRbDjVwrH8Mqs1mFLAYPR7tirbAhq");
            // ConfigUtil.commitActiveKey(getApplicationContext(), "U571-11VB-V13R-B8Q7");

            // ConfigUtil.commitAppId(getApplicationContext(), "5N7kKLzxPyu4nDTU9XwBnWd3Zg1PATTwBCM7r8aEaDtq");
            // ConfigUtil.commitSdkKey(getApplicationContext(), "HmJFBpgZCtzHciLov1dQDnkwwfbNdXGjp96QdPoeaKq8");
            // ConfigUtil.commitActiveKey(getApplicationContext(), "U571-11V8-R11D-58J7");

            ConfigUtil.commitAppId(getApplicationContext(),
                    SPUtils.getInstance().getString("Appid", AppKeyPopDialog.Appid));
            ConfigUtil.commitSdkKey(getApplicationContext(),
                    SPUtils.getInstance().getString("Sdkkey", AppKeyPopDialog.Sdkkey));
            ConfigUtil.commitActiveKey(getApplicationContext(),
                    SPUtils.getInstance().getString("Activecode", AppKeyPopDialog.Activecode));

        });
    }

    private void initData() {
        DEFAULT_AUTH_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + getString(R.string.active_file_name);
        // binding.setAppId(ConfigUtil.getAppId(this));
        // binding.setSdkKey(ConfigUtil.getSdkKey(this));
        // binding.setActiveKey(ConfigUtil.getActiveKey(this));

        // binding.setAppId("HaGKp5ySJwy3a5nA18VEn9Gh7qZVkbSe8TfGKBNG7JEw");
        // binding.setSdkKey("7aNcrgANFo1Zr5sVRbDjVwrH8Mqs1mFLAYPR7tirbAhq");
        // binding.setActiveKey("U571-11VB-V13R-B8Q7");

        // binding.setAppId("5N7kKLzxPyu4nDTU9XwBnWd3Zg1PATTwBCM7r8aEaDtq");
        // binding.setSdkKey("HmJFBpgZCtzHciLov1dQDnkwwfbNdXGjp96QdPoeaKq8");
        // binding.setActiveKey("U571-11V8-R11D-58J7");

        binding.setAppId(SPUtils.getInstance().getString("Appid", AppKeyPopDialog.Appid));
        binding.setSdkKey(SPUtils.getInstance().getString("Sdkkey", AppKeyPopDialog.Sdkkey));
        binding.setActiveKey(SPUtils.getInstance().getString("Activecode", AppKeyPopDialog.Activecode));

    }

    public void activeOnline(View view) {
        if (checkPermissions(NEEDED_PERMISSIONS_GET_DEVICE_INFO)) {
            snackbar = showIndefiniteSnackBar(binding.getRoot(), getString(R.string.please_wait), null, null);
            binding.setActiveKey(activeViewModel.formatActiveKey(binding.getActiveKey()));
            runOnSubThread(() -> activeViewModel.activeOnline(getApplicationContext(), binding.getActiveKey(),
                    binding.getAppId(), binding.getSdkKey()));
        } else {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS_GET_DEVICE_INFO, ACTION_REQUEST_ACTIVE_ONLINE);
        }
    }

    public void activeOffline(View view) {
        if (checkPermissions(NEEDED_PERMISSIONS_OFFLINE)) {
            snackbar = showIndefiniteSnackBar(binding.getRoot(), getString(R.string.please_wait), null, null);
            runOnSubThread(() -> activeViewModel.activeOffline(getApplicationContext(), DEFAULT_AUTH_FILE_PATH));
        } else {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS_OFFLINE, ACTION_REQUEST_ACTIVE_OFFLINE);
        }
    }

    public void copyDeviceFinger(View view) {
        if (checkPermissions(NEEDED_PERMISSIONS_GET_DEVICE_INFO)) {
            ActiveDeviceInfo activeDeviceInfo = new ActiveDeviceInfo();
            int code = FaceEngine.getActiveDeviceInfo(this, activeDeviceInfo);
            if (code == ErrorInfo.MOK) {
                ALog.e(activeDeviceInfo.getDeviceInfo());
                StringBuilder stringBuilder = new StringBuilder();
                int length = activeDeviceInfo.getDeviceInfo().length();
                if (length > DIRECT_SHOW_CHAR_COUNT) {
                    stringBuilder.append(activeDeviceInfo.getDeviceInfo().substring(0, DIRECT_SHOW_CHAR_COUNT / 2));
                    stringBuilder.append("......");
                    stringBuilder.append(
                            activeDeviceInfo.getDeviceInfo().substring(length - DIRECT_SHOW_CHAR_COUNT / 2, length));
                } else {
                    stringBuilder.append(activeDeviceInfo.getDeviceInfo());
                }
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("finger", activeDeviceInfo.getDeviceInfo());
                clipboardManager.setPrimaryClip(clipData);
                ALog.e(stringBuilder.toString());
                showLongSnackBar(binding.getRoot(), getString(R.string.device_info_copied, stringBuilder.toString()));
            } else {
                showLongSnackBar(binding.getRoot(), getString(R.string.get_device_finger_failed, code));
            }
        } else {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS_GET_DEVICE_INFO,
                    ACTION_REQUEST_COPY_DEVICE_FINGER);
        }
    }

    @Override
    protected void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (!isAllGranted) {
            showToast(getString(R.string.permission_denied));
            return;
        }
        switch (requestCode) {
        case ACTION_REQUEST_COPY_DEVICE_FINGER:
            copyDeviceFinger(null);
            break;
        case ACTION_REQUEST_ACTIVE_OFFLINE:
            activeOffline(null);
            break;
        case ACTION_REQUEST_ACTIVE_ONLINE:
            activeOnline(null);
            break;
        case ACTION_REQUEST_ACTIVE_FROM_CONFIG_FILE:
            readLocalConfigAndActive(null);
            break;
        default:
            break;
        }
    }

    public void readLocalConfigAndActive(View view) {
        if (checkPermissions(NEEDED_PERMISSIONS_ACTIVE_FROM_CONFIG_FILE)) {
            Properties properties = activeViewModel.loadProperties();
            if (properties == null) {
                return;
            }
            String appId = properties.getProperty("APP_ID");
            String sdkKey = properties.getProperty("SDK_KEY");
            String activeKey = properties.getProperty("ACTIVE_KEY");
            ALog.e("appId:" + appId);
            ALog.e("sdkKey:" + sdkKey);
            ALog.e("activeKey:" + activeKey);
            if (appId != null && sdkKey != null && activeKey != null) {
                binding.setAppId(appId);
                binding.setSdkKey(sdkKey);
                binding.setActiveKey(activeKey);
                activeOnline(null);
            } else {
                showToast(getString(R.string.read_config_failed));
            }
        } else {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS_ACTIVE_FROM_CONFIG_FILE,
                    ACTION_REQUEST_ACTIVE_FROM_CONFIG_FILE);
        }
    }

}
