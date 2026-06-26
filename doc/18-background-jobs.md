# 后台定时任务

由 `ArcFaceApplication` 统一调度。

## 涉及类

| 类 | 职责 |
|----|------|
| `ArcFaceApplication` | 定时器注册与调度 |
| `ApiUtils` | HTTP 请求 |
| `LongPassCardsReInitUtils` | 数据完整性检查 |
| `LongPassCardsReInitUtils` | 凌晨重新初始化 |
| `LogUploadUtils` | 日志上传 |
| `ImageUploader` | 记录照片上传 |

## 任务列表

| 任务 | 触发周期 | 说明 |
|------|----------|------|
| 心跳 | 后台配置间隔 | POST `heartbeat`，上报 MAC 与间隔 |
| 通行证增量同步 | 后台配置间隔 | 对比 `passCount`，拉取新增通行证 |
| 通行记录上传 | 30 秒 | `startUpDataToServer()`，上传未同步记录 |
| 网络 Ping 检测 | 定时 | 设置 `isOffLine` 标志 |
| 数据完整性检查 | 凌晨 1:00 | `LongPassCardsReInitUtils` |
| 设备重启 | 凌晨 2:00 | 大屏 `ZysjSystemManager` / 小屏 `MyManager` |
| 日志上传 | 上午 10:00 | `LogUploadUtils.upload()` |

## 心跳

```
POST heartbeat
Body: { mac, interval }
Header: tenant-id, Authorization
```

用于后台感知设备在线状态。

## 通行证增量同步

```
1. GET passCount → 服务端总数
2. 对比本地 LongTermPass 数量
3. 若有差异 → 分页 GET URL_GetLongPass
4. 新增通行证 → 下载图片 + 注册人脸
```

## 通行记录上传

`startUpDataToServer()` 逻辑：

1. 查询 `uploaded = false` 的 `LongTermRecords` / `TemporaryCardRecords`
2. 上传现场照片（`ImageUploader` → `URL_UPLOAD_FILE`）
3. POST 创建记录（`URL_CREATE_LONG_RECORD` / `URL_CREATE_TEMP_RECORD`）
4. 标记 `uploaded = true`

## 401 自动重登

定时任务中若 API 返回 **401**：

- 清除本地 Token
- 跳转 `LoginActivity` 重新登录

## 凌晨重启

| 设备类型 | API |
|----------|-----|
| 大屏 | `ZysjSystemManager.reboot()` |
| 小屏 | `MyManager.reboot()` |

通过设备型号判断调用哪个 API。

## 日志上传

`LogUploadUtils.upload()`：

- 收集 `{externalFilesDir}/log/` 下日志文件
- 压缩上传到后台
- 上传成功后清理本地旧日志

## 离线模式

网络 Ping 失败时 `isOffLine = true`：

- 心跳、同步、上传暂停或失败静默
- 本地记录照常写入
- 网络恢复后自动继续

## 启动时机

| 时机 | 调用 |
|------|------|
| Application.onCreate | 初始化定时器框架 |
| LoginActivity 登录成功 | `startUpDataToServer()` + `startPeriodicTask()` |
