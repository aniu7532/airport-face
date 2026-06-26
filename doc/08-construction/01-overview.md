# ConstructionWorkersActivity 结构

## 入口与可见性

| 项 | 说明 |
|---|---|
| 入口控件 | `widget/ConstructionWorkersEntrance.kt` |
| 挂载布局 | `activity_register_and_recognize.xml`、`activity_liveness_detect.xml` |
| 跳转 | `Intent(context, ConstructionWorkersActivity::class.java)` |
| 显示条件 | `VerifyFeatureSettings.needVerifyForNewRecord()` 为 `true`（SP 键 `verify_feature_enabled`） |
| 监听 | `onAttachedToWindow` 订阅 `NeedVerifyChangeListener`，开关变化时 `VISIBLE/GONE` |

## Activity 层级

```
ConstructionWorkersActivity
├── ActivityConstructionWorkersBinding（activity_construction_workers.xml）
│   ├── tabBar（自定义 Tab，非 TabLayout）
│   └── viewPager（ViewPager2）
└── ConstructionWorkersViewModel（Activity 级）
```

**源文件**

| 类 | 路径 |
|---|---|
| Activity | `ui/activity/ConstructionWorkersActivity.kt` |
| ViewModel | `ui/viewmodel/ConstructionWorkersViewModel.kt` |
| Adapter | `ui/adapter/ConstructionWorkersAdapter.kt` |
| Tab 布局 | `res/layout/activity_construction_workers_tab.xml` |

## Tab 枚举

`ConstructionWorkersTab`（定义于 `ConstructionWorkersViewModel.kt`）：

| ordinal | 枚举值 | label | Fragment |
|---:|---|---|---|
| 0 | `WriteOffRecord` | 核销记录 | `WriteOffRecordFragment` |
| 1 | `AccessRecord` | 通行记录 | `AccessRecordFragment` |
| 2 | `InOutStatistics` | 进出统计 | `InOutStatisticsFragment` |

## ViewPager2 配置

| 属性/行为 | 值 |
|---|---|
| `isUserInputEnabled` | `false`（禁止手势滑动） |
| Adapter | `ConstructionWorkersAdapter`（`FragmentStateAdapter`） |
| `getItemCount()` | `ConstructionWorkersTab.entries.size`（3） |
| 页面切换回调 | `onPageSelected` → `viewModel.changeTab(position)` |

## Tab 与 ViewPager 双向联动

1. **点击 Tab**：`tab.root.setOnClickListener { viewModel.changeTab(index) }`
2. **ViewModel 状态**：`_currentTab: MutableStateFlow<ConstructionWorkersTab>`，默认 `WriteOffRecord`
3. **collect 副作用**：
   - `viewPager.setCurrentItem(currentTab.ordinal, false)`
   - 选中 Tab 背景 `R.mipmap.loading_bg`，其余 `null`

## Fragment 工厂

`ConstructionWorkersAdapter.createFragment(position)`：

```kotlin
when (position) {
    WriteOffRecord.ordinal -> WriteOffRecordFragment.newInstance()
    AccessRecord.ordinal -> AccessRecordFragment.newInstance()
    InOutStatistics.ordinal -> InOutStatisticsFragment.newInstance()
}
```

## ViewModel 职责边界

| 层级 | 职责 |
|---|---|
| `ConstructionWorkersViewModel` | 仅维护当前 Tab 索引 |
| 各 Tab Fragment 自带 ViewModel | 筛选条件、分页/统计请求 |

`initData()` 当前为空，无额外初始化逻辑。

## 共享筛选组件（三 Tab 复用）

| 组件类 | 布局 | 绑定字段 |
|---|---|---|
| `ConstructionWorkersInput` | `activity_construction_workers_input.xml` | 姓名 / 证件号 |
| `ConstructionWorkersTimeSelector` | `activity_construction_workers_selector.xml` | 开始/结束时间 |
| `ConstructionWorkersCompanyAutoComplete` | `activity_construction_workers_company_autocomplete.xml` | 申办单位 |
| `ConstructionWorkersSelector` | `activity_construction_workers_selector.xml` | 通用选择器（核实弹窗等） |

申办单位数据源：`CheckUnitRepository.fetchSimpleList()` → 接口 `checkUnitSimpleList`，内存缓存。

绑定扩展：`Fragment.bindCompanyUnitField()`（`ui/CompanyUnitPicker.kt`）。

## 生命周期与刷新策略

三个 Fragment 均采用 `hasResumedOnce` 模式：

| 时机 | 行为 |
|---|---|
| 首次 `onResume` | 不主动刷新（ViewModel 初始态或 `initView` 末尾已触发） |
| 再次 `onResume` | 核销/通行：`viewModel.search()`；统计：`viewModel.request()` |

## AndroidManifest

```xml
<activity
    android:name=".ui.activity.ConstructionWorkersActivity"
    android:theme="@style/FullScreenTheme"
    android:exported="false" />
```

非导出 Activity，仅应用内通过入口图标启动。
