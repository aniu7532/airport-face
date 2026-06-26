# 查验模式

## checkType 与 Activity 映射

`SPUtils` 键名：`checkType`（int，默认 `0`）

| checkType | 显示名称 | Activity | 读卡 |
|-----------|----------|----------|------|
| 0 | 通行证（短距）+ 人脸 | `LivenessDetectJinActivity` | 近距 RFID |
| 1 | 通行证（长距）+ 人脸 | `LivenessDetectYuanActivity` | 远距 RFID |
| 2 | 通行证（长距+短距）+ 人脸 | `LivenessDetectYuanAndJinActivity` | 双读卡器 |
| 3 | 人脸（出区） | `RegisterAndRecognizeActivity` | 无 |

切换入口：`CustomDrawerPopupView` →「查验模式」列表。切换后会 `finish` 当前查验 Activity 并重启 `LoginActivity` 以重新路由。

## direction 进出方向

`SPUtils` 键名：`direction`（int，默认 `1`）

| 值 | 含义 | UI 文案 |
|----|------|---------|
| `1` | 进控制区 | 「进控制区」 |
| `-1` | 出控制区 | 「出控制区」 |

影响：

- 查验页顶部提示：`在线模式 {areaName}-进查验` / `-出查验`
- 写入 `LongTermRecords.direction` / `TemporaryCardRecords.direction`
- `RecordsPopDialog` 查询服务端记录时按方向筛选

切换入口：`CustomDrawerPopupView` →「选择进出」。

## tipsLoc 证件提示位置

`SPUtils` 键名：`tipsLoc`（int，默认 `0`）

| 值 | 位置 |
|----|------|
| 0 | 左下 |
| 1 | 左上 |
| 2 | 右下 |
| 3 | 右上 |

影响查验页证件状态提示 overlay 的 `Gravity` 布局。

## 其他相关 SP 键

| 键 | 类型 | 默认 | 说明 |
|----|------|------|------|
| `mobile` | String | `""` | 登录用户手机号，抽屉展示 |
| `wenan` | String | `""` | 自定义文案 |
| `Appid` / `Sdkkey` / `Activecode` | String | Constants 默认 | ArcFace SDK 密钥 |
| `reboot` | boolean | `true` | 凌晨 2 点重启标志（防重复） |
| `upload_log` | boolean | `true` | 上午 10 点日志上传标志 |
| `reinit_check` | boolean | `true` | 凌晨 1 点数据检查标志 |

## ViewModel 对应关系

| Activity | ViewModel | 核心职责 |
|----------|-----------|----------|
| `LivenessDetect*Activity` | `LivenessDetectViewModel` | 刷卡状态机、比对、卡面切换 |
| `RegisterAndRecognizeActivity` | `RecognizeViewModel` | 纯 1:N 搜索、无刷卡 |

三个 `LivenessDetect*Activity` **共用同一布局** `activity_liveness_detect.xml` 和同一 ViewModel 类，差异在串口打开逻辑（近/远/双读卡器）。

## 渠道限制

洛阳渠道 `ChannelConfig.SUPPORTS_TEMPORARY_PASS = false`：

- checkType 0/1/2 仍可用，但临时证刷卡、Document3、临时记录写入均被跳过
- checkType 3 不受影响（仅长期证人脸）

代码中判断示例：

```java
if (ChannelConfig.SUPPORTS_TEMPORARY_PASS) {
    // 临时证逻辑
}
```

## 核销功能开关（影响新记录）

`VerifyFeatureSettings`（详见 [11-construction-workers.md](./11-construction-workers.md)）：

- 总开关 `verify_feature_enabled` 开启时，新通行记录 `needVerify=true`
- 与 checkType 独立，作用于记录写入阶段

## 相关文档

- 刷卡+人脸流程 → [07-liveness-detect-flow.md](./07-liveness-detect-flow.md)
- 纯人脸出区 → [08-register-recognize-flow.md](./08-register-recognize-flow.md)
- 运维抽屉 → [13-settings-and-ops-drawer.md](./13-settings-and-ops-drawer.md)
