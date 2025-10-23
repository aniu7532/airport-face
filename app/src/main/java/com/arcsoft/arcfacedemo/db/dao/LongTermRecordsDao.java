package com.arcsoft.arcfacedemo.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.arcsoft.arcfacedemo.db.entity.LongTermRecords;

import java.util.List;

@Dao
public interface LongTermRecordsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LongTermRecords entity);

    // 根据 ID 查询数据
    @Query("SELECT * FROM long_term_records WHERE id = :id")
    LongTermRecords getById(String id);

    // 根据 ID 查询数据
    @Query("SELECT * FROM long_term_records")
    List<LongTermRecords> getAll();

    @Query("SELECT * FROM long_term_records ORDER BY checkTime DESC  LIMIT 1")
    LongTermRecords getByLast();


    /**
    * 根据 item 删除
    **/
    @Delete
    void delete(LongTermRecords item);

}
