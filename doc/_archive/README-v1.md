# airport-face 项目文档

> **历史快照，不保证与当前源码一致。** 当前维护入口为 [v2 技术文档](../README.md)。例如当前版本已是 `1.0.75`，且洛阳已启用临时证；本目录中的旧版本号、旧渠道开关和旧菜单入口仅用于追溯。

机场控制区通行证竖屏查验终端 Android 应用，基于 ArcSoft ArcFace 人脸识别 SDK，支持刷卡、二维码、纯人脸等多种查验模式。

## 按角色阅读

| 角色 | 建议阅读顺序 |
|------|-------------|
| 新接手开发 | 01 → 03 → 02 → 06 → 07/08 |
| 运维部署 | 02 → 12 → 13 → 20 → 21 |
| 接口联调 | 02 → 16 → 04 → 10 |
| 渠道定制 | 02 → 09 → 06 |
| 识别调参 | 14 → 15 → 07 |

## 术语表

| 术语 | 含义 |
|------|------|
| checkType | 查验模式（0~3），决定进入哪个 Activity |
| direction | 进出方向：1=进控制区，-1=出控制区 |
| 长期证 | `type=0`，对应 `Document2` |
| 临时证 | `type=1`，对应 `Document3`（洛阳渠道禁用） |
| tenant-id | HTTP 请求头，标识租户，值来自 `UrlConstants.TENANT_ID` |
| 零信任 | 深信服 SFUemSDK VPN，登录前需先认证 |
| needVerify | 通行记录是否需人工核销（施工人员模块） |

## 文档索引

| 文档 | 说明 |
|------|------|
| [01-project-overview.md](./01-project-overview.md) | 项目定位、技术栈、依赖、版本 |
| [02-product-flavors.md](./02-product-flavors.md) | 四渠道配置、打包、API 路径 |
| [03-architecture.md](./03-architecture.md) | 分层架构、初始化时序、存储路径 |
| [04-login-and-auth.md](./04-login-and-auth.md) | 零信任 + 后台登录 + Token |
| [05-pass-sync.md](./05-pass-sync.md) | 通行证全量/增量同步、人脸注册 |
| [06-check-modes.md](./06-check-modes.md) | checkType、direction、SP 键位 |
| [07-liveness-detect-flow.md](./07-liveness-detect-flow.md) | 刷卡+人脸状态机与业务规则 |
| [08-register-recognize-flow.md](./08-register-recognize-flow.md) | 纯人脸出区查验 |
| [09-pass-card-ui.md](./09-pass-card-ui.md) | Document2/3 卡面体系与状态章 |
| [10-offline-records-upload.md](./10-offline-records-upload.md) | 本地记录队列与上传细节 |
| [11-construction-workers.md](./11-construction-workers.md) | 施工人员三 Tab、筛选、核实 |
| [12-serial-port-config.md](./12-serial-port-config.md) | 串口 SP 键、默认值、弹窗 |
| [13-settings-and-ops-drawer.md](./13-settings-and-ops-drawer.md) | 运维抽屉完整菜单 |
| [14-recognize-settings.md](./14-recognize-settings.md) | ConfigUtil 阈值与 Preference |
| [15-face-database.md](./15-face-database.md) | FaceServer API 与容量 |
| [16-api-reference.md](./16-api-reference.md) | 接口路径、方法、参数 |
| [17-entity-models.md](./17-entity-models.md) | 实体字段、枚举、表结构 |
| [18-background-jobs.md](./18-background-jobs.md) | 定时任务时间表与标志位 |
| [19-app-update.md](./19-app-update.md) | XUpdate 集成与版本比较逻辑 |
| [20-boot-and-kiosk.md](./20-boot-and-kiosk.md) | 开机自启、HOME Launcher |
| [21-troubleshooting.md](./21-troubleshooting.md) | 分场景排查手册 |

## v1 → v2 迁移索引

| v1 | 当前 v2 文档 |
|----|-------------|
| 01 项目概览 | [01-overview/](../01-overview/) |
| 02 产品渠道 | [02-channel/](../02-channel/) |
| 03 架构 | [03-architecture/](../03-architecture/) |
| 04 登录鉴权 | [04-auth/](../04-auth/) |
| 05 通行证同步 | [06-pass-card/01 全量](../06-pass-card/01-pass-sync-full.md)、[02 增量](../06-pass-card/02-pass-sync-incremental.md) |
| 06 查验模式 | [05-check/01](../05-check/01-check-modes.md) |
| 07 刷卡活体 | [05-check/02-04](../05-check/) |
| 08 纯人脸 | [05-check/05](../05-check/05-register-recognize-activity.md) |
| 09 卡面 UI | [02-channel/02](../02-channel/02-flavor-document-ui.md)、[06-pass-card/04](../06-pass-card/04-document-ui-framework.md) |
| 10 离线记录 | [07-records/](../07-records/)、[14-background/01](../14-background/01-upload-scheduler.md) |
| 11 施工人员 | [08-construction/](../08-construction/) |
| 12 串口 | [09-serial/](../09-serial/) |
| 13 设置与运维 | [13-ops/](../13-ops/)、[18-widget/01](../18-widget/01-dialog-index.md) |
| 14 识别参数 | [10-face-engine/03](../10-face-engine/03-config-util.md)；设置 UI 细节仍以本 v1 篇和源码交叉核对 |
| 15 人脸库 | [10-face-engine/01](../10-face-engine/01-face-server.md)、[12-data/03](../12-data/03-face-database.md) |
| 16 API 参考 | [11-network/](../11-network/)；请求体字段仍以本 v1 篇和调用源码交叉核对 |
| 17 实体模型 | [12-data/02](../12-data/02-entity-field-reference.md)；网络 JSON 模型仍以本 v1 篇和源码交叉核对 |
| 18 后台任务 | [14-background/](../14-background/) |
| 19 应用更新 | [15-update/01](../15-update/01-xupdate-integration.md) |
| 20 开机/Kiosk | [16-device/01](../16-device/01-boot-kiosk.md) |
| 21 排查 | [17-troubleshooting/](../17-troubleshooting/) |

## 源码主包

```
app/src/main/java/com/arcsoft/arcfacedemo/
├── ArcFaceApplication.java   # 全局入口
├── ui/           # Activity、Fragment、ViewModel
├── network/      # UrlConstants、ApiUtils
├── data/         # FaceRepository、HTTP 回调
├── db/           # YinchuanAirportDB（业务库 v19）
├── facedb/       # FaceDatabase（人脸库 v1）
├── faceserver/   # FaceServer（ArcFace 引擎）
├── entity/       # 网络 JSON 实体
├── util/         # 工具（face/camera/glide/debug）
├── widget/       # 自定义 View、弹窗
├── manager/      # SoundManager、ToastDialogManager
├── service/      # TokenRefreshJobService
├── receiver/     # BootReceiver
├── Serial/       # 串口
├── preference/   # 识别参数 Preference
└── common/       # Constants（VPN、SDK Key）
```

渠道差异：`app/src/{yinchuan|chongqing|shihezi|luoyang}/`

## 关键文件速查

| 场景 | 文件 |
|------|------|
| 改 API 域名/租户 | `app/src/{flavor}/java/.../config/ChannelConfig.java` |
| 改接口路径 | `network/UrlConstants.java` |
| 改登录流程 | `ui/activity/LoginActivity.java` |
| 改定时任务 | `ArcFaceApplication.java` |
| 改卡面 UI | `app/src/{flavor}/res/layout/document2.xml` |
| 改查验逻辑 | `LivenessDetect*Activity.java` / `RegisterAndRecognizeActivity.java` |
| 改运维菜单 | `widget/dialog/CustomDrawerPopupView.java` |
