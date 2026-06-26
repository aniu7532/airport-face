# 开机自启与 Kiosk 模式

## BootReceiver

**路径**：`receiver/BootReceiver.java`  
**权限**：`RECEIVE_BOOT_COMPLETED`

```xml
<receiver android:name=".receiver.BootReceiver">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

`onReceive`：

1. 记录 `BOOT_COMPLETED`
2. 延迟启动 `LoginActivity`（`WeakHandler`）
3. 可携带 `auto=true` 走免密/自动登录分支

## Kiosk（默认桌面）

`LoginActivity` Manifest：

```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
    <category android:name="android.intent.category.HOME" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

效果：

- 作为系统桌面，Home 键回到登录/查验流程
- 防止用户进入其他应用（需配合设备管理策略）
- `launchMode="singleTop"` 避免重复栈

运维抽屉「跳转系统桌面」`tvGotoLuancher` 可尝试恢复系统 Launcher。

## 设备标识

| 方法 | 用途 |
|------|------|
| `DeviceUtils.getDeviceId()` | 心跳 mac、detail-mac 绑定 |
| `DeviceUtils.getMacAddress()` | 硬件 MAC（视权限） |

登录后 **GET detail-mac** 将设备注册到后台，拉取：

- 默认 `checkType` / `direction`
- 绑定区域 `areaName`
- 同步 `interval` 等

## 自动重启（非 Boot）

`ArcFaceApplication.startPeriodicTask()` 每日 **凌晨 2 点**：

| 屏宽 | 方式 |
|------|------|
| > 800px | `ZysjSystemManager.zYRebootSys()` |
| ≤ 800px | `MyManager.reboot()` |

SP `reboot` 标志保证当日只重启一次。

## 部署检查清单

- [ ] 安装对应渠道 Release APK
- [ ] 授予相机、存储、电话状态、开机广播权限
- [ ] Android 11+ `MANAGE_EXTERNAL_STORAGE`（若需要）
- [ ] 设为默认桌面（HOME）
- [ ] 配置读卡器/二维码串口（见 [12-serial-port-config.md](./12-serial-port-config.md)）
- [ ] 首次联网登录完成全量同步
- [ ] 验证心跳与记录上传
- [ ] 确认 tenant-id 与机场租户一致

## 相关文档

- 登录流程 → [04-login-and-auth.md](./04-login-and-auth.md)
- 定时重启 → [18-background-jobs.md](./18-background-jobs.md)
