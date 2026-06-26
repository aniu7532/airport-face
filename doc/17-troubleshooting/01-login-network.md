# 登录与网络排查

## 登录链路概览

```
LoginActivity
├── 零信任 VPN（SFAuth，部分渠道）
├── POST 后台登录 → ApiUtils.accessToken
├── getCheckMethod / 设备信息上报
├── 分支：
│   ├── 首次或无本地通行证 → getLongPassCards() 全量
│   └── 已有数据 → startUpDataToServer() + gotoActivity()
└── gotoActivity() → startPeriodicTask() + 跳转查验 Activity
```

## 常见现象与排查

### 零信任失败

| 检查项 | 说明 |
|---|---|
| `Constants.BASE_VPN` | VPN 网关地址 |
| 账号密码 | 运维配置或内置测试账号 |
| 日志 | `SFAuthResultListener.onAuthFailed` |

### 后台登录非 200

| 检查项 | 说明 |
|---|---|
| `UrlConstants.TENANT_ID` | 与后台租户一致（渠道包不同值不同） |
| 请求头 | 抓包确认 `tenant-id` |
| 渠道前缀 | 如石河子需 `/shf/` API 前缀 |

### 登录后一直加载

| 可能原因 | 排查 |
|---|---|
| `getLongPassCards` 分页未完成 | 日志搜「正在下载」、页码 |
| 人脸注册失败 | `registerFaceByBitmap` success=false |
| 网络超时 | Ping 任务、`isOffLine` |
| `checkAbnormalCreate` | 登录流程异常分支 |

### 频繁跳回登录页

| 原因 | 日志/代码 |
|---|---|
| Token 过期 | `ApiUtils.accessToken` 被清空 |
| 增量同步 401 | `fetchNextPage` → `LoginActivity` extra `auto=true` |
| 手动注销 | — |

```bash
adb logcat -s YCJC ALog | grep -iE "login|token|401"
```

## 网络离线检测

| 组件 | 周期 | 行为 |
|---|---|---|
| `startPeriodicTask` Ping 子任务 | 10 秒 | `NetworkUtils.isAvailableByPing()` |
| 结果 | — | `ArcFaceApplication.isOffLine` |

查验页可据此提示离线模式（若 UI 有绑定）。

## 心跳

| 项 | 值 |
|---|---|
| URL | `/check/device/heartbeat` |
| 参数 | `mac`、`interval` |
| 周期 | 与主定时任务相同（默认 5 分钟） |

失败仅打日志，不跳登录。

## 渠道易错点

| 渠道 | 注意 |
|---|---|
| 石河子 | API 必须带 `/shf/` |
| 洛阳 | TENANT_ID 长整型；可能无临时证 |
| 重庆 | `tenant-id` 必须为 `3` |
| 银川 | 生产域 `inckzqtxz` |

验证：日志打印 `UrlConstants.URL_GetLongPass` 完整 URL。

## 登录成功后的后台任务

| 方法 | 何时启动 |
|---|---|
| `startUpDataToServer` | 非首次且本地有通行证 |
| `startPeriodicTask` | `gotoActivity()` 内必定调用 |

若只登录不上传：查是否走了 `getLongPassCards` 分支且未完成。

## 相关 SP / Storage

| 键 | 含义 |
|---|---|
| `isFirstStart` | 首次启动标志 |
| `interval` | 同步间隔分钟 |
| `checkType` | 0进/1出/2进出/3注册识别 |
| `direction` | 通行方向配置 |

## ADB 命令

```bash
# 启动登录页
adb shell am start -n com.arcsoft.arcfacedemo/.ui.activity.LoginActivity

# 带 auto extra
adb shell am start -n com.arcsoft.arcfacedemo/.ui.activity.LoginActivity --ez auto true

# 查看版本
adb shell dumpsys package com.arcsoft.arcfacedemo | grep versionName
```

## 日志路径

| 路径 | 内容 |
|---|---|
| `{externalFilesDir}/log/` | ALog 文件，保留 2 天 |
| `adb logcat -s YCJC` | 实时 TAG=YCJC |

## 关联文档

- 定时任务：`doc/14-background/02-periodic-sync-heartbeat.md`
- 记录上传：`doc/14-background/01-upload-scheduler.md`
- 开机自启：`doc/16-device/01-boot-kiosk.md`
