# CustomDrawerPopupView 侧边栏菜单

源码：`app/src/main/java/com/arcsoft/arcfacedemo/widget/dialog/CustomDrawerPopupView.java`  
布局：`res/layout/dialog_draw.xml`

继承 `DrawerPopupView`（XPopup）。`onCreate` 中绑定各菜单项点击行为。

---

## 初始化展示

| 控件 | 行为 |
|------|------|
| `tvPhone` | 显示 `SPUtils "mobile"` |
| `tvVersion` | `AppUtils.getAppVersionName()`，去掉 `-debug` 后缀 |

---

## 菜单项 onClick 明细

### tvInOut — 进出方向

- 弹出中心列表：`["进控制区", "出控制区"]`
- 当前选中：`SPUtils "direction"`，默认 `1`；列表高亮 `direction==1 ? 0 : 1`
- 选择后：
  - `direction = position==0 ? 1 : -1`
  - 若当前非 `LoginActivity`：启动 `LoginActivity` 并 `finishOtherActivities`（近/远/组合活体、注册识别页）
  - `dismiss()`

### tvChayan — 查验模式

- 列表：`["通行证（短距）+人脸", "通行证（长距）+人脸", "通行证（长距+短距）+人脸", "人脸"]`
- `SPUtils "checkType"`，默认 `0`
- 选择后：写 `checkType`；非登录页则回 `LoginActivity` + 结束其它查验 Activity；`dismiss()`

### tvVerifyFeature — 核销功能设置

- `VerifyFeatureSettingsDialog.show(getContext())`
- 不自动 dismiss 抽屉（弹窗叠在上方）

### tvTipsLoc — 证件提示窗口位置

- 列表：`["左下", "左上", "右下", "右上"]`
- `SPUtils "tipsLoc"`，默认 `0`
- 选择后：写 `tipsLoc`；非登录页回 `LoginActivity` + 结束其它 Activity；`dismiss()`

### tvCanshu — 参数配置

- `XPopup.asCustom(new AppKeyPopDialog(context)).show()`
- AppId / SdkKey / ActiveKey 等

### tvCardSerial — 读卡器串口

- `CardSerialConfigPopDialog`

### tvQrSerial — 二维码串口

- `QrSerialConfigPopDialog`

### tvDelete — 删除本地缓存

- 确认框「确认删除本地缓存数据吗？」
- 后台 `FileUtils.delete` 顺序删除：
  1. `.../files/photo`
  2. `.../files/register`
  3. `.../files/records`
  4. `.../files/Pictures/faceDB`
  5. `.../files/db/airportDb.db`
  6. `.../files/database/faceDB.db`
- 成功：Toast「删除本地缓存数据成功，请重启重新登录初始化」，3 秒后 `AppUtils.exitApp()`
- 失败：Toast 失败

### tvWenan — 文案配置

- `DialogUtils.startInputConfirm`，标题「文案配置」
- 确认：`SPUtils.put("wenan", text)`

### tvGotoLuancher — 返回桌面

- 确认「确认返回系统桌面？」
- `Intent.ACTION_MAIN` + `CATEGORY_HOME` + `FLAG_ACTIVITY_NEW_TASK`

### tvGotoSetting — 系统设置

- 确认「确认进入系统设置？」
- `Settings.ACTION_SETTINGS`

### btnExit — 注销登录

- 确认「确认注销登录吗？」
- 非 `LoginActivity`：`startActivity(LoginActivity, extra auto=false)` + finish 其它查验 Activity
- `dismiss()`

### tvUploadLog — 上传日志

- 确认「确认上传本地日志？」
- Toast「请稍后，正在压缩文件...」
- 后台 `LogUploadUtils.upload(getActivity())`

### tvReInit — 在线数据完整性检查

- 确认「确认执行在线数据完整性检查功能？」
- `dismiss()` → `LongPassCardsReInitUtils.getInstance().start()`

### tvRemedial — 数据完整性检查（本地补救）

- 确认「确认执行数据完整性检查功能？」
- `dismiss()` → `LongPassCardsRemedialMeasuresUtils.getInstance().start()`

### tvClearDuplicateFace — 清除重复人脸

- 确认「确认清除重复人脸？（同一人存在多条时，保留有效证对应人脸）」
- `dismiss()` → `DuplicateFaceCleanupUtils.getInstance().start()`

---

## 共性模式

多数「模式类」菜单（进出、查验、提示位置）修改 `SPUtils` 后：

1. 若不在 `LoginActivity`，强制回到登录页重启查验流程
2. `finishOtherActivities` 四类 Activity：
   - `LivenessDetectJinActivity`
   - `LivenessDetectYuanActivity`
   - `LivenessDetectYuanAndJinActivity`
   - `RegisterAndRecognizeActivity`

---

## SPUtils 键汇总

| Key | 菜单 | 含义 |
|-----|------|------|
| `mobile` | tvPhone 展示 | 登录手机号 |
| `direction` | tvInOut | 1 进 / -1 出 |
| `checkType` | tvChayan | 0~3 查验模式 |
| `tipsLoc` | tvTipsLoc | 0~3 提示窗角落 |
| `wenan` | tvWenan | 自定义文案 |

---

## 生命周期

- `onShow` / `onDismiss`：仅调用 `super`，无额外逻辑
