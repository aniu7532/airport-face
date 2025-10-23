package com.arcsoft.arcfacedemo.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.arcsoft.arcfacedemo.db.entity.TemporaryCardRecords;

import java.util.List;

@Dao
public interface TemporaryCardRecordsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(TemporaryCardRecords entity);


    // 根据 ID 查询数据
    @Query("SELECT * FROM temporary_card_records")
    List<TemporaryCardRecords> getAll();


    /**
     * 根据 item 删除
     **/
    @Delete
    void delete(TemporaryCardRecords item);
}
