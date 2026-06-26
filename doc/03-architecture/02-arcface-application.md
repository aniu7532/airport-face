# ArcFaceApplication 应用全局入口

> 源码：`app/src/main/java/com/arcsoft/arcfacedemo/ArcFaceApplication.java`

`ArcFaceApplication` 是 Application 子类，负责数据库、网络、日志、定时上传、通行证同步、人脸引擎初始化及日级维护任务。

---

## 生命周期

### `onCreate()`

| 步骤 | 说明 |
|------|------|
| 单例赋值 | `application = this` |
| 日志目录 | `getExternalFilesDir("log")` → `wlyCacheDir` |
| XUpdate | `XUpdate.get().init(this)` |
| Crash 目录 | `initCrashDumper()` 创建 `DebugInfoDumper.CRASH_LOG_DIR` |
| Utils | `Utils.init(this)`（Blankj 工具库） |
| Toasty | 全局 Toast 配置：图标着色、字号 24、不排队 |
| Bugly | `CrashReport.initCrashReport(this, "7db9a3ce0b", DEBUG)` |
| 网络 | `HttpInitUtils.init(this)` |
| ALog | 开关、Tag=`YCJC`、写文件 2 天、`setDir(wlyCacheDir)` |
| InfoStorage | `infoStorage = new InfoStorage(this)` |
| ImageUploader | `imageUploader = new ImageUploader()` |
| 清理 | `infoStorage.remove("linshiID")` |
| Room DB | 外部存储 `getExternalFilesDir("db")/airportDb.db`，`YinchuanAirportDB`，`fallbackToDestructiveMigration()` |

**不在 onCreate 中启动**：`startUpDataToServer()`、`startPeriodicTask()` 由 `LoginActivity` 登录成功后调用。

### `onLowMemory()` / `onTrimMemory(int)`

记录 `ALog.e` 日志，无额外释放逻辑。

### `onTerminate()`

`application = null`（模拟器/测试环境，真机通常不调用）。

### `getApplication()`

返回静态单例 `application`。

---

## 静态常量表

| 常量 | 值 | 说明 |
|------|-----|------|
| `TAG` | `"YCJC"` | 全局日志 Tag |
| `TEST` | `false` | 测试模式：跳过人脸同步、缩短定时间隔 |
| `READ_TIME` | `1000` | 读卡相关（毫秒） |
| `UPLOAD_LOG_TIME` | `30 * 1000`（30 秒） | 记录上传定时间隔（注释中有 10 分钟旧值） |
| `UPDATE_DELAY_TIME` | `5` | 默认通行证同步间隔（分钟） |
| `PING_DELAY_TIME` | `10 * 1000` | 网络 Ping 间隔 |
| `POOL_SIZE` | `15` | 线程池大小 |
| `UPDATE_PAGE_SIZE` | `20`（private static） | 通行证分页大小 |
| `updatePage` | `1`（private static） | 当前同步页码 |
| `updateNext` | `true`（private static） | 是否允许发起新一轮通行证同步 |
| `PAGE_SIZE` | `20`（private static） | FaceRepository 分页 |

---

## `startUpDataToServer()` — 30 秒定时上传

**触发**：`LoginActivity` 非首次启动且本地已有通行证时；`reset()` 重启。

**调度**：`ThreadUtils.executeByFixedAtFixRate(POOL_SIZE, task, UPLOAD_LOG_TIME, TimeUnit.MILLISECONDS)`

### 并发控制（CAS）

```java
private final AtomicBoolean isUploadingRecord = new AtomicBoolean(false);

if (!isUploadingRecord.compareAndSet(false, true)) {
    return "";  // 上一轮未完成，跳过
}
try {
    // 上传逻辑
} finally {
    isUploadingRecord.set(false);
}
```

### 任务体 `doInBackground` 步骤

1. **诊断日志**：打印 `longTermPassDao().getCount()`、`faceRepository.getTotalFaceCount()`
2. **上传长期记录** `longTermRecordsDao().getAll()`：
   - 若 `sitePhoto` 为本地路径（以 `/` 或 `storage/` 开头）：
     - `AESUtils.decryptFileToBitmap` 解密
     - `imageUploader.uploadBitmap2(bitmap)` 上传
     - 上传失败 → `continue` 跳过本条
     - 成功 → 删本地文件，`item.sitePhoto = imgUrl`
   - `OkGo.post(URL_CREATE_LONG_RECORD)` **同步** `call.execute()`
   - `res.code() == 200` → `longTermRecordsDao().delete(item)`
3. **上传临时记录** `temporaryCardRecordsDao().getAll()`：逻辑同上，URL 为 `URL_CREATE_TEMP_RECORD`
4. **清理旧现场照片**：`getExternalFilesDir(null)/records/` 下超过 **3 天**（`3 * 86400000L`）的文件删除

### 相关方法

| 方法 | 说明 |
|------|------|
| `reset()` | 取消当前 task → 重新 `startUpDataToServer()` |
| `resetAll()` | 仅取消 task，不重启 |

---

## `startPeriodicTask()` — 通行证同步与日级任务

**触发**：`LoginActivity` 登录流程末尾 `ArcFaceApplication.getApplication().startPeriodicTask()`

**调度**：

- 主任务：`interval * 60 * 1000` ms（`InfoStorage` 键 `"interval"`，默认 `UPDATE_DELAY_TIME=5` 分钟）
- TEST 模式：`executeByCachedWithDelay(task, 30s)`
- Ping 任务：独立 `executeByFixedAtFixRate(..., PING_DELAY_TIME=10s)`

### 初始化（非 TEST）

- `FaceDatabase.faceDao()`
- `FaceServer.init()` 若引擎未初始化
- `faceRepository = new FaceRepository(PAGE_SIZE, faceDao, instance)`

### 每次主任务 tick 执行顺序

#### 1. 凌晨 2 点 — 设备重启

| 条件 | 行为 |
|------|------|
| 当前小时 == 2 且 `SPUtils "reboot" == true` | 宽屏（`ScreenUtils.getScreenWidth() > 800`）→ `ZysjSystemManager.zYRebootSys()`；窄屏 → `MyManager.reboot()`；置 `reboot=false` |
| 非 2 点 | `SPUtils.put("reboot", true)` 重置标志 |

#### 2. 上午 10 点 — 日志上传

| 条件 | 行为 |
|------|------|
| 小时 == 10 且 `upload_log == true` | `LogUploadUtils.upload(getApplication())`；置 `upload_log=false` |
| 非 10 点 | `upload_log=true` |

#### 3. 凌晨 1 点 — 数据完整性检查

| 条件 | 行为 |
|------|------|
| 小时 == 1 且 `reinit_check == true` | `LongPassCardsReInitUtils.getInstance().start()`；置 `reinit_check=false` |
| 非 1 点 | `reinit_check=true` |

#### 4. 业务定时逻辑

- `updatePage = 1`
- **心跳**：`OkGo.get(heartbeat).params("mac", deviceId).params("interval", interval)` 异步
- **`getLongPassCardsUpdate()`** — 增量同步通行证
- **监控**：`getJavaHeapUsage()`、`getMemoryUsage()`、`cpuMonitor.getCpuUsage()`

### Ping 子任务（task1）

```java
boolean result = NetworkUtils.isAvailableByPing();
isOffLine = !result;
```

---

## `getLongPassCardsUpdate()` — 通行证增量同步

**可见性**：`private`

### 入口守卫

```java
if (!updateNext) return;
updateNext = false;
```

防止并发重复拉取；同步结束或失败时在 `fetchNextPage` 末尾恢复 `updateNext = true`。

### 增量参数

| 参数 | 来源 |
|------|------|
| `startDate` | `db.longTermPassDao().getMaxUpdateTime()`；空则默认 `"2025-06-11 10:56:00"`；TEST 固定该默认值 |
| `endDate` | `DeviceUtils.getCurrentTime()` |
| `pageNo` | 静态 `updatePage`（初始 1） |
| `pageSize` | `UPDATE_PAGE_SIZE`（20） |
| `timestamp` | `fetchNextPage` 内注入 |

### `fetchNextPage(params)` 流程

1. OkGo **同步** GET `URL_GetLongPass`
2. 解析 `ApiResponse<LongPassCards>`
3. **code == 200** 且 list 非空：
   - 遍历每条 `LongPassCard`：
     - `status == 2`（注销）→ `ImageDeleter` 删 register/photo 目录图片
     - 否则 → `ImageDownloader` 下载 checkPhoto、photo；失败则 `updateNext=true` 并 return
   - `needFetchNext = true`
4. **code == 401** → 跳转 `LoginActivity`（`auto=true`），`updateNext=true`
5. `handleUpdateComplete(list)` → `updateLocalDatabase` → Room `insertOrUpdateUsers` + `updateFace`
6. 若 `needFetchNext`：延迟 `interval` 分钟后 `pageNo+1` 再调 `fetchNextPage`；否则 `updateNext=true`

---

## `isOffLine` 离线标志

| 属性 | 说明 |
|------|------|
| 字段 | `boolean isOffLine`（包内可见，默认 false） |
| 写入 | `startPeriodicTask` 的 Ping 子任务：`NetworkUtils.isAvailableByPing()` 失败时 `isOffLine=true` |
| 读取 | `isOffLine()` public getter |
| 设计意图 | 各 Activity `save*Records` 中 `if (isOffLine())` 走本地库，否则即时上传 |
| **当前实际** | 各 Activity 中条件写死 `if (true)`，**始终先写本地**，由 `startUpDataToServer` 批量上传 |

---

## 其他重要方法

| 方法 | 说明 |
|------|------|
| `getDb()` / `setDb()` | 访问 `YinchuanAirportDB` |
| `getWlyCacheDir()` | 日志目录路径 |
| `updateLocalDatabase(list)` | `Converters.convertToLongTermPass` → `insertOrUpdateUsers` → `updateFace` |
| `updateFace(list)` | 解密注册图 → `registerFaceByBitmap` |
| `registerFace(bitmap, callback, applyId)` | ArcSoft 对齐 → RxJava 注册到 FaceRepository |
| `loadData(reload)` | 刷新 LiveData 人脸列表与总数 |
| `getFaceEntityList()` / `getTotalFaceCount()` | LiveData 供 UI 观察 |
| `isValid()` | 固定返回 `true` |
| `CpuMonitor.getCpuUsage()` | 读 `/proc/{pid}/stat` 计算 CPU 占用 |

---

## 调用链总览

```
LoginActivity.login()
    ├── startUpDataToServer()     ← 非首次且有本地通行证
    └── startPeriodicTask()       ← 登录流程末尾

startPeriodicTask (每 interval 分钟)
    ├── 日级: 1点 reinit / 2点 reboot / 10点 log upload
    ├── heartbeat
    └── getLongPassCardsUpdate → fetchNextPage → updateLocalDatabase → updateFace

startUpDataToServer (每 30 秒)
    ├── CAS isUploadingRecord
    ├── 上传 long_term_records + temporary_card_records
    └── 清理 3 天前 records/ 图片
```
