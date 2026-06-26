# 串口读卡配置

## 涉及类

| 类 | 路径 | 职责 |
|----|------|------|
| `SerialManage` | `Serial/SerialManage.java` | 二维码串口单例管理 |
| `SerialHandle` | `Serial/SerialHandle.java` | 串口读写线程 |
| `SerialInter` | `Serial/SerialInter.java` | 串口回调接口 |
| `QrSerialConfigUtil` | `util/QrSerialConfigUtil.java` | 二维码扫描枪串口 SP 配置 |
| `CardSerialConfigUtil` | `util/CardSerialConfigUtil.java` | RFID 读卡器串口 SP 配置 |
| `QrSerialConfigPopDialog` | `widget/dialog/QrSerialConfigPopDialog.java` | 二维码串口配置弹窗 |
| `CardSerialConfigPopDialog` | `widget/dialog/CardSerialConfigPopDialog.java` | 读卡器串口配置弹窗 |

## 两类串口

| 类型 | 管理类 | 配置类 | 用途 |
|------|--------|--------|------|
| RFID 读卡器 | Activity 内直接打开 | `CardSerialConfigUtil` | 读取通行证卡号 |
| 二维码扫描枪 | `SerialManage` 单例 | `QrSerialConfigUtil` | 扫描二维码 |

## SerialManage

单例模式，职责：

| 方法 | 说明 |
|------|------|
| `open()` | 打开串口（路径、波特率从 SP 读取） |
| `close()` | 关闭串口 |
| `send()` | 发送数据 |
| `setSerialInter()` | 设置数据接收回调 |

底层使用 `android.serialport.SerialPort`。

## 配置参数

通过 SharedPreferences 持久化：

| 参数 | 说明 | 默认值来源 |
|------|------|------------|
| 串口路径 | 如 `/dev/ttyS1` | 配置弹窗 |
| 波特率 | 如 `115200` | 配置弹窗 |
| 数据位/停止位/校验位 | 串口基本参数 | 配置弹窗 |

## 配置入口

`CustomDrawerPopupView` 运维侧边栏：

- 「读卡器串口配置」→ `CardSerialConfigPopDialog`
- 「二维码串口配置」→ `QrSerialConfigPopDialog`

## 读卡数据流

```
硬件读卡器 → 串口数据
→ SerialHandle 读线程
→ SerialInter.onDataReceived() / Activity 回调
→ 解析卡号 → 查 LongTermPass
→ 触发查验流程
```

## 长距/短距差异

| Activity | 读卡器 |
|----------|--------|
| `LivenessDetectJinActivity` | 短距读卡器串口 |
| `LivenessDetectYuanActivity` | 长距读卡器串口 |
| `LivenessDetectYuanAndJinActivity` | 两个读卡器串口 |

串口路径在 `CardSerialConfigUtil` 中分别配置（近距/远距）。

## 注意事项

- 修改串口配置后需重启 Activity 生效
- 串口路径因设备硬件而异，部署时需逐台配置
- 二维码与 RFID 使用独立串口，互不干扰
