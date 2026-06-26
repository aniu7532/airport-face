# 通行记录上传

> 上传路径有两条：**定时批量**（主路径）与 **即时上传**（保留但未启用）

---

## 上传架构概览

```
查验完成
    └── save*RecordsToDb()          ← 写入 Room + 加密现场图到 records/

ArcFaceApplication.startUpDataToServer()  每 30s
    └── CAS 防重入
    └── 遍历 pending 记录
            ├── 上传 sitePhoto（若本地路径）
            ├── POST create-long / create-temporary
            └── HTTP 200 → delete 本地记录

（备选）Activity.upload*Records()   ← OkGo 异步，成功即 delete
```

---

## 路径一：定时批量上传（主路径）

**实现**：`ArcFaceApplication.startUpDataToServer()`  
**间隔**：`UPLOAD_LOG_TIME = 30 * 1000` ms  
**触发**：`LoginActivity` 非首次登录且本地有通行证；`reset()` 重启

### 步骤详解

#### Step 0 — CAS 并发控制

```java
if (!isUploadingRecord.compareAndSet(false, true)) {
    return "";  // 跳过本轮
}
```

- **Compare-And-Set**：仅当 `isUploadingRecord` 为 `false` 时设为 `true` 并执行
- **finally**：无论成功失败，`isUploadingRecord.set(false)`
- **作用**：避免 30 秒周期重叠导致重复上传同一条记录

#### Step 1 — 读取待上传队列

```java
List<LongTermRecords> list2 = db.longTermRecordsDao().getAll();
List<TemporaryCardRecords> list3 = db.temporaryCardRecordsDao().getAll();
```

无排序，按 DAO 默认顺序逐条处理。

#### Step 2 — 现场照片上传（每条记录）

**条件**：`sitePhoto` 非空且以 `/` 或 `storage/` 开头（本地绝对路径）

| 步骤 | 操作 |
|------|------|
| 1 | `AESUtils.decryptFileToBitmap(item.sitePhoto)` |
| 2 | bitmap 为 null → `sitePhoto = ""`，继续 POST |
| 3 | `imageUploader.uploadBitmap2(bitmap)` → 服务端 URL |
| 4 | URL 为空 → **`continue` 跳过整条**，下轮再试 |
| 5 | 成功 → `FileUtils.delete(item.sitePhoto)`，`item.sitePhoto = imgUrl` |

#### Step 3 — POST 记录 JSON

```java
PostRequest<String> request = OkGo.post(UrlConstants.URL_CREATE_LONG_RECORD); // 或 TEMP
request.headers("tenant-id", UrlConstants.TENANT_ID);
if (ApiUtils.accessToken != null) {
    request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
}
request.upJson(GsonUtils.toJson(item));
Response<String> res = call.execute();  // 同步，在 SmallTask 后台线程
```

#### Step 4 — 删除策略

| 条件 | 行为 |
|------|------|
| `res.code() == 200` | `longTermRecordsDao().delete(item)` 或 `temporaryCardRecordsDao().delete(item)` |
| 异常或非 200 | **保留**本地记录，下轮重试 |
| 图片上传失败（`continue`） | **保留**记录与本地图片 |

**注意**：批量路径只检查 HTTP status code，**不解析** body 内 `code` 字段。

#### Step 5 — 清理过期现场图文件

扫描 `getExternalFilesDir(null)/records/`：

- `lastModified` 超过 **3 天** → 删除文件
- 与 Room 记录删除独立（可能遗留无 DB 引用的 orphan 文件，或 DB 还在但文件已删）

---

## 路径二：即时上传（Activity 内，当前未走主流程）

**实现**：`uploadLongTermRecords` / `uploadTemporaryRecords`  
**所在**：`LivenessDetect*Activity`、`RegisterAndRecognizeActivity`

### 触发条件（设计）

```java
if (true) {  // 原为 !isOffLine()
    save*RecordsToDb(...);
} else {
    uploadBitmap2 → upload*Records(...);
}
```

当前 **`if (true)` 恒成立**，即时上传不会在正常查验流程触发。

### 即时上传步骤

1. `OkGo.post(URL_CREATE_*).upJson(gson.toJson(record))`
2. `JsonCallback<Base<String>>` 异步
3. **成功**（`res.getCode() == 200`）→ 后台线程 `dao.delete(record)`
4. **onError** → `save*RecordsToDb(record, null)` 降级落库（不再保存 bitmap，避免重复加密文件）

### 与批量路径对比

| 维度 | 批量（Application） | 即时（Activity） |
|------|---------------------|----------------|
| 时机 | 每 30s | 查验后立即 |
| 方式 | 同步 execute | 异步 JsonCallback |
| 成功判断 | HTTP 200 | body.code == 200 |
| 失败 | 保留 DB | onError 写 DB（无图） |
| CAS | 有 | 无 |

---

## CAS 详解

| 项目 | 说明 |
|------|------|
| 变量 | `AtomicBoolean isUploadingRecord` |
| 初始值 | `false` |
| 获取锁 | `compareAndSet(false, true)` |
| 释放锁 | `finally { isUploadingRecord.set(false); }` |
| 跳过行为 | 上一轮上传未完成时，本轮直接 return，**不排队** |
| 线程 | `ThreadUtils.executeByFixedAtFixRate(POOL_SIZE=15, ...)` |

---

## 删除策略总结

| 场景 | Room 记录 | 本地 sitePhoto 文件 |
|------|-----------|---------------------|
| 批量上传 HTTP 200 | delete | 上传前已 delete |
| 批量上传失败 | 保留 | 保留 |
| 批量图片上传失败 | 保留（continue） | 保留 |
| 即时上传 body.code 200 | delete | 已在 saveToDb 时写入，不自动删文件 |
| 即时 onError | insert（可能重复 id REPLACE） | 无新文件 |
| 3 天过期清理 | 不影响 DB | 按 mtime 删除 |

`@Insert(onConflict = REPLACE)`：即时失败降级可能与已有记录同 id 覆盖。

---

## 相关 API

| URL 常量 | 方法 | Body |
|----------|------|------|
| `URL_CREATE_LONG_RECORD` | POST | `LongTermRecords` JSON |
| `URL_CREATE_TEMP_RECORD` | POST | `TemporaryCardRecords` JSON |
| `URL_UPLOAD_FILE` | POST | 由 `ImageUploader.uploadBitmap2` 使用 |

---

## 调用时序

```
LoginActivity (非首次)
    └── startUpDataToServer()
            └── 每 30s tick
                    ├── CAS enter
                    ├── for each LongTermRecords → 图+JSON → delete?
                    ├── for each TemporaryCardRecords → 图+JSON → delete?
                    ├── cleanup records/ > 3 days
                    └── CAS exit

（未启用）查验 Activity
    └── save*Records
            └── upload*Records → JsonCallback → delete or fallback DB
```
