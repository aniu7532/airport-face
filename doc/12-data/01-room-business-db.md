# YinchuanAirportDB — Room 业务数据库

> 源码：`app/src/main/java/com/arcsoft/arcfacedemo/db/YinchuanAirportDB.java`  
> 初始化：`ArcFaceApplication.onCreate()`

---

## 数据库概览

| 属性 | 值 |
|------|-----|
| 类名 | `YinchuanAirportDB` |
| 继承 | `RoomDatabase` |
| version | `19` |
| exportSchema | `true` |
| 物理路径 | `{ExternalFilesDir}/db/airportDb.db` |
| JournalMode | `AUTOMATIC` |
| 迁移策略 | `fallbackToDestructiveMigration()`（版本不匹配则清库重建） |
| TypeConverters | `Converters.class` |

---

## 实体表

| Entity | 表名 | 说明 |
|--------|------|------|
| `LongTermPass` | `long_term_pass` | 长期/临时通行证档案 |
| `LongTermRecords` | `long_term_records` | 长期证通行记录（待上传队列） |
| `TemporaryCardRecords` | `temporary_card_records` | 临时证通行记录（待上传队列） |

---

## AutoMigration 链

| from → to |
|-----------|
| 10 → 11 |
| 11 → 12 |
| 12 → 13 |
| 13 → 14 |
| 14 → 15 |
| 15 → 16 |
| 16 → 17 |
| 17 → 18 |
| 18 → 19 |

无自定义 `AutoMigrationSpec`（注释中有 RenameColumn 示例未启用）。

---

## 抽象 Dao 方法

```java
public abstract LongTermPassDao longTermPassDao();
public abstract LongTermRecordsDao longTermRecordsDao();
public abstract TemporaryCardRecordsDao temporaryCardRecordsDao();
```

---

## LongTermPassDao 方法列表

> 文件：`app/src/main/java/com/arcsoft/arcfacedemo/db/dao/LongTermPassDao.java`

| 方法 | SQL / 注解 | 说明 |
|------|------------|------|
| `insert(LongTermPass)` | `@Insert(REPLACE)` | 单条插入或替换 |
| `insertAll(List<LongTermPass>)` | `@Insert(REPLACE)` | 批量插入或替换 |
| `insertOrUpdateUsers(List<LongTermPass>)` | `@Insert(REPLACE)` | 同步接口数据批量写入（`ArcFaceApplication.updateLocalDatabase`） |
| `update(LongTermPass)` | `@Update` | 更新单条 |
| `updateAll(LongTermPass...)` | `@Update` | 批量更新 |
| `getAll()` | `SELECT * FROM long_term_pass` | 全部通行证 |
| `getCount()` | `select count(*) from long_term_pass` | 总数 |
| `getById(String id)` | `WHERE id = :id` | 按通行证 ID |
| `getByCardId(String cardId)` | `WHERE cardId = :cardId` | 按实体卡号 |
| `getBycardIdLong(String cardIdLong)` | `WHERE cardIdLong = :cardIdLong AND type = 0` | 长卡号查长期证 |
| `getByApplyId(String applyId)` | `WHERE applyId = :applyId` | 按申请 ID |
| `getByNickname(String nickname)` | `WHERE nickname = :nickname` | 按姓名 |
| `getMaxUpdateTime()` | `SELECT MAX(updateTime) ...` | 增量同步游标 |
| `getByLast()` | `ORDER BY updateTime DESC LIMIT 1` | 最新一条 |
| `getByLastAndType(int type)` | `WHERE type=:type ORDER BY updateTime DESC LIMIT 1` | 按类型取最新 |
| `getCardByID(String cardId)` | `WHERE cardId = :cardId OR applyId = :cardId` | 卡号或申请 ID 模糊 |
| `getAllByIdCode(String idCode)` | `WHERE idCode = :idCode` | 按证件编号 |
| `getByStatusNotCancelled()` | `WHERE status != 2` | 未注销 |
| `getByUserId(String userId)` | `WHERE userId = :userId` | 用户全部证 |
| `getActiveByUserId(String userId)` | `WHERE userId = :userId AND status = 1 ORDER BY type DESC, updateTime DESC` | 有效证件（临时证优先） |
| `deleteByUpdateTime(String updateTime)` | `DELETE WHERE updateTime >= :updateTime` | 同步回滚删除 |

注释掉的 `@Delete` / `deleteAll` 未启用。

---

## LongTermRecordsDao 方法列表

> 文件：`app/src/main/java/com/arcsoft/arcfacedemo/db/dao/LongTermRecordsDao.java`

| 方法 | SQL / 注解 | 说明 |
|------|------------|------|
| `insert(LongTermRecords)` | `@Insert(REPLACE)` | 写入待上传长期记录 |
| `getById(String id)` | `WHERE id = :id` | 按记录 ID 查询 |
| `getAll()` | `SELECT * FROM long_term_records` | 全部待上传记录（`startUpDataToServer` 遍历） |
| `getByLast()` | `ORDER BY checkTime DESC LIMIT 1` | 最近一条通行 |
| `delete(LongTermRecords)` | `@Delete` | 上传成功后删除 |

---

## TemporaryCardRecordsDao 方法列表

> 文件：`app/src/main/java/com/arcsoft/arcfacedemo/db/dao/TemporaryCardRecordsDao.java`

| 方法 | SQL / 注解 | 说明 |
|------|------------|------|
| `insert(TemporaryCardRecords)` | `@Insert(REPLACE)` | 写入待上传临时记录 |
| `getAll()` | `SELECT * FROM temporary_card_records` | 全部待上传记录 |
| `delete(TemporaryCardRecords)` | `@Delete` | 上传成功后删除 |

---

## 访问方式

```java
// Application 内
YinchuanAirportDB db = ArcFaceApplication.getApplication().getDb();

// 典型用法
db.longTermPassDao().getByCardId(cardId);
db.longTermRecordsDao().insert(record);
db.temporaryCardRecordsDao().getAll();
```

---

## 与 Converters 的关系

`Converters` 提供 Room 字段类型转换：

- `String[]` ↔ 逗号分隔字符串
- `byte[]` ↔ 逗号分隔数字字符串
- `LeadingPeople[]` ↔ JSON
- `LongPassCard` → `LongTermPass` 网络实体转换（非 `@TypeConverter`，普通静态方法）

复杂 JSON 字段（`leadingPeople`、`timeControl`）在 Entity 层另有 Gson 辅助方法。

---

## 相关数据库（非本 DB）

| 数据库 | 用途 |
|--------|------|
| `FaceDatabase` | ArcSoft 人脸特征（`FaceEntity`） |
| 外部 `records/` 目录 | 加密现场照片文件，非 Room 表 |
