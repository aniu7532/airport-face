# BootReceiver + HOME Launcher

## Kiosk 双机制

本应用通过 **HOME 类别** + **开机广播** 实现设备常驻/Kiosk 行为：

| 机制 | 作用 |
|---|---|
| `LoginActivity` 声明 `HOME` | 按 Home 键回到登录页，替代系统桌面 |
| `BootReceiver` | 开机自启应用 |

## LoginActivity 作为 Launcher + HOME

**Manifest 片段**（`AndroidManifest.xml`）：

```xml
<activity
    android:name=".ui.activity.LoginActivity"
    android:exported="true"
    android:launchMode="singleTop"
    android:theme="@style/FullScreenTheme">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

### 属性说明

| 属性 | 值 | 含义 |
|---|---|---|
| `exported` | true | 系统可启动 |
| `launchMode` | singleTop | 已在栈顶时不重复创建 |
| `LAUNCHER` | — | 应用列表图标入口 |
| `HOME` | — | 可作为默认桌面 |
| `DEFAULT` | — | 隐式 Intent 默认类别 |

### 用户可见行为

- 首次安装后系统可能询问「选择桌面」→ 选本应用则 Home 键回到 `LoginActivity`
- `HomeActivity` 的 LAUNCHER 已注释，**不**作为桌面入口

## BootReceiver

**路径**：`receiver/BootReceiver.java`

### Manifest

```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<receiver
    android:name=".receiver.BootReceiver"
    android:enabled="true"
    android:exported="true"
    android:permission="android.permission.RECEIVE_BOOT_COMPLETED">
    <intent-filter>
        <action android:name="android.intent.action.ACTION_SHUTDOWN" />
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

### onReceive 逻辑

```java
Intent intent1 = IntentUtils.getLaunchAppIntent("com.arcsoft.arcfacedemo");
if (intent1 == null) {
    ALog.e("Didn't exist launcher activity.");
    return;
}
intent1.putExtra("auto", true);
Utils.getApp().startActivity(intent1);
```

| 步骤 | 说明 |
|---|---|
| 解析包名 | 固定 `com.arcsoft.arcfacedemo` |
| 获取启动 Intent | 对应带 MAIN/LAUNCHER 的 Activity（即 LoginActivity） |
| Extra | `auto=true`（与 Token 过期跳转、401 跳登录一致） |
| 启动 | `Utils.getApp().startActivity(intent1)` |

### 未使用的代码

注释掉的备选：

```java
// ActivityUtils.startActivity(LoginActivity.class);
// AppUtils.launchApp(packageName);
// FLAG_ACTIVITY_NEW_TASK | CLEAR_TOP | CLEAR_TASK
```

当前实现**未**加 `NEW_TASK` 等 flag，依赖 Application context 启动。

### WeakHandler

类内声明 `WeakHandler countdownHandler`，当前 `onReceive` **未使用**（遗留字段）。

## auto Extra 用途

`LoginActivity` 及其他处传入 `auto=true` 的场景：

| 来源 | 场景 |
|---|---|
| `BootReceiver` | 开机自启 |
| `ArcFaceApplication.fetchNextPage` | 增量同步 401 |
| 定时重启后 | 用户无感重新登录（若实现 auto 登录逻辑） |

具体 auto 登录分支需查 `LoginActivity.onCreate` 对 `getBooleanExtra("auto")` 的处理。

## 权限

`RECEIVE_BOOT_COMPLETED` 在 Manifest 中声明**两次**（重复，不影响功能）。

Receiver 额外声明 `android:permission="RECEIVE_BOOT_COMPLETED"` 限制发送方。

## 与定时重启配合

`ArcFaceApplication` 凌晨 2 点 `reboot` 后：

1. 设备重启
2. 系统发 `BOOT_COMPLETED`
3. `BootReceiver` 启动 `LoginActivity`（`auto=true`）
4. 登录成功后 `gotoActivity` → 查验页 + `startPeriodicTask`

形成 **7×24 无人值守** 闭环。

## 排查要点

| 现象 | 检查 |
|---|---|
| 开机不自启 | 厂商自启权限；Receiver exported；是否设默认桌面 |
| Home 键回系统桌面 | 未选本应用为默认 HOME |
| 启动闪退 | `getLaunchAppIntent` 为 null → 包名/Activity 导出 |
| 重复登录页 | `singleTop` 应缓解；查 Intent flags |

## ADB 验证

```bash
# 模拟开机广播
adb shell am broadcast -a android.intent.action.BOOT_COMPLETED \
  -p com.arcsoft.arcfacedemo

# 启动登录页（带 auto）
adb shell am start -n com.arcsoft.arcfacedemo/.ui.activity.LoginActivity --ez auto true

# 查看 HOME 解析
adb shell cmd package resolve-activity -a android.intent.action.MAIN \
  -c android.intent.category.HOME
```

## 相关文件

| 文件 | 角色 |
|---|---|
| `AndroidManifest.xml` | 权限、Activity intent-filter、Receiver |
| `BootReceiver.java` | 开机启动 |
| `LoginActivity.java` | 桌面 + 登录入口 |
| `ArcFaceApplication.java` | 2 点定时 reboot |
