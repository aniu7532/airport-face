# 人脸识别排查

## 架构组件

| 组件 | 路径/职责 |
|---|---|
| `FaceServer` | 虹软引擎初始化、特征提取 |
| `FaceRepository` | 人脸库 CRUD、分页加载 |
| `FaceDatabase` / `FaceDao` | Room 人脸实体 |
| `ArcFaceApplication.updateFace` | 增量同步后批量注册 |
| `RegisterAndRecognizeActivity` | 注册+识别合一模式 |
| `LivenessDetect*Activity` | 活体检测+识别查验 |
| `RecognizeSettingsActivity` | 阈值等参数 |
| `RecognizeDebugActivity` | 现场调优（运维抽屉） |

## 常见现象与处理

### 无人脸框

| 检查 | 操作 |
|---|---|
| 相机 ID | `CameraConfigureActivity` 配置 RGB/IR |
| 权限 | `CAMERA` 已授予 |
| 预览回调 | 日志搜 FaceHelper / Track |
| 硬件 | USB/内置相机是否被占用 |

### 活体失败

| 检查 | 操作 |
|---|---|
| IR 相机 | 与 RGB 对齐、偏移配置 |
| 活体阈值 | `RecognizeSettings` 或 SP 中 IR 阈值 |
| 环境 | 强逆光、红外干扰 |
| 日志 | liveness code 非 0 |

### 比对失败（识别通过率低）

| 检查 | 操作 |
|---|---|
| 识别阈值 | 默认约 **0.8**，可在设置中调低 |
| 人脸库 | 该 `applyId` 是否已注册特征 |
| 照片质量 | 增量同步 checkPhoto 是否下载成功 |
| 注册失败 | `registerFace` onError 日志 |

### 人脸库为空 / 数量不对

| 对比项 | 命令/日志 |
|---|---|
| 通行证数 | `longTermPassDao().getCount()` |
| 人脸数 | `faceRepository.getTotalFaceCount()` |
| 差值大 | 重新登录全量同步；或「数据重新初始化」 |

增量同步路径：`getLongPassCardsUpdate` → 下载图片 → `updateLocalDatabase` → `updateFace` → `registerFaceByBitmap`。

### 重复人脸 / 注册冲突

| 工具 | 入口 |
|---|---|
| `DuplicateFaceCleanupUtils` | 注册前 `prepareRegisterFace(applyId)` |
| 运维「重复人脸清理」 | 运维抽屉 |
| 运维「补救措施」 | 批量补注册失败人脸 |

## 增量同步与人脸注册时序

```
fetchNextPage 成功
  → handleUpdateComplete
  → updateLocalDatabase (longTermPassDao.insertOrUpdateUsers)
  → updateFace
       → DuplicateFaceCleanupUtils.prepareRegisterFace(id)
       → AESUtils.decryptRegisterFileToBitmap(id)
       → registerFaceByBitmap → registerFace
            → FaceRepository.registerBgr24
            → loadData(true)
```

下载图片失败会 **中止分页**（`updateNext=true`），人脸不会更新。

## 注销状态 status==2

增量列表中 `longPassCard.status == 2`：

- 删 `register/` 下 checkPhoto
- 删 `photo/` 下 photo
- **不**走注册流程

## 引擎初始化

`startPeriodicTask` 首次调用：

```java
if (FaceServer.getInstance().getFaceEngine() == null) {
    FaceServer.getInstance().init(application, callback);
}
```

若引擎 null 时注册：`registerFace` 直接 return 并打日志。

## RegisterAndRecognize vs LivenessDetect

| checkType (SP) | Activity |
|---:|---|
| 0 | `LivenessDetectJinActivity` |
| 1 | `LivenessDetectYuanActivity` |
| 2 | `LivenessDetectYuanAndJinActivity` |
| 3 | `RegisterAndRecognizeActivity` |

`registerFace` 内对 `RegisterAndRecognizeActivity` 使用 `registerBgr24(..., true)` 额外参数。

## 资源监控（辅助排障 OOM）

主定时任务每轮输出：

- Java Heap Used/Max
- 进程 PSS / 系统内存
- CPU 使用率

人脸库过大 + 图片缓存可能导致 `onLowMemory`。

## 数据路径

| 路径 | 内容 |
|---|---|
| `{externalFilesDir}/database/faceDB.db` | 虹软人脸 Room 库 |
| `{externalFilesDir}/register/` | 加密注册照 |
| `{externalFilesDir}/photo/` | 证件照 |

## 运维工具入口

连点进入 `CustomDrawerPopupView`：

| 菜单 | 用途 |
|---|---|
| 数据重新初始化 | 通行证与脸库严重不一致 |
| 补救措施 | 补注册失败 |
| 重复人脸清理 | 同一人多次注册 |
| RecognizeDebugActivity | 阈值现场调优 |

## 日志关键字

```
faceRepository.getTotalFaceCount()
FaceServer.getFaceEngine() == null
单个注册人脸: true/false
正在下载 / 下载失敗 checkPhoto
更新本地数据库成功
alignedBitmap.getWidth()
transform failed, code is
```

## 关联文档

- 背景同步：`doc/14-background/02-periodic-sync-heartbeat.md`
- 1 点完整性检查：`doc/14-background/03-daily-jobs.md`
