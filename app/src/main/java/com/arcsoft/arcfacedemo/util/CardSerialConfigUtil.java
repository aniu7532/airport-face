package com.arcsoft.arcfacedemo.util;

import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.SPUtils;

/**
 * 读卡串口配置工具类：串口路径 & 波特率
 */
public class CardSerialConfigUtil {

    private static final String KEY_CARD_SERIAL_PATH = "card_serial_path";
    private static final String KEY_CARD_SERIAL_BAUD = "card_serial_baud";

    private static final String DEFAULT_PATH = "/dev/ttyS3";
    private static final int DEFAULT_BAUD = 115200;

    public static String getCardSerialPath() {
        try {
            String path = SPUtils.getInstance().getString(KEY_CARD_SERIAL_PATH, DEFAULT_PATH);
            if (path == null || path.trim().length() == 0) {
                return DEFAULT_PATH;
            }
            return path;
        } catch (Exception e) {
            ALog.e("getCardSerialPath error: " + e.getMessage());
            return DEFAULT_PATH;
        }
    }

    public static int getCardSerialBaudRate() {
        try {
            int baud = SPUtils.getInstance().getInt(KEY_CARD_SERIAL_BAUD, DEFAULT_BAUD);
            if (baud <= 0) {
                return DEFAULT_BAUD;
            }
            return baud;
        } catch (Exception e) {
            ALog.e("getCardSerialBaudRate error: " + e.getMessage());
            return DEFAULT_BAUD;
        }
    }

    public static void saveConfig(String path, int baud) {
        try {
            if (path == null || path.trim().length() == 0) {
                path = DEFAULT_PATH;
            }
            if (baud <= 0) {
                baud = DEFAULT_BAUD;
            }
            SPUtils.getInstance().put(KEY_CARD_SERIAL_PATH, path);
            SPUtils.getInstance().put(KEY_CARD_SERIAL_BAUD, baud);
        } catch (Exception e) {
            ALog.e("saveCardSerialConfig error: " + e.getMessage());
        }
    }
}

