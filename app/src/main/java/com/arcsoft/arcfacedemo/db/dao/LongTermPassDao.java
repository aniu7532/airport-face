package com.arcsoft.arcfacedemo.db.dao;

import java.util.List;

import com.arcsoft.arcfacedemo.db.entity.LongTermPass;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * 长期/临时通行证本地数据访问接口，支持增量同步与证件查询。
 */
@Dao
public interface LongTermPassDao {
    /** 插入或替换单条通行证记录 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LongTermPass entity);

    /** 批量插入或替换通行证记录 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<LongTermPass> longTermPasses);

    /** 查询全部通行证记录 */
    @Query("SELECT * FROM long_term_pass")
    List<LongTermPass> getAll();

    /** 查询通行证总数 */
    @Query("select count(*) from long_term_pass")
    int getCount();

    /** 按通行证 ID 查询 */
    @Query("SELECT * FROM long_term_pass WHERE id = :id")
    LongTermPass getById(String id);

    /** 按实体卡号查询 */
    @Query("SELECT * FROM long_term_pass WHERE cardId = :cardId")
    LongTermPass getByCardId(String cardId);

    /** 按长卡号查询长期证（type=0） */
    @Query("SELECT * FROM long_term_pass WHERE cardIdLong = :cardIdLong AND type = 0")
    LongTermPass getBycardIdLong(String cardIdLong);

    /** 按通行申请 ID 查询 */
    @Query("SELECT * FROM long_term_pass WHERE applyId = :applyId")
    LongTermPass getByApplyId(String applyId);

    /** 按持卡人姓名查询 */
    @Query("SELECT * FROM long_term_pass WHERE nickname = :nickname")
    LongTermPass getByNickname(String nickname);

    /** 获取本地库中最新的 updateTime，用于增量同步 */
    @Query("SELECT MAX(updateTime) FROM long_term_pass WHERE updateTime IS NOT NULL;")
    String getMaxUpdateTime();

    /** 获取最近更新的一条通行证记录 */
    @Query("SELECT * FROM long_term_pass ORDER BY updateTime DESC  LIMIT 1")
    LongTermPass getByLast();

    /** 按证件类型获取最近更新的一条记录 */
    @Query("SELECT * FROM long_term_pass WHERE type=:type ORDER BY updateTime DESC  LIMIT 1")
    LongTermPass getByLastAndType(int type);

    /** 按实体卡号或申请 ID 模糊匹配查询 */
    @Query("SELECT * FROM long_term_pass WHERE cardId = :cardId OR applyId = :cardId")
    List<LongTermPass> getCardByID(String cardId);

    /** 按证件编号查询 */
    @Query("SELECT * FROM long_term_pass WHERE idCode = :idCode")
    List<LongTermPass> getAllByIdCode(String idCode);

    /** 查询未注销（status != 2）的通行证 */
    @Query("SELECT * FROM long_term_pass WHERE status != 2")
    List<LongTermPass> getByStatusNotCancelled();

    /** 按用户 ID 查询其全部通行证 */
    @Query("SELECT * FROM long_term_pass WHERE userId = :userId")
    List<LongTermPass> getByUserId(String userId);

    /** 查询用户有效证件：status=1，临时证优先，再按更新时间倒序 */
    @Query("SELECT * FROM long_term_pass WHERE userId = :userId AND status = 1 ORDER BY type DESC, updateTime DESC")
    List<LongTermPass> getActiveByUserId(String userId);

    /** 批量插入或更新通行证（冲突时替换） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdateUsers(List<LongTermPass> longTermPasses);

    /** 更新单条通行证记录 */
    @Update
    void update(LongTermPass longTermPass);

    /** 批量更新通行证记录 */
    @Update
    void updateAll(LongTermPass... entities);

    /** 删除 updateTime 大于等于指定时间的记录，用于同步回滚 */
    @Query("DELETE FROM long_term_pass WHERE updateTime >= :updateTime")
    void deleteByUpdateTime(String updateTime);

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
