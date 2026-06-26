# ConfigUtil 配置项

源码：`app/src/main/java/com/arcsoft/arcfacedemo/util/ConfigUtil.java`

通过 `PreferenceManager.getDefaultSharedPreferences` 读写，与 `PreferenceFragmentCompat` 共用同一 XML。

---

## 推荐默认值常量（类内 private / public）

| 常量名 | 值 | 用途 |
|--------|-----|------|
| `RECOMMEND_RECOGNIZE_THRESHOLD` | `0.80f` | 识别相似度阈值 |
| `RECOMMEND_SHELTER_THRESHOLD` | `0.50f` | 遮挡阈值 |
| `RECOMMEND_EYE_OPEN_THRESHOLD` | `0.50f` | 睁眼阈值 |
| `RECOMMEND_MOUTH_CLOSE_THRESHOLD` | `0.50f` | 闭嘴阈值 |
| `RECOMMEND_WEAR_GLASSES_THRESHOLD` | `0.50f` | 戴眼镜阈值 |
| `RECOMMEND_RGB_LIVENESS_THRESHOLD` | `0.50f` | RGB 活体阈值 |
| `RECOMMEND_IR_LIVENESS_THRESHOLD` | `0.50f` | IR 活体阈值 |
| `RECOMMEND_LIVENESS_FQ_THRESHOLD` | `0.65f` | 活体 FQ 阈值 |
| `RECOMMEND_RGB_LIVENESS_FACE_SIZE_THRESHOLD` | `80` | RGB 活体模型人脸尺寸界限 |
| `RECOMMEND_IR_LIVENESS_FACE_SIZE_THRESHOLD` | `90` | IR 活体模型人脸尺寸界限 |
| `IMAGE_QUALITY_NO_MASK_RECOGNIZE_THRESHOLD` | `0.49f` | 未戴口罩 · 识别场景质量阈值（public） |
| `IMAGE_QUALITY_NO_MASK_REGISTER_THRESHOLD` | `0.63f` | 未戴口罩 · 注册场景质量阈值（public） |
| `IMAGE_QUALITY_MASK_RECOGNIZE_THRESHOLD` | `0.29f` | 戴口罩 · 识别场景质量阈值（public） |
| `RECOMMEND_FACE_SIZE_LIMIT` | `160` | 人脸尺寸过滤（像素） |
| `RECOMMEND_FACE_MOVE_LIMIT` | `20` | 帧间移动像素限制 |
| `DEFAULT_MAX_DETECT_FACE_NUM` | `1` | 最大检测人脸数 |
| `DEFAULT_SCALE` | `16` | 预览人脸占比 scale（越大允许越远） |
| `DEFAULT_PREVIEW_SIZE` | `"1280x720"` | 默认相机分辨率字符串 |

---

## Getter 一览

### 追踪与人脸策略

| 方法 | Preference Key（string res） | 默认值 | 说明 |
|------|------------------------------|--------|------|
| `getTrackedFaceCount(context)` | `preference_track_face_count` | `0` | 历史 track 计数 |
| `setTrackedFaceCount(context, n)` | 同上 | — | commit 写入 |
| `getFtOrient(context)` | `preference_choose_detect_degree` | `ASF_OP_ALL_OUT` | 检测角度优先级 |
| `isKeepMaxFace(context)` | （硬编码） | **`true`** | 仅保留最大人脸；注释说明 Demo 原设计为可配置 |
| `isRecognizeAreaLimited(context)` | `preference_recognize_limit_recognize_area` | `false` | 是否限制识别区域 |

### 检测数量与 scale

| 方法 | Key | 默认值 |
|------|-----|--------|
| `getRecognizeMaxDetectFaceNum(context)` | `preference_recognize_max_detect_num` | `1` |
| `getRecognizeScale(context)` | `preference_recognize_scale_value` | `16` |

### 双目偏移

| 方法 | Key | 默认值 |
|------|-----|--------|
| `getDualCameraHorizontalOffset(context)` | `preference_dual_camera_offset_horizontal` | `0` |
| `getDualCameraVerticalOffset(context)` | `preference_dual_camera_offset_vertical` | `0` |

### 阈值类（String 存 float，解析失败用推荐值）

| 方法 | Key | 默认 |
|------|-----|------|
| `getRecognizeThreshold` | `preference_recognize_threshold` | 0.80 |
| `getRecognizeShelterThreshold` | `preference_shelter_threshold` | 0.50 |
| `getRecognizeEyeOpenThreshold` | `preference_eye_open_threshold` | 0.50 |
| `getRecognizeMouthCloseThreshold` | `preference_mouth_close_threshold` | 0.50 |
| `getRecognizeWearGlassesThreshold` | `preference_wear_glasses_threshold` | 0.50 |
| `getRgbLivenessThreshold` | `preference_rgb_liveness_threshold` | 0.50 |
| `getIrLivenessThreshold` | `preference_ir_liveness_threshold` | 0.50 |
| `getLivenessFqThreshold` | `preference_liveness_fq_threshold` | 0.65 |
| `getRgbLivenessFaceSizeThreshold` | `preference_rgb_liveness_face_size_threshold` | 80 |
| `getIrLivenessFaceSizeThreshold` | `preference_ir_liveness_face_size_threshold` | 90 |
| `getImageQualityNoMaskRecognizeThreshold` | `preference_image_quality_no_mask_recognize_threshold` | 0.49 |
| `getImageQualityNoMaskRegisterThreshold` | `preference_image_quality_no_mask_register_threshold` | 0.63 |
| `getImageQualityMaskRecognizeThreshold` | `preference_image_quality_mask_recognize_threshold` | 0.29 |

### 过滤器开关与限制

| 方法 | Key | 默认值 |
|------|-----|--------|
| `getFaceSizeLimit` | `preference_recognize_face_size_limit` | 160 |
| `getFaceMoveLimit` | `preference_recognize_move_pixel_limit` | 20 |
| `isEnableImageQualityDetect` | `preference_enable_image_quality_detect` | **true** |
| `isEnableFaceSizeLimit` | `preference_enable_face_size_limit` | false |
| `isEnableFaceMoveLimit` | `preference_enable_face_move_limit` | false |

### 活体与相机

| 方法 | Key | 默认值 |
|------|-----|--------|
| `getLivenessDetectType` | `preference_liveness_detect_type` | `value_liveness_type_rgb` |
| `isSwitchCamera` | `preference_switch_camera` | false |
| `getPreviewSize` | `preference_dual_camera_preview_size` | `1280x720` |
| `getRgbCameraAdditionalRotation` | `preference_rgb_camera_rotation` | `"0"` |
| `getIrCameraAdditionalRotation` | `preference_ir_camera_rotation` | `"0"` |

### SDK 激活信息

| 方法 | Key | 默认值 |
|------|-----|--------|
| `getAppId` | `preference_app_id` | `Constants.APP_ID` |
| `getSdkKey` | `preference_sdk_key` | `Constants.SDK_KEY` |
| `getActiveKey` | `preference_active_key` | `Constants.ACTIVE_KEY` |
| `commitAppId` / `commitSdkKey` / `commitActiveKey` | 同上 | commit 写入 |

### 预览/框绘制镜像

| 方法 | Key | 默认 |
|------|-----|------|
| `isDrawRgbRectHorizontalMirror` | `preference_draw_rgb_rect_horizontal_mirror` | false |
| `isDrawIrRectHorizontalMirror` | `preference_draw_ir_rect_horizontal_mirror` | false |
| `isDrawRgbRectVerticalMirror` | `preference_draw_rgb_rect_vertical_mirror` | false |
| `isDrawIrRectVerticalMirror` | `preference_draw_ir_rect_vertical_mirror` | false |
| `isDrawRgbPreviewHorizontalMirror` | `preference_rgb_preview_horizontal_mirror` | false |
| `isDrawIrPreviewHorizontalMirror` | `preference_ir_preview_horizontal_mirror` | false |

---

## 私有读写辅助

| 方法 | 类型 |
|------|------|
| `getString` / `commitString` | String |
| `getBoolean` | boolean |
| `getInt` / `commitInt` | int |
| `getFloat` | float |

`context == null` 时 getter 直接返回传入的 default。

---

## 与 FaceHelper 的关联

| ConfigUtil | FaceHelper 使用点 |
|------------|-------------------|
| `getRecognizeScale` | `doRecognize` 人脸过小跳过 |
| `isKeepMaxFace` | `onPreviewFrame` |
| `isEnableFaceSizeLimit` + `getFaceSizeLimit` | `FaceSizeFilter` |
| `isEnableFaceMoveLimit` + `getFaceMoveLimit` | `FaceMoveFilter` |
| `isRecognizeAreaLimited` | 识别区域限制（需业务设置 `setRecognizeArea`） |
| `getTrackedFaceCount` / `setTrackedFaceCount` | 跨会话 trackId 延续 |
