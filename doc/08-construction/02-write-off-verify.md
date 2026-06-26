# WriteOffRecordFragment / ViewModel / PagingSource / VerifyAndConfirmDialog

## 模块关系

```
WriteOffRecordFragment
  ├── WriteOffRecordViewModel（筛选 + Paging3）
  │     └── WriteOffRecordPagingSource → GET page-need-verify-no-out
  ├── WriteOffRecordHeaderAdapter（总数 + 刷新）
  └── WriteOffRecordAdapter（列表 + 核实按钮）
        └── VerifyAndConfirmDialog → PUT verify
```

## WriteOffRecordFragment

**路径**：`ui/fragment/WriteOffRecordFragment.kt`

### 筛选绑定

| UI 控件 | ViewModel 方法 | StateFlow |
|---|---|---|
| `inputName` | `setName(it)` | `name` |
| `inputCardNo` | `setCardNo(it)` | `cardNo` |
| `selectorStartTime` | `setStartTime(it)` | `startTime` |
| `selectorEndTime` | `setEndTime(it)` | `endTime` |
| `selectorCompany` | `setCompanyName(it)` | `companyName` |
| `btnReset` | `reset()` | — |
| `btnSearch` | `search()` | — |
| `foldView` | 折叠/展开 `foldGroup` | — |

### 列表结构

- `ConcatAdapter(WriteOffRecordHeaderAdapter, WriteOffRecordAdapter)`
- `LinearLayoutManager.VERTICAL`
- ItemDecoration：position 0（header）底部 8dp，其余 20dp

### 数据流订阅（`repeatOnLifecycle(STARTED)`）

| Flow | 作用 |
|---|---|
| `viewModel.name/cardNo/...` | reset 时反向清空 UI |
| `viewModel.listTotal` | `headerAdapter.setTotal(it)` |
| `viewModel.cardRecords` | `adapter.submitData(it)` |
| `adapter.loadStateFlow` | refresh Loading 时弹窗；NotLoading/Error 关闭 |

### 加载弹窗

- `LoadingPopDialog("加载中，请稍后......")`
- **不可** `isDestroyOnDismiss(true)`（Paging refresh 会多次 show/dismiss）
- `onDestroyView` 强制 dismiss

## WriteOffRecordViewModel

**路径**：`ui/viewmodel/WriteOffRecordViewModel.kt`

### 默认筛选值

| 字段 | 默认值 |
|---|---|
| `name` / `cardNo` / `companyName` | `""` |
| `startTime` | `Calendar.getInstance().dayStart` |
| `endTime` | `Calendar.getInstance().dayEnd` |

时间格式：`yyyy-MM-dd HH:mm:ss`（`Locale.CHINA`）

### PagingConfig

| 参数 | 值 |
|---|---|
| `pageSize` | 10 |
| `prefetchDistance` | 1 |
| `enablePlaceholders` | false |

### searchSnapshot 机制

```kotlin
private val searchSnapshot = MutableStateFlow(Pair(CheckRecordQuery(...), version: Long))
```

- 进入页面：`searchSnapshot` 初始值触发第一页加载（空条件）
- `search()`：重建 `CheckRecordQuery`，`version + 1`，保证同条件重复点击也重新请求
- `flatMapLatest`：切换查询时 `_listTotal = null`，重建 `Pager`
- `cachedIn(viewModelScope)`：配置变更缓存

### listTotal

首页成功后由 `WriteOffRecordPagingSource.onQueryTotal` 回调写入；切换查询前先置 `null`。

## CheckRecordQuery

**路径**：`ui/pagingsource/WriteOffRecordPagingSource.kt`（与 AccessRecord 共用）

| 字段 | 接口参数名 |
|---|---|
| `nickname` | `nickname` |
| `idCode` | `idCode` |
| `startCheckTime` | `startCheckTime` |
| `endCheckTime` | `endCheckTime` |
| `companyName` | `companyName`（非空才传） |

## WriteOffRecordPagingSource

### 接口

- **URL**：`UrlConstants.checkRecordPageNeedVerifyNoOut`
- **方法**：GET
- **Headers**：`tenant-id`、`Authorization: Bearer {accessToken}`（有 token 时）

### 请求参数

| 参数 | 值 |
|---|---|
| `pageNo` | 从 1 递增 |
| `pageSize` | 10（硬编码） |
| 筛选字段 | 见 CheckRecordQuery |

### 分页逻辑

| 场景 | prevKey | nextKey |
|---|---|---|
| 第 1 页 | `null` | 有数据 → `pageIndex + 1` |
| 中间页 | `pageIndex - 1` | 有数据 → `pageIndex + 1` |
| 空列表 | — | `null`（终止） |

### 错误处理

`onError` → `continuation.resume(emptyList())`（不抛异常，表现为空页）

### getRefreshKey

固定返回 `1`。

## WriteOffRecordAdapter

**路径**：`ui/adapter/WriteOffRecordAdapter.kt`

### 列表项展示

| 字段 | UI |
|---|---|
| `nickname` | 姓名 |
| `idCode` | 证件编号 |
| `companyName` | 申办单位 |
| `areaName` | 通行道口 |
| `checkTime` | 通行时间 |
| `checkUserName` | 查验人员 |
| `verifyRemark` | 核实备注（空则 GONE） |
| 照片 | `sitePhoto` 优先，否则 `checkPhoto` |

### 照片加载

| 路径类型 | 加载方式 |
|---|---|
| `http*` / `encrypted/` | `UrlConstants.fileStreamUrl` + Bearer 头 Glide |
| 本地路径 | `EncryptedGlideFile` |

### 核实按钮

```kotlin
VerifyAndConfirmDialog(context, value) {
    adapter.refresh()  // 核实成功后刷新 Paging
}
```

`XPopup.isDestroyOnDismiss(true)`

## VerifyAndConfirmDialog

**路径**：`widget/dialog/VerifyAndConfirmDialog.kt`  
**布局**：`verify_and_confirm_dialog.xml`

### 表单字段

| 控件 | 变量 | 提交字段 |
|---|---|---|
| `selector_area` | `selectedAreaPath: List<Area>` | `area`（末级 id）、`areaName` |
| `selector_time` | `calendar: Calendar?` | `checkTime` |
| `selector_device_code` | `device: DeviceResult?` | `deviceId`、`deviceName` |
| `et_mark` | — | `verifyRemark` |
| 固定 | `result.id` | `id` |

### 区域名称格式（API）

`formatAreaNameForApi`：每层 `code + name` 拼接，层间无分隔符。

### 设备列表

- 首次点击 → GET `checkDeviceList`
- 加载中 `LoadingPopDialog("初始化中，请稍后......")`
- 选择 → `StringListPickerDialog`

### 必填校验（VerifyFeatureSettings）

仅当 `isVerifyFeatureEnabled()` 为 true 时生效：

| 开关 | SP 键 | 校验 |
|---|---|---|
| 通行道口 | `verify_required_passage` | `selectedAreaPath` 非空 |
| 通行时间 | `verify_required_pass_time` | `calendar != null` |
| 设备编号 | `verify_required_device` | `device != null` |
| 备注 | `verify_required_remark` | 备注非空 |

### 提交

- **URL**：`UrlConstants.checkRecordVerify`
- **方法**：PUT JSON
- 成功：`dismiss` + `successCb()`（触发列表 refresh）
- 失败：关闭 loading 并 dismiss

## WriteOffRecordHeaderAdapter

| 状态 | 标题文案 |
|---|---|
| `total == null` | `有进无出列表` |
| `total != null` | `有进无出列表（总数：{total}）` |

刷新按钮 → `onRefresh` → Fragment 内 `adapter.refresh()` + Loading 弹窗。
