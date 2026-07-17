# 分层架构与模块依赖

## 四层模型

```
┌─────────────────────────────────────────────────────────┐
│ L1 表现层                                                │
│ Activity / Fragment / ViewModel / Adapter / Dialog       │
├─────────────────────────────────────────────────────────┤
│ L2 领域/业务层                                           │
│ FaceServer / FaceHelper / SerialManage / 业务 Utils      │
├─────────────────────────────────────────────────────────┤
│ L3 数据层                                                │
│ Room(DAO) / FaceRepository / CheckUnitRepository         │
├─────────────────────────────────────────────────────────┤
│ L4 基础设施                                              │
│ OkGo / ArcFace SDK / Camera / Glide / XUpdate            │
└─────────────────────────────────────────────────────────┘
```

> SQLCipher 4.5.2 目前仅存在于 Gradle 依赖中，两个 Room 数据库均未配置 `SupportFactory`，因此不属于当前实际运行链路。

## 核心调用链（查验成功路径）

```
用户刷卡
  → LivenessDetect*Activity.onCardRead
  → LongTermPassDao 查询
  → Document2/3 展示
  → FaceHelper.onPreviewFrame
  → FaceServer.searchFace
  → saveLongTermRecords / saveTemporaryCardRecords
  → Room insert
  → ArcFaceApplication.startUpDataToServer (30s)
  → ImageUploader + POST create-long/create-temporary
```

## 模块依赖矩阵

| 上游 | 下游 | 耦合方式 |
|------|------|----------|
| LoginActivity | ApiUtils, InfoStorage, FacePhotoViewModel | 直接调用 |
| Liveness* | FaceHelper, FaceServer, SerialManage, InfoStorage | 直接调用 |
| ArcFaceApplication | Room DAO, OkGo, ImageUploader | 定时任务 |
| 各 Activity | UrlConstants, OkGo | 大量直接 OkGo（非全经 ApiUtils） |
| Document2/3 | Glide Encrypted*, LongTermPass | 数据绑定 |
| ConstructionWorkers* | PagingSource → OkGo | 协程 |

## 双库职责分离

| 库 | 职责 | 不负责 |
|----|------|--------|
| YinchuanAirportDB | 通行证档案、待上传通行记录 | 人脸特征向量 |
| FaceDatabase | 人脸特征、注册图路径 | 通行证业务字段 |

关联键：注册时 `FaceEntity.userName` ≈ `LongTermPass.nickname`；比对后用 faceId 反查通行证。

## 网络双通道

1. **ApiUtils**：`get` / `post` / `getPassCard` — 自动 `tenant-id` + `timestamp` + Bearer
2. **直接 OkGo**：Activity、ArcFaceApplication、PagingSource — 手动加相同 Header

> 规范：Header 统一使用 `UrlConstants.TENANT_ID`。

## 配置三件套

| 存储 | 类 | 用途 |
|------|-----|------|
| SPUtils | UtilCodeX | checkType、direction、串口、日任务标志 |
| InfoStorage | InfoStorage | 设备、用户、interval、linshiID |
| DefaultSharedPreferences | ConfigUtil + Preference | ArcFace 识别阈值 |

全局索引：[00-glossary.md](../00-glossary.md)

## 渠道编译期注入

```
ChannelConfig (flavor)
    → UrlConstants.TENANT_ID / URL / businessAppApiBase()
    → ChannelConfig.SUPPORTS_TEMPORARY_PASS
    → Document2/3 + layout (flavor 源码)
```

## 线程模型

| 场景 | 线程 |
|------|------|
| 相机预览 | Camera 回调线程 |
| FaceHelper | 预览线程 / 内部 Handler |
| OkGo 回调 | OkGo 线程池 → UI |
| startUpDataToServer | ThreadUtils 固定速率池 |
| startPeriodicTask | 同上 + interval 分钟 |
| Room | 调用方线程（上传任务在后台线程） |
| Paging | IO 调度器 + Main |

## 文档地图

| 层 | 细文档目录 |
|----|------------|
| L1 登录 | [04-auth/](../04-auth/) |
| L1 查验 | [05-check/](../05-check/) |
| L2 人脸 | [10-face-engine/](../10-face-engine/) |
| L3 数据 | [12-data/](../12-data/) |
| L4 网络 | [11-network/](../11-network/) |
| 应用入口 | [02-arcface-application.md](./02-arcface-application.md) |
