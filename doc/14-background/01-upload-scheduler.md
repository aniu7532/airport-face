# startUpDataToServer 细节

## 概述

**路径**：`ArcFaceApplication.java` → `startUpDataToServer()`  
**触发**：登录成功后（本地已有通行证时）`LoginActivity` 调用；亦可通过 `reset()` 重启。

周期性将本地离线通行记录上传服务端，并清理过期本地文件。

## 调度参数

| 常量 | 值 | 含义 |
|---|---|---|
| `UPLOAD_LOG_TIME` | `30 * 1000`（30 秒） | 执行间隔 |
| `POOL_SIZE` | 15 | 固定线程池大小 |

```java
ThreadUtils.executeByFixedAtFixRate(POOL_SIZE, task, UPLOAD_LOG_TIME, TimeUnit.MILLISECONDS);
```

## 并发控制

```java
private final AtomicBoolean isUploadingRecord = new AtomicBoolean(false);
```

- 进入 `doInBackground`：`compareAndSet(false, true)`，失败则直接 return（跳过本轮）
- `finally`：`isUploadingRecord.set(false)`
- 防止上一轮未结束时重叠上传

## 单次执行流程

```
doInBackground()
├── [互斥] isUploadingRecord CAS
├── 日志：通行证 count、faceRepository 人脸数
├── 上传 LongTermRecords（长期证离线记录）
├── 上传 TemporaryCardRecords（临时证离线记录）
├── 清理 records/ 目录 3 天前文件
└── [finally] 释放互斥
```

## 长期证记录 LongTermRecords

**数据源**：`db.longTermRecordsDao().getAll()`

### 每条记录处理

1. **现场图上传**（若 `sitePhoto` 以 `/` 或 `storage/` 开头）：
   - `AESUtils.decryptFileToBitmap(sitePhoto)`
   - bitmap 为 null → `sitePhoto = ""`
   - 否则 `imageUploader.uploadBitmap2(bitmap)`
   - 上传 URL 为空 → `continue`（跳过本条，**不删除**）
   - 成功 → 删本地文件，`item.sitePhoto = imgUrl`

2. **POST 创建记录**：
   - URL：`UrlConstants.URL_CREATE_LONG_RECORD`（`/check/record/create-long`）
   - Headers：`tenant-id`、`Authorization`
   - Body：`GsonUtils.toJson(item)` 同步 POST

3. **成功判定**：
   - `res.code() == 200` → `db.longTermRecordsDao().delete(item)`
   - 异常或非 200 → 保留本地记录，下轮重试

## 临时证记录 TemporaryCardRecords

**数据源**：`db.temporaryCardRecordsDao().getAll()`

流程与长期证相同，区别：

| 项 | 长期证 | 临时证 |
|---|---|---|
| DAO | `longTermRecordsDao` | `temporaryCardRecordsDao` |
| POST URL | `URL_CREATE_LONG_RECORD` | `URL_CREATE_TEMP_RECORD` |

现场图逻辑完全一致：`uploadBitmap2` 失败则 `continue`。

## 本地 records 目录清理

| 项 | 值 |
|---|---|
| 目录 | `{externalFilesDir}/records/` |
| 保留时长 | 3 天（`3 * 86400000L` ms） |
| 判定 | `now - file.lastModified() > max` → `delete()` |

与 DB 记录上传独立，清理的是目录下文件列表。

## 相关方法

| 方法 | 行为 |
|---|---|
| `reset()` | `ThreadUtils.cancel(task)` + `task.cancel()` + 重新 `startUpDataToServer()` |
| `resetAll()` | 仅 cancel，不重启 |

## 启动时机（LoginActivity）

```java
// 非首次登录且本地已有通行证
ArcFaceApplication.getApplication().startUpDataToServer();
```

首次登录或本地无通行证时走 `getLongPassCards()` 全量同步，不在此启动上传（全量完成后由 `gotoActivity` 间接进入查验流程）。

## 依赖组件

| 组件 | 作用 |
|---|---|
| `ImageUploader.uploadBitmap2` | 现场图上传 OSS/文件服务 |
| `AESUtils.decryptFileToBitmap` | 解密本地加密图片 |
| `ApiUtils.accessToken` | 鉴权头 |
| `OkGo` 同步 `call.execute()` | 在后台线程阻塞上传 |

## 日志关键字

```
SimpleTask startGetDataFromServer
通行证数量 count:
list2.size() / list3.size()
上传图片路径:
上传长期证件成功返回 / 上传长期证件日志失败返回
上传临时证件日志成功返回
temp:{毫秒差} ... delete:true/false
```

## 排查要点

| 现象 | 原因 |
|---|---|
| 记录堆积 | 未调用 `startUpDataToServer`；或 token 失效 POST 失败 |
| 永远不上传 | `sitePhoto` 本地路径存在但 `uploadBitmap2` 持续失败 → `continue` |
| 上传成功仍有多条 | HTTP code 非 200 不会 delete；需确认响应码 |
| 重叠上传 | 正常被 `isUploadingRecord` 挡住，30s 内只跑一轮 |
