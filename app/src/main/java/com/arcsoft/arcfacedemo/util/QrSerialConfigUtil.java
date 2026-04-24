package com.arcsoft.arcfacedemo.util;

import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.SPUtils;

/**
 * 二维码串口配置工具类：路径、波特率、数据位、停止位、校验位
 */
public class QrSerialConfigUtil {

    private static final String KEY_QR_SERIAL_PATH = "qr_serial_path";
    private static final String KEY_QR_SERIAL_BAUD = "qr_serial_baud";
    private static final String KEY_QR_SERIAL_DATABITS = "qr_serial_data_bits";
    private static final String KEY_QR_SERIAL_STOPBITS = "qr_serial_stop_bits";
    private static final String KEY_QR_SERIAL_PARITY = "qr_serial_parity";

    private static final String DEFAULT_PATH = "/dev/ttyS4";
    private static final int DEFAULT_BAUD = 115200;
    private static final int DEFAULT_DATA_BITS = 7;
    private static final int DEFAULT_STOP_BITS = 1;
    private static final int DEFAULT_PARITY = 2;

    public static String getDevicePath() {
        try {
            String path = SPUtils.getInstance().getString(KEY_QR_SERIAL_PATH, DEFAULT_PATH);
            if (path == null || path.trim().length() == 0) {
                return DEFAULT_PATH;
            }
            return path;
        } catch (Exception e) {
            ALog.e("getQrSerialPath error: " + e.getMessage());
            return DEFAULT_PATH;
        }
    }

    public static int getBaudRate() {
        return getPositiveInt(KEY_QR_SERIAL_BAUD, DEFAULT_BAUD);
    }

    public static int getDataBits() {
        return getPositiveInt(KEY_QR_SERIAL_DATABITS, DEFAULT_DATA_BITS);
    }

    public static int getStopBits() {
        return getPositiveInt(KEY_QR_SERIAL_STOPBITS, DEFAULT_STOP_BITS);
    }

    public static int getParity() {
        return getPositiveInt(KEY_QR_SERIAL_PARITY, DEFAULT_PARITY);
    }

    public static void saveConfig(String path, int baud, int dataBits, int stopBits, int parity) {
        try {
            if (path == null || path.trim().length() == 0) {
                path = DEFAULT_PATH;
            }
            if (baud <= 0) {
                baud = DEFAULT_BAUD;
            }
            if (dataBits <= 0) {
                dataBits = DEFAULT_DATA_BITS;
            }
            if (stopBits <= 0) {
                stopBits = DEFAULT_STOP_BITS;
            }
            if (parity < 0) {
                parity = DEFAULT_PARITY;
            }
            SPUtils.getInstance().put(KEY_QR_SERIAL_PATH, path);
            SPUtils.getInstance().put(KEY_QR_SERIAL_BAUD, baud);
            SPUtils.getInstance().put(KEY_QR_SERIAL_DATABITS, dataBits);
            SPUtils.getInstance().put(KEY_QR_SERIAL_STOPBITS, stopBits);
            SPUtils.getInstance().put(KEY_QR_SERIAL_PARITY, parity);
        } catch (Exception e) {
            ALog.e("saveQrSerialConfig error: " + e.getMessage());
        }
    }

    private static int getPositiveInt(String key, int defaultValue) {
        try {
            return SPUtils.getInstance().getInt(key, defaultValue);
        } catch (Exception e) {
            ALog.e("getQrSerialConfig error: " + e.getMessage());
            return defaultValue;
        }
    }
}
