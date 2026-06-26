# HTTP 回调与转换器（data/http）

> 目录：`app/src/main/java/com/arcsoft/arcfacedemo/data/http/`

本包基于 [OkGo](https://github.com/jeasonlzy/okhttp-OkGo) 的 `AbsCallback`，提供 JSON/XML 响应解析、网络错误 Toast 及 HTTP 日志拦截。全局 OkGo 初始化见 `HttpInitUtils`。

---

## 类关系

```
HttpInitUtils.init()
    └── 配置 OkGo + HttpLog 拦截器 + SSL

OkGo.execute(callback)
    ├── JsonCallback<T>        ← 最常用，JSON → Bean
    ├── StringStateCallback    ← 字符串 + 网络错误 Toast（无子类引用）
    ├── XmlStringCallback      ← XML 剥壳 → String（无子类引用）
    ├── XmlJsonCallback<T>     ← XML 剥壳 → JSON → Bean（无子类引用）
    └── OkGo 内置 StringCallback ← ApiUtils 使用

XmlStringConvert  ← XmlStringCallback / XmlJsonCallback 依赖
HttpLog           ← OkHttp Interceptor，非 Callback
```

---

## JsonCallback&lt;T&gt;

**文件**：`JsonCallback.java`  
**继承**：`AbsCallback<T>`

### 构造方法

| 构造 | 说明 |
|------|------|
| `JsonCallback()` | 通过泛型父类反射获取 `T` |
| `JsonCallback(Type type)` | 显式指定反序列化类型（如 `TypeToken`） |
| `JsonCallback(Class<T> clazz)` | 显式指定 Class |

### 核心方法

| 方法 | 行为 |
|------|------|
| `onStart(Request)` | 空扩展点，可在此加公共 Header（当前未实现） |
| `convertResponse(Response)` | 子线程解析响应体 |

### `convertResponse` 解析规则

1. `response.body().string()` 读全文
2. 空 body → 抛 `IllegalStateException("返回值为空")`
3. `"{}"` → 抛 `IllegalStateException("json内容为空")`
4. 按目标类型转换：
   - `String.class` → 原样返回
   - `JSONObject.class` → `new JSONObject(data)`
   - `JSONArray.class` → `new JSONArray(data)`
   - 其他 → `new Gson().fromJson(data, type/clazz)`

### 项目内使用方

| 类 | 泛型目标 | 场景 |
|----|----------|------|
| `LoginActivity` | （间接，Login 走 ApiUtils String） | — |
| `LivenessDetectJinActivity` | `Base<String>`、`Base<Version>` | 即时上传记录、检查更新 |
| `LivenessDetectYuanActivity` | 同上 | 同上 |
| `LivenessDetectYuanAndJinActivity` | 同上 | 同上 |
| `RegisterAndRecognizeActivity` | 同上 | 同上 |
| `RecordsPopDialog` | `Base<CardRecords>` | 弹窗查询记录 |
| `AccessRecordPagingSource` | `Base<CardRecords?>` | 通行记录分页 |
| `WriteOffRecordPagingSource` | `Base<CardRecords?>` | 核销记录分页 |
| `AreaPickerDialog` | `Base<List<Area?>>` | 区域树 |
| `VerifyAndConfirmDialog` | `Base<List<DeviceResult>>`、`Base<Boolean>` | 设备列表、核实 |
| `InOutStatisticsViewModel` | `Base<List<InOutStatisticsResult>>` | 进出统计 |
| `CheckUnitRepository` | `Base<List<CheckUnit>>` | 申办单位 |
| `LogUploadUtils` | `Base2<String>` | 日志上传 |
| `LongPassCardsRemedialMeasuresUtils` | `Base<String>` | 异常通行证上报 |

---

## StringStateCallback

**文件**：`StringStateCallback.java`  
**继承**：`AbsCallback<String>`

### 行为

| 方法 | 说明 |
|------|------|
| `convertResponse` | 委托 `StringConvert`，关闭 response，返回字符串 |
| `onError` | 按异常类型 Toast： |
| | `ConnectException` → 「请检查服务器地址是否正确及服务器是否正常运行！」 |
| | `UnknownHostException` → 「请检查网络连接是否正常及通畅！」 |
| | `SocketTimeoutException` → 「请求超时，请重试或稍后再试！」 |
| | 其他 → 「网络异常，请重试或稍后再试！」 |

**现状**：项目中 **无子类继承**，属于预留基类。

---

## XmlStringCallback

**文件**：`XmlStringCallback.java`  
**继承**：`AbsCallback<String>`

### 行为

- 内部持有 `XmlStringConvert`
- `convertResponse` → 剥 XML 后返回内部文本

**现状**：**无子类引用**，可能为历史 SOAP/XML 接口预留。

---

## XmlJsonCallback&lt;T&gt;

**文件**：`XmlJsonCallback.java`  
**继承**：`AbsCallback<T>`

### 与 JsonCallback 的差异

`convertResponse` 在 JSON 解析前增加 XML 剥壳：

```java
if (ObjectUtils.isNotEmpty(data) && data.length() > 65) {
    data = data.substring(data.indexOf("\">") + 2, data.indexOf("</"));
}
```

其余解析逻辑与 `JsonCallback` 相同。

**现状**：**无项目内引用**。

---

## XmlStringConvert

**文件**：`XmlStringConvert.java`  
**实现**：`Converter<String>`

| 条件 | 返回值 |
|------|--------|
| body 非空且长度 > 85 | 截取 `">` 与 `</` 之间文本；若非 `"null"` 则返回 |
| 其他 | `null` |

用于从 XML 包裹响应中提取 JSON 或纯文本。

---

## HttpLog

**文件**：`HttpLog.java`  
**实现**：`okhttp3.Interceptor`（非 Callback）

### 配置（HttpInitUtils）

```java
HttpLog loggingInterceptor = new HttpLog(ArcFaceApplication.TAG);
loggingInterceptor.setPrintLevel(HttpLog.Level.BODY);
loggingInterceptor.setColorLevel(Level.INFO);
builder.addInterceptor(loggingInterceptor);
```

### Level 枚举

| 级别 | 打印内容 |
|------|----------|
| `NONE` | 不打印 |
| `BASIC` | 请求/响应首行 |
| `HEADERS` | 全部 Header |
| `BODY` | Header + Body（文本类型） |

日志同时写入 `ALog.file()` 本地文件。

---

## HttpInitUtils

**文件**：`HttpInitUtils.java`

| 方法 | 说明 |
|------|------|
| `init(Application)` | 默认超时 2 分钟 |
| `init(Application, long time)` | 自定义读写连接超时 |
| `clearSession()` | 空实现，预留 |

### 初始化要点

- 添加 `HttpLog` 拦截器（BODY 级别）
- SSL：`ImageDownloader.unsafeOkHttpClient()` 信任所有证书
- `OkGo.getInstance().init(context).setOkHttpClient(...).setRetryCount(0)`
- 全局公共 Header/Params 已注释，各请求自行添加

---

## Callback 选用指南

| 场景 | 推荐 |
|------|------|
| 标准 JSON API（本项目主流） | `JsonCallback<Base<T>>` |
| 仅需原始字符串 | OkGo `StringCallback` 或 `ApiUtils` |
| 需统一网络错误 Toast 的字符串 API | 继承 `StringStateCallback` |
| XML 包裹的 JSON（遗留接口） | `XmlJsonCallback<T>` |
| XML 包裹的纯文本 | `XmlStringCallback` |

**注意**：`ArcFaceApplication.startUpDataToServer()` 批量上传记录使用 **同步** `Call.execute()` + `StringConvert`，不走上述 Callback。
