# 常见问题排查

## 1. 登录与网络

| 现象 | 排查步骤 |
|------|----------|
| 零信任失败 | 检查 `Constants.BASE_VPN`、账号密码；看 `SFAuthResultListener.onAuthFailed` 日志 |
| 后台登录非 200 | 确认渠道包 `TENANT_ID` 与后台一致；抓包看 `tenant-id` 头 |
| 登录后一直加载 | `getLongPassCards` 分页卡住 → 查网络、人脸注册错误、`checkAbnormalCreate` |
| 频繁跳登录 | Token 过期或 401；查 `ApiUtils.accessToken` 是否被清空 |

```bash
adb logcat -s YCJC ALog | grep -i "login\|token\|401"
```

## 2. 渠道与接口

| 渠道 | 易错点 |
|------|--------|
| 石河子 | API 必须带 `/shf/` 前缀 |
| 洛阳 | TENANT_ID 为长整型；无临时证属正常 |
| 重庆 | tenant-id 必须为 `3` |
| 银川 | 域名 `inckzqtxz` 非测试域 |

验证命令：反编译或日志打印 `UrlConstants.URL_GetLongPass` 完整 URL。

## 3. 人脸识别

| 现象 | 处理 |
|------|------|
| 无人脸框 | `CameraConfigureActivity` 检查相机 ID；权限 CAMERA |
| 活体失败 | 调低 IR 活体阈值；检查 IR 相机与 RGB 偏移 |
| 比对失败 | 调低识别阈值（默认 0.8）；确认人脸库有该用户 |
| 人脸库为空 | 重新登录全量同步；查 `longTermPassDao().getCount()` vs `faceRepository.getTotalFaceCount()` |

## 4. 刷卡/串口

| 现象 | 处理 |
|------|------|
| 无刷卡反应 | `CardSerialConfigUtil` 路径默认 `/dev/ttyS3`，按设备改 |
| 二维码无数据 | 默认 `/dev/ttyS4`；查 `SerialManage.open()` 日志 |
| 卡号有但无卡面 | 本地无该 `cardId` → 等增量同步或重登 |

## 5. 记录上传

| 现象 | 处理 |
|------|------|
| 记录堆积 | 查离线标志；`startUpDataToServer` 是否启动 |
| 上传失败 | 现场图 `uploadBitmap2` 失败会 `continue` 跳过；查 `URL_UPLOAD_FILE` |
| 上传成功仍显示多 | 成功后会 `delete` 本地记录，若未删查 HTTP code 是否真 200 |

周期：**30 秒**。

## 6. 定时任务

| 时间 | 任务 | SP 标志 |
|------|------|---------|
| 每 interval 分钟 | 心跳+增量同步 | — |
| 每 30 秒 | 记录上传 | — |
| 每 10 秒 | Ping 离线检测 | — |
| 01:00 | 数据完整性 | reinit_check |
| 02:00 | 重启 | reboot |
| 10:00 | 日志上传 | upload_log |

## 7. 运维工具

| 工具 | 场景 |
|------|------|
| 数据重新初始化 | 通行证与脸库严重不一致 |
| 补救措施 | 批量补注册失败人脸 |
| 重复人脸清理 | 同一人多次注册 |
| 上传日志 | 远程排障 |
| RecognizeDebugActivity | 识别参数现场调优 |

入口：连点进入 `CustomDrawerPopupView`。

## 8. 日志与数据路径

| 路径 | 内容 |
|------|------|
| `{externalFilesDir}/log/` | ALog 文件，保留 2 天 |
| `adb logcat -s YCJC` | 实时日志 |
| `{externalFilesDir}/db/airportDb.db` | 业务库 |
| `{externalFilesDir}/database/faceDB.db` | 人脸库 |

## 9. 常用 ADB

```bash
# 启动登录页
adb shell am start -n com.arcsoft.arcfacedemo/.ui.activity.LoginActivity

# 带自动登录（若实现了 auto extra）
adb shell am start -n com.arcsoft.arcfacedemo/.ui.activity.LoginActivity --ez auto true

# 查看版本
adb shell dumpsys package com.arcsoft.arcfacedemo | grep versionName
```

## 10. 文档索引

| 问题类型 | 文档 |
|----------|------|
| 登录 | [04-login-and-auth.md](./04-login-and-auth.md) |
| 渠道 | [02-product-flavors.md](./02-product-flavors.md) |
| 查验流程 | [07](./07-liveness-detect-flow.md) / [08](./08-register-recognize-flow.md) |
| 接口 | [16-api-reference.md](./16-api-reference.md) |
| 定时任务 | [18-background-jobs.md](./18-background-jobs.md) |
