# 人脸库管理

## 涉及类

| 类 | 路径 | 职责 |
|----|------|------|
| `FaceServer` | `faceserver/FaceServer.java` | ArcFace 引擎封装 |
| `FaceRepository` | `data/FaceRepository.java` | 人脸 CRUD 仓库 |
| `FaceDatabase` | `facedb/FaceDatabase.java` | Room 数据库 |
| `FaceEntity` | `facedb/entity/FaceEntity.java` | 人脸实体 |
| `FaceDao` | `facedb/dao/FaceDao.java` | DAO |
| `FaceManageActivity` | `ui/activity/FaceManageActivity.java` | 人脸库管理页 |
| `FacePhotoViewModel` | `ui/viewmodel/FacePhotoViewModel.java` | 批量注册 |
| `FacePhotoAdapter` | `widget/FacePhotoAdapter.java` | 人脸列表适配器 |
| `RegisterFailedException` | `faceserver/RegisterFailedException.java` | 注册失败异常 |

## FaceServer 核心 API

| 方法 | 说明 |
|------|------|
| `getInstance()` | 单例 |
| `init(Context)` | 初始化引擎（检测+识别+活体） |
| `registerFace(Context, byte[], String)` | 注册人脸特征 |
| `registerFace(Context, Bitmap, String)` | 从 Bitmap 注册 |
| `searchFace(FaceFeature)` | 1:N 搜索 |
| `removeFace(long faceId)` | 删除单张 |
| `clearAllFaces()` | 清空全部 |
| `getFaceNumber()` | 当前人脸数量 |
| `unInit()` | 释放引擎 |

最大容量：**30000** 张人脸。

## FaceEntity 字段

| 字段 | 说明 |
|------|------|
| `faceId` | 自增主键 |
| `userName` | 关联姓名（通行证 nickname） |
| `imagePath` | 注册原图路径 |
| `featureData` | 特征向量（BLOB） |
| `registerTime` | 注册时间戳 |

## 注册流程

```
通行证同步 → ImageDownloader 下载照片
→ FaceServer.registerFace(bitmap, nickname)
→ ArcFace extractFeature → 特征向量
→ 写入 FaceEntity（Room）
→ 加入内存搜索库
```

## 搜索流程

```
相机帧 → FaceHelper.detectFace → extractFeature
→ FaceServer.searchFace(feature)
→ SearchResult（faceId, similarScore）
→ 映射 FaceEntity → CompareResult
```

## FaceManageActivity

运维/开发用页面，功能：

- 查看已注册人脸列表
- 手动添加/删除人脸
- 批量注册（`FacePhotoViewModel` + `BatchRegisterCallback`）

非生产主路径，Launcher 未指向此页。

## 人脸与通行证关联

- 注册时 `userName` 存通行证 `nickname`
- 搜索返回 `faceId` 后，通过 `LongTermPass` 表查完整通行证信息
- `DuplicateFaceCleanupUtils` 处理同一人多次注册导致的重复

## 数据库

| 项 | 值 |
|----|-----|
| 库名 | `faceDB.db` |
| 路径 | `{externalFilesDir}/database/faceDB.db` |
| Schema 版本 | 1 |
| 加密 | SQLCipher（与业务库相同） |
