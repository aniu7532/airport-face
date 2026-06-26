# 项目概览

## 项目定位

`airport-face`（Gradle 工程名 `ArcFaceDemo`）是一套部署在机场控制区闸机/立式查验设备上的 **竖屏 Android 终端应用**，用于：

- 验证人员身份（人脸识别 + 通行证刷卡/二维码）
- 展示长期证/临时证电子卡面
- 记录进/出控制区通行数据并同步后台
- 管理施工人员核销、通行记录与统计

## 版本信息

| 项 | 值 |
|----|-----|
| 包名 | `com.arcsoft.arcfacedemo` |
| 当前版本 | `1.0.72`（versionCode `45092621`） |
| minSdk | 26 |
| targetSdk | 33 |
| ABI | `arm64-v8a` |
| APK 命名 | `YCJC_{flavor}_v{versionName}_{versionCode}_{date}.apk` |

## 技术栈

| 类别 | 技术 |
|------|------|
| 人脸识别 | ArcSoft ArcFace SDK（RGB + IR 双目、活体、1:N） |
| 本地存储 | Room + SQLCipher |
| 网络 | OkGo / OkHttp + Gson |
| 图片 | Glide（AES 加密文件加载） |
| 异步 | RxJava2、Kotlin Coroutines、Paging3 |
| 鉴权 | 深信服零信任 SFUemSDK + 后台 Token |
| 更新 | XUpdate（`:xupdate-lib` 模块） |
| UI | DataBinding、ViewBinding、XPopup |

## Gradle 模块

| 模块 | 路径 | 说明 |
|------|------|------|
| `:app` | `app/` | 主应用 |
| `:xupdate-lib` | `xupdate-lib/` | 应用内更新库，已被 app 依赖 |
| `:update` | `update/` | 独立自动更新测试包，app 未依赖 |

## 主要入口

| 入口 | 类 | 说明 |
|------|-----|------|
| Application | `ArcFaceApplication` | 全局初始化、定时任务 |
| Launcher | `LoginActivity` | 登录、数据初始化、路由查验页 |
| 开机广播 | `BootReceiver` | 开机自启登录页 |
| 主查验 | `LivenessDetect*Activity` | 刷卡+人脸（进区） |
| 出区查验 | `RegisterAndRecognizeActivity` | 纯人脸 |
| 施工人员 | `ConstructionWorkersActivity` | 核销/记录/统计 |

## 产品渠道

通过 `productFlavors` 支持四个机场渠道，详见 [02-product-flavors.md](./02-product-flavors.md)。
