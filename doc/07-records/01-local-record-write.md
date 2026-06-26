# 本地通行记录写入

> 涉及实体：`LongTermRecords`、`TemporaryCardRecords`  
> 涉及 Activity：`LivenessDetectJinActivity`、`LivenessDetectYuanActivity`、`LivenessDetectYuanAndJinActivity`、`RegisterAndRecognizeActivity`

通行记录在 **人脸/证件查验完成** 后写入 Room，采用「先落库、后批量上传」策略（各 Activity 中 `if (true)` 强制本地优先）。

---

## 写入入口与触发时机

### 长期证（type == 0）

| Activity | 入口方法 | 触发场景 |
|----------|----------|----------|
| `LivenessDetectJinActivity` | `saveLongTermRecords(...)` | 活体检测 + 人证比对完成后（进/出/核验） |
| `LivenessDetectYuanActivity` | `saveLongTermRecords(...)` | 同上（远距模式） |
| `LivenessDetectYuanAndJinActivity` | `saveLongTermRecords(...)` | 同上（远+近模式） |
| `RegisterAndRecognizeActivity` | `saveLongRecord(...)` | 注册识别页查验通过后 |

**调用链示例（LivenessDetect）**：

```
人证比对结果
    └── saveLongTermRecords(longTermPass, bitmap, faceSimilar, quality, status)
            └── ThreadUtils → saveLongTermRecordsToDb(records, bitmap)
                    └── longTermRecordsDao().insert(records)
```

### 临时证（type == 1）

| Activity | 入口方法 | 前置条件 |
|----------|----------|----------|
| 上述四个 Activity | `saveTemporaryRecords` / `saveShortRecord` | `ChannelConfig.SUPPORTS_TEMPORARY_PASS == true`（洛阳为 false，直接 return） |

**洛阳渠道**：不支持临时证，`saveShortRecord` / `saveTemporaryRecords` 开头判断后直接返回。

---

## 字段赋值来源

以下以 `LivenessDetectJinActivity.saveLongTermRecords` 为准（其余 Activity 逻辑一致，命名略有差异）。

### 系统生成字段

| 字段 | 赋值来源 |
|------|----------|
| `id` | `new SnowFlake(1,1,1).nextId() + ""` |
| `checkTime` | `TimeUtils.getNowString()` |
| `needVerify` | `VerifyFeatureSettings.needVerifyForNewRecord()` |

### 查验结果

| 字段 | 赋值来源 |
|------|----------|
| `status` | 参数 `status` / `isPass`，`String.valueOf(...)` → `"true"` / `"false"` |
| `reason` | `status == false` 时固定 `"人证不匹配"` |
| `faceSimilar` | 参数 `faceSimilar` 转字符串 |
| `faceQuality` | `livenessDetectViewModel.getFeatureValue(bitmap)` 覆盖传入的 quality |

### 来自 LongTermPass（证件档案）

| 字段 | 来源 |
|------|------|
| `passid` | `longTermPass.id` |
| `idCode` | `longTermPass.idCode` |
| `cardId` | `longTermPass.cardId` |
| `applyId` | `longTermPass.applyId` |
| `nickname` | `longTermPass.nickname` |
| `photo` | `longTermPass.photo` |
| `leadingPeople` | `longTermPass.leadingPeople`（JSON 字符串） |
| `companyName` | `longTermPass.companyName` |
| `expiryDate` | `longTermPass.expiryDate` |
| `templateType` | `longTermPass.templateType` |
| `areaDisplayCode` | `longTermPass.areaDisplayCode` |

### 来自 InfoStorage（登录/设备配置）

| 字段 | InfoStorage 键 | 默认值 |
|------|----------------|--------|
| `direction` | Activity 成员 `direction` | — |
| `deviceId` | `"deviceId"` | `""` |
| `deviceName` | `"deviceName"` | `"立式查验终端"` |
| `checkUserName` | `"loginName"` | `""` |
| `area` / `areaName` | `"deviceAreaDetail"` JSON → `Area` 对象 | — |

**区域名称拼接**：`area.getCode() + area.getName()`，若有 `children` 则逐个追加子区域 `code+name`。

### 来自 ApiUtils / 会话

| 字段 | 来源 |
|------|------|
| `checkUserId` | `ApiUtils.userId` |

### C 类证件引领关系（仅 LongTermRecords）

| 条件 | 字段 | 赋值 |
|------|------|------|
| `idCode.startsWith("C")` 且 `leadingPeopleId` 非空 | `parentld` | Activity 成员 `localLongId`（引领人长期记录 ID） |
| 同上 | `leadingPeopleld` | `infoStorage.getString("linshiID", "")` |
| 否则 | — | `localLongId = longTermRecords.id`（当前记录作为后续临时证 parent） |

### 现场照片 sitePhoto（saveLongTermRecordsToDb / saveTemporaryRecordsToDb）

| 步骤 | 说明 |
|------|------|
| 目录 | `getExternalFilesDir(null)/records/` |
| 文件名 | `{records.id}.jpg` |
| 加密 | `AESUtils.encryptBitmapToFile(bitmap, file2, AESUtils.generateKey())` |
| 字段值 | `records.sitePhoto = file2.getAbsolutePath()` |
| 无 bitmap | 不设置 sitePhoto，直接 insert |

---

## TemporaryCardRecords 额外/差异字段

| 字段 | 来源 | 说明 |
|------|------|------|
| `parentId` | `localLongId` | 关联引领人长期记录 ID |
| `leadingPeopleId` | `infoStorage.getString("linshiID", "")` | 引领人 userId |
| `parentld` / `leadingPeopleld` | — | **仅 LongTermRecords 使用**（Temporary 用 `parentId` + `leadingPeopleId`） |

其余字段与长期记录相同来源。

---

## `saveRecord(LongTermPass)` — 引领人 ID 缓存

查验长期证（尤其 C 类）后调用：

```java
if (longTermPass != null && ObjectUtils.isNotEmpty(longTermPass.userId)) {
    infoStorage.saveString("linshiID", longTermPass.userId);
} else {
    infoStorage.saveString("linshiID", "");
}
```

供后续临时证记录写入 `leadingPeopleId` / `leadingPeopleld`。

`ArcFaceApplication.onCreate()` 会 `infoStorage.remove("linshiID")` 清除上次会话缓存。

---

## 写入后 UI 更新

`saveLongTermRecords` / `saveTemporaryRecords` 的 `onSuccess`：

- `checkList` 头部插入新记录（最多保留 20 条）
- `mListAdapter.notifyDataSetChanged()`

`RegisterAndRecognizeActivity` 仅在 `isPass == true` 时更新列表。

---

## 分支逻辑（设计 vs 现状）

```java
// 设计意图
if (ArcFaceApplication.getApplication().isOffLine()) {
    save*RecordsToDb(...);
} else {
    // 先 uploadBitmap2，再 upload*Records(...)
}

// 当前代码
if (true) {
    save*RecordsToDb(...);  // 始终本地
}
```

即时上传路径 `uploadLongTermRecords` / `uploadTemporaryRecords` 仍保留，供未来恢复在线即时上传或手动调用。

---

## 各 Activity 方法名对照

| 长期证 | 临时证 | 落库 | Activity |
|--------|--------|------|----------|
| `saveLongTermRecords` | `saveTemporaryRecords` | `saveLongTermRecordsToDb` / `saveTemporaryRecordsToDb` | LivenessDetect* |
| `saveLongRecord` | `saveShortRecord` | 同上 | RegisterAndRecognize |
| 统一入口 | `savePassRecord(card, ...)` 按 type 分发 | — | RegisterAndRecognize |
