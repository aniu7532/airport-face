# 施工人员管理

## 页面结构

**Activity**：`ui/activity/ConstructionWorkersActivity.kt`  
**布局**：ViewPager2 + Tab  
**Adapter**：`ConstructionWorkersAdapter.kt`  
**ViewModel**：`ConstructionWorkersViewModel.kt`（Tab 枚举）

| Tab 序号 | Fragment | ViewModel | API |
|----------|----------|-----------|-----|
| 0 | `WriteOffRecordFragment` | `WriteOffRecordViewModel` | `page-need-verify-no-out` |
| 1 | `AccessRecordFragment` | `AccessRecordViewModel` | `page-need-verify` |
| 2 | `InOutStatisticsFragment` | `InOutStatisticsViewModel` | `statistic-need-verify` |

**入口**：查验页 `ConstructionWorkersEntrance` 图标 → `startActivity(ConstructionWorkersActivity)`

## 筛选组件

| 组件 | 类 | 绑定字段 |
|------|-----|----------|
| 姓名 | `ConstructionWorkersInput` | nickname |
| 证件号 | `ConstructionWorkersInput` | idCode |
| 时间范围 | `ConstructionWorkersTimeSelector` | startCheckTime / endCheckTime |
| 申办单位 | `ConstructionWorkersCompanyAutoComplete` | companyName |
| 区域 | `AreaPickerDialog` | 区域树选择 |

单位数据源：`CheckUnitRepository`（缓存 `checkUnitSimpleList` 结果）。

## 核销 Tab（有进无出）

### 数据加载

`WriteOffRecordPagingSource` → **GET** `checkRecordPageNeedVerifyNoOut`

| 参数 | 默认 |
|------|------|
| pageNo | 从 1 递增 |
| pageSize | 10 |
| nickname / idCode | 筛选 |
| startCheckTime / endCheckTime | 时间 |
| companyName | 可选 |

### 核实流程

1. 列表项点击 → `VerifyAndConfirmDialog`
2. 表单字段必填受 `VerifyFeatureSettings` 子开关控制
3. 确认 → **POST** `checkRecordVerify`
4. 成功刷新 Paging 列表

## 通行记录 Tab

`AccessRecordPagingSource` → **GET** `checkRecordPageNeedVerify`

参数与核销 Tab 相同，展示施工人员通行明细。  
Adapter：`AccessRecordAdapter.kt`（direction 显示进/出）。

## 进出统计 Tab

`InOutStatisticsViewModel` → **GET** `checkRecordStatisticNeedVerify`

展示按日统计结果，实体 `InOutStatisticsResult`，Adapter `InOutStatisticsAdapter`。

## VerifyFeatureSettings 联动

| 开关 | SP 键 | 影响 |
|------|-------|------|
| 总开关 | `verify_feature_enabled` | 新记录 `needVerify` |
| 必填通道 | `verify_required_passage` | 核实表单 |
| 必填通行时间 | `verify_required_pass_time` | 核实表单 |
| 必填设备 | `verify_required_device` | 核实表单 |
| 必填备注 | `verify_required_remark` | 核实表单 |

配置入口：运维抽屉 → `VerifyFeatureSettingsDialog`。

## CheckUnitRepository

```kotlin
// 挂起函数拉取并缓存申办单位
suspend fun fetchUnits(): List<CheckUnit>
```

接口：**GET** `checkUnitSimpleList`  
用于单位自动完成与 `CompanyUnitPicker.bindCompanyUnitField` 扩展。

## 相关文档

- 接口参数 → [16-api-reference.md](./16-api-reference.md)
- 运维核实开关 → [13-settings-and-ops-drawer.md](./13-settings-and-ops-drawer.md)
- needVerify 写入 → [10-offline-records-upload.md](./10-offline-records-upload.md)
