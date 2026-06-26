package com.arcsoft.arcfacedemo.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.arcsoft.arcfacedemo.db.entity.LongTermRecords;

import java.util.List;

/**
 * 长期证通行记录本地数据访问接口。
 */
@Dao
public interface LongTermRecordsDao {

    /** 插入或替换单条长期证通行记录 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LongTermRecords entity);

    /** 按记录 ID 查询 */
    @Query("SELECT * FROM long_term_records WHERE id = :id")
    LongTermRecords getById(String id);

    /** 查询全部长期证通行记录 */
    @Query("SELECT * FROM long_term_records")
    List<LongTermRecords> getAll();

    /** 获取最近一条通行记录 */
    @Query("SELECT * FROM long_term_records ORDER BY checkTime DESC  LIMIT 1")
    LongTermRecords getByLast();

    /** 删除指定通行记录 */
    @Delete
    void delete(LongTermRecords item);

}
