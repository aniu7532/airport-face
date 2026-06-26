# 实体字段参考 — LongTermPass / LongTermRecords

> LongTermPass：`app/src/main/java/com/arcsoft/arcfacedemo/db/entity/LongTermPass.java`  
> LongTermRecords：`app/src/main/java/com/arcsoft/arcfacedemo/db/entity/LongTermRecords.java`  
> 基类 Records：`app/src/main/java/com/arcsoft/arcfacedemo/entity/Records.java`（空类，无字段）

---

## LongTermPass（表 `long_term_pass`）

长期/临时 **通行证档案**，由服务端分页接口同步至本地，供查验时查询与人脸注册。

### 主键与标识

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `String` `@PrimaryKey @NonNull` | 通行证 ID，主键 |
| `applyId` | `String` | 通行申请 ID |
| `idCode` | `String` | 系统证件编号（如 A/B/C 类前缀） |
| `cardId` | `String` | 实体卡号（短） |
| `cardIdLong` | `String` | 实体卡长卡号 |

### 状态与分类

| 字段 | 类型 | 说明 |
|------|------|------|
| `score` | `int` | 证件积分 |
| `status` | `int` | 证件状态；**2 = 已注销**（同步时删本地图） |
| `type` | `int` | 证件类型：**0 = 长期证，1 = 临时证** |
| `templateType` | `int` | 模板类型：**1 = 蓝色，2 = 黄色** |
| `sex` | `int` | 性别 |

### 人员与组织

| 字段 | 类型 | 说明 |
|------|------|------|
| `userId` | `String` | 持卡人用户 ID |
| `nickname` | `String` | 持卡人姓名 |
| `idNo` | `String` | 身份证号 |
| `companyId` | `String` | 所属单位 ID |
| `companyName` | `String` | 所属单位名称 |
| `orgId` | `String` | 所属部门 ID |
| `orgName` | `String` | 所属部门名称 |
| `unitName` | `String` | 查验单位名称 |

### 有效期

| 字段 | 类型 | 说明 |
|------|------|------|
| `startDate` | `String` | 证件生效起始日期 |
| `expiryDate` | `String` | 证件有效期截止日期 |

### 通行区域

| 字段 | 类型 | 说明 |
|------|------|------|
| `areaRootIds` | `String[]` | 通行区域根节点 ID 列表 |
| `areaRootCodes` | `String[]` | 通行区域根节点编码列表 |
| `areaIds` | `String[]` | 可通行区域 ID 列表 |
| `areaCodes` | `String[]` | 可通行区域编码列表 |
| `areaDisplayCode` | `String[]` | 通行区域展示编码列表 |

### 引领与时段

| 字段 | 类型 | 说明 |
|------|------|------|
| `leadingPeople` | `String` | 引领人信息 **JSON 字符串** |
| `leadingPeopleId` | `String[]` | 引领人用户 ID 列表 |
| `timeControl` | `String` | 通行时段限制 **JSON 字符串**（`TimeControl[]`） |

辅助方法：

- `setleadingPeople(LeadingPeople[])` / `getLeadingPeople()` — Gson 序列化/反序列化
- `setTimeControl(TimeControl[])` / `getTimeControl()` — 同上

### 照片

| 字段 | 类型 | 说明 |
|------|------|------|
| `photo` | `String` | 证件照片路径或 URL（同步后存 `photo/` 目录） |
| `photoBytes` | `byte[]` | 证件照片二进制（Room 存逗号分隔 byte 字符串） |
| `checkPhoto` | `String` | 现场核验/注册照路径或 URL（`register/` 目录） |
| `checkPhotoBytes` | `byte[]` | 核验照二进制 |

### 风控标志

| 字段 | 类型 | 说明 |
|------|------|------|
| `isBlacklist` | `boolean` | 是否黑名单人员 |
| `isWithhold` | `boolean` | 是否暂扣证件 |
| `isWithdraw` | `boolean` | 是否已撤回 |
| `withholdStartDate` | `String` | 暂扣开始日期 |
| `withholdEndDate` | `String` | 暂扣结束日期 |

### 其他

| 字段 | 类型 | 说明 |
|------|------|------|
| `businessScope` | `String` | 经营范围 |
| `updateTime` | `String` | 数据最后更新时间；**增量同步游标**（`getMaxUpdateTime()`） |

### 数据来源

| 来源 | 说明 |
|------|------|
| 服务端 | `UrlConstants.URL_GetLongPass` 分页接口 → `LongPassCard` |
| 转换 | `Converters.convertToLongTermPass(LongPassCard)` |
| 写入 | `LongTermPassDao.insertOrUpdateUsers()` |

---

## LongTermRecords（表 `long_term_records`）

长期证 **单次通行/查验记录**，查验完成后写入，由 `startUpDataToServer` 上传后删除。

### 主键与关联

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `String` `@PrimaryKey @NonNull` | 记录 ID（SnowFlake 生成） |
| `passid` | `String` | 关联 `LongTermPass.id` |
| `applyId` | `String` | 通行申请 ID（二维码信息） |
| `parentld` | `String` | 引领人通行记录 ID（C 类临时随行） |
| `leadingPeopleld` | `String` | 引领人用户 ID（注意字段名拼写为 `ld`） |

### 证件快照

| 字段 | 类型 | 说明 |
|------|------|------|
| `cardId` | `String` | 实体卡号 |
| `idCode` | `String` | 系统证件编号 |
| `nickname` | `String` | 持卡人姓名 |
| `photo` | `String` | 证件照片 URL/路径 |
| `companyName` | `String` | 所属单位名称 |
| `expiryDate` | `String` | 证件有效期 |
| `templateType` | `int` | 1 蓝色 / 2 黄色 |
| `areaDisplayCode` | `String[]` | 区域展示编码 |
| `leadingPeople` | `String` | 引领人 JSON |

### 通行与区域

| 字段 | 类型 | 说明 |
|------|------|------|
| `direction` | `String` | 通行方向：**1 = 进，-1 = 出，2 = 核验** |
| `area` | `String` | 通行区域 ID |
| `areaName` | `String` | 区域名称（编码+名称，含子区域拼接） |

### 查验设备与人员

| 字段 | 类型 | 说明 |
|------|------|------|
| `deviceId` | `String` | 查验设备 ID |
| `deviceName` | `String` | 查验设备名称 |
| `checkUserId` | `String` | 查验人 ID（登录用户） |
| `checkUserName` | `String` | 查验人姓名 |
| `checkTime` | `String` | 查验时间 |

### 结果与质量

| 字段 | 类型 | 说明 |
|------|------|------|
| `status` | `String` | 通行状态字符串 `"true"` / `"false"`（正常/异常） |
| `reason` | `String` | 异常原因，如 `"人证不匹配"` |
| `faceSimilar` | `String` | 人脸相似度 |
| `faceQuality` | `String` | 人脸质量评分 |
| `needVerify` | `Boolean` | 是否需要人工复核 |

### 现场证据

| 字段 | 类型 | 说明 |
|------|------|------|
| `sitePhoto` | `String` | 现场照片：落库时为本地加密路径；上传后为服务端 URL |

### 辅助方法

- `setleadingPeople(LeadingPeople[])` / `getLeadingPeople()` — Gson JSON

### 字段赋值速查

| 字段 | 典型来源 |
|------|----------|
| `id`, `checkTime`, `needVerify` | 本地生成 |
| `passid` ~ `areaDisplayCode` | `LongTermPass` 快照 |
| `direction` | Activity `direction` |
| `deviceId`, `deviceName`, `checkUserName` | `InfoStorage` |
| `checkUserId` | `ApiUtils.userId` |
| `area`, `areaName` | `InfoStorage.deviceAreaDetail` → `Area` |
| `status`, `reason`, `faceSimilar`, `faceQuality` | 查验结果 |
| `parentld`, `leadingPeopleld` | C 类引领逻辑 + `linshiID` |
| `sitePhoto` | 加密 bitmap 至 `records/{id}.jpg` |

---

## LongTermPass vs LongTermRecords 对照

| 维度 | LongTermPass | LongTermRecords |
|------|--------------|-----------------|
| 语义 | 证件主数据 | 单次通行事件 |
| 生命周期 | 服务端同步，长期保留 | 查验产生，上传后删除 |
| 主键 | 通行证 `id` | SnowFlake 记录 `id` |
| 人脸 | 注册照 `checkPhoto` | 现场照 `sitePhoto` |
| 方向 | — | `direction` |
| 同步字段 | `updateTime` | — |

---

## TemporaryCardRecords 差异摘要

结构与 `LongTermRecords` 高度相似，主要差异：

| 字段 | LongTermRecords | TemporaryCardRecords |
|------|-----------------|----------------------|
| 引领记录 ID | `parentld` | `parentId` |
| 引领人 ID | `leadingPeopleld` | `leadingPeopleId` |
| 表名 | `long_term_records` | `temporary_card_records` |

字段说明可参照 LongTermRecords 同名语义。
