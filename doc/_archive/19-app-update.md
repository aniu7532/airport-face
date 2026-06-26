# 版本更新

## 依赖与初始化

- 模块：`:xupdate-lib`（`com.xuexiang.xupdate`）
- Application：`ArcFaceApplication.onCreate()` → `XUpdate.get().init(this)`

## UpdateUtils

**路径**：`util/UpdateUtils.java`

### 检查更新

```java
UpdateUtils.update(context, onFailureListener);
```

内部配置：

| 配置项 | 值 |
|--------|-----|
| debug | true |
| isWifiOnly | false |
| isGet | true |
| supportSilentInstall | false |
| updateUrl | `URL_GET_APP_LAST_VERSION` |
| 额外参数 | `type=3`（竖屏客户端） |
| 布局 | `R.layout.xupdate_dialog_update_port` |
| 宽度 | 屏幕 80% |

### 版本比较逻辑（CustomUpdateParser）

1. 解析响应为 `Base<Version>`
2. `code != 200` → 无更新
3. `result.getVersion().compareTo(AppUtils.getAppVersionName()) > 0` → 有新版本
4. 构建 `UpdateEntity`：
   - `force` ← `isForceUpdate == 1`
   - `versionName` ← `result.getVersion()`
   - `updateContent` ← `remark`
   - `downloadUrl` ← `url`

> 使用字符串 **compareTo** 比较版本名，需保证后台版本号格式与本地 `versionName`（如 `1.0.72`）可字典序比较。

### HTTP 层 OKHttpUpdateHttpService

下载与检查均通过 OkGo，自动附加：

```java
.headers("tenant-id", UrlConstants.TENANT_ID)
.headers("Authorization", "Bearer " + accessToken)  // 若存在
```

## 触发入口

| 入口 | 说明 |
|------|------|
| `CustomDrawerPopupView` | 运维手动检查 |
| `UpdatePopDialog` | 查验页弹窗式更新（带进度） |
| 部分 Activity onResume | 自动静默检查（视实现） |

## UpdatePopDialog

- 自定义竖屏更新 UI
- 集成下载进度 `NumberProgressBar`
- 下载完成调起安装 Intent

## 安装

- `FileProvider` 授权安装 APK
- `AndroidManifest` 配置 `update_file_paths.xml`
- `DefaultInstallListener` / 自定义 `OnInstallListener`

## 接口

**GET** `/app-api/system/appVersion/get-lastVersion?type=3`

响应 `Version` 字段见 [17-entity-models.md](./17-entity-models.md)。

## 独立 update 模块

`update/`（`com.sz.zysx.autoupdate`）未被 app 依赖，仅作安装测试，**不在生产更新链路中**。

## 相关文档

- 接口清单 → [16-api-reference.md](./16-api-reference.md)
- 运维入口 → [13-settings-and-ops-drawer.md](./13-settings-and-ops-drawer.md)
