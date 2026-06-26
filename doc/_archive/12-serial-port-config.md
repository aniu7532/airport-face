# 串口读卡配置

## 架构

```
硬件读卡器/扫码枪
    ↓ 串口 /dev/ttySx
SerialPort (android.serialport)
    ↓
SerialHandle 读线程          Activity 内 RFID 读卡回调
    ↓                              ↓
SerialManage（二维码单例）     CardSerialConfigUtil 读 SP
    ↓
QrSerialConfigUtil 读 SP
```

## 读卡器（RFID）

**配置类**：`util/CardSerialConfigUtil.java`  
**弹窗**：`widget/dialog/CardSerialConfigPopDialog.java`

### SP 键与默认值

| SP 键 | 默认值 | 说明 |
|-------|--------|------|
| `card_serial_path` | `/dev/ttyS3` | 串口设备路径 |
| `card_serial_baud` | `115200` | 波特率 |

API：

```java
CardSerialConfigUtil.getCardSerialPath()
CardSerialConfigUtil.getCardSerialBaudRate()
CardSerialConfigUtil.saveConfig(path, baud)
```

### 与 Activity 关系

| Activity | 读卡器 |
|----------|--------|
| `LivenessDetectJinActivity` | 短距（近） |
| `LivenessDetectYuanActivity` | 长距（远） |
| `LivenessDetectYuanAndJinActivity` | 近 + 远各一路 |

读到卡号后匹配 `LongTermPass.cardId` / `idCode` / `cardIdLong`。

## 二维码扫描枪

**管理类**：`Serial/SerialManage.java`（单例）  
**读写**：`Serial/SerialHandle.java`  
**回调**：`Serial/SerialInter.java`  
**配置类**：`util/QrSerialConfigUtil.java`  
**弹窗**：`widget/dialog/QrSerialConfigPopDialog.java`

### SP 键与默认值

| SP 键 | 默认值 | 说明 |
|-------|--------|------|
| `qr_serial_path` | `/dev/ttyS4` | 设备路径 |
| `qr_serial_baud` | `115200` | 波特率 |
| `qr_serial_data_bits` | `7` | 数据位 |
| `qr_serial_stop_bits` | `1` | 停止位 |
| `qr_serial_parity` | `2` | 校验位 |

### SerialManage 主要方法

| 方法 | 说明 |
|------|------|
| `getInstance()` | 单例 |
| `open()` | 按 QrSerialConfigUtil 打开串口 |
| `close()` | 关闭 |
| `send(byte[])` | 发送数据 |
| `setSerialInter(SerialInter)` | 注册接收回调 |

## 部署调参步骤

1. 运维抽屉 → 读卡器/二维码串口配置
2. 填入设备实际 `/dev/ttyS*` 路径（因硬件而异）
3. 波特率与读卡器说明书一致（通常 115200）
4. 保存后 **重启查验 Activity** 或重新登录
5. 刷卡/扫码测试，Logcat 过滤 `SerialManage`、`ALog`

## 常见问题

| 现象 | 排查 |
|------|------|
| 刷卡完全无反应 | 路径错误、权限、读卡器未上电 |
| 乱码 | 波特率/数据位/校验位不匹配 |
| 二维码有反应、RFID 无 | 两路串口独立配置，勿混用路径 |
| 双读卡器模式仅一路有效 | 检查 Jin/Yuan 两路 SP 是否分别配置 |

## 相关文档

- 刷卡查验流程 → [07-liveness-detect-flow.md](./07-liveness-detect-flow.md)
- 运维入口 → [13-settings-and-ops-drawer.md](./13-settings-and-ops-drawer.md)
