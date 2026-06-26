package com.arcsoft.arcfacedemo.util.debug.model;

import android.os.Build;

import androidx.annotation.NonNull;

/**
 * 调试信息中的设备与 SDK 基础信息模型。
 */
public class BasicInfo {
    private String cpu;
    private String memory;
    private String appId;
    private String sdkKey;
    private String activeKey;
    private String sdkVersion;

    public BasicInfo(String cpu, String memory, String appId, String sdkKey, String activeKey, String sdkVersion) {
        this.cpu = cpu;
        this.memory = memory;
        this.appId = appId;
        this.sdkKey = sdkKey;
        this.activeKey = activeKey;
        this.sdkVersion = sdkVersion;
    }

    @NonNull
    @Override
    public String toString() {
        return "cpu:" + cpu + "\r\n" +
                "memory:" + memory + "\r\n" +
                "appId:" + appId + "\r\n" +
                "sdkKey:" + sdkKey + "\r\n" +
                "activeKey:" + activeKey + "\r\n" +
                "sdkVersion:" + sdkVersion;
    }

    /**
     * 获取包含设备型号、Android 版本等完整基础信息字符串。
     */
    public String getBasicInfoString() {
        return this.toString() + "\r\n" +
                "androidVersion:" + Build.VERSION.RELEASE + "\r\n" +
                "brand:" + Build.BRAND + "\r\n" +
                "board:" + Build.BOARD + "\r\n" +
                "model:" + Build.MODEL + "\r\n" +
                "device:" + Build.DEVICE;
    }
}
