# 人脸库管理

## FaceServer 单例

**路径**：`faceserver/FaceServer.java`  
**容量**：`MAX_REGISTER_FACE_COUNT = 30000`

### 生命周期

```java
FaceServer.getInstance().init(context, onInitFinishedCallback);
// 使用...
FaceServer.getInstance().unInit();
```

`init` 创建 `FaceEngine`，加载 DB 中已有特征到内存搜索库。

### 核心方法

| 方法 | 说明 |
|------|------|
| `registerFace(Context, Bitmap, String userName)` | 检测→提特征→写 Room+内存 |
| `registerFace(Context, byte[] nv21, ...)` | NV21 注册 |
| `searchFace(FaceFeature)` | 1:N，返回 `SearchResult` |
| `removeFace(long faceId)` | 删除单个 |
| `clearAllFaces()` | 清空库 |
| `getFaceNumber()` | 当前数量 |
| `getFaceEngine()` | 底层引擎引用 |

注册失败抛 `RegisterFailedException`，登录同步时 POST `checkAbnormalCreate`。

## FaceEntity 表结构

表名：Room 默认 `face`（`FaceDatabase` v1）

| 列 | 类型 | 说明 |
|----|------|------|
| faceId | LONG PK AI | 自增 |
| userName | TEXT | 通常 = 通行证 nickname |
| imagePath | TEXT | 注册原图路径 |
| featureData | BLOB | 特征向量 |
| registerTime | LONG | 时间戳 |

## FaceRepository

**路径**：`data/FaceRepository.java`

| 能力 | 说明 |
|------|------|
| 分页加载 | 配合 `FacePhotoViewModel` |
| 删除 | 单条/批量 |
| 计数 | `getTotalFaceCount()` |

## 与通行证关联

```
LongPassCard.nickname  ──注册──>  FaceEntity.userName
比对 SearchResult.faceId  ──反查──>  FaceEntity  ──业务──>  LongTermPass
```

注意：nickname 重复会导致重复人脸，用 `DuplicateFaceCleanupUtils` 清理。

## FaceManageActivity

**路径**：`ui/activity/FaceManageActivity.java`

- 人脸列表 `FacePhotoAdapter`
- 手动添加/删除
- 批量注册回调 `BatchRegisterCallback`、`OnRegisterFinishedCallback`
- 非生产 Launcher 路径，运维调试用

## FacePhotoViewModel

- `LoginActivity` 全量同步时持有，完成后 `release()`
- `gotoActivity()` 前释放引擎避免重复 init

## 数据库文件

```
{externalFilesDir}/database/faceDB.db
```

与业务库 `airportDb.db` 分离，独立 Room 实例 `FaceDatabase.getInstance()`。

## 引擎与 ConfigUtil

识别阈值、活体阈值、检测角度等均从 `ConfigUtil` 读取，在 `FaceHelper` 构造时注入 `RecognizeConfiguration`。

## 相关文档

- 同步注册 → [05-pass-sync.md](./05-pass-sync.md)
- 识别参数 → [14-recognize-settings.md](./14-recognize-settings.md)
- 重复清理 → [21-troubleshooting.md](./21-troubleshooting.md)
