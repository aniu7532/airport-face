# 记录同步排查

## 两类「记录」

| 类型 | 存储 | 上传机制 | 周期 |
|---|---|---|---|
| 离线通行记录 | Room：`LongTermRecords` / `TemporaryCardRecords` | `startUpDataToServer` | **30 秒** |
| 通行证主数据 | Room：`LongTermPass` | `getLongPassCardsUpdate` 增量 | **interval 分钟**（默认 5） |

本文侧重 **离线通行记录上传**；通行证同步见 background 文档。

## 离线记录产生

查验成功但网络不可用（或业务 deliberate 离线写库）时，记录写入：

- `longTermRecordsDao.insert(...)` — 长期证
- `temporaryCardRecordsDao.insert(...)` — 临时证

现场图可能为本地加密路径（`/...` 或 `storage/...`）。

## startUpDataToServer 要点

### 启动条件

- `LoginActivity`：非首次且本地已有 `LongTermPass` 列表
- **未**在首次 `getLongPassCards` 全量完成前启动

### 互斥

`AtomicBoolean isUploadingRecord`：30 秒内多 tick 只跑一轮。

### 单条上传流程

```
读 DAO getAll()
  → 若有本地 sitePhoto：
       decryptFileToBitmap
       uploadBitmap2 → 失败则 continue（保留记录）
       成功 → 删本地文件，替换为 URL
  → POST create-long / create-temp
  → HTTP 200 → delete 本地记录
  → 否则保留，下轮重试
```

## 常见现象

### 记录堆积越来越多

| 原因 | 排查 |
|---|---|
| 未启动上传任务 | 登录分支是否调用 `startUpDataToServer` |
| Token 失效 | POST 非 200，查 Authorization |
| 现场图一直上传失败 | `uploadBitmap2` 返回空 → `continue` 永不 POST |
| 网络离线 | Ping 失败，`isOffLine=true` |
| 服务端拒绝 | 响应 body code 非 200（同步只看 HTTP code） |

### 上传失败日志

```
上传长期证件日志失败返回: ...
上传临时证件日志失败返回: ...
上传图片路径: （空则跳过）
bitmap == null
```

### 「上传成功」但 App 仍显示有记录

- 代码仅 `res.code() == 200` 时 delete
- 若服务端返回 200 但 business code 失败，本地仍会删 — 反之 HTTP 非 200 不删

### 记录目录占用磁盘

`{externalFilesDir}/records/` 超过 **3 天** 的文件会被定时删除（与 DB 无关）。

## 增量通行证同步（相关）

若 **人员信息** 旧但离线记录能上传：

- 查 `getLongPassCardsUpdate` 是否 401 跳登录
- 查 `updateNext` 是否因图片下载失败卡住
- 查 `startDate = longTermPassDao.getMaxUpdateTime()`

401 处理：

```java
Intent(LoginActivity).putExtra("auto", true)
```

## 施工人员核实记录

核销列表来自 **服务端** API（`page-need-verify-no-out`），非本地 Room 离线表。

本地离线记录上传成功后，服务端才会出现在核销/通行 Tab。

## 心跳与记录无直接关系

`heartbeat` 仅上报设备在线；**不**触发记录上传。

## 重启上传任务

```java
ArcFaceApplication.getApplication().reset();      // 重启 30s 上传
ArcFaceApplication.getApplication().resetAll(); // 停止上传
```

## 核查 SQL（调试）

数据库：`{externalFilesDir}/db/airportDb.db`

关注表（命名以 DAO 为准）：

- 长期离线记录 DAO
- 临时离线记录 DAO

日志中直接打印：

```
list2.size(): N   // LongTermRecords
list3.size(): N   // TemporaryCardRecords
```

## 时间线总览

```
查验写本地记录
    ↓ (最多 30s)
startUpDataToServer
    ↓ 上传图片 + POST
服务端 create-long/temp
    ↓ 成功 delete 本地
    ↓
施工人员 Tab 可见（若 needVerify）
```

## 关联文档

- `doc/14-background/01-upload-scheduler.md` — 上传实现细节
- `doc/14-background/02-periodic-sync-heartbeat.md` — 增量同步
- `doc/08-construction/02-write-off-verify.md` — 服务端核实列表
- `doc/17-troubleshooting/01-login-network.md` — Token/401

## 日志命令

```bash
adb logcat -s YCJC | grep -iE "list2|list3|上传|SimpleTask|create-long|create-temp"
```
