# airport-face 技术文档（细粒度版 v2）

> 机场控制区通行证竖屏查验终端 · 基于 ArcSoft ArcFace  
> 本文档体系按 **模块 → 子模块 → 类/方法** 三级组织，共 **60** 篇（不含本索引与 `_archive`）。

---

## 快速导航

| 我想… | 从这里开始 |
|--------|------------|
| 查 SP / 配置键 | [00-glossary.md](./00-glossary.md) |
| 理解登录全流程 | [04-auth/](./04-auth/) |
| 改查验逻辑 | [05-check/](./05-check/) |
| 查 ApiUtils / 接口 | [11-network/01-api-utils.md](./11-network/01-api-utils.md) |
| 改渠道配置 | [02-channel/01-flavor-config.md](./02-channel/01-flavor-config.md) |
| 升级德卡读卡器 SDK | [sdk/Android_sdk_release2.56/README.md](./sdk/Android_sdk_release2.56/README.md) |
| 排查线上问题 | [17-troubleshooting/](./17-troubleshooting/) |
| 看旧版总览 | [_archive/](./_archive/) |

---

## 文档目录树

### 全局

- [00-glossary.md](./00-glossary.md) — SP / InfoStorage / 常量 / 枚举 / 路径 **全局索引**

### 01-overview 项目总览

- [01-project-overview.md](./01-overview/01-project-overview.md)
- [02-tech-stack-and-deps.md](./01-overview/02-tech-stack-and-deps.md)
- [03-repo-layout.md](./01-overview/03-repo-layout.md)
- [04-build-release.md](./01-overview/04-build-release.md) — 构建变体、签名、ABI、混淆与发布检查

### 02-channel 产品渠道

- [01-flavor-config.md](./02-channel/01-flavor-config.md) — 四渠道 ChannelConfig 逐字段
- [02-flavor-document-ui.md](./02-channel/02-flavor-document-ui.md) — Document2/3 与 layout 差异
- [03-flavor-business-rules.md](./02-channel/03-flavor-business-rules.md) — TENANT_PREFIX、临时证开关、API 路径

### 03-architecture 架构

- [01-layered-architecture.md](./03-architecture/01-layered-architecture.md) — 分层、调用链、依赖矩阵
- [02-arcface-application.md](./03-architecture/02-arcface-application.md) — Application 每个重要方法
- [03-thread-and-async.md](./03-architecture/03-thread-and-async.md) — ThreadUtils、OkGo、RxJava
- [04-storage-paths.md](./03-architecture/04-storage-paths.md) — register/photo/records 等路径
- [05-viewmodels.md](./03-architecture/05-viewmodels.md) — 11 个 ViewModel 索引

### 04-auth 登录鉴权

- [01-zero-trust-vpn.md](./04-auth/01-zero-trust-vpn.md) — SFUemSDK、initZeroTrust
- [02-backend-login-token.md](./04-auth/02-backend-login-token.md) — login()、Token、vertical-client-login
- [03-login-init-chain.md](./04-auth/03-login-init-chain.md) — MAC/Config/User/Pass 初始化链
- [04-login-activity-methods.md](./04-auth/04-login-activity-methods.md) — LoginActivity 方法索引

### 05-check 查验核心 ★

- [01-check-modes.md](./05-check/01-check-modes.md) — checkType / direction / tipsLoc
- [02-liveness-jin-activity.md](./05-check/02-liveness-jin-activity.md) — **短距**刷卡+人脸
- [03-liveness-yuan-activity.md](./05-check/03-liveness-yuan-activity.md) — **长距**刷卡+人脸
- [04-liveness-yuan-jin-activity.md](./05-check/04-liveness-yuan-jin-activity.md) — **双读卡器**
- [05-register-recognize-activity.md](./05-check/05-register-recognize-activity.md) — **纯人脸**出区
- [06-card-read-validate.md](./05-check/06-card-read-validate.md) — 刷卡校验、linshiID、引领人
- [07-face-match-pipeline.md](./05-check/07-face-match-pipeline.md) — FaceHelper→FaceServer 管线

### 06-pass-card 通行证与卡面

- [01-pass-sync-full.md](./06-pass-card/01-pass-sync-full.md) — 登录全量同步逐步
- [02-pass-sync-incremental.md](./06-pass-card/02-pass-sync-incremental.md) — 周期增量同步
- [03-encrypted-image-pipeline.md](./06-pass-card/03-encrypted-image-pipeline.md) — 下载/AES/Glide
- [04-document-ui-framework.md](./06-pass-card/04-document-ui-framework.md) — Document1/2/3 框架

### 07-records 通行记录

- [01-local-record-write.md](./07-records/01-local-record-write.md) — 写入时机、字段赋值
- [02-record-upload.md](./07-records/02-record-upload.md) — 30s 上传、CAS、删除

### 08-construction 施工人员

- [01-overview.md](./08-construction/01-overview.md)
- [02-write-off-verify.md](./08-construction/02-write-off-verify.md) — 核销 + VerifyDialog
- [03-access-record-paging.md](./08-construction/03-access-record-paging.md)
- [04-statistics-filters.md](./08-construction/04-statistics-filters.md)

### 09-serial 串口

- [01-rfid-card-reader.md](./09-serial/01-rfid-card-reader.md) — 短距德卡/华大、长距 EC_API、SDK 版本与生命周期
- [02-qr-scanner.md](./09-serial/02-qr-scanner.md)

### 10-face-engine 人脸引擎

- [01-face-server.md](./10-face-engine/01-face-server.md) — 全部 public API
- [02-face-helper-filters.md](./10-face-engine/02-face-helper-filters.md)
- [03-config-util.md](./10-face-engine/03-config-util.md) — 阈值默认值

### 11-network 网络层

- [01-api-utils.md](./11-network/01-api-utils.md) — **ApiUtils 专篇**
- [02-url-constants.md](./11-network/02-url-constants.md) — 每个 URL + 渠道示例
- [03-http-callbacks.md](./11-network/03-http-callbacks.md)

### 12-data 数据层

- [01-room-business-db.md](./12-data/01-room-business-db.md) — DB + 三个 Dao 方法表
- [02-entity-field-reference.md](./12-data/02-entity-field-reference.md) — 逐字段说明
- [03-face-database.md](./12-data/03-face-database.md) — facedb / FaceDao / FaceEntity

### 13-ops 运维

- [01-custom-drawer-menu.md](./13-ops/01-custom-drawer-menu.md) — 每个菜单 onClick
- [02-verify-feature-settings.md](./13-ops/02-verify-feature-settings.md)
- [03-remedial-tools.md](./13-ops/03-remedial-tools.md) — ReInit/Remedial/去重

### 14-background 后台任务

- [01-upload-scheduler.md](./14-background/01-upload-scheduler.md)
- [02-periodic-sync-heartbeat.md](./14-background/02-periodic-sync-heartbeat.md)
- [03-daily-jobs.md](./14-background/03-daily-jobs.md) — 1点/2点/10点

### 15-update 版本更新

- [01-xupdate-integration.md](./15-update/01-xupdate-integration.md)

### 16-device 设备

- [01-boot-kiosk.md](./16-device/01-boot-kiosk.md)
- [02-runtime-permissions.md](./16-device/02-runtime-permissions.md) — 相机/存储权限、所有文件访问与厂商硬件授权边界

### 17-troubleshooting 排查

- [01-login-network.md](./17-troubleshooting/01-login-network.md)
- [02-face-recognize.md](./17-troubleshooting/02-face-recognize.md)
- [03-card-serial.md](./17-troubleshooting/03-card-serial.md)
- [04-record-sync.md](./17-troubleshooting/04-record-sync.md)

### 18-widget 组件

- [01-dialog-index.md](./18-widget/01-dialog-index.md) — 弹窗/抽屉/XPopup 全索引

### sdk 厂商原始资料

- [Android_sdk_release2.56/README.md](./sdk/Android_sdk_release2.56/README.md) — 德卡读卡器 SDK 资源清单、升级兼容性、接入流程与验收

> `sdk/` 下的 AAR、APK、PDF、图片和 Demo ZIP 是厂商原始交付物，仅供归档与联调；App 实际编译依赖仍位于 `app/libs/`。

### 延伸阅读

- [../refactor/README.md](../refactor/README.md) — 架构全景、技术债与重构优先级；该目录有独立版本基准，涉及当前行为时以 v2 文档与源码为准

### _archive 旧版文档（v1 扁平 22 篇）

仅保留作历史参考，不保证与当前源码一致。迁移关系见 [_archive/README-v1.md](./_archive/README-v1.md)，日常开发以 v2 子目录为准。

---

## 单篇文档结构约定

每篇细文档尽量包含：

1. 职责边界  
2. 涉及类表（类名 | 路径 | 职责 | 调用方）  
3. public / 关键方法表  
4. 主流程 mermaid 图  
5. 异常分支表  
6. SP / InfoStorage / 配置键  
7. 渠道差异  
8. 联调检查清单  

---

## 源码主包

```
app/src/main/java/com/arcsoft/arcfacedemo/
├── ArcFaceApplication.java
├── ui/ network/ data/ db/ facedb/ faceserver/
├── entity/ util/ widget/ Serial/ preference/
├── manager/ service/ receiver/ common/
└── config/  → 仅存在于 app/src/{flavor}/java/
```

---

## 推荐阅读顺序（新人）

```
00-glossary → 01-overview → 03-architecture
→ 04-auth → 06-pass-card → 05-check
→ 07-records → 11-network/01-api-utils
→ 02-channel（若做渠道定制）
```
