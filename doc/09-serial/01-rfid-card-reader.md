# 长短距读卡器、SDK 与串口配置

## 名称边界

项目代码里“长期证读卡”包含两类距离、三种实现，排查时不能只说“RFID 读卡器”：

| 能力 | SDK | 读取标识 | 连接方式 |
|---|---|---|---|
| 短距大屏 | 德卡 `BasicOper` | `cardId`，含 PSAM/ACPU 外部认证 | `CardSerialConfigUtil` 指定的 COM 串口 |
| 短距小屏 | 华大 `AndroidSerialPort` + `Card` | `cardId`，当前仅取 CPU 卡 UID | 同上 |
| 长距 | `EC_API` | `cardIdLong` | SDK 枚举串口，`38400/8E1` |

临时证二维码属于另一套串口体系，详见 [二维码串口](./02-qr-scanner.md)。

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

| 项 | 短距读卡 | 二维码 |
|---|---|---|
| 工具类 | `CardSerialConfigUtil` | `QrSerialConfigUtil` |
| 默认路径 | `/dev/ttyS3` | `/dev/ttyS4` |
| 管理类 | Activity 内直接开串口 | `SerialManage` 单例 |
| 用途 | 长期证 RFID 卡号 | 临时证二维码扫描 |

## Activity 使用方式

读卡逻辑分布在三个查验 Activity，但只有 Jin 与 YuanAndJin 的短距初始化读取 `CardSerialConfigUtil`：

| Activity | 短距初始化 | 长距初始化 |
|---|---|---|
| `LivenessDetectJinActivity` | 按屏宽打开德卡或华大短距读卡器 | 无 |
| `LivenessDetectYuanActivity` | **当前未调用 `BasicOper.dc_open`**，但轮询会调用短距 `BasicOper` 方法 | `initLongReader()` |
| `LivenessDetectYuanAndJinActivity` | 按屏宽打开德卡或华大短距读卡器 | 大屏分支调用 `initLongReader()` |

> Yuan 页面“轮询短距但未在本页面打开短距端口”是当前源码事实，不应把它文档化为已完整初始化。它可能依赖 SDK 静态状态或此前页面遗留连接，独立进入 Yuan 页面时必须真机验证。

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

### 德卡短距认证流程（typeDevice=1）

`getLongPassCardID()` 不是简单读取 UID，而是以下协议链：

```text
选择并复位 PSAM
  → 读取终端编号、选择 PSAM 应用
  → 配置 Type A 卡并寻卡
  → 调整 RFID 字节序
  → 通行证 GET CHALLENGE 取随机数
  → PSAM 加密初始化、加密随机数
  → 通行证执行外部认证
  → 返回 9000 后查询本地长期证
```

SDK 字符串接口通常使用 `0000|数据` 格式；当前代码多处拆分后直接取下标，升级后应验证异常返回格式、各 APDU 步骤和 `9000` 状态字。

### SDK 版本与资源

| 项 | 位置 |
|---|---|
| 当前编译 AAR | `app/libs/dc_reader_release_V1.0.0_20230516162946.aar` |
| 待升级 AAR | `doc/sdk/Android_sdk_release2.56/Android_sdk_release2.56/SDK/dc_reader_release_V1.0.0_20231121115913.aar` |
| 厂商 Demo/手册/校验值 | [Android_sdk_release2.56 接入文档](../sdk/Android_sdk_release2.56/README.md) |

项目通过 `implementation(fileTree("libs"))` 加载本地库，升级时必须替换旧 AAR，不能新旧共存。

### 生命周期

- `stopReadLongPassCardID()` 只把轮询标志设为 `false`，不是关闭硬件端口。
- 三个查验 Activity 均未调用 `BasicOper.dc_exit()`。
- Yuan/YuanAndJin 会用 `unInitLongReader()` 关闭 `EC_API` 长距连接；这不会释放德卡短距端口。
- 反复进入页面、模式切换和应用前后台切换时，应检查串口占用、重复线程和 native 资源。

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
| Yuan 长距可读、短距不可读 | 页面本身未 `dc_open`；检查进入路径及德卡端口状态 |
| 替换 AAR 后启动崩溃 | 检查 ABI、native so、重复 AAR 和 `UnsatisfiedLinkError` |
| 能寻卡但认证失败 | 按 PSAM 复位、APDU、随机数、外部认证和 `9000` 分步定位 |

## 相关依赖

- 德卡 SDK：`BasicOper`（大屏）；版本与升级见 [SDK 专篇](../sdk/Android_sdk_release2.56/README.md)
- 小屏：`AndroidSerialPort`、`Card`（`com.hc.reader`）
- 串口底层：`android.serialport.SerialPort`（二维码侧 `SerialHandle` 也用，读卡侧 SDK 封装）
