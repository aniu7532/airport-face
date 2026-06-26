# 开机自启与 Kiosk 模式

## 涉及类

| 类 | 路径 | 职责 |
|----|------|------|
| `BootReceiver` | `receiver/BootReceiver.java` | 开机广播接收 |
| `LoginActivity` | `ui/activity/LoginActivity.java` | 自启目标页 |

## 开机自启

### BootReceiver

监听广播：

```xml
<action android:name="android.intent.action.BOOT_COMPLETED" />
```

收到开机广播后启动 `LoginActivity`，携带 `auto=true` 参数，跳过部分 UI 交互直接走自动登录流程。

### 权限

```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

## Kiosk 桌面模式

`LoginActivity` 在 Manifest 中注册为 Launcher 且带 `HOME` category：

```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
    <category android:name="android.intent.category.HOME" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

效果：

- 应用可作为设备默认桌面
- 按 Home 键回到 `LoginActivity` 而非系统桌面
- 适用于闸机/立式终端禁止用户退出的场景

## 设备 MAC 绑定

登录时调用 `URL_GET_MAC_DETAIL`：

- 上报设备 MAC 地址
- 后台绑定设备与机场/通道
- 返回设备专属配置（查验模式默认值、同步间隔等）

MAC 获取：`DeviceUtils.getMacAddress()`

## 设备重启

后台定时任务在凌晨 2:00 触发设备重启（详见 [18-background-jobs.md](./18-background-jobs.md)）：

| 设备 | 管理方式 |
|------|----------|
| 大屏 | `ZysjSystemManager` |
| 小屏 | `com.ys.rkapi.MyManager` |

## 部署建议

1. 安装 APK 后设为默认桌面（HOME）
2. 授予所有必要权限（相机、存储、电话状态等）
3. 配置串口参数（读卡器、二维码）
4. 首次手动登录完成数据初始化
5. 之后依赖开机自启 + 自动登录

## 退出 Kiosk

通过运维侧边栏「退出登录」可回到 `LoginActivity` 登录页。如需完全退出应用需通过 ADB 或系统设置切换默认桌面。
