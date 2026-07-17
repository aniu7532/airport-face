# 技术栈与依赖

> 来源：`app/build.gradle`、`settings.gradle`

## Gradle 模块

| 模块 | 依赖关系 |
|------|----------|
| `:app` | 主应用 |
| `:xupdate-lib` | `implementation project(':xupdate-lib')` |
| `:update` | 未被子模块依赖 |

## 主要第三方依赖

| 依赖 | 版本 | 用途 |
|------|------|------|
| androidx.appcompat | 1.1.0 | UI 基础 |
| androidx.room:room-runtime | 2.4.3 | Room |
| net.zetetic:android-database-sqlcipher | 4.5.2 | 已引入依赖；当前 Room 建库未配置 `SupportFactory`，数据库并未使用 SQLCipher 加密 |
| com.github.bumptech.glide:glide | 4.16.0 | 图片（含加密文件） |
| com.squareup.okhttp3:okhttp | 4.9.1 | HTTP |
| OkGo | 3.0.4 | 网络封装（libs） |
| io.reactivex.rxjava2 | 2.2.20 | 异步 |
| androidx.paging:paging-runtime-ktx | 3.4.2 | 施工人员列表 |
| com.google.zxing:core | 3.4.1 | 卡面二维码 |
| pub.devrel:easypermissions | 3.0.0 | 权限 |
| com.github.GrenderG:Toasty | 1.5.2 | Toast |
| com.blankj:utilcodex | — | SPUtils、ThreadUtils 等 |
| com.tencent.bugly:crashreport / nativecrashreport | 4.0.4 / 3.9.2 | Java/Native 崩溃上报 |
| com.lxj:xpopup | — | 弹窗/抽屉 |
| ArcSoft Face SDK | libs/*.jar/aar | 人脸 |

## 厂商 / 本地依赖

`app/build.gradle` 使用 `implementation(fileTree("libs"))`，因此 `app/libs/` 中所有 JAR/AAR 都会参与编译。同一 SDK 的新旧 AAR 不能同时保留。

| 文件 | 主要包/能力 | 用途 |
|------|-------------|------|
| `arcsoft_face.jar`、`arcsoft_image_util.jar` | ArcSoft | 人脸检测、特征、图像处理 |
| `dc_reader_release_V1.0.0_20230516162946.aar` | `com.decard.NDKMethod.BasicOper` | 大屏短距卡、PSAM/ACPU 外部认证 |
| `hcreader_v3.0.1.jar` | `com.hc.reader` | 小屏短距卡 |
| `EC_RFID.jar` | `com.pc_rfid.api.EC_API` | 远距离 RFID |
| `Android-SerialPort-API-2.0.0.aar` | `android.serialport` | 串口底层 |
| `SangforSDK.aar` | `com.sangfor.sdk` | 零信任 VPN |
| `yface_api.jar` | `com.ys.rkapi.MyManager` | 小屏设备控制、重启 |
| `ZY-Interface-11.jar` | `android.app.ZysjSystemManager` | 大屏设备控制、重启 |

### 德卡 SDK 版本边界

- 当前 App 编译版本：`app/libs/dc_reader_release_V1.0.0_20230516162946.aar`。
- 待升级厂商交付版本：`doc/sdk/Android_sdk_release2.56/.../dc_reader_release_V1.0.0_20231121115913.aar`。
- 当前仅打包 `arm64-v8a`，新版 AAR 包含对应 ABI。
- 升级说明、兼容风险与验收步骤见 [德卡读卡器 SDK 接入与开发流程](../sdk/Android_sdk_release2.56/README.md)。

## DataBinding / ViewBinding

```gradle
dataBinding { enabled = true }
buildFeatures { viewBinding true; buildConfig true }
```

## 渠道维度

```gradle
flavorDimensions "channel"
productFlavors { yinchuan chongqing shihezi luoyang }
```

编译期注入 `com.arcsoft.arcfacedemo.config.ChannelConfig`。
