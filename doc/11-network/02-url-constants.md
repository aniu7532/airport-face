# UrlConstants 接口 URL 常量

> 源码：`app/src/main/java/com/arcsoft/arcfacedemo/network/UrlConstants.java`  
> 渠道配置：`app/src/{flavor}/java/.../config/ChannelConfig.java`

`UrlConstants` 按 **Gradle Product Flavor** 读取 `ChannelConfig`，拼接 system 接口与业务接口 URL。

---

## 基础常量

| 常量 | 定义 | 说明 |
|------|------|------|
| `URL` | `ChannelConfig.BASE_URL` | 各渠道 API 域名 |
| `TENANT_ID` | `ChannelConfig.TENANT_ID` | 请求头 `tenant-id` |
| `Test_URL` | `"http://test.sczhbf.com:58088"` | 慧能测试环境（硬编码，未参与拼接） |
| `URL_TOKEN` | `""` | 空字符串，未使用 |
| `URL_ClIENTID` | `"VERTICAL"` | 登录参数 `clientId` |

---

## `businessAppApiBase()` 逻辑

```java
private static String businessAppApiBase() {
    String prefix = ChannelConfig.TENANT_PREFIX;
    if (prefix == null || prefix.isEmpty()) {
        return URL + "/app-api";
    }
    return URL + "/" + prefix + "/app-api";
}
```

| 条件 | 业务 API 根路径 |
|------|----------------|
| `TENANT_PREFIX` 为空 | `{BASE_URL}/app-api` |
| `TENANT_PREFIX` 非空 | `{BASE_URL}/{TENANT_PREFIX}/app-api` |

**system 接口** 固定为 `{BASE_URL}/app-api/system`，**不带**租户路径前缀。

---

## 各渠道 URL 示例

### 银川（yinchuan）

| 配置项 | 值 |
|--------|-----|
| `BASE_URL` | `https://inckzqtxz.caacsri.com` |
| `TENANT_PREFIX` | `""` |
| `TENANT_ID` | `"1"` |

| 接口 | 完整 URL 示例 |
|------|---------------|
| 登录 | `https://inckzqtxz.caacsri.com/app-api/system/auth/vertical-client-login` |
| 刷新 Token | `https://inckzqtxz.caacsri.com/app-api/system/auth/refresh-token` |
| 获取通行证 | `https://inckzqtxz.caacsri.com/app-api/check/pass/page-pass` |
| 创建长期记录 | `https://inckzqtxz.caacsri.com/app-api/check/record/create-long` |
| 心跳 | `https://inckzqtxz.caacsri.com/app-api/check/device/heartbeat` |
| 加密文件流 | `https://inckzqtxz.caacsri.com/app-api/infra/file/stream?path=...` |

### 洛阳（luoyang）

| 配置项 | 值 |
|--------|-----|
| `BASE_URL` | `https://txzcloudservice.caacsri.com` |
| `TENANT_PREFIX` | `"fy"` |
| `TENANT_ID` | `"2054084946120802305"` |

| 接口 | 完整 URL 示例 |
|------|---------------|
| 登录 | `https://txzcloudservice.caacsri.com/app-api/system/auth/vertical-client-login` |
| 获取通行证 | `https://txzcloudservice.caacsri.com/fy/app-api/check/pass/page-pass` |
| 创建长期记录 | `https://txzcloudservice.caacsri.com/fy/app-api/check/record/create-long` |
| 心跳 | `https://txzcloudservice.caacsri.com/fy/app-api/check/device/heartbeat` |

### 重庆（chongqing）

| 配置项 | 值 |
|--------|-----|
| `BASE_URL` | `https://cqakzqtxz.caacsri.com` |
| `TENANT_PREFIX` | `""` |
| `TENANT_ID` | `"3"` |

| 接口 | 完整 URL 示例 |
|------|---------------|
| 登录 | `https://cqakzqtxz.caacsri.com/app-api/system/auth/vertical-client-login` |
| 获取通行证 | `https://cqakzqtxz.caacsri.com/app-api/check/pass/page-pass` |
| 创建临时记录 | `https://cqakzqtxz.caacsri.com/app-api/check/record/create-temporary` |

### 石河子（shihezi）

| 配置项 | 值 |
|--------|-----|
| `BASE_URL` | `https://txzcloudservice.caacsri.com` |
| `TENANT_PREFIX` | `"shf"` |
| `TENANT_ID` | `"1"` |

| 接口 | 完整 URL 示例 |
|------|---------------|
| 登录 | `https://txzcloudservice.caacsri.com/app-api/system/auth/vertical-client-login` |
| 获取通行证 | `https://txzcloudservice.caacsri.com/shf/app-api/check/pass/page-pass` |
| 同步时间 | `https://txzcloudservice.caacsri.com/shf/app-api/check/configInfo/sync-time` |

---

## 全部 URL 常量列表

### System 域（`SYSTEM_API = URL + "/app-api/system"`）

| 常量 | 路径后缀 | 用途 | 主要调用方 |
|------|----------|------|-----------|
| `URL_LOGIN` | `/auth/vertical-client-login` | 竖屏客户端登录 | `LoginActivity` |
| `URL_refresh_token` | `/auth/refresh-token` | 刷新 Token | `TokenRefreshJobService`（已实现，当前未注册/调度） |
| `URL_GET_APP_LAST_VERSION` | `/appVersion/get-lastVersion` | 获取最新 App 版本 | `UpdateUtils`、各 Activity |

### 业务域（`businessAppApiBase()` 前缀）

| 常量 | 路径后缀 | 用途 | 主要调用方 |
|------|----------|------|-----------|
| `URL_GETCHECKMETH` | `/check/device/check-method` | 获取验证方式 | `LoginActivity` |
| `URL_GetLongPass` | `/check/pass/page-pass` | 分页获取通行证 | `ArcFaceApplication.fetchNextPage` |
| `URL_GET_MAC_DETAIL` | `/check/device/detail-mac` | 按 MAC 获取设备详情 | `LoginActivity` |
| `URL_GETCONFIGINFO` | `/check/configInfo/get` | 获取配置信息 | `LoginActivity` |
| `URL_CREATE_LONG_RECORD` | `/check/record/create-long` | 创建长期证通行记录 | `ArcFaceApplication.startUpDataToServer`、各 Activity |
| `URL_CREATE_TEMP_RECORD` | `/check/record/create-temporary` | 创建临时证通行记录 | 同上 |
| `URL_GET_USER_DETAIL` | `/check/user/get` | 获取用户详情 | `LoginActivity` |
| `URL_UPLOAD_FILE` | `/infra/file/upload-encrypt-url` | 上传加密文件 | `ImageUploader` |
| `URL_GET_SYSTEM_TIME` | `/check/configInfo/sync-time` | 同步系统时间 | `LivenessDetect*Activity` |
| `URL_GET_RESORD_PAGE` | `/check/record/page` | 分页查询通行记录 | `RecordsPopDialog`、`AccessRecordPagingSource` |
| `heartbeat` | `/check/device/heartbeat` | 设备心跳 | `ArcFaceApplication.startPeriodicTask` |
| `passCount` | `/check/pass/pass-count` | 通行证总数 | `LongPassCardsReInitUtils` |
| `checkAbnormalCreate` | `/check/check-abnormal/create` | 上报注册失败通行证 | `LongPassCardsRemedialMeasuresUtils` |
| `checkRecordPageNeedVerifyNoOut` | `/check/record/page-need-verify-no-out` | 施工人员有进无出 | `WriteOffRecordPagingSource` |
| `checkRecordPageNeedVerify` | `/check/record/page-need-verify` | 施工人员待核实记录 | 施工人员模块 |
| `checkRecordStatisticNeedVerify` | `/check/record/statistic-need-verify` | 施工人员每日进入统计 | `InOutStatisticsViewModel` |
| `checkAreaGetDetailChannelTree` | `/check/area/get-detail-channel-tree` | 管制区域树 | `AreaPickerDialog` |
| `checkDeviceList` | `/check/device/list` | 设备列表 | `VerifyAndConfirmDialog` |
| `checkRecordVerify` | `/check/record/verify` | 核实记录 | `VerifyAndConfirmDialog` |
| `checkUnitSimpleList` | `/check/unit/simpleList` | 申办单位列表 | `CheckUnitRepository` |

### 动态方法

| 方法 | 签名 | 说明 |
|------|------|------|
| `fileStreamUrl` | `String fileStreamUrl(String path)` | 返回 `{businessAppApiBase()}/infra/file/stream?path={path}`，用于 Glide 加载加密图片 |

---

## URL 拼接规则总结

```
System 接口:  {BASE_URL}/app-api/system/{path}
业务接口:     {BASE_URL}[/TENANT_PREFIX]/app-api/{path}
文件流:       {BASE_URL}[/TENANT_PREFIX]/app-api/infra/file/stream?path=...
```

洛阳、石河子等多租户渠道的业务接口比 system 接口多一层 `/{TENANT_PREFIX}/`。
