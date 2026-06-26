# 运维侧边栏与设置

## CustomDrawerPopupView

**类**：`widget/dialog/CustomDrawerPopupView.java`  
**布局**：`res/layout/dialog_draw.xml`  
**基类**：XPopup `DrawerPopupView`

### 入口

| 位置 | 触发 |
|------|------|
| `LoginActivity` | 连续点击 `btnGo` 5 次 |
| 各查验 Activity | 隐藏区域连点（实现各异） |

### 完整菜单项

| 控件 ID | 菜单文案 | 行为 |
|---------|----------|------|
| `tvPhone` | （展示） | 显示 `SPUtils.mobile` |
| `tvInOut` | 选择进出 | 列表：进控制区/出控制区 → `direction` 1/-1，重启 Login |
| `tvChayan` | 查验模式 | 4 种 checkType → 重启 Login |
| `tvTipsLoc` | 证件提示窗口位置 | 4 角 → `tipsLoc` 0~3 |
| `tvVerifyFeature` | 核实功能设置 | 打开 `VerifyFeatureSettingsDialog` |
| `tvCanshu` | 识别参数设置 | 跳转 `RecognizeSettingsActivity` |
| `tvCardSerial` | 读卡器串口配置 | `CardSerialConfigPopDialog` |
| `tvQrSerial` | 二维码串口配置 | `QrSerialConfigPopDialog` |
| `tvWenan` | 文案设置 | 输入框 → `wenan` |
| `tvDelete` | 清除数据 | 确认后清 DB/人脸库等 |
| `tvUploadLog` | 上传日志 | `LogUploadUtils.upload()` |
| `tvReInit` | 数据重新初始化 | `LongPassCardsReInitUtils` |
| `tvRemedial` | 补救措施 | `LongPassCardsRemedialMeasuresUtils` |
| `tvClearDuplicateFace` | 重复人脸清理 | `DuplicateFaceCleanupUtils` |
| `tvGotoLuancher` | 跳转系统桌面 | 尝试启动系统 Launcher |
| `tvGotoSetting` | 系统设置 | `Settings.ACTION_SETTINGS` |
| `tvVersion` | （展示） | 当前 `versionName` |
| `btnExit` | 退出 | 清 Token，跳转 `LoginActivity` |

切换进出/查验模式后，会 `finish` 所有查验 Activity 并 `startActivity(LoginActivity)` 以重新走路由。

## VerifyFeatureSettings

**类**：`util/VerifyFeatureSettings.kt`

| SP 键 | 默认 | 说明 |
|-------|------|------|
| `verify_feature_enabled` | false | 核销总开关 |
| `verify_required_passage` | false | 核实时必填通道 |
| `verify_required_pass_time` | false | 必填通行时间 |
| `verify_required_device` | false | 必填设备 |
| `verify_required_remark` | false | 必填备注 |

总开关开启时，新写入的通行记录 `needVerify=true`。  
配置 UI：`VerifyFeatureSettingsDialog.kt`。

## RecognizeSettingsActivity

- 加载 `RecognizeSettingsPreferenceFragment`
- XML：`res/xml/preferences_recognize.xml`
- 读写 `ConfigUtil`（与 `PreferenceManager` 同一 SP 文件）

## CameraConfigureActivity

- 相机 ID、分辨率、双目偏移
- XML：`preference_camera.xml` 等
- 影响 `DualCameraHelper` 预览与 `FaceRectTransformer` 对齐

## ArcFace AppKey

`AppKeyPopDialog` → 写入 SP：

| 键 | 说明 |
|----|------|
| `Appid` | APP_ID |
| `Sdkkey` | SDK_KEY |
| `Activecode` | ACTIVE_KEY |

`ActivationActivity` 读取上述值做在线激活。

## 相关弹窗速查

| 弹窗 | 文件 | 用途 |
|------|------|------|
| `RecordsPopDialog` | RecordsPopDialog.java | 在线通行记录 |
| `UpdatePopDialog` | UpdatePopDialog.java | 版本更新 |
| `LoadingPopDialog` | LoadingPopDialog.java | 加载中 |
| `AreaPickerDialog` | AreaPickerDialog.kt | 区域树选择 |
| `AppKeyPopDialog` | AppKeyPopDialog.java | SDK 密钥 |

## 相关文档

- checkType/direction → [06-check-modes.md](./06-check-modes.md)
- 串口配置 → [12-serial-port-config.md](./12-serial-port-config.md)
- 识别阈值 → [14-recognize-settings.md](./14-recognize-settings.md)
