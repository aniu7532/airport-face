//package com.arcsoft.arcfacedemo.db;
//
//import android.content.ContextWrapper;
//import android.database.DatabaseErrorHandler;
//import android.database.sqlite.SQLiteDatabase;
//import android.os.Environment;
//import android.text.TextUtils;
//
//import com.arcsoft.arcfacedemo.ArcFaceApplication;
//import com.arcsoft.arcfacedemo.util.log.ALog;
//
//import java.io.File;
//import java.io.IOException;
//
///**
// * @作者:TJ
// * @时间:2018-10-19-10:29
// * @描述:GreenDao3自定义数据库路径
// */
//public class GreenDaoContext extends ContextWrapper {
//
//    public GreenDaoContext() {
//        super(ArcFaceApplication.getApplication());
//    }
//
//    /**
//     * 获得数据库路径，如果不存在，则创建对象
//     *
//     * @param dbName
//     */
//    @Override
//    public File getDatabasePath(String dbName) {
//        // String dbDir = FileUtils.getDBPath();
//        // ALog.e("数据库地址：" + dbDir);
//        // File baseFile = new File(dbDir);
//        // // 目录不存在则自动创建目录
//        // if (!baseFile.exists()) {
//        // baseFile.mkdirs();
//        // }
//
//        File logFile = this.getExternalFilesDir("sqllite");
//        if (!logFile.exists()) {
//            logFile.mkdir();
//        }
//        String dbDir = logFile.getAbsolutePath();
//        ALog.e("数据库地址：" + dbDir);
//
//        File baseFile = new File(dbDir);
//        // 目录不存在则自动创建目录
//        if (!baseFile.exists()) {
//            baseFile.mkdirs();
//        }
//
//        // 如果需要建立多个库，根据用户之类建立文件夹自行处理
//        StringBuffer buffer = new StringBuffer();
//        buffer.append(baseFile.getPath());
//        dbDir = buffer.toString();
//        buffer.append(File.separator);
//        buffer.append(dbName);
//        String dbPath = buffer.toString();
//        // 判断目录是否存在，不存在则创建该目录
//        File dirFile = new File(dbDir);
//        if (!dirFile.exists()) {
//            dirFile.mkdirs();
//        }
//        // 数据库文件是否创建成功
//        boolean isFileCreateSuccess = false;
//        // 判断文件是否存在，不存在则创建该文件
//        File dbFile = new File(dbPath);
//        if (!dbFile.exists()) {
//            try {
//                isFileCreateSuccess = dbFile.createNewFile();// 创建文件
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else {
//            isFileCreateSuccess = true;
//        }
//        // 返回数据库文件对象
//        if (isFileCreateSuccess) {
//            return dbFile;
//        } else {
//            return super.getDatabasePath(dbName);
//        }
//    }
//
//    /**
//     * 重载这个方法，是用来打开SD卡上的数据库的，android 2.3及以下会调用这个方法。
//     *
//     * @param name
//     * @param mode
//     * @param factory
//     */
//    @Override
//    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
//        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
//        return result;
//    }
//
//    /**
//     * Android 4.0会调用此方法获取数据库。
//     *
//     * @param name
//     * @param mode
//     * @param factory
//     * @param errorHandler
//     * @see ContextWrapper#openOrCreateDatabase(String, int,
//     * SQLiteDatabase.CursorFactory,
//     * DatabaseErrorHandler)
//     */
//    @Override
//    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory,
//                                               DatabaseErrorHandler errorHandler) {
//        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), factory);
//        return result;
//    }
//
//    public static class FileUtils {
//
//        public static String getSDPath() {
//            boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
//            if (sdCardExist) {
//                return Environment.getExternalStorageDirectory().toString();
//            } else {
//                return "";
//            }
//        }
//
//        /**
//         * 数据库文件的路径
//         *
//         * @return
//         */
//        public static String getDBPath() {
//            String sdCardPath = getSDPath();
//            if (TextUtils.isEmpty(sdCardPath)) {
//                return "";
//            } else {
//                return sdCardPath + File.separator + ArcFaceApplication.TAG + File.separator + "sqllite";
//            }
//
//            // Android 10.0+
//            // File logFile = App.getInstance().getExternalFilesDir("sqllite");
//            // if (!logFile.exists()) {
//            // logFile.mkdir();
//            // }
//            //
//            // return logFile.getPath();
//        }
//    }
//
//}
