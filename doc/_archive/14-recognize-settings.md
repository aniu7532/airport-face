# ArcFace 识别参数

## 配置存储

**读写类**：`util/ConfigUtil.java`  
**UI**：`RecognizeSettingsPreferenceFragment` + `res/xml/preferences_recognize.xml`  
**SP 文件**：`PreferenceManager.getDefaultSharedPreferences`（与 Preference 组件共享）

> 修改识别参数后通常需重启查验 Activity 或重新初始化 `FaceHelper` 生效。

## 推荐默认值（ConfigUtil 常量）

| 参数 | 常量 | 默认值 |
|------|------|--------|
| 识别阈值 | `RECOMMEND_RECOGNIZE_THRESHOLD` | **0.80** |
| 遮挡阈值 | `RECOMMEND_SHELTER_THRESHOLD` | 0.50 |
| 睁眼阈值 | `RECOMMEND_EYE_OPEN_THRESHOLD` | 0.50 |
| 闭嘴阈值 | `RECOMMEND_MOUTH_CLOSE_THRESHOLD` | 0.50 |
| 戴眼镜阈值 | `RECOMMEND_WEAR_GLASSES_THRESHOLD` | 0.50 |
| RGB 活体阈值 | `RECOMMEND_RGB_LIVENESS_THRESHOLD` | 0.50 |
| IR 活体阈值 | `RECOMMEND_IR_LIVENESS_THRESHOLD` | 0.50 |
| 活体 FQ 阈值 | `RECOMMEND_LIVENESS_FQ_THRESHOLD` | 0.65 |
| 最小人脸尺寸 | `RECOMMEND_FACE_SIZE_LIMIT` | 160 px |
| 帧间移动限制 | `RECOMMEND_FACE_MOVE_LIMIT` | 20 px |
| 最大检测人脸数 | `DEFAULT_MAX_DETECT_FACE_NUM` | 1 |
| 默认预览分辨率 | `DEFAULT_PREVIEW_SIZE` | 1280x720 |

### 图像质量阈值

| 场景 | 常量 | 值 |
|------|------|-----|
| 识别-未戴口罩 | `IMAGE_QUALITY_NO_MASK_RECOGNIZE_THRESHOLD` | 0.49 |
| 注册-未戴口罩 | `IMAGE_QUALITY_NO_MASK_REGISTER_THRESHOLD` | 0.63 |
| 识别-戴口罩 | `IMAGE_QUALITY_MASK_RECOGNIZE_THRESHOLD` | 0.29 |

## Preference 组件

| 类 | 用途 |
|----|------|
| `ThresholdPreference` | 识别阈值条 |
| `ThresholdLivePreference` | 活体阈值条 |
| `ThresholdPreferenceDialogFragmentCompat` | 识别阈值对话框 |
| `ThresholdLivePreferenceDialogFragmentCompat` | 活体阈值对话框 |
| `ChooseDetectDegreeListPreference` | 检测角度优先级 |
| `AdjustableIntegerPreference` | 可调整数项 |
| `IntegerPreferenceDialogFragmentCompat` | 整数输入对话框 |

## 人脸滤镜链

路径：`util/face/facefilter/`

| 过滤器 | 作用 |
|--------|------|
| `FaceSizeFilter` | 过滤小于 `RECOMMEND_FACE_SIZE_LIMIT` 的人脸 |
| `FaceMoveFilter` | 过滤帧间移动超过 `RECOMMEND_FACE_MOVE_LIMIT` |
| `FaceRecognizeAreaFilter` | 仅识别框定区域内的人脸 |
| `FaceRecognizeFilter` | 过滤器链接口 |

## 相机与预览

| 类 | 说明 |
|----|------|
| `CameraHelper` | 单摄像头 |
| `DualCameraHelper` | RGB + IR 双摄同步 |
| `CameraGLSurfaceView` | OpenGL 预览 |
| `NV21Drawer` | YUV 渲染 |
| `FaceRectTransformer` | 预览坐标 → 人脸框坐标（含双目偏移） |
| `CameraConfigureActivity` | 相机 ID、分辨率、偏移配置 |

## 活体类型

`util/face/constants/LivenessType.java`：

- RGB 单目活体
- IR 红外活体（生产环境主用）

状态常量：`RequestLivenessStatus`、`RequestFeatureStatus`、`RecognizeColor`。

## SDK 激活

| 入口 | 说明 |
|------|------|
| `Constants.APP_ID/SDK_KEY/ACTIVE_KEY` | 代码默认密钥 |
| `AppKeyPopDialog` | 运维修改 SP |
| `ActivationActivity` | 在线激活 UI |
| `activeConfig.txt` | 外部配置文件激活（方式二） |

## 调试

| 类 | 用途 |
|----|------|
| `RecognizeDebugActivity` | 实时参数与识别信息 |
| `DebugFaceHelper` | 带 dump 的 FaceHelper |
| `DebugInfoDumper` | 导出 NV21/比对结果到 `debugDump/` |

## 相关文档

- 查验比对流程 → [07-liveness-detect-flow.md](./07-liveness-detect-flow.md)
- 人脸库 → [15-face-database.md](./15-face-database.md)
- 运维入口 → [13-settings-and-ops-drawer.md](./13-settings-and-ops-drawer.md)
