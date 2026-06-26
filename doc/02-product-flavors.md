# 产品渠道（Product Flavors）

配置位置：`app/build.gradle` → `flavorDimensions "channel"`

## 渠道对照表

| 渠道 | Flavor | APK 前缀 | API 域名 | 租户路径 | TENANT_ID | 临时证 |
|------|--------|----------|----------|----------|-----------|--------|
| 银川河东 | `yinchuan` | `YCJC_yinchuan_` | `https://inckzqtxz.caacsri.com` | 无 | `1` | ✅ |
| 重庆江北 | `chongqing` | `YCJC_chongqing_` | `https://cqakzqtxz.caacsri.com` | 无 | `3` | ✅ |
| 石河子花园 | `shihezi` | `YCJC_shihezi_` | `https://txzcloudservice.caacsri.com` | `shf` | `1` | ✅ |
| 洛阳北郊 | `luoyang` | `YCJC_luoyang_` | `https://txzcloudservice.caacsri.com` | `fy` | `2054084946120802305` | ❌ |

## 渠道配置文件

每个渠道在独立源码目录维护 `ChannelConfig`：

```
app/src/yinchuan/java/com/arcsoft/arcfacedemo/config/ChannelConfig.java
app/src/chongqing/java/com/arcsoft/arcfacedemo/config/ChannelConfig.java
app/src/shihezi/java/com/arcsoft/arcfacedemo/config/ChannelConfig.java
app/src/luoyang/java/com/arcsoft/arcfacedemo/config/ChannelConfig.java
```

关键字段：

| 字段 | 说明 |
|------|------|
| `BASE_URL` | API 根域名 |
| `TENANT_PREFIX` | 租户路径前缀，拼在域名与 `app-api` 之间 |
| `TENANT_ID` | 请求头 `tenant-id` 的值，运行时通过 `UrlConstants.TENANT_ID` 引用 |
| `SUPPORTS_TEMPORARY_PASS` | 是否支持临时通行证业务 |

## API 路径规则

由 `UrlConstants.java` 统一拼接：

- **系统接口**（登录、刷新 Token、版本）：`{BASE_URL}/app-api/system/...`（不带租户前缀）
- **业务接口**（无租户）：`{BASE_URL}/app-api/...`
- **业务接口**（有租户）：`{BASE_URL}/{TENANT_PREFIX}/app-api/...`

示例（石河子）：

```
https://txzcloudservice.caacsri.com/shf/app-api/check/pass/page-pass
```

## 渠道定制资源

除 `ChannelConfig` 外，各渠道还定制：

| 资源 | 路径模式 | 说明 |
|------|----------|------|
| 长期证 Fragment | `app/src/{flavor}/java/.../Document2.java` | 卡面数据绑定 |
| 临时证 Fragment | `app/src/{flavor}/java/.../Document3.java` | 卡面数据绑定 |
| 卡面布局 | `app/src/{flavor}/res/layout/document2.xml`、`document3.xml` | UI 排版 |
| 渠道文案 | `app/src/{flavor}/res/values/strings.xml` | 机场名称等 |
| Logo/背景 | `app/src/{flavor}/res/drawable/` | 机场 Logo、卡面背景 |

## 洛阳渠道特殊逻辑

`SUPPORTS_TEMPORARY_PASS = false`，以下场景会跳过临时证：

- 临时证刷卡/展示
- 临时证引领人流程
- 临时证通行记录创建

相关判断散布在 `RegisterAndRecognizeActivity`、`LivenessDetect*Activity` 等类中，统一读取 `ChannelConfig.SUPPORTS_TEMPORARY_PASS`。

## 打包命令示例

```bash
./gradlew assembleYinchuanRelease
./gradlew assembleLuoyangRelease
```
