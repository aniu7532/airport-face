package com.arcsoft.arcfacedemo.db.dao;

import java.util.List;

import com.arcsoft.arcfacedemo.db.entity.LongTermPass;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface LongTermPassDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LongTermPass entity);

    // 批量插入数据
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LongTermPass> longTermPasses);

    // 查询所有数据
    @Query("SELECT * FROM long_term_pass")
    List<LongTermPass> getAll();

    @Query("select count(*) from long_term_pass")
    int getCount();

    // 根据 ID 查询数据
    @Query("SELECT * FROM long_term_pass WHERE id = :id")
    LongTermPass getById(String id);

    // 根据 cardId 查询数据
    @Query("SELECT * FROM long_term_pass WHERE cardId = :cardId")
    LongTermPass getByCardId(String cardId);

    // 根据 cardId 查询数据
    @Query("SELECT * FROM long_term_pass WHERE cardIdLong = :cardIdLong")
    LongTermPass getBycardIdLong(String cardIdLong);

    @Query("SELECT * FROM long_term_pass WHERE applyId = :applyId")
    LongTermPass getByApplyId(String applyId);

    @Query("SELECT * FROM long_term_pass WHERE nickname = :nickname")
    LongTermPass getByNickname(String nickname);




    // SELECT * FROM records ORDER BY timestamp DESC LIMIT 1;

    // SELECT MAX(时间列) FROM 表名;

    @Query("SELECT MAX(updateTime) FROM long_term_pass WHERE updateTime IS NOT NULL;")
    String getMaxUpdateTime();

    @Query("SELECT * FROM long_term_pass ORDER BY updateTime DESC  LIMIT 1")
    LongTermPass getByLast();

    @Query("SELECT * FROM long_term_pass WHERE type=:type ORDER BY updateTime DESC  LIMIT 1")
    LongTermPass getByLastAndType(int type);

    /**
     * @param cardId
     * @return
     */
    @Query("SELECT * FROM long_term_pass WHERE cardId = :cardId OR applyId = :cardId")
    List<LongTermPass> getCardByID(String cardId);

    @Query("SELECT * FROM long_term_pass WHERE idCode = :idCode")
    List<LongTermPass> getAllByIdCode(String idCode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateUsers(List<LongTermPass> longTermPasses);

    // 更新数据
    @Update
    void update(LongTermPass longTermPass);

    // 批量更新数据
    @Update
    void updateAll(LongTermPass... entities);

    // // 删除数据
    // @Delete
    // void delete(LongTermPass entity);
    //
    // // 批量删除数据
    // @Delete()
    // void deleteList(List<LongTermPass> longTermPassList);
    //
    // @Query("DELETE FROM long_term_pass")
    // void deleteAll();
}
