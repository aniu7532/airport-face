# AccessRecordFragment / PagingSource 参数

## 模块关系

```
AccessRecordFragment
  ├── AccessRecordViewModel
  │     └── AccessRecordPagingSource → GET page-need-verify
  ├── AccessRecordHeaderAdapter（固定标题）
  └── AccessRecordAdapter（分页列表）
```

与核销 Tab 结构对称，差异见下表。

## 与 WriteOffRecord 对比

| 项 | 核销 Tab | 通行记录 Tab |
|---|---|---|
| Fragment | `WriteOffRecordFragment` | `AccessRecordFragment` |
| ViewModel | `WriteOffRecordViewModel` | `AccessRecordViewModel` |
| PagingSource | `WriteOffRecordPagingSource` | `AccessRecordPagingSource` |
| 接口 | `checkRecordPageNeedVerifyNoOut` | `checkRecordPageNeedVerify` |
| 语义 | 有进无出待核实 | 全部需核实通行记录 |
| 总数 header | 有（`listTotal`） | 无 |
| 列表操作 | 核实按钮 | 只读 |
| 加载弹窗 | 有 | 无 |

## AccessRecordFragment

**路径**：`ui/fragment/AccessRecordFragment.kt`

### 筛选控件（与核销 Tab 相同）

| 控件 | ViewModel |
|---|---|
| `inputName` | `setName` |
| `inputCardNo` | `setCardNo` |
| `selectorStartTime` | `setStartTime` |
| `selectorEndTime` | `setEndTime` |
| `selectorCompany` | `setCompanyName` |
| `btnReset` | `reset()` |
| `btnSearch` | `search()` |

无 `listTotal` 订阅，无 `loadStateFlow` 加载弹窗。

### onResume 刷新

与核销 Tab 相同：`hasResumedOnce` 控制，再次进入调用 `viewModel.search()`。

## AccessRecordViewModel

**路径**：`ui/viewmodel/AccessRecordViewModel.kt`

### 默认筛选值

| 字段 | 默认值 |
|---|---|
| `name` / `cardNo` / `companyName` | `""` |
| `startTime` | `dayStart` |
| `endTime` | `dayEnd` |

### PagingConfig

与核销 Tab 一致：`pageSize=10`、`prefetchDistance=1`、`enablePlaceholders=false`。

### searchSnapshot

```kotlin
Pair(CheckRecordQuery(...), version: Long)
```

`search()` 递增 version；`flatMapLatest` 重建 Pager；`cachedIn(viewModelScope)`。

**差异**：无 `_listTotal`，PagingSource 不接收 `onQueryTotal` 回调。

## CheckRecordQuery（共用）

**定义位置**：`ui/pagingsource/WriteOffRecordPagingSource.kt`

```kotlin
data class CheckRecordQuery(
    val nickname: String = "",
    val idCode: String = "",
    val startCheckTime: String = "",
    val endCheckTime: String = "",
    val companyName: String = "",
)
```

时间格式化：`yyyy-MM-dd HH:mm:ss`。

## AccessRecordPagingSource

**路径**：`ui/pagingsource/AccessRecordPagingSource.kt`

### HTTP 请求

| 项 | 值 |
|---|---|
| URL | `UrlConstants.checkRecordPageNeedVerify` |
| 完整路径 | `{businessAppApiBase}/check/record/page-need-verify` |
| Method | GET |
| Tag | `UrlConstants.checkRecordPageNeedVerify` |

### Headers

| Header | 值 |
|---|---|
| `tenant-id` | `UrlConstants.TENANT_ID` |
| `Authorization` | `Bearer {ApiUtils.accessToken}`（token 非空时） |

### Query 参数明细

| 参数名 | 来源 | 是否必传 | 说明 |
|---|---|---|---|
| `pageNo` | `params.key ?: 1` | 是 | 页码从 1 开始 |
| `pageSize` | 硬编码 `10` | 是 | 与 ViewModel PagingConfig 一致 |
| `nickname` | `query.nickname` | 是 | 可为空字符串 |
| `idCode` | `query.idCode` | 是 | 可为空字符串 |
| `startCheckTime` | `query.startCheckTime` | 是 | 默认当天 00:00:00 |
| `endCheckTime` | `query.endCheckTime` | 是 | 默认当天 23:59:59 |
| `companyName` | `query.companyName` | 条件 | **仅非空时** `.params("companyName", ...)` |

### 响应解析

```kotlin
val list = response?.body()?.data?.list ?: emptyList()
```

实体：`CardRecords.ListDTO`（与核销 Tab 相同）。

### 分页键

| 条件 | prevKey | nextKey |
|---|---|---|
| pageIndex == 1 | null | list 非空 → pageIndex+1 |
| pageIndex > 1 | pageIndex-1 | list 非空 → pageIndex+1 |
| list 为空 | — | null |

### getRefreshKey

固定 `1`。

### 错误处理

`onError` → `resume(emptyList())`，Paging 显示空数据而非 Error 态（除非 load 抛异常）。

## AccessRecordAdapter

**路径**：`ui/adapter/AccessRecordAdapter.kt`

### 额外展示字段（相对核销 Tab）

| 字段 | 展示 |
|---|---|
| `deviceCode` | 设备编号 |
| `direction` | 1→进，-1→出，2→核验 |

无核实按钮，无 `VerifyAndConfirmDialog`。

## AccessRecordHeaderAdapter

固定标题：`通行记录列表`，无总数、无刷新按钮。

## 调用时序

```
用户点击「查询」/ 再次 onResume
  → ViewModel.search()
  → searchSnapshot 更新 (query, version+1)
  → flatMapLatest 新建 Pager
  → AccessRecordPagingSource.load(pageNo)
  → OkGo GET page-need-verify
  → adapter.submitData(PagingData)
```
