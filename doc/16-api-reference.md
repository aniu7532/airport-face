# 接口清单

定义类：`network/UrlConstants.java`

## 请求规范

### 公共 Header

| Header | 值 | 说明 |
|--------|-----|------|
| `tenant-id` | `UrlConstants.TENANT_ID` | 来自渠道 `ChannelConfig.TENANT_ID` |
| `Authorization` | `Bearer {accessToken}` | 登录后携带，部分接口可选 |
| `Content-Type` | `application/json` | POST 请求 |

### URL 拼接规则

```
系统 API：  {BASE_URL}/app-api/system/{path}
业务 API：  {BASE_URL}/app-api/{path}                    （无租户）
业务 API：  {BASE_URL}/{TENANT_PREFIX}/app-api/{path}    （有租户）
```

## 系统接口

| 常量 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `URL_LOGIN` | POST | `/auth/vertical-client-login` | 竖屏客户端登录 |
| `URL_refresh_token` | POST | `/auth/refresh-token` | 刷新 Token |
| `URL_GET_APP_LAST_VERSION` | GET | `/appVersion/get-lastVersion` | 获取最新版本 |

## 设备与配置

| 常量 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `URL_GET_MAC_DETAIL` | GET | `/check/device/detail-mac` | 设备 MAC 绑定详情 |
| `URL_GETCONFIGINFO` | GET | `/check/configInfo/get` | 查验配置信息 |
| `URL_GETCHECKMETH` | GET | `/check/device/check-method` | 获取验证方式 |
| `heartbeat` | POST | `/check/device/heartbeat` | 设备心跳 |
| `checkDeviceList` | GET | `/check/device/list` | 设备列表 |

## 通行证

| 常量 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `URL_GetLongPass` | GET | `/check/pass/page-pass` | 分页拉取通行证 |
| `passCount` | GET | `/check/pass/pass-count` | 通行证总数 |
| `checkAbnormalCreate` | POST | `/check/check-abnormal/create` | 上报注册异常 |

## 通行记录

| 常量 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `URL_CREATE_LONG_RECORD` | POST | `/check/record/create-long` | 创建长期证记录 |
| `URL_CREATE_TEMP_RECORD` | POST | `/check/record/create-temporary` | 创建临时证记录 |
| `URL_GET_RESORD_PAGE` | GET | `/check/record/page` | 分页查询通行记录 |

## 用户与文件

| 常量 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `URL_GET_USER_DETAIL` | GET | `/check/user/get` | 当前用户详情 |
| `URL_UPLOAD_FILE` | POST | `/infra/file/upload-encrypt-url` | 上传加密文件 |
| `fileStreamUrl(path)` | GET | `/infra/file/stream?path={path}` | 下载加密文件流 |
| `URL_GET_SYSTEM_TIME` | GET | `/check/configInfo/sync-time` | 同步系统时间 |

## 施工人员

| 常量 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `checkRecordPageNeedVerifyNoOut` | GET | `/check/record/page-need-verify-no-out` | 有进无出记录 |
| `checkRecordPageNeedVerify` | GET | `/check/record/page-need-verify` | 施工人员通行记录 |
| `checkRecordStatisticNeedVerify` | GET | `/check/record/statistic-need-verify` | 进出统计 |
| `checkRecordVerify` | POST | `/check/record/verify` | 核实记录 |

## 区域与单位

| 常量 | 方法 | 路径 | 说明 |
|------|------|------|------|
| `checkAreaGetDetailChannelTree` | GET | `/check/area/get-detail-channel-tree` | 管制区域树 |
| `checkUnitSimpleList` | GET | `/check/unit/simpleList` | 申办单位列表 |

## HTTP 工具类

| 类 | 说明 |
|----|------|
| `ApiUtils` | GET/POST 封装，Token 管理 |
| `OkHttpUtils` | OkHttp 客户端 |
| `HttpInitUtils` | OkGo 全局初始化 |
| `JsonCallback` | JSON 响应回调 |
| `StringStateCallback` | 字符串状态回调 |
| `XmlStringCallback` | XML 响应回调 |
| `XmlJsonCallback` | XML 转 JSON 回调 |
