# 离线记录与上传

## 涉及类

| 类 | 职责 |
|----|------|
| `ArcFaceApplication.startUpDataToServer()` | 30 秒周期上传调度 |
| `ImageUploader` | `uploadBitmap2()` 上传现场图 |
| `AESUtils` | 现场图本地加解密 |
| `LongTermRecords` / `TemporaryCardRecords` | Room 实体 |
| `LongTermRecordsDao` / `TemporaryCardRecordsDao` | 查询全部待上传 |

## 写入时机

查验 Activity 在 **人脸比对成功** 后：

1. 抓拍当前帧 → AES 加密保存到本地路径
2. 组装 `LongTermRecords` 或 `TemporaryCardRecords`
3. `db.*RecordsDao().insert(item)`
4. UI 刷新底部日志列表

此时记录仅在本地，**尚未**调用 create-long/create-temporary。

## 上传周期与并发

| 项 | 值 |
|----|-----|
| 周期 | `UPLOAD_LOG_TIME = 30_000` ms |
| 并发控制 | `AtomicBoolean isUploadingRecord` |
| 启动 | `LoginActivity` 登录成功 / 本地已有通行证时 |

若上一轮上传未完成，本轮直接跳过（CAS 失败）。

## 单条长期证上传步骤

```
1. 从 longTermRecordsDao().getAll() 取记录
2. 若 sitePhoto 为本地路径（/ 或 storage/ 开头）：
   a. AESUtils.decryptFileToBitmap(sitePhoto)
   b. imageUploader.uploadBitmap2(bitmap) → 服务端 path
   c. 删除本地加密文件
   d. item.sitePhoto = 服务端 path
3. POST URL_CREATE_LONG_RECORD，Body = Gson(LongTermRecords)
4. HTTP 200 → longTermRecordsDao().delete(item)
5. 失败则保留本地，下轮重试
```

临时证同理，接口为 `URL_CREATE_TEMP_RECORD`。

## 现场图存储

| 阶段 | 格式 |
|------|------|
| 查验写入 | AES 加密文件，路径存 `sitePhoto` |
| 上传前 | 解密为 Bitmap |
| 上传后 | 服务端返回 URL/path 写入 JSON |
| 清理 | 本地加密文件 `FileUtils.delete` |

## 抓拍文件清理

上传任务末尾扫描 `{externalFilesDir}/records/`：

- 删除修改时间超过 **3 天**（`3 * 86400000` ms）的文件

## 离线行为

`ArcFaceApplication.isOffLine == true`（Ping 失败）时：

- 记录仍正常写入 Room
- 上传 POST 可能失败，记录保留
- 网络恢复后 30 秒任务自动重试

## 在线查询（非上传）

`RecordsPopDialog`：

- **GET** `URL_GET_RESORD_PAGE`
- 按 `direction` 查服务端已入库记录
- 与本地待上传队列无关

## LongTermRecords 核心字段

| 字段 | 说明 |
|------|------|
| id | 主键（业务生成） |
| direction | `"1"` 进 / `"-1"` 出 / `"2"` 核验 |
| sitePhoto | 上传前本地路径，上传后服务端 path |
| faceSimilar | 相似度字符串 |
| needVerify | 是否需施工人员核销 |
| checkTime | 查验时间 |
| status / reason | 正常或异常及原因 |

完整字段见 [17-entity-models.md](./17-entity-models.md)。

## 相关文档

- 定时任务总览 → [18-background-jobs.md](./18-background-jobs.md)
- 施工人员核实 → [11-construction-workers.md](./11-construction-workers.md)
