# 查验模式

## 四种 checkType

查验模式由 `SPUtils.checkType` 持久化，在 `CustomDrawerPopupView` 运维侧边栏中切换。

| checkType | 名称 | Activity | 说明 |
|-----------|------|----------|------|
| 0 | 短距刷卡+人脸 | `LivenessDetectJinActivity` | 近距 RFID 读卡器 |
| 1 | 长距刷卡+人脸 | `LivenessDetectYuanActivity` | 远距 RFID 读卡器 |
| 2 | 长距+短距刷卡+人脸 | `LivenessDetectYuanAndJinActivity` | 双读卡器 |
| 3 | 仅人脸（出区） | `RegisterAndRecognizeActivity` | 无刷卡，纯 1:N 识别 |

## 进出方向

`SPUtils.direction` 控制通行方向：

| 值 | 含义 |
|----|------|
| `1` | 进控制区 |
| `-1` | 出控制区 |

在 `CustomDrawerPopupView` 中切换，影响记录上传时的 `direction` 字段。

## Activity 与 ViewModel 映射

| Activity | ViewModel | 布局 |
|----------|-----------|------|
| `LivenessDetectJinActivity` | `LivenessDetectViewModel` | `activity_liveness_detect.xml` |
| `LivenessDetectYuanActivity` | `LivenessDetectViewModel` | 同上 |
| `LivenessDetectYuanAndJinActivity` | `LivenessDetectViewModel` | 同上 |
| `RegisterAndRecognizeActivity` | `RecognizeViewModel` | `activity_register_and_recognize.xml` |

三个 `LivenessDetect*Activity` 共用同一 ViewModel 和布局，差异在读卡器串口配置与读卡距离逻辑。

## 渠道对模式的影响

洛阳渠道（`SUPPORTS_TEMPORARY_PASS = false`）：

- checkType 0/1/2 仍可用，但临时证刷卡流程被跳过
- 仅展示长期证 `Document2`

## 模式切换流程

```
运维侧边栏 → 选择查验模式 → 写入 SPUtils.checkType
→ 重启当前 Activity 或重新 LoginActivity.gotoActivity()
→ 跳转到对应 Activity
```

## 相关文档

- 刷卡+人脸详细流程：[07-liveness-detect-flow.md](./07-liveness-detect-flow.md)
- 纯人脸出区流程：[08-register-recognize-flow.md](./08-register-recognize-flow.md)
- 运维侧边栏：[13-settings-and-ops-drawer.md](./13-settings-and-ops-drawer.md)
