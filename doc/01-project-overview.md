# 项目概览

## 项目定位

`airport-face`（Gradle 工程名 `ArcFaceDemo`）是部署在机场控制区闸机/立式查验设备上的 **竖屏 Android 终端**，核心能力：

| 能力 | 说明 |
|------|------|
| 人脸识别 | ArcSoft ArcFace RGB+IR 双目、活体、1:N（最大 30000 人） |
| 通行证验证 | RFID 刷卡 + 二维码串口读卡 |
| 电子卡面 | 长期证/临时证竖屏展示（按渠道定制 layout） |
| 通行记录 | 本地 Room 缓存，定时上传后台 |
| 施工人员 | 核销、通行记录查询、进出统计 |
| 安全接入 | 深信服零信任 VPN + 后台 Token 鉴权 |
| 离线容错 | Ping 检测离线标志，恢复后补传记录 |

## 版本与构建

| 项 | 值 |
|----|-----|
| applicationId | `com.arcsoft.arcfacedemo` |
| versionName | `1.0.72` |
| versionCode | `45092621` |
| minSdk / targetSdk | 26 / 33 |
| compileSdk | 36 |
| ABI | `arm64-v8a` |
| Release APK 名 | `YCJC_{flavor}_v{versionName}_{versionCode}_{yyyyMMdd}.apk` |

```bash
# 打包示例
./gradlew assembleYinchuanRelease
./gradlew assembleLuoyangDebug
```

## 技术栈

### 核心框架

| 类别 | 技术 | 版本（参考 build.gradle） |
|------|------|--------------------------|
| 语言 | Java + Kotlin | JVM 1.8 |
| UI | DataBinding、ViewBinding、Material | appcompat 1.1.0 |
| 架构 | ViewModel、LiveData、Paging3 | paging 3.4.2 |
| 本地 DB | Room | 2.4.3 |
| 加密 DB | SQLCipher | 4.5.2 |
| 网络 | OkGo + OkHttp + Gson | okhttp 4.9.1 |
| 图片 | Glide + OkHttp 集成 | 4.16.0 |
| 异步 | RxJava2 | 2.2.20 |
| 弹窗 | XPopup | — |
| 更新 | XUpdate（:xupdate-lib） | — |
| 崩溃 | Bugly | appId `7db9a3ce0b` |
| 人脸 | ArcSoft ArcFace SDK | `libs/` 本地 AAR |

### 硬件/厂商 SDK

| SDK | 用途 |
|-----|------|
| `com.ys.rkapi.MyManager` | 小屏设备重启 |
| `ZysjSystemManager` | 大屏设备重启 |
| `android.serialport.SerialPort` | 串口读写 |
| `SFUemSDK`（深信服） | 零信任 VPN |

## Gradle 子模块

| 模块 | 被 app 依赖 | 说明 |
|------|------------|------|
| `:app` | — | 主应用 |
| `:xupdate-lib` | ✅ | 应用内更新 |
| `:update` | ❌ | 独立测试包 `com.sz.zysx.autoupdate` |

## 主要 Activity 一览

| Activity | 生产路径 | 说明 |
|----------|----------|------|
| `LoginActivity` | ✅ Launcher | 登录、同步、路由 |
| `LivenessDetectJinActivity` | ✅ | 短距刷卡+人脸 |
| `LivenessDetectYuanActivity` | ✅ | 长距刷卡+人脸 |
| `LivenessDetectYuanAndJinActivity` | ✅ | 双读卡器+人脸 |
| `RegisterAndRecognizeActivity` | ✅ | 纯人脸出区 |
| `ConstructionWorkersActivity` | ✅ | 施工人员管理 |
| `RecognizeSettingsActivity` | 运维 | ArcFace 参数 |
| `CameraConfigureActivity` | 运维 | 相机配置 |
| `FaceManageActivity` | 开发 | 人脸库维护 |
| `HomeActivity` | Demo | 原 ArcFace Demo 首页 |
| `ActivationActivity` | 运维 | SDK 在线激活 |

## 本地存储路径

| 用途 | 路径 |
|------|------|
| 业务数据库 | `{externalFilesDir}/db/airportDb.db` |
| 人脸数据库 | `{externalFilesDir}/database/faceDB.db` |
| 运行日志 | `{externalFilesDir}/log/`（保留 2 天） |
| 加密人脸图 | `{externalFilesDir}/faceDB/` |
| 通行抓拍 | `{externalFilesDir}/records/`（3 天自动清理） |
| 调试 dump | `{externalFilesDir}/debugDump/` |

## 相关文档

- 渠道差异 → [02-product-flavors.md](./02-product-flavors.md)
- 架构分层 → [03-architecture.md](./03-architecture.md)
- 登录流程 → [04-login-and-auth.md](./04-login-and-auth.md)
