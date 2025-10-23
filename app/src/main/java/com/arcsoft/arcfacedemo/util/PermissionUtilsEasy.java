package com.arcsoft.arcfacedemo.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import pub.devrel.easypermissions.EasyPermissions;

public class PermissionUtilsEasy {

    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
    };

    private PermissionUtilsEasy() {
        // 防止实例化
    }

    public static boolean hasPermissions(Activity activity) {
        return EasyPermissions.hasPermissions(activity, PERMISSIONS);
    }

    public static void requestPermissions(Activity activity, String rationale) {
        EasyPermissions.requestPermissions(activity, rationale, 1, PERMISSIONS);
    }

    public static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, OnPermissionResultListener listener) {
        if (requestCode == 1) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) { // 修改此处
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                if (listener != null) {
                    listener.onPermissionsGranted();
                }
            } else {
                if (listener != null) {
                    listener.onPermissionsDenied();
                }
            }
        }
    }

    public interface OnPermissionResultListener {
        void onPermissionsGranted();

        void onPermissionsDenied();
    }
}

