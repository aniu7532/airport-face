# 接口清单

定义类：`network/UrlConstants.java`  
所有业务请求需带：`tenant-id: UrlConstants.TENANT_ID`  
已登录请求建议带：`Authorization: Bearer {accessToken}`

## URL 拼接规则

```
系统 API：  {ChannelConfig.BASE_URL}/app-api/system/{path}
业务 API（无租户）：  {BASE_URL}/app-api/{path}
业务 API（有租户）：  {BASE_URL}/{TENANT_PREFIX}/app-api/{path}
```

加密文件流：`fileStreamUrl(path)` → `{businessBase}/infra/file/stream?path={path}`

---

## 认证

### POST 竖屏客户端登录

| 项 | 值 |
|----|-----|
| 常量 | `URL_LOGIN` |
| 路径 | `/app-api/system/auth/vertical-client-login` |

**Body（JSON）**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | ✅ | 账号 |
| clientSecret | string | ✅ | 密钥，如 `admin123` |
| clientId | string | ✅ | 固定 `VERTICAL` |

**响应 data**：`Login`（accessToken、refreshToken、userId、expiresTime）

### POST 刷新 Token

| 常量 | `URL_refresh_token` |
| 路径 | `/app-api/system/auth/refresh-token` |

---

## 设备与配置

### GET 设备 MAC 详情

| 常量 | `URL_GET_MAC_DETAIL` |
| 路径 | `/check/device/detail-mac` |

| 参数 | 说明 |
|------|------|
| timestamp | 毫秒时间戳 |
| mac | 设备唯一标识 |

### GET 配置信息

| 常量 | `URL_GETCONFIGINFO` |
| 路径 | `/check/configInfo/get` |

| 参数 | type 含义（代码中使用） |
|------|----------------------|
| type=5 | 进出设备配置 → devicesEnter/devicesOut |
| type=6 | 同步间隔 → interval（分钟） |
| timestamp | 毫秒时间戳 |

### GET 验证方式

| 常量 | `URL_GETCHECKMETH` |
| 参数 | clientId=VERTICAL |

### GET 设备列表

| 常量 | `checkDeviceList` |

### GET/POST 心跳

| 常量 | `heartbeat` |
| 参数 | mac, interval |

---

## 通行证

### GET 分页通行证

| 常量 | `URL_GetLongPass` |
| 路径 | `/check/pass/page-pass` |

| 参数 | 说明 |
|------|------|
| pageNo | 页码，从 1 开始 |
| pageSize | 每页条数，登录全量 20，增量 20 |
| timestamp | 毫秒时间戳 |

**响应**：`LongPassCards`（list: `LongPassCard[]`, total）

### GET 通行证总数

| 常量 | `passCount` |

### POST 异常通行证上报

| 常量 | `checkAbnormalCreate` |
| 场景 | 人脸注册失败时上报 |

---

## 通行记录

### POST 创建长期证记录

| 常量 | `URL_CREATE_LONG_RECORD` |
| Body | `LongTermRecords` 整表 JSON |

### POST 创建临时证记录

| 常量 | `URL_CREATE_TEMP_RECORD` |
| Body | `TemporaryCardRecords` JSON |

### GET 分页通行记录

| 常量 | `URL_GET_RESORD_PAGE` |
| 用途 | `RecordsPopDialog` 在线查询 |

---

## 用户与文件

### GET 用户详情

| 常量 | `URL_GET_USER_DETAIL` |
| 参数 | timestamp, id(userId) |

### POST 上传加密文件

| 常量 | `URL_UPLOAD_FILE` |
| 路径 | `/infra/file/upload-encrypt-url` |
| 用途 | 现场抓拍图上传 |

### GET 同步系统时间

| 常量 | `URL_GET_SYSTEM_TIME` |

---

## 施工人员

### GET 有进无出（待核销）

| 常量 | `checkRecordPageNeedVerifyNoOut` |

| 参数 | 说明 |
|------|------|
| pageNo | 页码 |
| pageSize | 10 |
| nickname | 姓名筛选 |
| idCode | 证件号 |
| startCheckTime / endCheckTime | 时间范围 |
| companyName | 单位（可选） |

### GET 施工人员通行记录

| 常量 | `checkRecordPageNeedVerify` |
| 参数 | 同上 |

### GET 进出统计

| 常量 | `checkRecordStatisticNeedVerify` |

### POST 核实

| 常量 | `checkRecordVerify` |
| 用途 | `VerifyAndConfirmDialog` 提交核实结果 |

### GET 管制区域树

| 常量 | `checkAreaGetDetailChannelTree` |

### GET 申办单位列表

| 常量 | `checkUnitSimpleList` |

---

## 版本更新

### GET 最新版本

| 常量 | `URL_GET_APP_LAST_VERSION` |
| 路径 | `/app-api/system/appVersion/get-lastVersion` |
| 参数 | type=3（竖屏客户端） |

**响应 data**：`Version`（version、url、remark、isForceUpdate）

---

## HTTP 封装

| 类 | 说明 |
|----|------|
| `ApiUtils.get/post` | 统一附加 tenant-id、Authorization |
| `JsonCallback` | Gson 解析 `Base<T>` |
| `HttpInitUtils` | OkGo 全局 SSL、超时配置 |

## 相关文档

- 渠道 URL 差异 → [02-product-flavors.md](./02-product-flavors.md)
- 实体字段 → [17-entity-models.md](./17-entity-models.md)
