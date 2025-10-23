package com.arcsoft.arcfacedemo.db;

import com.arcsoft.arcfacedemo.db.dao.LongTermPassDao;
import com.arcsoft.arcfacedemo.db.dao.LongTermRecordsDao;
import com.arcsoft.arcfacedemo.db.dao.TemporaryCardRecordsDao;
import com.arcsoft.arcfacedemo.db.entity.LongTermPass;
import com.arcsoft.arcfacedemo.db.entity.LongTermRecords;
import com.arcsoft.arcfacedemo.db.entity.TemporaryCardRecords;
import com.arcsoft.arcfacedemo.util.Converters;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

//exportSchema = false
@Database(entities = { LongTermPass.class, LongTermRecords.class,
        TemporaryCardRecords.class }, version = 17, exportSchema = true, autoMigrations = {
                @AutoMigration(from = 10, to = 11), @AutoMigration(from = 11, to = 12),
                @AutoMigration(from = 12, to = 13), @AutoMigration(from = 13, to = 14),
                @AutoMigration(from = 14, to = 15), @AutoMigration(from = 15, to = 16),
                @AutoMigration(from = 16, to = 17)
        // , @AutoMigration(from = 1, to = 10, spec= YinchuanAirportDB.RenameColumnSpec.class // 关联自定义的 Spec )
})
@TypeConverters({ Converters.class })
public abstract class YinchuanAirportDB extends RoomDatabase {
    public abstract LongTermPassDao longTermPassDao();

    public abstract LongTermRecordsDao longTermRecordsDao();

    public abstract TemporaryCardRecordsDao temporaryCardRecordsDao();

    // // @DeleteTable(tableName)
    // // @RenameTable(fromTableName, toTableName)
    // // @DeleteColumn(tableName, columnName)
    // // @RenameColumn(tableName, fromColumnName, toColumnName)
    // // 自定义 AutoMigrationSpec 类
    // @RenameColumn(
    // tableName = "User", // 表名
    // fromColumnName = "oldName", // 数据库中的旧列名
    // toColumnName = "newName" // 实体类中的新列名
    // )
    // public static class RenameColumnSpec implements AutoMigrationSpec {
    // // 无需实现任何方法，仅通过注解声明映射关系
    // }
}
