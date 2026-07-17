# ApiUtils 网络请求工具类

> 源码：`app/src/main/java/com/arcsoft/arcfacedemo/network/ApiUtils.java`

`ApiUtils` 是对 OkGo 的轻量封装，负责 **Token 静态存储**、**统一请求头**（`tenant-id`、`Authorization`）以及 **GET/POST 异步调用**。项目内大量网络请求直接使用 OkGo，仅部分场景走 `ApiUtils`。

---

## 类结构概览

| 成员 | 类型 | 说明 |
|------|------|------|
| `client` | `private static final OkHttpClient` | 通过 `getUnsafeOkHttpClient()` 创建，**当前未被任何 public 方法使用**（旧版 OkHttp 实现已注释） |
| `accessToken` | `public static String` | 访问令牌，登录成功后写入 |
| `refreshToken` | `public static String` | 刷新令牌 |
| `userId` | `public static String` | 当前登录用户 ID |

---

## Token 静态字段

### `accessToken`

- **写入**：`LoginActivity.login()` 解析登录响应后调用 `ApiUtils.setAccessToken(accessToken)`
- **读取**：`getAccessToken()` 或直接访问 `ApiUtils.accessToken`
- **用途**：作为 `Authorization: Bearer {accessToken}` 请求头附加到 OkGo / ApiUtils 请求

### `refreshToken`

- **写入**：`LoginActivity.login()` → `ApiUtils.setRefreshToken(refreshToken)`
- **读取**：`getRefreshToken()`；`TokenRefreshJobService` 的实现会读取该值
- **用途现状**：刷新 Job 的 Manifest 注册和 Activity 调度均被注释，当前运行版本不会自动刷新 Token

### `userId`

- **写入**：`LoginActivity.login()` → `ApiUtils.userId = userId`（同时写入 `InfoStorage`）
- **读取**：各 Activity 写入通行记录时赋值 `checkUserId`
- **无 getter/setter**，直接访问静态字段

---

## Public 方法详解

### Token 管理

| 方法 | 签名 | 行为 |
|------|------|------|
| `setAccessToken` | `void setAccessToken(String token)` | 赋值 `accessToken = token` |
| `getAccessToken` | `String getAccessToken()` | 返回 `accessToken` |
| `setRefreshToken` | `void setRefreshToken(String token)` | 赋值 `refreshToken = token` |
| `getRefreshToken` | `String getRefreshToken()` | 返回 `refreshToken` |

### 回调接口 `ApiCallback`

```java
public interface ApiCallback {
    void onSuccess(String response);  // HTTP 2xx 且 body 非空
    void onFailure(Throwable e);      // 网络错误或 HTTP 非成功
}
```

返回原始 JSON 字符串，调用方自行 Gson 解析。

### `get(String url, Map<String, String> params, ApiCallback callback)`

**用途**：通用 GET 请求。

**执行流程**：

1. `OkGo.<String>get(url).tag(url)` 构建请求
2. 若 `params != null`：
   - 自动注入 `timestamp = System.currentTimeMillis()`
   - 遍历 `params` 添加 query 参数
3. 添加请求头：
   - `tenant-id: UrlConstants.TENANT_ID`
   - 若 `accessToken != null`：`Authorization: Bearer {accessToken}`
4. `request.execute(new StringCallback() { ... })` 异步执行
5. `onSuccess`：`response.isSuccessful()` 时回调 `callback.onSuccess(response.body())`，否则 `onFailure`
6. `onError`：回调 `callback.onFailure(response.getException())`

**与 `getPassCard` 的区别**：逻辑几乎相同；`getPassCard` 命名语义专用于通行证分页，但实现代码一致。

### `getPassCard(String url, Map<String, String> params, ApiCallback callback)`

**用途**：通行证分页 GET（设计意图），实现与 `get()` 相同。

**现状**：全项目 **无任何调用方**，`ArcFaceApplication.fetchNextPage()` 直接使用 OkGo 同步请求 `UrlConstants.URL_GetLongPass`。

### `post(String url, String json, ApiCallback callback)`

**用途**：JSON Body POST 请求。

**执行流程**：

1. `OkGo.<String>post(url).tag(url)`
2. 请求头：`tenant-id`、可选 `Authorization`
3. `request.upJson(json)` 设置 JSON Body
4. 异步 `StringCallback`，成功/失败逻辑同 `get()`

### `upload(String url, String imgStr, ApiCallback callback)`

**现状**：**空实现**，方法体为空。图片上传由 `ImageUploader` 直接使用 OkGo 完成。

---

## SSL「信任所有证书」

### `getUnsafeOkHttpClient()`（private static）

创建自定义 `OkHttpClient`：

1. `X509TrustManager` 空实现：`checkClientTrusted` / `checkServerTrusted` 不做校验
2. `SSLContext.getInstance("SSL")` + 上述 TrustManager
3. `builder.sslSocketFactory(sslSocketFactory, trustManager)`
4. `builder.hostnameVerifier((hostname, session) -> true)` — 跳过主机名校验

**注意**：

- 该 `client` 字段 **未被当前 public 方法引用**（旧 OkHttp 直连代码已注释）
- 项目实际 HTTPS 信任策略在 `HttpInitUtils.init()` 中通过 `ImageDownloader.unsafeOkHttpClient()` 配置到 **OkGo 全局 OkHttpClient**
- ApiUtils 内的 SSL 代码属于 **遗留/冗余**，OkGo 请求不走此 client

---

## 与 OkGo 的关系

```
Application.onCreate()
    └── HttpInitUtils.init()          ← 初始化 OkGo 单例、超时、SSL、日志拦截器

ApiUtils.get/post/getPassCard()
    └── OkGo.get/post()               ← 使用 HttpInitUtils 配置的全局 OkHttpClient
        └── StringCallback            ← 返回 String，非 JsonCallback

大量业务代码（ArcFaceApplication、LoginActivity、各 Activity）
    └── OkGo.get/post() 直接使用      ← 绕过 ApiUtils，自行加 headers
        └── JsonCallback / 同步 Call.execute()
```

| 维度 | ApiUtils | 直接使用 OkGo |
|------|----------|---------------|
| 返回类型 | 原始 `String` | 可指定泛型 + `JsonCallback<T>` |
| 请求方式 | 仅异步 `execute(callback)` | 异步或同步 `Call.execute()` |
| Token 头 | 自动附加 | 各调用方手动 `request.headers("Authorization", ...)` |
| timestamp | GET 自动注入 | 调用方自行添加（如 `fetchNextPage`） |
| SSL | 独立 unused client | 走 `HttpInitUtils` 全局配置 |

---

## 调用关系（谁调用谁）

### ApiUtils 被谁调用

| 调用方 | 方法 | 场景 |
|--------|------|------|
| `LoginActivity` | `post(URL_LOGIN, ...)` | 登录 |
| `LoginActivity` | `get(URL_GETCHECKMETH, ...)` | 获取验证方式 |
| `LivenessDetectJinActivity` | `get(URL_GET_SYSTEM_TIME, ...)` | 同步系统时间 |
| `LivenessDetectYuanActivity` | `get(URL_GET_SYSTEM_TIME, ...)` | 同步系统时间 |
| `LivenessDetectYuanAndJinActivity` | `get(URL_GET_SYSTEM_TIME, ...)` | 同步系统时间 |

### ApiUtils 静态字段被谁读取（非 ApiUtils 方法）

| 调用方 | 用途 |
|--------|------|
| `LoginActivity` | 写入 Token / userId |
| `ArcFaceApplication` | `startUpDataToServer`、`fetchNextPage`、heartbeat 加 Authorization |
| `LivenessDetect*Activity` | 写记录 `checkUserId`、OkGo 请求加 Token |
| `RegisterAndRecognizeActivity` | 同上 |
| `ImageUploader` / `ImageDownloader` | 上传/下载加密文件加 Token |
| `TokenRefreshJobService` | `getRefreshToken()`；代码已实现但当前未注册、未调度 |
| `RecordsListAdapter` 等 | `getAccessToken()` 加载图片 |
| `VerifyAndConfirmDialog`、`AreaPickerDialog` 等 | OkGo 请求加 Token |

### ApiUtils 不再使用的路径

- `getPassCard()`：无调用方
- `upload()`：空实现
- 各 Activity 中 `ApiUtils.post(URL_CREATE_*_RECORD, ...)` 已注释，改为 OkGo + `JsonCallback` 或 `ArcFaceApplication.startUpDataToServer()` 批量上传

---

## 请求头规范（ApiUtils 自动附加）

| Header | 值 | 条件 |
|--------|-----|------|
| `tenant-id` | `UrlConstants.TENANT_ID`（来自 `ChannelConfig.TENANT_ID`） | 始终 |
| `Authorization` | `Bearer {accessToken}` | `accessToken != null` |
| Query `timestamp` | 毫秒时间戳 | GET 且 `params != null` |
