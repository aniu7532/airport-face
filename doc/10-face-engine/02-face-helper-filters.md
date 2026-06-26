# FaceHelper 与 facefilter 过滤器

源码目录：

- `app/src/main/java/com/arcsoft/arcfacedemo/util/face/FaceHelper.java`
- `app/src/main/java/com/arcsoft/arcfacedemo/util/face/facefilter/`
- `app/src/main/java/com/arcsoft/arcfacedemo/util/face/constants/`

---

## FaceHelper 概述

人脸操作辅助类，实现 `FaceListener`。在相机预览帧上完成：检测 → 追踪 → 口罩 → 过滤 → 活体 → 特征提取 → 1:N 搜索。

### 四引擎分工

| 引擎 | 字段 | 职责 |
|------|------|------|
| 追踪 | `ftEngine` | `detectFaces`、trackId |
| 口罩 | `maskEngine` | `ASF_MASK_DETECT` |
| 特征 | `frEngine` | 质量检测、特征提取、搜索（委托 FaceServer） |
| 活体 | `flEngine` | RGB / IR 活体 |

### 线程池

- `frExecutor` + `frThreadQueue`：特征提取，队列默认 `recognizeConfiguration.getMaxDetectFaces()`。
- `flExecutor` + `flThreadQueue`：活体检测，同上。

### 内部错误码

| 常量 | 值 | 含义 |
|------|-----|------|
| `ERROR_BUSY` | -1 | 队列满，任务未提交 |
| `ERROR_FR_ENGINE_IS_NULL` | -2 | 特征引擎为空 |
| `ERROR_FL_ENGINE_IS_NULL` | -3 | 活体引擎为空 |

---

## FaceHelper 公共 API

### 构造

通过 `FaceHelper.Builder` 构建，必填 `previewSize`、`recognizeConfiguration`、`recognizeCallback` 及四引擎（按需）。

### 帧处理

#### `onPreviewFrame(byte[] rgbNv21, byte[] irNv21, boolean doRecognize)`

1. `ftEngine.detectFaces` → 可选 `keepMaxFace`
2. `refreshTrackId`：`trackId = faceId + trackedFaceCount`
3. 非 `onlyDetectLiveness` 时口罩检测
4. 组装 `FacePreviewInfoList`（RGB/IR 框变换）
5. `clearLeftFace` 清理离开人脸的状态
6. `doRecognize=true` 时进入 `doRecognize`

### 异步请求

| 方法 | 说明 |
|------|------|
| `requestFaceFeature(nv21, facePreviewInfo, w, h, format)` | 提交 `FaceRecognizeRunnable` |
| `requestFaceLiveness(nv21, faceInfo, w, h, format, livenessType, waitLock)` | 提交 `FaceLivenessDetectRunnable` |

### 识别区域与变换

| 方法 | 说明 |
|------|------|
| `setRgbFaceRectTransformer` | 预览坐标 → View 坐标（RGB） |
| `setIrFaceRectTransformer` | IR 框变换 |
| `setRecognizeArea(Rect)` | 可识别区域（相对 View） |
| `setDualCameraFaceInfoTransformer` | 双目标定转换 |

### 状态管理

| 方法 | 说明 |
|------|------|
| `getRecognizeInfo(map, trackId)` | 获取/创建 `RecognizeInfo` |
| `changeRecognizeStatus(trackId, status)` | 修改识别状态 |
| `changeLiveness(trackId, liveness)` | 修改活体值 |
| `getRecognizeStatus(trackId)` | 查询识别状态 |
| `getLiveness(trackId)` | 查询活体值 |
| `setName(trackId, name)` / `getName(trackId)` | 显示名 |

### 工具静态方法

| 方法 | 说明 |
|------|------|
| `isSameFace(List<FaceInfo>, FaceInfo)` | 框是否相交 |
| `getOverlapArea(Rect, Rect)` | 重叠矩形 |
| `isFaceSame(rgb, ir, threshold)` | 重叠面积比 > threshold 视为同脸（IR 活体用，默认 0.3） |

### 生命周期

#### `release()`

关闭 Disposable、shutdown 线程池、清空队列与 `faceInfoList`。

#### `getTrackedFaceCount()`

返回 `trackedFaceCount + currentMaxFaceId + 1`，供退出时 `ConfigUtil.setTrackedFaceCount` 持久化。

### FaceListener 回调实现

| 回调 | 行为 |
|------|------|
| `onFaceFeatureInfoGet` | 无活体或活体通过 → `searchFace`；否则 `waitLock.wait()` 等活体 |
| `onFaceLivenessInfoGet` | 非 ALIVE → 提示并重试活体 |
| `onFail` | 打错误日志 |

### Builder 配置项

`recognizeConfiguration`、`recognizeCallback`、`ftEngine`、`maskEngine`、`frEngine`、`flEngine`、`previewSize`、`frQueueSize`、`flQueueSize`、`trackedFaceCount`、`onlyDetectLiveness`、`dualCameraFaceInfoTransformer`。

---

## doRecognize 主流程（过滤器之后）

1. 遍历 `faceRecognizeFilterList` 执行 `filter`
2. 人脸框过小：`previewMaxEdge / rectMaxEdge > ConfigUtil.getRecognizeScale()` → skip
3. `!facePreviewInfo.isQualityPass()` → skip
4. 口罩 `UNKNOWN` → skip
5. 活体开启且未成功：触发 `requestFaceLiveness`
6. 状态 `TO_RETRY` → `SEARCHING` + `requestFaceFeature`

### searchFace

- `FaceServer.searchFaceFeature` → 相似度 vs `similarThreshold`
- 通过：`SUCCEED`、回调 `onRecognized(image, similar, quality, userName, true)`
- 未通过：重试或「未注册」

---

## facefilter 过滤器

接口：`FaceRecognizeFilter.filter(List<FacePreviewInfo>)`  
原则：**仅 `qualityPass=true` 的人脸进入后续活体/识别**；过滤器通过 `setQualityPass` 改写该标志。

### FaceSizeFilter

- **条件**：RGB 与 IR（若有）框宽高均 > `horizontalSize` 且 > `verticalSize`。
- **构造**：`FaceHelper` 在 `recognizeConfiguration.isEnableFaceSizeLimit()` 时添加，水平/垂直均取 `getFaceSizeLimit()`。

### FaceMoveFilter

- **条件**：最近 `CHECK_QUEUE_SIZE=5` 帧，相邻帧人脸中心距离均 ≤ `movePixels`。
- **构造**：`isEnableFaceMoveLimit()` 时添加，阈值 `getFaceMoveLimit()`。
- **辅助**：`getDistance(Rect, Rect)` 欧氏距离；离开画面的人脸从 `facePositionQueueMap` 清除。

### FaceRecognizeAreaFilter

- **条件**：`validArea.contains(facePreviewInfo.getRgbTransformedRect())`。
- **构造**：`isEnableFaceAreaLimit()` 时添加，`validArea` 来自 `setRecognizeArea`（默认可识别全屏）。

### 过滤器链顺序（Builder 内）

```
FaceSizeFilter → FaceMoveFilter → FaceRecognizeAreaFilter
```

（后两者仅在对应 Config 开关开启时加入。）

---

## constants 常量包

### RequestFeatureStatus（@IntDef）

| 值 | 常量 | 含义 |
|----|------|------|
| -1 | `DEFAULT` | 默认 |
| 0 | `SEARCHING` | 特征搜索中 |
| 1 | `SUCCEED` | 识别成功 |
| 2 | `TO_RETRY` | 待重试 |
| 3 | `FAILED` | 失败（延迟后转 TO_RETRY） |

### RequestLivenessStatus

| 值 | 常量 | 含义 |
|----|------|------|
| 10 | `ANALYZING` | 活体分析中 |

（与 `LivenessInfo.ALIVE/NOT_ALIVE/UNKNOWN/...` 共用 `changeLiveness` 整型参数。）

### LivenessType（enum）

| 枚举 | 说明 |
|------|------|
| `RGB` | `flEngine.process(ASF_LIVENESS)` + `getLiveness` |
| `IR` | IR 检测 + 与 RGB 框重叠校验 + `processIr` + `getIrLiveness` |

### RecognizeColor

| 常量 | 颜色 | 用途 |
|------|------|------|
| `COLOR_UNKNOWN` | Yellow | 未知 |
| `COLOR_SUCCESS` | Green | 成功 |
| `COLOR_FAILED` | Yellow | 失败 |

---

## FaceRecognizeRunnable 要点

- 可选 `imageQualityDetect`：戴口罩/不戴口罩用不同质量阈值。
- `extractFaceFeature(..., ExtractType.RECOGNIZE, isMask)`。
- 失败重试次数由 `recognizeConfiguration.getExtractRetryCount()` 控制。

## FaceLivenessDetectRunnable 要点

- IR 模式：`detectFaces` → `keepMaxFace` → `isFaceSame(0.3)` → `processIr`。
- 活体通过时 `waitLock.notifyAll()` 唤醒特征线程。
