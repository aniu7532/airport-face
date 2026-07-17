# 构建、签名与发布

## 构建基线

| 项 | 当前值 |
|----|--------|
| Android Gradle Plugin | 8.9.1 |
| Kotlin 插件 | 2.2.10 |
| compileSdk / targetSdk / minSdk | 36 / 33 / 26 |
| Java / Kotlin JVM target | 1.8 |
| versionName / versionCode | 1.0.75 / 45092624 |
| ABI | 仅 `arm64-v8a` |

## 构建变体

渠道维度 `channel` 包含：

```text
yinchuan
chongqing
shihezi
luoyang
```

示例：

```bash
./gradlew assembleYinchuanDebug
./gradlew assembleYinchuanRelease
./gradlew assembleLuoyangRelease
```

发布包命名：

```text
release: YCJC_{flavor}_v{versionName}_{versionCode}_{yyyyMMdd}.apk
debug:   YCJC_{flavor}_v{versionName-debug}_{versionCode}.apk
```

## 本地 SDK 与 ABI

`implementation(fileTree("libs"))` 会加载 `app/libs/` 中全部 JAR/AAR。构建前应确认：

- 同一厂商 SDK 只保留一个版本，避免 duplicate class。
- 所有 native AAR/JAR 的 so 都包含 `arm64-v8a`。
- 替换德卡 SDK 时按 [SDK 升级文档](../sdk/Android_sdk_release2.56/README.md) 验证 APK 内 native so。

## 签名现状

- release 使用仓库内 `app/signedfile/face.jks`。
- debug 构建当前也显式使用 release 签名配置，而不是 `signingConfigs.debug`。
- `signingConfigs.debug` 中还保留了特定 Linux 用户目录下的绝对 keystore 路径，但按当前 buildTypes 不会被 debug 变体使用。
- 签名口令目前直接写在 `app/build.gradle`，属于安全与维护风险；迁移时应改用未入库的 Gradle properties 或 CI Secret，且不得把口令复制到文档或日志。

## 混淆与资源压缩

debug/release 当前均为：

```text
minifyEnabled false
shrinkResources false
zipAlignEnabled false
```

`app/proguard-rules.pro` 只有模板注释，未配置 ArcSoft、德卡、深信服、EC_RFID 等厂商 SDK 的 keep 规则。因此不能只把 `minifyEnabled` 改为 `true`；启用 R8 前必须取得各 SDK 的官方规则，并回归反射类、JNI 方法和序列化实体。

## 发布检查清单

- [ ] flavor、机场名称、BASE_URL、TENANT_ID 与目标环境一致。
- [ ] `versionName` / `versionCode` 已递增。
- [ ] `app/libs/` 无同 SDK 多版本。
- [ ] 目标设备为 arm64，APK 包含所需 native so。
- [ ] release 签名证书与已安装生产包一致，可覆盖升级。
- [ ] 四类查验模式、登录、同步、离线上传与应用更新完成回归。
- [ ] 若启用混淆，完成全部厂商 SDK 真机回归。
