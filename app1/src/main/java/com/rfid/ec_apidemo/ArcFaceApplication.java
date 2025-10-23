package com.rfid.ec_apidemo;

import android.app.Application;

import com.blankj.utilcode.util.Utils;
import com.rfid.ec_apidemo.log.ALog;

import java.io.File;

public class ArcFaceApplication extends Application {
    private static ArcFaceApplication application;
    public static final String TAG = "YCJC";
    private String wlyCacheDir;

    public String getWlyCacheDir() {
        return wlyCacheDir;
    }

    public static ArcFaceApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        File logFile = this.getExternalFilesDir("log");
        if (!logFile.exists()) {
            logFile.mkdir();
        }
        wlyCacheDir = logFile.getAbsolutePath();
        Utils.init(this);
        // Toasty.Config.getInstance().allowQueue(false).setTextSize(24).apply();
        ALog.getConfig().setLogSwitch(BuildConfig.DEBUG)// 设置 log 总开关，包括输出到控制台和文件，默认开
                .setConsoleSwitch(BuildConfig.DEBUG)// 设置是否输出到控制台开关，默认开
                .setGlobalTag(TAG) // 设置 log 全局标签，默认为空，当全局标签不为空时，我们输出的 log 全部为该 tag， 为空时，如果传入的 tag
                // 为空那就显示类名，否则显示 tag
                .setLogHeadSwitch(true) // 设置 log 头信息开关，默认为开
                .setLog2FileSwitch(true) // 打印 log 时是否存到文件的开关，默认关
                .setFilePrefix(TAG) // 当文件前缀为空时，默认为 "alog"，即写入文件为 "alog-MM-dd.txt"
                .setDir(getWlyCacheDir())// 当自定义路径为空时，写入应用的 /cache/log/ 目录中
                .setBorderSwitch(false) // 输出日志是否带边框开关，默认开
                .setSingleTagSwitch(true) // 一条日志仅输出一条，默认开，为美化 AS 3.1 的 Logcat
                .setConsoleFilter(ALog.V) // log 的控制台过滤器，和 logcat 过滤器同理，默认 Verbose
                .setFileFilter(ALog.D).setSaveDays(2); // log 文件过滤器，和 logcat 过滤器同理，默认 Verbose


    }


}
