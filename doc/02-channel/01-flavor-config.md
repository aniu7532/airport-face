# 四渠道 ChannelConfig 字段对比

各风味独立源码：`app/src/{flavor}/java/com/arcsoft/arcfacedemo/config/ChannelConfig.java`

构建时仅编译当前 flavor 对应文件，类名均为 `ChannelConfig`。

---

## 逐字段对比表

| 字段 | 洛阳 luoyang | 银川 yinchuan | 重庆 chongqing | 石河子 shihezi |
|------|-------------|---------------|----------------|----------------|
| **BASE_URL_Test0** | `https://txzcloudservice.caacsri.com` | —（未定义） | `https://caq-kzqtxz.caacsri.com` | `https://txzcloudservice.caacsri.com` |
| **BASE_URL** | `https://txzcloudservice.caacsri.com` | `https://inckzqtxz.caacsri.com`（正式） | `https://cqakzqtxz.caacsri.com`（正式） | `https://txzcloudservice.caacsri.com` |
| **BASE_URL_Test1** | `https://txzcloudservice.caacsri.com` | —（未定义） | `http://test.sczhbf.com:58088`（慧能测试） | `https://txzcloudservice.caacsri.com` |
| **TENANT_PREFIX** | `"fy"` | `""`（空） | `""`（空） | `"shf"` |
| **TENANT_ID** | `"2054084946120802305"` | `"1"` | `"3"` | `"1"` |
| **SUPPORTS_TEMPORARY_PASS** | **`false`** | `true` | `true` | `true` |

---

## 字段语义

### BASE_URL / BASE_URL_Test0 / BASE_URL_Test1

- 业务 API 根域名；Test 变体供切换测试环境（具体使用见 `UrlConstants` / 构建配置）。
- 银川仅声明 `BASE_URL`（注释中含测试/正式切换说明）。

### TENANT_PREFIX

- 拼在域名与 `app-api` 之间。
- **有前缀**：`{BASE_URL}/{TENANT_PREFIX}/app-api/...`（洛阳 `fy`、石河子 `shf`）
- **空前缀**：`{BASE_URL}/app-api/...`（银川、重庆）
- **system 接口**：`{BASE_URL}/app-api/system/...`（**不带**租户路径前缀，见类注释）

### TENANT_ID

- HTTP Header `tenant-id` 的值（与 `UrlConstants.TENANT_ID` 对应）。

### SUPPORTS_TEMPORARY_PASS

| 值 | 影响 |
|----|------|
| `false`（洛阳） | 无临时证业务；`Document3` 占位「不支持临时通行证」 |
| `true` | 支持临时证 `Document3` 完整 UI 与数据流 |

---

## 环境 URL 备注（源码注释）

| 渠道 | 注释中的其它环境 |
|------|------------------|
| 银川 | `inc-kzqtxz` 二所测试；`inckzqtxz` 二所正式；`test.sczhbf.com:58088` 慧能测试 |
| 重庆 | `caq-kzqtxz` 测试；`cqakzqtxz` 正式；慧能测试同银川 |

---

## 架构示意

```text
洛阳/石河子:
  https://txzcloudservice.caacsri.com/fy/app-api/...     (洛阳)
  https://txzcloudservice.caacsri.com/shf/app-api/...    (石河子)

银川/重庆:
  https://inckzqtxz.caacsri.com/app-api/...
  https://cqakzqtxz.caacsri.com/app-api/...

System（四渠道相同规则）:
  {BASE_URL}/app-api/system/...
```

---

## 与 UI 风味的关联

| ChannelConfig | UI |
|---------------|-----|
| `SUPPORTS_TEMPORARY_PASS=false` | 洛阳 `Document3` 占位布局 |
| `TENANT_PREFIX` | 不影响证件 UI，仅 API 路径 |
| 机场名称等 | 各 flavor `res/values` 中 `channel_airport_name` 等（非 ChannelConfig 字段） |
