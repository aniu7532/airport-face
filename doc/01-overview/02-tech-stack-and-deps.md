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
| net.zetetic:android-database-sqlcipher | 4.5.2 | DB 加密 |
| com.github.bumptech.glide:glide | 4.16.0 | 图片（含加密文件） |
| com.squareup.okhttp3:okhttp | 4.9.1 | HTTP |
| OkGo | 3.0.4 | 网络封装（libs） |
| io.reactivex.rxjava2 | 2.2.20 | 异步 |
| androidx.paging:paging-runtime-ktx | 3.4.2 | 施工人员列表 |
| com.google.zxing:core | 3.4.1 | 卡面二维码 |
| pub.devrel:easypermissions | 3.0.0 | 权限 |
| es.dmoral:toasty | — | Toast |
| com.blankj:utilcodex | — | SPUtils、ThreadUtils 等 |
| com.tencent.bugly | — | 崩溃上报 |
| com.lxj:xpopup | — | 弹窗/抽屉 |
| ArcSoft Face SDK | libs/*.jar/aar | 人脸 |

## 厂商 / 本地 JAR

- `android.serialport` — 串口
- `com.sangfor.sdk` — 零信任 VPN
- `com.ys.rkapi.MyManager` — 小屏重启
- `ZysjSystemManager` — 大屏重启

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
