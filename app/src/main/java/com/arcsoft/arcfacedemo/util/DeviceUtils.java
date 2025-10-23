package com.arcsoft.arcfacedemo.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Insets;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowInsets;
import android.view.WindowMetrics;

import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.TimeUtils;

import java.io.IOException;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DeviceUtils {

    public static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getMACAdress(Context context) {
        String macAddressFromWifiManager = getWifiMacAddress();
        String macAddressFromNetworkInterface = getMacAddressFromNetworkInterface();
        if (macAddressFromWifiManager == null && macAddressFromNetworkInterface == null) {
            return null;
        } else if (macAddressFromWifiManager != null) {
            return macAddressFromWifiManager;
        } else {
            return macAddressFromNetworkInterface;
        }
    }

    public static String getWifiMacAddress() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (intf.getName().equalsIgnoreCase("wlan0")) {
                    byte[] mac = intf.getHardwareAddress();
                    if (mac == null) {
                        return null;
                    }
                    StringBuilder buf = new StringBuilder();
                    for (byte aMac : mac) {
                        buf.append(String.format("%02X:", aMac));
                    }
                    if (buf.length() > 0) {
                        buf.deleteCharAt(buf.length() - 1);
                    }
                    return buf.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getMacAddressFromNetworkInterface() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (IOException e) {
            Log.e("MainActivity", "Error getting MAC address: " + e.getMessage());
        }
        return null;
    }

    public static boolean isCurrentTimeInRange(String startTime, String endTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        startTime += " 00:00:00";
        endTime += " 23:59:59";
        String cur = TimeUtils.getNowString();
        long span1 = TimeUtils.getTimeSpan(cur, startTime, format, TimeConstants.SEC);
        long span2 = TimeUtils.getTimeSpan(endTime, cur, format, TimeConstants.SEC);
        ALog.d("span1:" + span1 + ",span2:" + span2);
        if (span1 >= 0 && span2 >= 0) {
            return true;
        }
        return false;
    }


    public static String getCurrentTime() {
        // 定义日期时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        // 获取当前时间
        Date now = new Date();
        // 格式化时间
        return sdf.format(now);
    }


    /**
     * 获取设备型号（如 "Pixel 5" 或 "Galaxy S21"）
     */
    public static String getDeviceModel() {
        return Build.MODEL; // 返回设备型号
    }

    /**
     * 获取设备制造商（如 "Google" 或 "Samsung"）
     */
    public static String getDeviceManufacturer() {
        return Build.MANUFACTURER; // 返回设备制造商
    }

    /**
     * 获取完整的设备信息（制造商 + 型号）
     */
    public static String getDeviceInfo() {
        return getDeviceManufacturer() + " " + getDeviceModel();
    }

    public static int[] getScreenSize(Activity activity) {
        int[] size = new int[2];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            size[0] = windowMetrics.getBounds().width() - insets.left - insets.right;
            size[1] = windowMetrics.getBounds().height() - insets.top - insets.bottom;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            size[0] = displayMetrics.widthPixels;
            size[1] = displayMetrics.heightPixels;
        }
        return size;
    }
}
