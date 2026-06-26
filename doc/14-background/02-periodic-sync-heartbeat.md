# startPeriodicTask 每次执行内容

## 概述

**路径**：`ArcFaceApplication.java` → `startPeriodicTask()`  
**触发**：`LoginActivity.gotoActivity()` 跳转查验页面前。

包含两条独立定时任务：**主任务**（心跳+增量同步+日Job）与 **Ping 任务**（离线检测）。

## 初始化（仅首次 startPeriodicTask）

当 `!ArcFaceApplication.TEST`：

1. `faceDao = FaceDatabase.getInstance().faceDao()`
2. 若 `FaceServer.getFaceEngine() == null` → `FaceServer.init(...)`
3. 若 `faceRepository == null` → `new FaceRepository(PAGE_SIZE, faceDao, instance)`

## 主任务调度

| 模式 | 间隔 |
|---|---|
| 正式 | `infoStorage.getInt("interval", UPDATE_DELAY_TIME)` **分钟** × 60 × 1000 ms |
| TEST | 延迟 30 秒单次（`executeByCachedWithDelay`） |

`UPDATE_DELAY_TIME` 默认 **5** 分钟。

```java
ThreadUtils.executeByFixedAtFixRate(POOL_SIZE, task, interval * 60 * 1000, MILLISECONDS);
```

## 主任务单次 doInBackground 完整顺序

```
1. 02:00 重启逻辑（SP reboot）
2. 10:00 日志上传（SP upload_log）
3. 01:00 数据完整性检查（SP reinit_check）
4. 心跳 heartbeat
5. getLongPassCardsUpdate() 增量同步通行证
6. getJavaHeapUsage()
7. getMemoryUsage()
8. cpuMonitor.getCpuUsage()
```

以下逐项说明。

---

### 1. 凌晨 2 点自动重启

```java
if (DateUtil.getHour(now) == 2) {
    boolean flag = SPUtils.getBoolean("reboot", true);
    if (flag) {
        SPUtils.put("reboot", false);
        // 大屏 ZysjSystemManager.zYRebootSys()
        // 小屏 MyManager.reboot()
        return null;  // 本轮后续步骤不执行
    }
} else {
    SPUtils.put("reboot", true);  // 非 2 点恢复标志
}
```

设备分支：`ScreenUtils.getScreenWidth() > 800` → typeDevice=1（Zysj），否则 typeDevice=2（MyManager）。

---

### 2. 上午 10 点日志上传

```java
if (hour == 10) {
    if (SPUtils.getBoolean("upload_log", true)) {
        LogUploadUtils.upload(getApplication());
        SPUtils.put("upload_log", false);
    }
} else {
    SPUtils.put("upload_log", true);
}
```

---

### 3. 凌晨 1 点数据完整性检查

```java
if (hour == 1) {
    if (SPUtils.getBoolean("reinit_check", true)) {
        LongPassCardsReInitUtils.getInstance().start();
        SPUtils.put("reinit_check", false);
    }
} else {
    SPUtils.put("reinit_check", true);
}
```

---

### 4. 设备心跳

| 项 | 值 |
|---|---|
| URL | `UrlConstants.heartbeat` |
| Method | GET |
| Params | `mac` = `DeviceUtils.getDeviceId()`，`interval` = 上面 interval 分钟值 |
| Headers | `tenant-id`、`Authorization` |

异步 `StringCallback`，成功/失败打 ALog。

---

### 5. 增量同步 getLongPassCardsUpdate()

每轮主任务开始时：

```java
updatePage = 1;
getLongPassCardsUpdate();
```

#### 参数构建

| 参数 | 来源 |
|---|---|
| `pageNo` | `updatePage`（初始 1） |
| `pageSize` | `UPDATE_PAGE_SIZE` = 20 |
| `startDate` | `db.longTermPassDao().getMaxUpdateTime()`，空则 `"2025-06-11 10:56:00"` |
| `endDate` | `DeviceUtils.getCurrentTime()` |
| `timestamp` | `System.currentTimeMillis()`（fetchNextPage 内追加） |

#### fetchNextPage 流程

1. `updateNext = false`（防重入）
2. GET `UrlConstants.URL_GetLongPass` 同步请求
3. code 200 且 list 非空：
   - status==2（注销）→ 删本地 register/photo 图片
   - 否则 → 下载 checkPhoto、photo
   - 下载失败 → `updateNext=true` return（下轮重试）
   - 成功 → `needFetchNext=true`
4. code 401 → 跳 `LoginActivity`（extra `auto=true`）
5. `handleUpdateComplete` → 写 DB + `updateFace` 注册人脸
6. 若 `needFetchNext`：延迟 **interval 分钟** 后 `pageNo+1` 再 `fetchNextPage`
7. 否则 `updateNext=true`

---

### 6~8. 资源监控日志

| 方法 | 输出 |
|---|---|
| `getJavaHeapUsage()` | Java 堆 Max/Used |
| `getMemoryUsage()` | 系统总/可用内存、进程 PSS |
| `cpuMonitor.getCpuUsage()` | App CPU 使用率 % |

仅写 ALog，无 UI。

---

## Ping 任务（独立）

| 常量 | 值 |
|---|---|
| `PING_DELAY_TIME` | `10 * 1000`（10 秒） |

```java
boolean result = NetworkUtils.isAvailableByPing();
isOffLine = !result;
```

- 成功 → `isOffLine = false`
- 失败 → `isOffLine = true`

供业务层读取 `ArcFaceApplication.isOffLine()`。

## 与 startUpDataToServer 关系

| 任务 | 周期 | 职责 |
|---|---|---|
| `startUpDataToServer` | 30 秒 | 离线**通行记录**上传 |
| `startPeriodicTask` 主任务 | interval 分钟 | 心跳、**通行证**增量、日 Job |
| Ping | 10 秒 | 网络可达性 |

两者并行，互不取消。

## TEST 模式差异

`ArcFaceApplication.TEST == true` 时：

- 不初始化 FaceRepository/FaceServer
- 主任务 30 秒延迟单次
- `getLongPassCardsUpdate` 内跳过图片下载/DB 写入（`if (!TEST)` 分支）

## 日志关键字

```
更新通行证任务执行中...
heartbeat成功返回 / heartbeat失败返回
通行证数量 getMaxUpdateTime:
正在下载 / 正在删除
等待5分钟后获取下一页数据...
Ping  成功 / Ping  失败
App CPU Usage:
Java Heap / Total Memory
```
