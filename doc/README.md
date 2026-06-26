# airport-face 项目文档

机场控制区通行证竖屏查验终端 Android 应用，基于 ArcSoft ArcFace 人脸识别 SDK，支持刷卡、二维码、纯人脸等多种查验模式。

## 文档索引

| 文档 | 说明 |
|------|------|
| [01-project-overview.md](./01-project-overview.md) | 项目定位、技术栈、模块总览 |
| [02-product-flavors.md](./02-product-flavors.md) | 四渠道配置与差异 |
| [03-architecture.md](./03-architecture.md) | 整体架构与分层设计 |
| [04-login-and-auth.md](./04-login-and-auth.md) | 登录与鉴权 |
| [05-pass-sync.md](./05-pass-sync.md) | 通行证同步与人脸注册 |
| [06-check-modes.md](./06-check-modes.md) | 四种查验模式 |
| [07-liveness-detect-flow.md](./07-liveness-detect-flow.md) | 刷卡+人脸查验流程 |
| [08-register-recognize-flow.md](./08-register-recognize-flow.md) | 纯人脸出区查验 |
| [09-pass-card-ui.md](./09-pass-card-ui.md) | 通行证卡面 UI |
| [10-offline-records-upload.md](./10-offline-records-upload.md) | 离线记录与上传 |
| [11-construction-workers.md](./11-construction-workers.md) | 施工人员管理 |
| [12-serial-port-config.md](./12-serial-port-config.md) | 串口读卡配置 |
| [13-settings-and-ops-drawer.md](./13-settings-and-ops-drawer.md) | 运维侧边栏与设置 |
| [14-recognize-settings.md](./14-recognize-settings.md) | ArcFace 识别参数 |
| [15-face-database.md](./15-face-database.md) | 人脸库管理 |
| [16-api-reference.md](./16-api-reference.md) | 接口清单 |
| [17-entity-models.md](./17-entity-models.md) | 数据模型说明 |
| [18-background-jobs.md](./18-background-jobs.md) | 后台定时任务 |
| [19-app-update.md](./19-app-update.md) | 版本更新 |
| [20-boot-and-kiosk.md](./20-boot-and-kiosk.md) | 开机自启与 Kiosk |
| [21-troubleshooting.md](./21-troubleshooting.md) | 常见问题排查 |

## 源码主包

```
app/src/main/java/com/arcsoft/arcfacedemo/
├── ui/           # 界面层
├── network/      # 网络层
├── data/         # 数据仓库与 HTTP
├── db/           # 业务 Room 数据库
├── facedb/       # 人脸 Room 数据库
├── faceserver/   # ArcFace 引擎封装
├── entity/       # 网络/业务实体
├── util/         # 工具类
├── widget/       # 自定义控件与弹窗
├── manager/      # 音效、Toast 管理
├── service/      # 后台服务
├── receiver/     # 广播接收
├── Serial/       # 串口通信
├── preference/   # 识别参数配置
└── common/       # 全局常量
```

渠道差异代码位于 `app/src/{yinchuan|chongqing|shihezi|luoyang}/`。
