package com.arcsoft.arcfacedemo.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogUtils {

    private static final String TAG = "LogUtils";
    private static final String LOG_DIR = "MyAppLogs"; // 日志文件夹名称
    private static final String LOG_FILE_NAME = "app_log.txt"; // 日志文件名

    /**
     * 将日志写入文件
     */
    public static void writeLogToFile(Context context, String logMessage) {
        // 检查是否有写入权限
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "没有写入外部存储的权限");
            return;
        }

        // 获取日志文件路径
        File logFile = getLogFile(context);
        if (logFile == null) {
            Log.e(TAG, "无法创建日志文件");
            return;
        }

        // 写入日志
        try (FileOutputStream fos = new FileOutputStream(logFile, true)) {
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(new Date());
            String logEntry = timeStamp + ": " + logMessage + "\n";
            fos.write(logEntry.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "写入日志失败", e);
        }
    }

    /**
     * 获取日志文件
     */
    private static File getLogFile(Context context) {
        File logDir;
        // 判断外部存储是否可用
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            logDir = new File(Environment.getExternalStorageDirectory(), LOG_DIR);
        } else {
            logDir = new File(context.getFilesDir(), LOG_DIR);
        }

        // 创建日志文件夹
        if (!logDir.exists() && !logDir.mkdirs()) {
            Log.e(TAG, "无法创建日志文件夹");
            return null;
        }

        // 创建日志文件
        File logFile = new File(logDir, LOG_FILE_NAME);
        if (!logFile.exists()) {
            try {
                if (!logFile.createNewFile()) {
                    Log.e(TAG, "无法创建日志文件");
                    return null;
                }
            } catch (IOException e) {
                Log.e(TAG, "创建日志文件失败", e);
                return null;
            }
        }

        return logFile;
    }
}
