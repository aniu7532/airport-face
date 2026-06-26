# 产品渠道（Product Flavors）

## Gradle 配置

文件：`app/build.gradle`

```gradle
flavorDimensions "channel"
productFlavors {
    yinchuan { dimension "channel" }
    chongqing { dimension "channel" }
    shihezi { dimension "channel" }
    luoyang { dimension "channel" }
}
```

## 渠道对照表

| 渠道 | Flavor | APK 前缀 | BASE_URL | TENANT_PREFIX | TENANT_ID | 临时证 |
|------|--------|----------|----------|---------------|-----------|--------|
| 银川河东 | yinchuan | `YCJC_yinchuan_` | `https://inckzqtxz.caacsri.com` | `""` | `1` | ✅ |
| 重庆江北 | chongqing | `YCJC_chongqing_` | `https://cqakzqtxz.caacsri.com` | `""` | `3` | ✅ |
| 石河子花园 | shihezi | `YCJC_shihezi_` | `https://txzcloudservice.caacsri.com` | `shf` | `1` | ✅ |
| 洛阳北郊 | luoyang | `YCJC_luoyang_` | `https://txzcloudservice.caacsri.com` | `fy` | `2054084946120802305` | ❌ |

配置文件路径：`app/src/{flavor}/java/com/arcsoft/arcfacedemo/config/ChannelConfig.java`

运行时统一通过 `UrlConstants.TENANT_ID`（= `ChannelConfig.TENANT_ID`）设置请求头。

## API 路径示例

### 银川（无租户前缀）

```
GET https://inckzqtxz.caacsri.com/app-api/check/pass/page-pass
POST https://inckzqtxz.caacsri.com/app-api/system/auth/vertical-client-login
```

### 石河子（租户 shf）

```
GET https://txzcloudservice.caacsri.com/shf/app-api/check/pass/page-pass
POST https://txzcloudservice.caacsri.com/app-api/system/auth/vertical-client-login
```

> 注意：**system 接口不带租户前缀**，仅业务 `/check/*`、`/infra/*` 带前缀。

### 洛阳（租户 fy，无临时证）

```
GET https://txzcloudservice.caacsri.com/fy/app-api/check/pass/page-pass
```

业务代码中 `SUPPORTS_TEMPORARY_PASS = false`。

## 渠道源码目录结构

```
app/src/{flavor}/
├── java/com/arcsoft/arcfacedemo/
│   ├── config/ChannelConfig.java      # 必改
│   └── ui/fragment/
│       ├── Document2.java             # 长期证绑定
│       └── Document3.java             # 临时证绑定
└── res/
    ├── layout/document2.xml
    ├── layout/document3.xml
    ├── values/strings.xml             # 机场名称
    └── drawable/                      # Logo、卡面背景
```

## 渠道定制差异明细

| 资源 | yinchuan | chongqing | shihezi | luoyang |
|------|----------|-----------|---------|---------|
| 机场名称 strings | 银川河东 | 重庆江北 | 石河子花园 | 洛阳北郊 |
| Logo | airport_logo.xml | airport_logo.png | shihezi 系列 | luoyang 系列 |
| 长期证背景 | 默认 | 默认 | shihezi_pass_*_bg | luoyang_pass_long_term_bg |
| 区域 badge | 通用 | 通用 | shihezi_area_badge_* | luoyang_area_badge_* |
| Document3 / 临时证 layout | ✅ | ✅ | ✅ | 有 layout 但业务禁用 |

## 打包命令

```bash
./gradlew assembleYinchuanRelease
./gradlew assembleChongqingRelease
./gradlew assembleShiheziRelease
./gradlew assembleLuoyangRelease
```

输出：`app/build/outputs/apk/{flavor}/release/YCJC_{flavor}_v1.0.72_*.apk`

## 新增渠道检查清单

1. 在 `productFlavors` 添加 flavor
2. 创建 `app/src/{flavor}/java/.../ChannelConfig.java`（BASE_URL、TENANT_PREFIX、TENANT_ID、SUPPORTS_TEMPORARY_PASS）
3. 复制并修改 Document2/3、layout、strings、drawable
4. 确认 `UrlConstants` 无需改代码（自动读 ChannelConfig）
5. 全量登录测试：登录、拉证、刷卡、人脸、上传记录
6. 验证 tenant-id 请求头与后台租户一致

## 相关文档

- 接口规则 → [16-api-reference.md](./16-api-reference.md)
- 卡面 UI → [09-pass-card-ui.md](./09-pass-card-ui.md)
