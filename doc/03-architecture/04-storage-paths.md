# 本地存储路径全索引

> 根路径均为应用 **外部私有目录** `Context.getExternalFilesDir(...)`，卸载应用即清除（除非另有说明）。

典型根路径（示例）：

```text
/storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/
```

---

## 路径总表

| 相对路径 | 创建方 | 内容 | 关联模块 |
|----------|--------|------|----------|
| `database/faceDB.db` | `FaceDatabase` | Room 人脸特征库 | [12-data/03-face-database.md](../12-data/03-face-database.md) |
| `Pictures/faceDB/registerFaces/` | `FaceServer` | 注册人脸原图（引擎侧） | [10-face-engine/01-face-server.md](../10-face-engine/01-face-server.md) |
| `register/` | `AESUtils` / 登录下载 | 通行证证件照（**AES 加密**） | [06-pass-card/03-encrypted-image-pipeline.md](../06-pass-card/03-encrypted-image-pipeline.md) |
| `photo/` | `AESUtils` / 登录下载 | 比对用人脸照（**AES 加密**） | 同上 |
| `decrypted_register/` | `LongPassCardsRemedialMeasuresUtils` | 补救工具解密输出 | [13-ops/03-remedial-tools.md](../13-ops/03-remedial-tools.md) |
| `records/` | 各查验 Activity | 通行抓拍图（未加密 JPEG） | [07-records/01-local-record-write.md](../07-records/01-local-record-write.md) |
| `log/` | `ALog` / `ArcFaceApplication` | 运行日志 | 运维导出 |
| `debugDump/` | `DebugInfoDumper` | 调试 dump | 开发 |
| `crashLog/` | `DebugInfoDumper` | 崩溃日志 | 开发 |
| `db/` | `ArcFaceApplication` | 业务 Room（`YinchuanAirportDB` 等）相关 | [12-data/01-room-business-db.md](../12-data/01-room-business-db.md) |

---

## AESUtils 路径 API

| 方法 | 返回目录/文件 |
|------|---------------|
| `getPhotoPath(passid)` | `{externalFilesDir}/photo/{passid}` |
| `getRegisterPath(passid)` | `{externalFilesDir}/register/{passid}` |
| `saveEncryptedFile(...)` | 写入上述目录 |

文件名通常以 **通行证 ID（passid）** 为键，无扩展名或固定后缀见 `AESUtils` 实现。

---

## 通行记录图片

写入位置（各 Activity 一致模式）：

```java
File directory = new File(getExternalFilesDir(null), "records");
// 文件名常含时间戳、passId 等
```

上传成功后由 `ArcFaceApplication.startUpDataToServer` 批量 POST，成功后删除本地记录与图片（见记录上传专篇）。

---

## SharedPreferences / 配置

| 存储 | 路径逻辑 | 说明 |
|------|----------|------|
| `InfoStorage` | SP 名 `yunduanchayan` | 设备、配置、登录用户 |
| `SPUtils` 其他键 | 系统 SP | 如 `mobile` |
| ArcFace 激活文件 | `Environment.getExternalStorageDirectory()` + `Constants.ACTIVE_CONFIG_FILE_NAME` | **公有存储**，`ActivationActivity` / `ActiveViewModel` |

激活文件走公有存储是历史设计，与通行证加密目录分离。

---

## FaceServer vs facedb 双存储说明

| 存储 | 路径 | 内容 |
|------|------|------|
| Room `face` 表 | `database/faceDB.db` | `feature_data` 字节数组 + `image_path` |
| FaceServer 目录 | `Pictures/faceDB/registerFaces/` | 引擎注册用的图片文件 |

登录 `registerFromFile(register/)` 从 **加密 register 目录** 解密注册进引擎；Room 与引擎需保持一致，补救工具可重建（见运维篇）。

---

## 渠道差异

存储路径 **无渠道差异**，均为同一包名 `getExternalFilesDir`。差异仅在下载的 API 与是否含临时证数据。

---

## 磁盘清理策略

| 触发 | 行为 |
|------|------|
| 登录全量同步前 | 可能清空 `register/`、`photo/` 后重新下载 |
| 增量同步 | 按 passid 更新单文件 |
| 运维「重新初始化」 | 删库、删目录、重下 |
| 上传成功 | 删 `records/` 对应文件 |
| `ImageDeleter` | 过期通行证关联图片清理 |

---

## 联调检查清单

- [ ] `register/{passid}` 存在且 `length > 0` 才能本地显示证件照
- [ ] Glide 加载加密图走 `EncryptedGlideFile`，勿直接用 `File` 路径
- [ ] 人脸搜不到时查 `faceDB.db` 与 `registerFaces/` 是否同步
- [ ] 存储权限：Android 11+ `MANAGE_EXTERNAL_STORAGE` 仅影响激活文件等公有路径
