# CardSerialConfigUtil 默认值与 Activity 使用

## CardSerialConfigUtil

**路径**：`util/CardSerialConfigUtil.java`  
**存储**：`SPUtils`（SharedPreferences）

### SP 键

| 键 | 常量 |
|---|---|
| 串口路径 | `card_serial_path` |
| 波特率 | `card_serial_baud` |

### 默认值

| 配置项 | 默认值 |
|---|---|
| 路径 | `/dev/ttyS3` |
| 波特率 | `115200` |

### API

| 方法 | 行为 |
|---|---|
| `getCardSerialPath()` | 读 SP；空/null/异常 → `DEFAULT_PATH` |
| `getCardSerialBaudRate()` | 读 SP；≤0/异常 → `DEFAULT_BAUD` |
| `saveConfig(path, baud)` | 校验后写入 SP；path 空则回退默认，baud≤0 回退 115200 |

### 配置 UI

**路径**：`widget/dialog/CardSerialConfigPopDialog.java`  
**布局**：`dialog_card_serial.xml`

- 打开时回填 `getCardSerialPath()` / `getCardSerialBaudRate()`
- 保存调用 `CardSerialConfigUtil.saveConfig(path, baud)`
- 入口：运维抽屉

## 与 QrSerialConfigUtil 区分

| 项 | 读卡（RFID） | 二维码 |
|---|---|---|
| 工具类 | `CardSerialConfigUtil` | `QrSerialConfigUtil` |
| 默认路径 | `/dev/ttyS3` | `/dev/ttyS4` |
| 管理类 | Activity 内直接开串口 | `SerialManage` 单例 |
| 用途 | 长期证 RFID 卡号 | 临时证二维码扫描 |

## Activity 使用方式

读卡逻辑分布在三个查验 Activity，均通过 `CardSerialConfigUtil` 读取 SP 配置：

| Activity | 路径 |
|---|---|
| `LivenessDetectJinActivity` | 进港查验 |
| `LivenessDetectYuanActivity` | 出港查验 |
| `LivenessDetectYuanAndJinActivity` | 进出港合一 |

### 设备类型分支（screenWidth）

```java
int typeDevice = screenSize[0] > 800 ? 1 : 2;
```

| typeDevice | 屏幕 | 读卡 SDK | 串口打开方式 |
|---:|---|---|---|
| 1 | 宽度 > 800 | `BasicOper`（德卡） | `BasicOper.dc_open("COM", activity, path, baudrate)` |
| 2 | 宽度 ≤ 800 | `AndroidSerialPort` + `Card` | `card.OpenReader(path, band)` + `rf_select_protocol(0)` |

path/baud 均来自：

```java
String path = CardSerialConfigUtil.getCardSerialPath();
int baudrate = CardSerialConfigUtil.getCardSerialBaudRate();
```

### initReadCard 流程（typeDevice=1 大屏）

1. 显示 `LoadingPopDialog("初始化中，请稍后......")`
2. 子线程 `BasicOper.dc_open(...)`
3. `portSate >= 0` → `BasicOper.dc_beep(5)`
4. `Thread.sleep(1000)`
5. UI 线程 dismiss 弹窗
6. `startReadLongPassCardID()` 循环读卡

### initReadCard 流程（typeDevice=2 小屏）

1. 同样 Loading 弹窗
2. `Card card = new AndroidSerialPort(activity)`
3. `OpenReader(path, band)` + `rf_select_protocol(0)`（非接触式）
4. dismiss 后 `startReadCarIDMini(card)`

### 生命周期中的重复读取

Activity 内多处再次读取配置用于重连/日志：

```java
// 示例：LivenessDetectJinActivity ~1197
String path = CardSerialConfigUtil.getCardSerialPath();
int band = CardSerialConfigUtil.getCardSerialBaudRate();
```

### 日志关键字

```
读卡串口[{path}] portSate:{portSate}
OpenReader success/fail
获取屏幕尺寸宽度:{width}
```

## 排查要点

| 现象 | 检查 |
|---|---|
| 无刷卡反应 | SP 路径是否为设备实际 tty；默认 `/dev/ttyS3` |
| 大屏 beep 无响 | `portSate < 0`，查权限与硬件接线 |
| 小屏 OpenReader 失败 | 路径/波特率；`rf_select_protocol` 返回值 |
| 改配置不生效 | 需重启读卡初始化（重新进入查验页或重开串口） |

## 相关依赖

- 德卡 SDK：`BasicOper`（大屏）
- 小屏：`AndroidSerialPort`、`Card`（`com.zkteco.android.biometric` 系列）
- 串口底层：`android.serialport.SerialPort`（二维码侧 `SerialHandle` 也用，读卡侧 SDK 封装）
