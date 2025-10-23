//package com.arcsoft.arcfacedemo.db;
//
//import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;
//
//import com.arcsoft.arcfacedemo.util.log.ALog;
//
//import org.greenrobot.greendao.database.Database;
//
///**
// * Created by Administrator on 2017/11/29.
// */
//
//public class DBHelper extends DaoMaster.OpenHelper {
//
//    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
//        super(context, name, factory);
//    }
//
//    @Override
//    public void onUpgrade(Database db, int oldVersion, int newVersion) {
//        super.onUpgrade(db, oldVersion, newVersion);
//        ALog.i(oldVersion + "---先前和更新之后的版本---" + newVersion);
////        if (oldVersion < newVersion) {
////            Log.i("version", oldVersion + "---先前和更新之后的版本---" + newVersion);
////            MigrationHelper.migrate(db, MsgInfoDao.class);
////            //更改过的实体类(新增的不用加)   更新UserDao文件 可以添加多个  XXDao.class 文件
//////             MigrationHelper.getInstance().migrate(db, UserDao.class,XXDao.class);
////        }
//        if (oldVersion < newVersion) {
//            MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {
//                @Override
//                public void onCreateAllTables(Database db, boolean ifNotExists) {
//                    DaoMaster.createAllTables(db, ifNotExists);
//                }
//
//                @Override
//                public void onDropAllTables(Database db, boolean ifExists) {
//                    DaoMaster.dropAllTables(db, ifExists);
//                }
//            }, LongTermPassDao.class, LongTermRecordsDao.class, TemporaryCardRecordsDao.class, FaceEntityDao.class);
//        }
//
//    }
//}