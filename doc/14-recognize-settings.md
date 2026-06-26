# ArcFace 识别参数

## 涉及类

| 类 | 路径 | 职责 |
|----|------|------|
| `RecognizeSettingsActivity` | `ui/activity/RecognizeSettingsActivity.java` | 设置页入口 |
| `RecognizeSettingsPreferenceFragment` | `preference/RecognizeSettingsPreferenceFragment.java` | Preference 加载 |
| `ConfigUtil` | `util/ConfigUtil.java` | 参数读写 SP |
| `FaceHelper` | `util/face/FaceHelper.java` | 识别引擎封装 |
| `FaceServer` | `faceserver/FaceServer.java` | ArcFace 引擎 |
| `CameraConfigureActivity` | `ui/activity/CameraConfigureActivity.java` | 相机配置 |
| `CameraHelper` | `util/camera/CameraHelper.java` | 单摄辅助 |
| `DualCameraHelper` | `util/camera/DualCameraHelper.java` | 双摄辅助 |

## 识别阈值

| 参数 | Preference | 默认值域 | 说明 |
|------|------------|----------|------|
| 识别阈值 | `ThresholdPreference` | 0.0 ~ 1.0 | 1:N 比对通过最低相似度 |
| 活体阈值 | `ThresholdLivePreference` | 0.0 ~ 1.0 | 活体检测通过最低分数 |

对话框：`ThresholdPreferenceDialogFragmentCompat`、`ThresholdLivePreferenceDialogFragmentCompat`。

## 检测参数

| 参数 | 类 | 说明 |
|------|-----|------|
| 检测角度 | `ChooseDetectDegreeListPreference` | 人脸检测方向优先级（0°/90°/180°/270°） |
| 最大检测人脸数 | `AdjustableIntegerPreference` | 单帧最大检测人脸数 |
| 最小人脸尺寸 | `FaceSizeFilter` | 过滤过小的人脸 |

## 活体检测

| 类 | 说明 |
|----|------|
| `LivenessType` | 活体类型枚举（RGB / IR） |
| `RequestLivenessStatus` | 活体检测状态常量 |
| `RequestFeatureStatus` | 特征提取状态常量 |

双摄模式下 RGB 做人脸检测，IR 做活体检测。

## 人脸过滤器

`util/face/facefilter/` 包：

| 过滤器 | 说明 |
|--------|------|
| `FaceSizeFilter` | 过滤尺寸过小的人脸 |
| `FaceMoveFilter` | 过滤移动过快的人脸 |
| `FaceRecognizeAreaFilter` | 限制识别区域范围内的人脸 |
| `FaceRecognizeFilter` | 过滤器链接口 |

## 相机配置

`CameraConfigureActivity` 管理：

| 配置项 | 说明 |
|--------|------|
| RGB 相机 ID | 可见光相机设备号 |
| IR 相机 ID | 红外相机设备号 |
| 预览分辨率 | 宽 × 高 |
| 双目水平偏移 | RGB 与 IR 人脸框对齐偏移量 |
| 预览旋转角度 | `displayOrientation` |

## 调试工具

| 类 | 说明 |
|----|------|
| `RecognizeDebugActivity` | 识别调试页 |
| `RecognizeDebugViewModel` | 调试数据 |
| `DebugFaceHelper` | 调试版 FaceHelper |
| `DebugInfoDumper` | 导出调试 dump 文件 |
| `DumpConfig` | dump 配置 |

## 配置持久化

所有参数通过 `ConfigUtil` 读写 SharedPreferences，应用启动时 `FaceHelper` / `FaceServer` 读取并应用到 ArcFace 引擎。

## ArcFace SDK 激活

| 类 | 说明 |
|----|------|
| `ActivationActivity` | 在线激活页面 |
| `ActiveViewModel` | 激活状态管理 |
| `Constants` | `APP_ID`、`SDK_KEY`、`ACTIVE_KEY` |

SDK 密钥也可通过 `AppKeyPopDialog` 在运维侧边栏配置。
