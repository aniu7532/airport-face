package com.arcsoft.arcfacedemo.util;

/**
 * 虹软 SDK 三字段是否齐全：服务端有值才覆盖本地，缺任一则保持原激活逻辑。
 */
public final class ArcSoftKeys {

    private ArcSoftKeys() {
    }

    public static boolean isComplete(String appId, String sdkKey, String activeCode) {
        return usable(appId) && usable(sdkKey) && usable(activeCode);
    }

    public static String fromApiValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private static boolean usable(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
