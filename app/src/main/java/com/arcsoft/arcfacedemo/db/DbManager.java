//package com.arcsoft.arcfacedemo.db;
//
///**
// * Created by hp on 2018/1/23.
// */
//
//import android.content.Context;
//
//public class DbManager {
//    // 是否加密
//    public static final boolean ENCRYPTED = true;
//
//    private static final String DB_NAME = "airportDb.db";
//    private static DbManager mDbManager;
//    private static DaoMaster mDaoMaster;
//    private static DaoSession mDaoSession;
//
//    private Context mContext;
//
//    private DbManager(Context context) {
//        this.mContext = context;
//        getDaoMaster(context);
//        getDaoSession(context);
//    }
//
//    /**
//     * 获取DaoMaster
//     * <p>
//     * 判断是否存在数据库，如果没有则创建数据库
//     *
//     * @param context
//     * @return
//     */
//    public static DaoMaster getDaoMaster(Context context) {
//        if (null == mDaoMaster) {
//            synchronized (DbManager.class) {
//                if (null == mDaoMaster) {
//                    DBHelper helper = new DBHelper(new GreenDaoContext(), DB_NAME, null);
//                    mDaoMaster = new DaoMaster(helper.getWritableDatabase());
//                }
//            }
//        }
//        return mDaoMaster;
//    }
//
//    /**
//     * 获取DaoSession
//     *
//     * @param context
//     * @return
//     */
//    public static DaoSession getDaoSession(Context context) {
//        if (null == mDaoSession) {
//            synchronized (DbManager.class) {
//                mDaoSession = getDaoMaster(context).newSession();
//            }
//        }
//        return mDaoSession;
//    }
//
//    public static DbManager getInstance(Context context) {
//        if (null == mDbManager) {
//            synchronized (DbManager.class) {
//                if (null == mDbManager) {
//                    mDbManager = new DbManager(context);
//                }
//            }
//        }
//        return mDbManager;
//    }
//}
