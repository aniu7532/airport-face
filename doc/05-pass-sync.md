# 通行证同步与人脸注册

## 涉及类

| 类 | 路径 | 职责 |
|----|------|------|
| `LoginActivity` | `ui/activity/LoginActivity.java` | 首次全量拉取 |
| `ArcFaceApplication` | `ArcFaceApplication.java` | 定时增量同步 |
| `FacePhotoViewModel` | `ui/viewmodel/FacePhotoViewModel.java` | 批量注册协调 |
| `FaceRepository` | `data/FaceRepository.java` | 人脸 CRUD |
| `FaceServer` | `faceserver/FaceServer.java` | ArcFace 引擎注册/搜索 |
| `ImageDownloader` | `util/ImageDownloader.java` | 下载并 AES 加密存储照片 |
| `LongPassCardsReInitUtils` | `util/LongPassCardsReInitUtils.java` | 凌晨数据完整性检查 |
| `LongPassCardsRemedialMeasuresUtils` | `util/LongPassCardsRemedialMeasuresUtils.java` | 补救措施（补注册人脸） |
| `DuplicateFaceCleanupUtils` | `util/DuplicateFaceCleanupUtils.java` | 重复人脸清理 |

## 同步接口

| 接口 | 常量 | 说明 |
|------|------|------|
| 分页拉通行证 | `URL_GetLongPass` | `GET /check/pass/page-pass` |
| 通行证总数 | `passCount` | 用于判断是否需要全量同步 |
| 异常通行证上报 | `checkAbnormalCreate` | 注册失败时上报 |

## 全量同步（登录时）

1. 分页请求 `URL_GetLongPass`，携带 `pageNo`、`pageSize`
2. 解析 `LongPassCards` → 列表 `LongPassCard`
3. 每条通行证：
   - 写入 `LongTermPass`（Room 业务库）
   - `ImageDownloader` 下载头像 → AES 加密存本地
   - `FaceServer.registerFace()` 提取特征写入 `FaceEntity`
4. 更新进度 UI，完成后跳转查验页

## 增量同步（后台定时）

`ArcFaceApplication` 定时任务中：

1. 请求 `passCount` 获取服务端总数
2. 与本地 `LongTermPass` 数量对比
3. 若有新增，分页拉取并注册新人脸
4. 同步间隔由后台 `configInfo` 配置

## 图片存储

| 步骤 | 说明 |
|------|------|
| 下载 | Glide + `Authorization` 头，URL 为 `fileStreamUrl(path)` |
| 加密 | AES 加密后存 `{externalFilesDir}/faceDB/` |
| 展示 | Glide `SecureGlideModule` + `EncryptedFileDecoder` 解密显示 |

## 人脸注册

`FaceServer` 核心方法：

| 方法 | 说明 |
|------|------|
| `init()` | 初始化 ArcFace 引擎（检测 + 识别 + 活体） |
| `registerFace()` | 从 Bitmap 提取特征，写入 Room + 内存搜索库 |
| `searchFace()` | 1:N 比对，返回 `CompareResult` |
| `clearAllFaces()` | 清空人脸库 |

最大支持 **30000** 张人脸。

## 数据完整性保障

| 工具 | 触发时机 | 作用 |
|------|----------|------|
| `LongPassCardsReInitUtils` | 凌晨 1 点 | 检查本地通行证与人脸是否一致，必要时重新初始化 |
| `LongPassCardsRemedialMeasuresUtils` | 运维手动触发 | 批量补救缺失人脸的通行证 |
| `DuplicateFaceCleanupUtils` | 运维手动触发 | 清理重复注册的人脸记录 |

## 本地数据表

通行证主数据存于 `LongTermPass`（`db/entity/LongTermPass.java`），字段包括卡号、姓名、单位、区域、有效期、照片路径等。详见 [17-entity-models.md](./17-entity-models.md)。
