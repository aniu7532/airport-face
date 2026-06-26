package com.arcsoft.arcfacedemo.facedb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.arcsoft.arcfacedemo.facedb.dao.FaceDao;
import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;

import java.io.File;

/**
 * 本地人脸特征库 Room 数据库，存储已注册人脸的特征向量与图片路径。
 */
@Database(entities = { FaceEntity.class }, version = 1, exportSchema = false)
public abstract class FaceDatabase extends RoomDatabase {
    /** 人脸数据访问接口 */
    public abstract FaceDao faceDao();

    private static volatile FaceDatabase faceDatabase = null;

    /** 获取人脸库单例，数据库文件位于外部存储 database/faceDB.db */
    public static FaceDatabase getInstance(Context context) {
        if (faceDatabase == null) {
            synchronized (FaceDatabase.class) {
                if (faceDatabase == null) {
                    faceDatabase = Room.databaseBuilder(context, FaceDatabase.class,
                            context.getExternalFilesDir("database") + File.separator + "faceDB.db").build();
                }
            }
        }
        return faceDatabase;
    }
}
