package com.arcsoft.arcfacedemo.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.arcsoft.arcfacedemo.db.entity.TemporaryCardRecords;

import java.util.List;

/**
 * 临时证通行记录本地数据访问接口。
 */
@Dao
public interface TemporaryCardRecordsDao {

    /** 插入或替换单条临时证通行记录 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TemporaryCardRecords entity);

    /** 查询全部临时证通行记录 */
    @Query("SELECT * FROM temporary_card_records")
    List<TemporaryCardRecords> getAll();

    /** 删除指定通行记录 */
    @Delete
    void delete(TemporaryCardRecords item);
}
