# 常见问题排查

## 登录问题

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| 零信任连接失败 | VPN 地址/账号错误 | 检查 `Constants` 中 VPN 配置 |
| 登录 401 | tenant-id 不匹配 | 确认打包渠道与后台租户一致 |
| 登录后白屏 | 通行证同步失败 | 查看日志，检查网络与 `URL_GetLongPass` |
| 反复跳回登录页 | Token 过期 | 检查 `accessToken` 是否正常刷新 |

## 人脸识别问题

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| 无法检测到人脸 | 相机 ID/分辨率配置错误 | 进入 `CameraConfigureActivity` 调整 |
| 比对始终失败 | 识别阈值过高 | 降低 `ThresholdPreference` 阈值 |
| 活体检测失败 | IR 相机偏移不对 | 调整双目水平偏移量 |
| 人脸库为空 | 同步未完成 | 重新登录触发全量同步 |
| 重复识别同一人 | 重复注册 | 运维侧边栏 → 重复人脸清理 |

## 刷卡问题

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| 刷卡无反应 | 串口路径/波特率错误 | `CardSerialConfigPopDialog` 重新配置 |
| 二维码无法识别 | 二维码串口未打开 | `QrSerialConfigPopDialog` 配置并重启 |
| 卡号匹配不到 | 本地无该通行证 | 等待增量同步或重新登录 |

## 通行记录问题

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| 记录未上传 | 离线模式 | 检查网络 Ping，恢复后自动上传 |
| 上传失败 | 照片上传接口异常 | 查看 `ImageUploader` 日志 |
| 记录重复 | 重复触发比对 | 检查识别状态机是否正常回到 idle |

## 渠道特有问题

| 渠道 | 现象 | 处理 |
|------|------|------|
| 洛阳 | 临时证无法使用 | 正常，`SUPPORTS_TEMPORARY_PASS=false` |
| 石河子/洛阳 | API 404 | 检查 `TENANT_PREFIX` 是否正确（`shf`/`fy`） |
| 重庆 | tenant-id 错误 | 应为 `3`，确认使用 `chongqing` 渠道包 |

## 运维工具

| 工具 | 入口 | 作用 |
|------|------|------|
| 数据重新初始化 | 运维侧边栏 | `LongPassCardsReInitUtils`，重建本地数据 |
| 补救措施 | 运维侧边栏 | `LongPassCardsRemedialMeasuresUtils`，补注册缺失人脸 |
| 重复人脸清理 | 运维侧边栏 | `DuplicateFaceCleanupUtils` |
| 日志上传 | 运维侧边栏 | `LogUploadUtils.upload()` |
| 识别调试 | `RecognizeDebugActivity` | 查看实时识别参数 |

## 日志位置

| 路径 | 说明 |
|------|------|
| `{externalFilesDir}/log/` | 应用运行日志（`ALog`） |
| `{externalFilesDir}/debugDump/` | 识别调试 dump |
| Logcat tag `ALog` | 实时日志 |

## 数据库排查

```bash
# 业务库
adb shell ls {externalFilesDir}/db/airportDb.db

# 人脸库
adb shell ls {externalFilesDir}/database/faceDB.db
```

可通过 `FaceManageActivity` 查看人脸库数量，通过 `RecordsPopDialog` 查看在线记录。

## 常用 ADB 命令

```bash
# 查看当前渠道
adb shell dumpsys package com.arcsoft.arcfacedemo | grep versionName

# 强制启动登录页
adb shell am start -n com.arcsoft.arcfacedemo/.ui.activity.LoginActivity

# 查看日志
adb logcat -s ALog
```

## 401 统一处理

全局 401 响应由 `ArcFaceApplication` 捕获，自动跳转 `LoginActivity`。若频繁出现：

1. 检查 `tenant-id` 是否与后台匹配
2. 检查 Token 是否过期
3. 检查网络是否能访问 `BASE_URL`
