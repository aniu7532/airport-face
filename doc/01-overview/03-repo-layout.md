# 仓库目录结构

```
airport-face/
├── app/
│   ├── build.gradle
│   ├── src/
│   │   ├── main/java/com/arcsoft/arcfacedemo/   # 主源码
│   │   ├── main/res/
│   │   ├── yinchuan|chongqing|shihezi|luoyang/  # 渠道覆盖
│   │   └── schemas/                             # Room schema 导出
│   └── libs/                                    # ArcSoft 等本地库
├── xupdate-lib/                                 # 应用内更新
├── update/                                      # 独立更新测试包
├── doc/                                         # 本文档体系
└── settings.gradle
```

## 主包 `com.arcsoft.arcfacedemo`

| 目录 | 文件数 | 说明 |
|------|--------|------|
| `ui/activity` | 14+1 kt | 页面入口 |
| `ui/fragment` | 若干 + 渠道 Document | 卡面 Fragment |
| `ui/viewmodel` | 11 | 状态与业务 |
| `ui/adapter` | 7 | 列表 |
| `ui/pagingsource` | 2 | Paging3 |
| `network` | 4 | URL + ApiUtils |
| `data/http` | 8 | 回调 |
| `db` | 7 | 业务 Room |
| `facedb` | 3 | 人脸 Room |
| `faceserver` | 2 | FaceServer |
| `entity` | 17 | JSON 模型 |
| `util` | 69 | 工具（含 face/camera/glide/debug） |
| `widget` | 30 | 自定义 View + dialog |
| `Serial` | 3 | 串口 |
| `preference` | 8 | 识别参数 UI |
| `manager` | 2 | 音效/Toast |
| `service` | 1 | TokenRefreshJobService |
| `receiver` | 1 | BootReceiver |
| `common` | 1 | Constants |

## 渠道覆盖规则

同包名类在 `app/src/{flavor}/java/` 下覆盖 main：

- `config/ChannelConfig.java` — **必覆盖**
- `ui/fragment/Document2.java`、`Document3.java`
- `res/layout/document2.xml`、`document3.xml`
- `res/values/strings.xml`、渠道 drawable

## 关键单文件

| 文件 | 职责 |
|------|------|
| `ArcFaceApplication.java` | 全局初始化、定时任务 |
| `LoginActivity.java` | Launcher、登录链 |
| `LivenessDetect*Activity.java` | 刷卡+人脸 |
| `RegisterAndRecognizeActivity.java` | 纯人脸 |
| `network/UrlConstants.java` | 全部 API 路径 |
| `network/ApiUtils.java` | Token + GET/POST 封装 |
