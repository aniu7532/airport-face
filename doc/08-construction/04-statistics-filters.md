# InOutStatistics + 筛选组件

## 模块关系

```
InOutStatisticsFragment
  ├── InOutStatisticsViewModel → GET statistic-need-verify
  └── InOutStatisticsAdapter
        ├── InOutStatisticsHeader（标题）
        ├── InOutStatisticsTitle（表头）
        └── 数据行
```

**非 Paging**：单次 GET 返回 `List<InOutStatisticsResult>`，全量 `setList` 刷新。

## InOutStatisticsFragment

**路径**：`ui/fragment/InOutStatisticsFragment.kt`  
**布局**：`fragment_in_out_statistics.xml`

### 筛选控件（比核销/通行 Tab 少）

| 控件 | ViewModel | 说明 |
|---|---|---|
| `selectorStartTime` | `setStartTime` | 统计起始日 |
| `selectorEndTime` | `setEndTime` | 统计结束日 |
| `selectorCompany` | `setCompanyName` | 申办单位（可选） |
| `foldView` | 折叠 `foldGroup` | — |
| `btnReset` | `reset()` | 恢复默认并 request |
| `btnSearch` | `request()` | 重新请求 |

**无**姓名、证件号筛选。

### 默认 vs 重置值

| 字段 | 默认值 |
|---|---|
| `startTime` | `Calendar.getInstance().dayBefore(7)`（7 天前零点） |
| `endTime` | `Calendar.getInstance().dayEnd`（今天结束） |
| `companyName` | `""` |

核销/通行 Tab 默认时间为**当天**，统计 Tab 默认**近 7 天**。

### 生命周期

| 时机 | 行为 |
|---|---|
| `initView` 末尾 | `viewModel.request()` 首次加载 |
| 再次 `onResume` | `viewModel.request()` |

### ItemDecoration

| position | bottom 间距 |
|---|---|
| 0（header） | 10dp |
| 1（表头） | 0 |
| 其余 | 1dp |

## InOutStatisticsViewModel

**路径**：`ui/viewmodel/InOutStatisticsViewModel.kt`

### request() 请求

| 项 | 值 |
|---|---|
| URL | `UrlConstants.checkRecordStatisticNeedVerify` |
| Method | GET |
| Headers | `tenant-id`、`Authorization`（有 token） |

### Query 参数

| 参数 | 条件 |
|---|---|
| `startCheckTime` | `_startTime` 非 null，格式 `yyyy-MM-dd HH:mm:ss` |
| `endCheckTime` | `_endTime` 非 null |
| `companyName` | 非空才传 |

### 响应

```kotlin
_list.value = response?.body()?.data?.filterNotNull() ?: emptyList()
```

`onError` 无 UI 反馈（静默失败）。

## InOutStatisticsResult

**路径**：`entity/InOutStatisticsResult.kt`

| 字段 | 类型 | 含义 |
|---|---|---|
| `date` | String | 统计日期 |
| `inCount` | Int | 进港人次 |
| `outCount` | Int | 出港人次 |

## InOutStatisticsAdapter

**路径**：`ui/adapter/InOutStatisticsAdapter.kt`

### ConcatAdapter 结构

```
InOutStatisticsHeader     → "每日进出统计"
InOutStatisticsTitle      → 日期 | 进入人次 | 出来人次
InOutStatisticsAdapter    → 数据行
```

### 行背景

| 位置 | Drawable |
|---|---|
| 首行数据 | `bg_round_10_top`（表头用） |
| 中间行 | `bg_round_10_middle` |
| 末行 | `bg_round_10_bottom` |

## 共享筛选组件详解

### ConstructionWorkersInput

**路径**：`widget/ConstructionWorkersInput.kt`  
**布局**：`activity_construction_workers_input.xml`

- 带标题的文本输入
- `addTextChangedListener { callback }` 实时同步 ViewModel
- reset 时 ViewModel Flow 反向 `clear()`

### ConstructionWorkersTimeSelector

**路径**：`widget/ConstructionWorkersTimeSelector.kt`  
**布局**：`activity_construction_workers_selector.xml`

- 点击弹出时间选择
- `addOnTimeChangedListener(Calendar)`
- `setValue(Calendar)` 反向绑定
- 输出格式与 ViewModel `formatCheckTime` 一致

### ConstructionWorkersCompanyAutoComplete

**路径**：`widget/ConstructionWorkersCompanyAutoComplete.kt`

| 能力 | 实现 |
|---|---|
| 数据源 | `CheckUnitRepository.fetchSimpleList()` |
| 绑定 | `field.bind(fragment, onCompanyNameChanged)` |
| 交互 | 可输入 + 联想下拉，`threshold = 0` |
| 缓存 | Repository 内存 `cached`，失败回退缓存 |

**CheckUnitRepository**（`network/CheckUnitRepository.kt`）：

- 接口：`UrlConstants.checkUnitSimpleList`
- `fetchSimpleList(forceRefresh=false)` 优先返回缓存
- `invalidateCache()` 清除缓存

### bindCompanyUnitField

**路径**：`ui/CompanyUnitPicker.kt`

```kotlin
fun Fragment.bindCompanyUnitField(
    field: ConstructionWorkersCompanyAutoComplete,
    onCompanyNameChanged: (String) -> Unit,
)
```

Fragment 生命周期内自动 `field.bind(this, callback)`。

### 折叠区 foldView

三个 Tab 均有 `foldView` + `foldGroup`：

- 折叠时 `foldGroup.visibility = GONE`
- 展开时 `VISIBLE`
- 用于隐藏次要筛选项（具体字段见各 fragment 布局）

## VerifyFeatureSettings 与入口联动

| SP 键 | 默认 | 影响 |
|---|---|---|
| `verify_feature_enabled` | false | 施工人员入口显示；新记录 `needVerify` |
| `verify_required_passage` | false | 核实弹窗道口必填 |
| `verify_required_pass_time` | false | 核实弹窗时间必填 |
| `verify_required_device` | false | 核实弹窗设备必填 |
| `verify_required_remark` | false | 核实弹窗备注必填 |

配置入口：运维抽屉 → `VerifyFeatureSettingsDialog`。

## 三 Tab 筛选能力矩阵

| 筛选项 | 核销 | 通行 | 统计 |
|---|---|---|---|
| 姓名 | ✓ | ✓ | ✗ |
| 证件号 | ✓ | ✓ | ✗ |
| 开始时间 | ✓（默认当天） | ✓（默认当天） | ✓（默认 7 天前） |
| 结束时间 | ✓（默认当天） | ✓（默认当天） | ✓（默认今天） |
| 申办单位 | ✓ | ✓ | ✓ |
