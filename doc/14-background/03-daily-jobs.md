# 1 点 / 2 点 / 10 点 SP 标志

## 设计模式

三个日 Job 均采用**同一套「小时窗口 + SP 布尔门闩」**：

1. 当前小时 == 目标小时 **且** SP 标志为 `true` → 执行任务，随后置 `false`
2. 当前小时 != 目标小时 → 将 SP 标志重置为 `true`，供下次目标小时使用

目的：在同一小时内多次触发主定时任务时，**只执行一次**（主任务间隔默认 5 分钟，2 点内可能 tick 多次）。

**代码位置**：`ArcFaceApplication.startPeriodicTask()` → 主任务 `doInBackground`。

## 标志一览

| 时间 | SP 键 | 默认 | 任务 | 执行后 |
|---|---|---|---|---|
| 01:00 | `reinit_check` | `true` | 数据完整性检查 | `false` |
| 02:00 | `reboot` | `true` | 设备重启 | `false` + **return**（跳过后续步骤） |
| 10:00 | `upload_log` | `true` | 上传日志 | `false` |

非目标小时：`SPUtils.put(key, true)` 恢复门闩。

---

## 01:00 — reinit_check

### 触发条件

```java
DateUtil.getHour(TimeUtils.getNowDate()) == 1
&& SPUtils.getInstance().getBoolean("reinit_check", true)
```

### 执行内容

```java
LongPassCardsReInitUtils.getInstance().start();
SPUtils.getInstance().put("reinit_check", false);
```

**LongPassCardsReInitUtils**：比对本地通行证与人脸库一致性，异常时触发重新初始化（运维抽屉也可手动触发）。

### 恢复

```java
// hour != 1
SPUtils.put("reinit_check", true);
```

---

## 02:00 — reboot

### 触发条件

```java
DateUtil.getHour(...) == 2
&& SPUtils.getBoolean("reboot", true)
```

### 执行内容

1. `SPUtils.put("reboot", false)`
2. 按屏幕宽度选重启 API：
   - 宽度 > 800：`ZysjSystemManager.zYRebootSys()`
   - 否则：`MyManager.getInstance(app).reboot()`
3. **`return null`** — 本轮不再执行心跳、增量同步等

### 恢复

```java
// hour != 2
SPUtils.put("reboot", true);
```

### 注意

若 2 点整主任务未运行（应用未启动），则当日不会重启；需依赖下次启动后进入 `startPeriodicTask`。

---

## 10:00 — upload_log

### 触发条件

```java
DateUtil.getHour(...) == 10
&& SPUtils.getBoolean("upload_log", true)
```

### 执行内容

```java
LogUploadUtils.upload(getApplication());
SPUtils.put("upload_log", false);
```

上传 ALog 本地文件（`{externalFilesDir}/log/`，保留 2 天配置）到服务端，供远程排障。

### 恢复

```java
// hour != 10
SPUtils.put("upload_log", true);
```

---

## 时序示意

```
主任务 tick（每 interval 分钟）
│
├─ hour==2 && reboot?
│    YES → 重启, return
│    NO  → reboot=true
│
├─ hour==10 && upload_log?
│    YES → LogUploadUtils.upload, upload_log=false
│    NO  → upload_log=true
│
├─ hour==1 && reinit_check?
│    YES → LongPassCardsReInitUtils.start, reinit_check=false
│    NO  → reinit_check=true
│
└─ heartbeat → getLongPassCardsUpdate → 内存/CPU 日志
```

## 与手动运维工具关系

| 日 Job | 手动等价入口 |
|---|---|
| `reinit_check` | 运维抽屉「数据重新初始化」 |
| `upload_log` | 运维抽屉「上传日志」 |
| `reboot` | 无 UI（纯定时） |

## 排查

| 现象 | 检查 |
|---|---|
| 10 点未上传日志 | 10 点应用是否在跑；`upload_log` 是否已为 false 且未恢复 |
| 2 点未重启 | 同上；大屏/小屏 SDK 是否可用 |
| 1 点检查未跑 | `LongPassCardsReInitUtils` 日志；`reinit_check` 状态 |
| 同一天重复执行 | 不应发生；若 SP 被清则可能重复 |

## 相关 SP（非日 Job）

| 键 | 用途 |
|---|---|
| `interval` | 主任务间隔分钟数（默认 5） |
| `verify_feature_enabled` | 施工人员核实总开关 |
| `card_serial_path` / `qr_serial_path` | 串口配置 |

日 Job 标志均存储于默认 `SPUtils` 实例，无独立文件名。
