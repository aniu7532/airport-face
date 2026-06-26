# XUpdate 集成与 CustomUpdateParser

## 组件位置

| 类/模块 | 路径 |
|---|---|
| `UpdateUtils` | `util/UpdateUtils.java` |
| XUpdate 库 | `xupdate-lib/` |
| 自定义弹窗布局 | `res/layout/xupdate_dialog_update_port.xml` |
| Application 初始化 | `ArcFaceApplication.onCreate()` → `XUpdate.get().init(this)` |

## UpdateUtils.update 入口

```java
UpdateUtils.update(context, OnUpdateFailureListener listener)
UpdateUtils.update(context, listener, OnInstallListener onInstallListener)
```

### XUpdate 全局配置（每次 update 调用时）

| 链式配置 | 值 |
|---|---|
| `debug` | true |
| `isWifiOnly` | false |
| `isGet` | true |
| `supportSilentInstall` | false |
| `setIUpdateHttpService` | `OKHttpUpdateHttpService`（内部 OkGo） |
| `setOnInstallListener` | 默认 `DefaultInstallListener` 或传入 |
| `setOnUpdateFailureListener` | 调用方传入 |
| `init` | `ArcFaceApplication.getApplication()` |

### 单次检查构建

```java
XUpdate.newBuild(context)
    .updateUrl(mJsonAddr)                    // UrlConstants.URL_GET_APP_LAST_VERSION
    .param("type", 3)
    .promptLayout(R.layout.xupdate_dialog_update_port)
    .promptThemeColor(R.color.light_red)
    .promptFocusColor(ColorUtils.colorDeep(...))
    .promptButtonTextColor(Color.WHITE)
    .promptTopResId(R.drawable.update_top_bg)
    .promptWidthRatio(0.8F)
    .updateParser(new CustomUpdateParser())
    .update();
```

### 检查 URL

| 项 | 值 |
|---|---|
| 常量 | `UpdateUtils.mJsonAddr` |
| 路径 | `SYSTEM_API + "/appVersion/get-lastVersion"` |
| 额外参数 | `type=3` |
| 注释掉的参数 | `versionCode`（未传） |

## CustomUpdateParser

实现 `IUpdateParser`，`isAsyncParser()` 返回 **false**（同步解析）。

### parseJson 流程

```
JSON 字符串
  → Gson 解析 Base<Version>
  → getParseResult(json)
```

### getParseResult 判定逻辑

| 条件 | 返回 |
|---|---|
| `base.getCode() != 200` | `UpdateEntity.setHasUpdate(false)` |
| `base.getData()` 为空 | `setHasUpdate(false)` |
| `result.getVersion().compareTo(AppUtils.getAppVersionName()) > 0` | 有更新，填充 UpdateEntity |
| 否则 | `null`（无更新） |

### UpdateEntity 字段映射

| UpdateEntity | Version 字段 | 说明 |
|---|---|---|
| `hasUpdate` | — | true |
| `isIgnorable` | — | false |
| `force` | `isForceUpdate == 1` | 强制更新 |
| `isSilent` | — | false |
| `isAutoInstall` | — | false |
| `showNotification` | — | false |
| `versionName` | `version` | 服务端版本名字符串 |
| `updateContent` | `remark` | 更新说明 |
| `downloadUrl` | `url` | APK 下载地址 |

未启用：`versionCode`、`md5`、`size`（代码中已注释）。

### 异步回调

```java
parseJson(json, callback) → callback.onParseResult(getParseResult(json))
```

## OKHttpUpdateHttpService

实现 `IUpdateHttpService`，底层统一 **OkGo**。

### asyncGet

- `OkGo.get(url).params(transform(params))`
- 追加 `timestamp`
- Headers：`tenant-id`、`Authorization`（有 token）

### asyncPost

- 默认 Form；`mIsPostJson=true` 时 `upJson`
- 同样带 timestamp 与鉴权头

### download

- `OkGo.get(url).execute(FileCallback(path, fileName))`
- 进度 → `callback.onProgress(fraction, totalSize)`
- Headers：`tenant-id`、`Authorization`

### cancelDownload

`OkGo.getInstance().cancelTag(url)`

## Version 实体（推断字段）

Parser 使用的 `entity/Version` 字段：

- `version`：String，与 `AppUtils.getAppVersionName()` 字符串比较
- `isForceUpdate`：int，1 为强制
- `remark`：更新文案
- `url`：下载链接

## 调用方

常见入口：

- 运维抽屉 `UpdatePopDialog`
- 手动检查更新菜单项

失败时由传入的 `OnUpdateFailureListener` 处理（Toast 等）。

## 与 Application 关系

| 时机 | 动作 |
|---|---|
| `ArcFaceApplication.onCreate` | `XUpdate.get().init(this)` 一次 |
| 每次 `UpdateUtils.update` | 再次 `.init(ArcFaceApplication)` 并链式配置 |

## 排查要点

| 现象 | 检查 |
|---|---|
| 永远无更新 | 服务端 `version` 字符串是否大于本地 `versionName`（字符串 compareTo） |
| 401/非 200 | `CustomUpdateParser` 返回 hasUpdate=false；查 token |
| 下载失败 | `OKHttpUpdateHttpService.download` 鉴权头；URL 可达性 |
| 强制更新不生效 | `isForceUpdate` 是否为 1 |

## 日志

```
UpdateUtils: update
CustomUpdateParser: 检查更新失败 / 无可用更新
CustomUpdateParser: entity.toString()（有更新时）
```
