# 弹窗与对话框索引

> 路径：`widget/dialog/`、`preference/*Dialog*`、`util/DialogUtils`、`util/CustomToastDialog`

项目混用 **XPopup**、**AlertDialog**、**DialogFragment（Preference）** 三类弹窗体系。

---

## 运维与配置（高频）

| 类 | 布局 | 触发入口 | 职责 |
|----|------|----------|------|
| `CustomDrawerPopupView` | `dialog_draw` | 查验页隐藏入口 | 运维抽屉：同步、重初始化、串口配置、退出等 |
| `VerifyFeatureSettingsDialog` | `dialog_verify_feature_settings` | 抽屉菜单 | 施工人员核销总开关与必填项设置 |
| `CardSerialConfigPopDialog` | `dialog_card_serial` | 抽屉 | 短距读卡器串口参数 |
| `QrSerialConfigPopDialog` | `dialog_qr_serial` | 抽屉 | 二维码串口参数 |
| `RecordsPopDialog` | `dialog_records` | 抽屉 | 本地未上传记录查看 |
| `AppKeyPopDialog` | `dialog_key` | 登录/设置 | AppKey 输入 |
| `LogingPopDialog` | `dialog_loging` | 登录 | 登录中遮罩 |

详见 [13-ops/01-custom-drawer-menu.md](../13-ops/01-custom-drawer-menu.md)。

---

## 查验与施工

| 类 | 说明 |
|----|------|
| `VerifyAndConfirmDialog` | 施工人员核销确认（Kotlin） |
| `ImagePopDialog` | 大图预览 |
| `DrawPopDialog` | 手绘/标注（调试） |

---

## 通用 UI

| 类 | 说明 |
|----|------|
| `CustomPopDialog` | XPopup 基础自定义 |
| `LoadingPopDialog` | 加载中 |
| `UpdatePopDialog` | 应用更新提示 |
| `CustomToastDialog` / `ToastDialogManager` | 自定义 Toast 样式 |
| `DialogUtils` | 统一 `showConfirm` 等工具方法 |

---

## 选择器

| 类 | 布局 | 说明 |
|----|------|------|
| `AreaPickerDialog` | `dialog_area_picker` | 区域树选择 |
| `DateTimePickerDialogHelper` | `dialog_datetime_picker` | 日期时间 |
| `StringListPickerDialog` | `dialog_string_list_picker` | 字符串列表 |

施工模块筛选使用上述选择器，见 [08-construction/04-statistics-filters.md](../08-construction/04-statistics-filters.md)。

---

## Preference DialogFragment

| 类 | 说明 |
|----|------|
| `ThresholdPreferenceDialogFragmentCompat` | 识别阈值 |
| `ThresholdLivePreferenceDialogFragmentCompat` | 活体阈值 |
| `IntegerPreferenceDialogFragmentCompat` | 整数配置项 |

用于 `RecognizeSettingsActivity` 等设置页。

---

## Activity 内嵌 Dialog

| 类 | 说明 |
|----|------|
| `PermissionDegreeDialog` | 相机角度权限说明 |
| `LoginActivity` 内 `AlertDialog` | 零信任失败、确认框 |

---

## XPopup 使用模式

```java
new XPopup.Builder(context)
    .popupPosition(PopupPosition.Right)
    .asCustom(new CustomDrawerPopupView(context))
    .show();
```

`LoginActivity` 运维入口、`LivenessDetect*Activity` 长按/组合键打开抽屉。

---

## 弹窗选型约定

| 场景 | 推荐 |
|------|------|
| 侧边运维菜单 | XPopup + `CustomDrawerPopupView` |
| 简单确认 | `DialogUtils` / `AlertDialog` |
| 设置项数值 | Preference DialogFragment |
| 全屏加载 | `LoadingPopDialog` / `LogingPopDialog` |

---

## 联调检查清单

- [ ] 抽屉能否弹出（隐藏手势是否触发）
- [ ] 串口配置保存后是否写入 `SPUtils`
- [ ] 阈值修改是否同步 `ConfigUtil` + SP
- [ ] 多弹窗叠加时返回键行为（XPopup `dismissOnBackPressed`）
