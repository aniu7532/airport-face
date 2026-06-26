# 数据模型说明

## entity 包（网络实体）

路径：`entity/`

| 类 | 说明 | 关键字段 |
|----|------|----------|
| `Login` | 登录响应 | `accessToken`, `refreshToken`, `userId` |
| `User` | 用户信息 | `nickname`, `username`, `deptName` |
| `ApiResponse<T>` | 通用 API 响应 | `code`, `msg`, `data` |
| `Base` / `Base2` | 通用响应基类 | `code`, `msg` |
| `LongPassCard` | 单条通行证 | `id`, `cardId`, `nickname`, `type`, `status`, `areaIds`, `leadingPeople` |
| `LongPassCards` | 通行证分页 | `list`, `total` |
| `Records` | 通行记录摘要 | `nickname`, `checkTime`, `direction` |
| `CardRecords` | 卡记录 | 卡号、时间等 |
| `Area` | 管制区域 | `id`, `name`, `code`, `children` |
| `LeadingPeople` | 引领人 | `id`, `nickname`, `cardId` |
| `TimeControl` | 时段控制 | 生效时间段 |
| `Tag` | 标签 | `name`, `color` |
| `Version` | 版本信息 | `versionCode`, `downloadUrl`, `tenantId` |
| `CheckUnit` (kt) | 申办单位 | `id`, `name` |
| `InOutStatisticsResult` (kt) | 进出统计 | 日期、人数 |
| `DeviceResult` (kt) | 设备查询结果 | 设备列表 |

## db 包（Room 业务实体）

路径：`db/entity/`

### LongTermPass（通行证主表）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | String (PK) | 通行证 ID |
| `cardId` | String | 实体卡号 |
| `idCode` | String | 系统证件编号 |
| `nickname` | String | 持卡人姓名 |
| `companyName` | String | 单位名称 |
| `orgName` | String | 部门名称 |
| `type` | int | 0 长期 / 1 临时 |
| `status` | int | 证件状态（2=注销） |
| `expiryDate` | String | 有效期 |
| `startDate` | String | 生效日期 |
| `areaIds` | String | 可通行区域 ID（JSON） |
| `areaCodes` | String | 可通行区域编码（JSON） |
| `imagePath` | String | 本地加密照片路径 |
| `featureData` | byte[] | 人脸特征（可选冗余） |
| `leadingPeople` | String | 引领人 JSON |

### LongTermRecords（长期证通行记录）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long (PK, 自增) | 本地记录 ID |
| `passId` | String | 通行证 ID |
| `direction` | int | 1 进 / -1 出 |
| `checkTime` | long | 查验时间戳 |
| `photoPath` | String | 现场抓拍路径 |
| `uploaded` | boolean | 是否已上传 |
| `mac` | String | 设备 MAC |

### TemporaryCardRecords（临时证通行记录）

结构与 `LongTermRecords` 类似，额外包含：

| 字段 | 说明 |
|------|------|
| `tempPassId` | 临时证 ID |
| `leaderPassId` | 引领人通行证 ID |

## facedb 包（人脸实体）

### FaceEntity

| 字段 | 类型 | 说明 |
|------|------|------|
| `faceId` | long (PK, 自增) | 人脸 ID |
| `userName` | String | 关联姓名 |
| `imagePath` | String | 注册原图路径 |
| `featureData` | byte[] | 特征向量 BLOB |
| `registerTime` | long | 注册时间 |

## ui/model 包（UI 模型）

| 类 | 说明 |
|----|------|
| `CompareResult` | 比对结果（faceEntity, similar, pass） |
| `PreviewConfig` | 相机预览配置 |
| `ItemShowInfo` | 列表展示项 |

## DAO 接口

| DAO | 实体 | 主要方法 |
|-----|------|----------|
| `LongTermPassDao` | `LongTermPass` | `insert`, `queryByCardId`, `getCount`, `deleteAll` |
| `LongTermRecordsDao` | `LongTermRecords` | `insert`, `queryNotUploaded`, `markUploaded` |
| `TemporaryCardRecordsDao` | `TemporaryCardRecords` | 同上 |
| `FaceDao` | `FaceEntity` | `insert`, `delete`, `getAll`, `queryByFaceId` |

## 类型转换

`util/Converters.java` 提供 Room `@TypeConverter`：

- `String[]` ↔ JSON String
- `List<LeadingPeople>` ↔ JSON String
- `byte[]` ↔ 数据库存储
