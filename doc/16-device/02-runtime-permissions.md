# Android 权限与设备授权

## 权限声明

主清单 `app/src/main/AndroidManifest.xml` 当前声明：

| 权限 | 用途 | 授权方式 |
|------|------|----------|
| `CAMERA` | RGB/IR 人脸采集 | 运行时权限 |
| `ACCESS_FINE_LOCATION` | 设备/网络 SDK 兼容 | 运行时权限 |
| `READ_PHONE_STATE` | 设备信息 | 运行时权限 |
| `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` | 历史外部存储兼容 | 运行时权限；在新 Android 版本上能力受限 |
| `MANAGE_EXTERNAL_STORAGE` | 公有目录激活文件等 | 特殊设置页授权 |
| `INTERNET`、网络/Wi-Fi 状态 | 登录、同步、上传 | 普通权限，无运行时弹窗 |
| `RECEIVE_BOOT_COMPLETED` | 开机自启 | 普通权限；Manifest 当前重复声明一次 |

## LoginActivity 申请流程

`LoginActivity.NEEDED_PERMISSIONS` 包含相机、存储、电话、定位和网络权限：

1. 首次布局完成时，`onGlobalLayout()` 检查并调用 `ActivityCompat.requestPermissions()`。
2. 点击登录时再次检查；缺少权限则提示“请允许权限”并中止登录。
3. 注册本地人脸前也会再次检查。

`INTERNET`、`ACCESS_NETWORK_STATE` 等普通权限虽然被放入数组，但不会产生运行时授权弹窗。

## MANAGE_EXTERNAL_STORAGE

该权限不能通过普通 `requestPermissions()` 获得：

```text
PermissionUtils.hasManageExternalStoragePermission()
  → Android 11+ 检查 Environment.isExternalStorageManager()
  → 未授权时打开 ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
```

应用私有的 `getExternalFilesDir()` 不依赖“所有文件访问”权限；公有存储中的 ArcFace 激活配置等历史路径才需要重点验证。

## 厂商硬件权限边界

- `/dev/tty*` 串口节点是否可读写由设备 ROM、SELinux、文件属主和厂商授权决定，Manifest 权限不能替代。
- 德卡 Android USB 模式需要 SDK 的异步 USB 授权；当前业务使用 COM 串口模式。
- 如果未来接入蓝牙/BLE，需要补充 Android 12+ 的 `BLUETOOTH_SCAN`、`BLUETOOTH_CONNECT` 及运行时授权。

## 排查清单

- [ ] 系统设置中相机权限已允许，预览未被其他应用占用。
- [ ] Android 11+ 的“所有文件访问”状态符合公有激活文件使用要求。
- [ ] 拒绝权限后重新登录能再次触发申请。
- [ ] 串口失败时同时检查 `/dev/tty*` 节点权限与设备 ROM，不只检查 Manifest。
- [ ] 清理 Manifest 中重复的 `RECEIVE_BOOT_COMPLETED` 声明时验证开机自启。
