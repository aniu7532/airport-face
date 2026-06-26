# SerialManage / SerialHandle / QrSerialConfigUtil

## 架构

```
Activity.initScanCard()
  └── SerialManage（单例）
        ├── init(SerialInter)     注册回调
        ├── open()                读 QrSerialConfigUtil → SerialHandle.open
        ├── send(msg)             入队，100ms 轮询发送
        └── colse()               关闭串口

SerialHandle
  ├── open(path, baud, dataBits, stopBits, parity, isRead)
  ├── send(hexString)             十六进制字符串 → byte[]
  └── run() 每 150ms 读 InputStream → SerialInter.readData
```

二维码扫描与 RFID 读卡**分离**：二维码走 `SerialManage`，RFID 走 `CardSerialConfigUtil` + 各 Activity 自有 SDK。

## QrSerialConfigUtil

**路径**：`util/QrSerialConfigUtil.java`

### SP 键与默认值

| 配置 | SP 键 | 默认值 |
|---|---|---|
| 路径 | `qr_serial_path` | `/dev/ttyS4` |
| 波特率 | `qr_serial_baud` | `115200` |
| 数据位 | `qr_serial_data_bits` | `7` |
| 停止位 | `qr_serial_stop_bits` | `1` |
| 校验位 | `qr_serial_parity` | `2`（偶校验 EVEN） |

校验位映射（`SerialHandle.open` 注释）：

| 值 | 含义 |
|---:|---|
| 0 | 无校验 NONE |
| 1 | 奇校验 ODD |
| 2 | 偶校验 EVEN |

### API

| 方法 | 说明 |
|---|---|
| `getDevicePath()` | 路径，空回退 `/dev/ttyS4` |
| `getBaudRate()` | 波特率 |
| `getDataBits()` | 数据位 |
| `getStopBits()` | 停止位 |
| `getParity()` | 校验位 |
| `saveConfig(path, baud, dataBits, stopBits, parity)` | 非法值回退默认 |

### 配置 UI

**路径**：`widget/dialog/QrSerialConfigPopDialog.java`  
**布局**：`dialog_qr_serial.xml`  
入口：运维抽屉

## SerialManage

**路径**：`Serial/SerialManage.java`

### 单例与线程池

```java
scheduledExecutor = Executors.newScheduledThreadPool(8);
```

### init(SerialInter)

1. 若 `serialHandle == null`：new `SerialHandle` + `startSendTask()`
2. `serialHandle.addSerialInter(serialInter)` 设置回调

### open()

从 `QrSerialConfigUtil` 读取五项参数，调用：

```java
isConnect = serialHandle.open(path, baudrate, dataBits, stopBits, parity, true);
```

日志 tag：`LivenessDetectActivity`，内容含路径与五项参数及连接结果。

### send(String msg)

- **不直接** `serialHandle.send`
- `queueMsg.offer(msg)` 入 `ConcurrentLinkedQueue`
- 原因：232 通讯极短时间内多指令会物理干扰，需排队

### 发送任务 startSendTask

| 参数 | 值 |
|---|---|
| 调度 | `scheduleAtFixedRate` |
| 初始延迟 | 0 |
| 间隔 | **100 ms** |
| 条件 | `isConnect && serialHandle != null` |
| 动作 | `queueMsg.poll()` → 非空则 `serialHandle.send(msg)` |

### colse()

`serialHandle.close()`（注意方法名拼写为 `colse`）

## SerialHandle

**路径**：`Serial/SerialHandle.java`

### open 重载

```java
open(devicePath, baudrate, isRead)
  → open(devicePath, baudrate, 7, 1, 2, isRead)  // 默认 7N2 偶校验
```

完整 open 使用 `SerialPort.newBuilder`：

```java
SerialPort.newBuilder(device, baudrate)
    .dataBits(dataBits)   // 5~8
    .stopBits(stopBits)   // 1 或 2
    .parity(parity)     // 0/1/2
    .build()
```

成功且 `isRead=true` → `readData()` 启动读取任务。

### 读取任务 readData

| 参数 | 值 |
|---|---|
| 调度 | `scheduleAtFixedRate(this, 0, 150, MILLISECONDS)` |
| 执行体 | `run()` |

### run() 逻辑

1. 检查线程中断
2. `mBuffInputStream.available()`，为 0 则 return
3. 读最多 1024 字节
4. `serialInter.readData(path, received, size)`

### send(String msg)

- `hexStr2bytes(msg)`：十六进制字符串转 byte[]
- `mOutputStream.write(bytes)`

**注意**：传入的 `msg` 必须是十六进制字符串（如 `"AABBCC"`），非普通文本。

### close()

依次关闭 InputStream、OutputStream、SerialPort，置 `mSerialPort = null`。

## SerialInter

**路径**：`Serial/SerialInter.java`

| 回调 | 参数 |
|---|---|
| `connectMsg(path, isSucc)` | 连接结果（当前实现中 open 未主动回调） |
| `readData(path, bytes, size)` | 原始字节与长度 |

## Activity 集成：initScanCard

**使用 Activity**（临时证渠道 `ChannelConfig.SUPPORTS_TEMPORARY_PASS`）：

- `LivenessDetectJinActivity`
- `LivenessDetectYuanActivity`
- `LivenessDetectYuanAndJinActivity`

### 典型实现

```java
public void initScanCard() {
    if (!ChannelConfig.SUPPORTS_TEMPORARY_PASS) return;

    SerialManage.getInstance().init(new SerialInter() {
        @Override
        public void connectMsg(String path, boolean isSucc) { ... }

        @Override
        public void readData(String path, byte[] bytes, int size) {
            String data = new String(bytes, 0, size);
            data = data.replace("\r", "");
            getShortPassCardID(data);  // 解析临时证
        }
    });
    SerialManage.getInstance().open();
}
```

### 数据格式

- 扫码枪通常输出 ASCII 字符串 + `\r`
- 去掉 `\r` 后交给 `getShortPassCardID(data)`

## 时序参数汇总

| 环节 | 间隔 |
|---|---|
| 发送队列轮询 | 100 ms |
| 串口读取轮询 | 150 ms |
| 取消读任务等待 | 160 ms sleep |

## 排查要点

| 现象 | 检查 |
|---|---|
| 二维码无数据 | 默认 `/dev/ttyS4`；`open()` 日志 `=> true/false` |
| 乱码 | 数据位/校验位是否与扫码枪一致（默认 7E1） |
| 指令无响应 | 是否误用 `send` 传非 hex 字符串 |
| 多指令丢失 | 应走 `SerialManage.send` 队列，勿直接 `serialHandle.send` |
