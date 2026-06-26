# 渠道业务规则差异

> 源码：`app/src/{flavor}/java/.../config/ChannelConfig.java`  
> 消费方：查验 Activity、`UrlConstants`、部分 UI

四渠道通过 **Gradle productFlavor** 编译期注入不同 `ChannelConfig`，运行时无动态切换。

---

## 渠道对照总表

| 渠道 flavor | BASE_URL（正式） | TENANT_PREFIX | TENANT_ID | SUPPORTS_TEMPORARY_PASS |
|-------------|------------------|---------------|-----------|-------------------------|
| yinchuan 银川 | `https://inckzqtxz.caacsri.com` | `""` | `"1"` | `true` |
| chongqing 重庆 | `https://cqakzqtxz.caacsri.com` | `""` | `"3"` | `true` |
| shihezi 石河子 | `https://txzcloudservice.caacsri.com` | `"shf"` | `"1"` | `true` |
| luoyang 洛阳 | `https://txzcloudservice.caacsri.com` | `"fy"` | `"2054084946120802305"` | **`false`** |

`UrlConstants.TENANT_ID` = `ChannelConfig.TENANT_ID`，所有 HTTP 请求头 `tenant-id` 均取此值。

---

## TENANT_PREFIX → API 路径

`UrlConstants` 拼接业务 API 时：

```text
{BASE_URL}/{TENANT_PREFIX}/app-api/...
```

| 渠道 | 示例 login URL 片段 |
|------|---------------------|
| 银川/重庆 | `.../app-api/...`（无前缀段） |
| 石河子 | `.../shf/app-api/...` |
| 洛阳 | `.../fy/app-api/...` |

**联调注意**：换渠道 APK 后若 404，先核对 `TENANT_PREFIX` 是否与后台租户路由一致。

---

## SUPPORTS_TEMPORARY_PASS（临时证开关）

### 定义

```java
// luoyang
public static final boolean SUPPORTS_TEMPORARY_PASS = false;

// 其余三渠道
public static final boolean SUPPORTS_TEMPORARY_PASS = true;
```

### 影响范围（代码引用点）

| Activity | 典型判断 |
|----------|----------|
| `LivenessDetectJinActivity` | 4 处 `!ChannelConfig.SUPPORTS_TEMPORARY_PASS` |
| `LivenessDetectYuanActivity` | 同上 |
| `LivenessDetectYuanAndJinActivity` | 同上 |
| `RegisterAndRecognizeActivity` | 3 处 |

### 行为差异

当 `SUPPORTS_TEMPORARY_PASS == false`（洛阳）：

1. **刷卡入口**：不处理/不展示临时证相关分支（`type == 1` 的临时通行证）
2. **卡面 UI**：跳过临时证 Document 展示逻辑
3. **校验规则**：`longTermPass.type == 1` 时直接拒绝或走长期证以外路径

当 `true` 时：长期证（`type != 1`）与临时证均支持，含临时证照片加载 `DocumentCardSupport.loadTemporaryCardPhoto`。

### 排查

| 现象 | 检查 |
|------|------|
| 洛阳刷临时证无反应 | 预期行为，`SUPPORTS_TEMPORARY_PASS=false` |
| 其他渠道临时证不显示 | 查 `LongTermPass.type`、Document3 是否被 inflate |
| 接口 tenant 错误 | 查 `TENANT_ID` 与后台租户配置 |

---

## 卡面 UI 渠道差异（摘要）

详见 [06-pass-card/04-document-ui-framework.md](../06-pass-card/04-document-ui-framework.md)。

| 渠道 | Document2 特点 | Document3 特点 |
|------|----------------|----------------|
| 银川 | 黄条 `yellow_stripes`、区域单行 `access_area` | 继承 `AbstractDocument3` |
| 重庆 | 渠道 layout 覆盖 | 渠道 layout 覆盖 |
| 石河子 | 继承抽象类默认 | 继承抽象类默认 |
| 洛阳 | **区域徽章 4 列网格**、`formatValidityPeriod` | 洛阳专属字段布局 |

各渠道 `Document2`/`Document3` 类路径：

```text
app/src/{yinchuan|chongqing|shihezi|luoyang}/java/.../ui/fragment/
```

---

## 测试环境 URL

各 `ChannelConfig` 内保留注释或 `BASE_URL_Test0` / `BASE_URL_Test1` 备用地址。正式包使用 `BASE_URL`；本地联调需在对应 flavor 的 `ChannelConfig` 中切换（需重新编译）。

---

## 联调检查清单

- [ ] 安装包 flavor 与目标机场一致
- [ ] `tenant-id` 请求头与后台租户 ID 匹配
- [ ] API 路径是否含 `shf`/`fy` 前缀
- [ ] 洛阳场景不测临时证；其他渠道需测临时证全流程
- [ ] 卡面区域展示样式是否符合该渠道设计稿
