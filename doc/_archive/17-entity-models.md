# 数据模型说明

## 枚举与业务常量

### 通行证 type

| 值 | 含义 |
|----|------|
| 0 | 长期证 |
| 1 | 临时证 |

### 通行证 status

| 值 | 含义 |
|----|------|
| 2 | 已注销 |
| 其他 | 正常/业务定义状态 |

### 通行 direction（记录）

| 值 | 含义 |
|----|------|
| `"1"` | 进控制区 |
| `"-1"` | 出控制区 |
| `"2"` | 核验 |

### checkType（SP）

| 值 | Activity |
|----|----------|
| 0~3 | 见 [06-check-modes.md](./06-check-modes.md) |

---

## entity 包（网络 JSON）

### Login

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | String | 用户 ID |
| accessToken | String | 访问令牌 |
| refreshToken | String | 刷新令牌 |
| expiresTime | String | 过期时间 |

### LongPassCard（API 单条通行证）

与 `LongTermPass` 字段基本对应，数组字段为 `String[]`，含 `leadingPeople` 对象数组、`timeControl` 等。

### LongPassCards（分页包装）

| 字段 | 说明 |
|------|------|
| list | `LongPassCard[]` |
| total | 总条数 |

### CardRecords / ListDTO（施工人员列表项）

含 `nickname`、`idCode`、`checkTime`、`direction`、`companyName`、`needVerify` 等，供 Paging Adapter 展示。

### ApiResponse\<T\> / Base\<T\>

| 字段 | 说明 |
|------|------|
| code | 200 成功 |
| msg | 消息 |
| data | 业务载荷 |

### Version（更新）

| 字段 | 说明 |
|------|------|
| version | 版本名字符串，与 `AppUtils.getAppVersionName()` 比较 |
| url | APK 下载地址 |
| remark | 更新说明 |
| isForceUpdate | 1=强制 |

### LeadingPeople

| 字段 | 说明 |
|------|------|
| id | 引领人用户 ID |
| nickname | 姓名 |
| cardId | 卡号 |

### TimeControl

时段通行限制，序列化进 `LongTermPass.timeControl` JSON。

---

## db 包（Room）

### 表 long_term_pass → LongTermPass

主键：`id`（String）

| 字段 | 说明 |
|------|------|
| cardId / cardIdLong | 短/长卡号 |
| idCode | 证件编号 |
| nickname / companyName / orgName | 人员与组织 |
| type / status / score | 类型、状态、积分 |
| expiryDate / startDate | 有效期 |
| areaIds / areaCodes / areaRootIds | 通行区域 |
| areaDisplayCode | 展示用区域编码 |
| photo / photoBytes | 证件照 path 或二进制 |
| leadingPeople | JSON 字符串 |
| leadingPeopleId | 引领人 ID 数组 |
| timeControl | 时段 JSON |
| templateType | 1 蓝 / 2 黄 |
| isBlacklist / isWithhold / isWithdraw | 风险状态 |
| withholdStartDate / withholdEndDate | 暂扣区间 |
| updateTime | 增量同步游标 |
| businessScope / sex / idNo / unitName | 扩展信息 |

### 表 long_term_records → LongTermRecords

主键：`id`（String，业务生成）

继承 `Records`，额外含：

| 字段 | 说明 |
|------|------|
| passid | 通行证 ID |
| sitePhoto | 现场照（先本地后服务端 path） |
| faceSimilar / faceQuality | 比对与质量 |
| deviceId / deviceName | 查验设备 |
| checkUserId / checkUserName | 查验员 |
| area / areaName | 通行区域 |
| status / reason | 正常或异常 |
| needVerify | 是否待核销 |
| leadingPeopleld / parentld | 引领人关联 |

### 表 temporary_card_records → TemporaryCardRecords

结构类似长期记录，增加临时证 ID、引领人记录 ID 等字段。

---

## facedb → FaceEntity

见 [15-face-database.md](./15-face-database.md)。

---

## ui/model

| 类 | 用途 |
|----|------|
| CompareResult | faceEntity + similar + liveness + pass 标志 |
| PreviewConfig | 预览宽高手旋转 |
| ItemShowInfo | 调试列表项 |

---

## Converters（Room）

`util/Converters.java`：

- `String[]` ↔ JSON
- `byte[]` 存储
- 复杂对象列表序列化

## 相关文档

- 接口返回 → [16-api-reference.md](./16-api-reference.md)
- 上传记录 → [10-offline-records-upload.md](./10-offline-records-upload.md)
