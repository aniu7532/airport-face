# 施工人员管理

## 涉及类

| 类 | 路径 | 职责 |
|----|------|------|
| `ConstructionWorkersActivity` | `ui/activity/ConstructionWorkersActivity.kt` | 三 Tab 主页面 |
| `ConstructionWorkersViewModel` | `ui/viewmodel/ConstructionWorkersViewModel.kt` | Tab 状态 |
| `ConstructionWorkersAdapter` | `ui/adapter/ConstructionWorkersAdapter.kt` | ViewPager2 适配 |
| `ConstructionWorkersEntrance` | `widget/ConstructionWorkersEntrance.kt` | 查验页入口图标 |
| `WriteOffRecordFragment` | `ui/fragment/WriteOffRecordFragment.kt` | 核销记录 Tab |
| `AccessRecordFragment` | `ui/fragment/AccessRecordFragment.kt` | 通行记录 Tab |
| `InOutStatisticsFragment` | `ui/fragment/InOutStatisticsFragment.kt` | 进出统计 Tab |
| `WriteOffRecordViewModel` | `ui/viewmodel/WriteOffRecordViewModel.kt` | 核销逻辑 |
| `AccessRecordViewModel` | `ui/viewmodel/AccessRecordViewModel.kt` | 通行记录筛选 |
| `InOutStatisticsViewModel` | `ui/viewmodel/InOutStatisticsViewModel.kt` | 统计数据 |
| `VerifyAndConfirmDialog` | `widget/dialog/VerifyAndConfirmDialog.kt` | 核实确认弹窗 |

## 入口

查验页布局 `activity_liveness_detect.xml` 中嵌入 `ConstructionWorkersEntrance`，点击跳转 `ConstructionWorkersActivity`。

## 三个 Tab

| Tab | Fragment | API | 功能 |
|-----|----------|-----|------|
| 核销记录 | `WriteOffRecordFragment` | `page-need-verify-no-out` | 有进无出记录，支持核实 |
| 通行记录 | `AccessRecordFragment` | `page-need-verify` | 施工人员通行记录分页 |
| 进出统计 | `InOutStatisticsFragment` | `statistic-need-verify` | 按日统计进出人数 |

## 筛选组件

| 组件 | 类 | 说明 |
|------|-----|------|
| 姓名/证件号输入 | `ConstructionWorkersInput` | 文本筛选 |
| 时间范围 | `ConstructionWorkersTimeSelector` | 起止日期 |
| 申办单位 | `ConstructionWorkersCompanyAutoComplete` | 自动完成，数据来自 `CheckUnitRepository` |
| 通用选择器 | `ConstructionWorkersSelector` | 下拉选择 |
| 区域选择 | `AreaPickerDialog` | 管制区域树 |

## 核实流程

`WriteOffRecordFragment` 中对「有进无出」记录：

1. 列表展示待核实记录（Paging3 + `WriteOffRecordPagingSource`）
2. 点击记录 → `VerifyAndConfirmDialog`
3. 确认后 POST `checkRecordVerify`（`URL: checkRecordVerify`）
4. 刷新列表

## 分页加载

| PagingSource | 对应 API |
|--------------|----------|
| `WriteOffRecordPagingSource` | `checkRecordPageNeedVerifyNoOut` |
| `AccessRecordPagingSource` | `checkRecordPageNeedVerify` |

## 申办单位数据

`CheckUnitRepository`（Kotlin object）：

- 调用 `checkUnitSimpleList` 拉取单位列表
- 内存缓存，供 `ConstructionWorkersCompanyAutoComplete` 和 `CompanyUnitPicker` 使用

## 数据模型

| 类 | 说明 |
|----|------|
| `InOutStatisticsResult` | 进出统计结果 |
| `CheckUnit` | 申办单位 |
| `DeviceResult` | 设备信息（区域树接口返回） |

详见 [17-entity-models.md](./17-entity-models.md)。
