# 运维侧边栏与设置

## 运维侧边栏

### 入口

| 位置 | 触发方式 |
|------|----------|
| `LoginActivity` | 连续点击 `btnGo` 5 次 |
| 查验 Activity | 连续点击隐藏区域（各 Activity 内实现） |

弹出 `CustomDrawerPopupView`（XPopup 侧边抽屉）。

### 功能菜单

| 菜单项 | 说明 |
|--------|------|
| 进出方向 | 切换 `SPUtils.direction`（进/出） |
| 查验模式 | 切换 `SPUtils.checkType`（四种模式） |
| 证件窗口位置 | 切换 `SPUtils.tipsLoc` |
| 读卡器串口配置 | `CardSerialConfigPopDialog` |
| 二维码串口配置 | `QrSerialConfigPopDialog` |
| ArcFace AppKey | `AppKeyPopDialog`（SDK 密钥配置） |
| 识别参数设置 | 跳转 `RecognizeSettingsActivity` |
| 相机配置 | 跳转 `CameraConfigureActivity` |
| 核实功能设置 | `VerifyFeatureSettingsDialog` |
| 通行记录查询 | `RecordsPopDialog` |
| 日志上传 | 调用 `LogUploadUtils.upload()` |
| 数据重新初始化 | `LongPassCardsReInitUtils` |
| 重复人脸清理 | `DuplicateFaceCleanupUtils` |
| 补救措施 | `LongPassCardsRemedialMeasuresUtils` |
| 版本更新 | `UpdatePopDialog` |
| 退出登录 | 清除 Token，跳转 `LoginActivity` |

## 设置页面

### RecognizeSettingsActivity

ArcFace 识别参数，加载 `RecognizeSettingsPreferenceFragment`：

| 参数 | Preference 类 | 说明 |
|------|---------------|------|
| 识别阈值 | `ThresholdPreference` | 1:N 比对相似度阈值 |
| 活体阈值 | `ThresholdLivePreference` | RGB/IR 活体检测阈值 |
| 检测角度 | `ChooseDetectDegreeListPreference` | 人脸检测方向优先级 |
| 其他整数参数 | `AdjustableIntegerPreference` | 可调节整型配置 |

持久化通过 `ConfigUtil` 读写 SharedPreferences。

### CameraConfigureActivity

相机硬件配置：

- RGB / IR 相机 ID 选择
- 预览分辨率
- 双目偏移量（识别区域适配）
- 镜像/旋转设置

### VerifyFeatureSettingsDialog

查验特征开关，配置存于 `VerifyFeatureSettings.kt`（SharedPreferences）。

## 相关弹窗

| 弹窗 | 用途 |
|------|------|
| `LoadingPopDialog` | 加载中提示 |
| `LogingPopDialog` | 登录等待 |
| `CustomPopDialog` | 自定义结果提示 |
| `ImagePopDialog` | 图片预览 |
| `DrawPopDialog` | 侧边图片预览 |
| `AreaPickerDialog` | 管制区域树选择 |
| `StringListPickerDialog` | 字符串列表选择 |
| `DateTimePickerDialogHelper` | 日期时间选择 |

## SP 关键键位

| 键 | 类型 | 说明 |
|----|------|------|
| `checkType` | int | 查验模式 0~3 |
| `direction` | int | 进出方向 1/-1 |
| `tipsLoc` | int | 证件提示位置 0~3 |

存储工具：`InfoStorage` / `SPUtils`（blankj UtilCodeX）。
