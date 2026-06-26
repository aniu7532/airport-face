# 刷卡 / 串口排查

## 两套串口体系

| 用途 | 配置类 | 默认路径 | 管理 |
|---|---|---|---|
| RFID 长期证读卡 | `CardSerialConfigUtil` | `/dev/ttyS3` | Activity 内 SDK |
| 临时证二维码 | `QrSerialConfigUtil` | `/dev/ttyS4` | `SerialManage` 单例 |

**不可混用配置**：改 QR 串口不影响读卡，反之亦然。

## RFID 读卡无反应

### 配置检查

```java
CardSerialConfigUtil.getCardSerialPath()   // 默认 /dev/ttyS3
CardSerialConfigUtil.getCardSerialBaudRate() // 默认 115200
```

运维抽屉 → **读卡串口配置** → `CardSerialConfigPopDialog`

### 设备类型

```java
typeDevice = screenWidth > 800 ? 1 : 2;
```

| 类型 | SDK | 成功日志 |
|---|---|---|
| 1 大屏 | `BasicOper.dc_open` | `portSate >= 0`，beep |
| 2 小屏 | `Card.OpenReader` | `OpenReader success` |

### 排查步骤

1. 日志搜 `读卡串口[` 或 `OpenReader`
2. 确认 tty 节点存在：`adb shell ls -l /dev/ttyS*`
3. 权限：串口读写、厂商 ROM 授权
4. 改 SP 后需 **重新进入查验页** 触发 `initReadCard`
5. 波特率与读卡器说明书一致（默认 115200）

### 有读卡日志但无业务响应

- 查 `startReadLongPassCardID` / `startReadCarIDMini` 循环是否启动
- 卡号解析后是否匹配 `longTermPassDao` 本地记录

## 二维码无数据

### 配置检查

| 参数 | 默认 |
|---|---|
| path | `/dev/ttyS4` |
| baud | 115200 |
| dataBits | 7 |
| stopBits | 1 |
| parity | 2（偶校验） |

运维抽屉 → **二维码串口配置**

### 初始化条件

```java
if (!ChannelConfig.SUPPORTS_TEMPORARY_PASS) return;
initScanCard();
```

渠道不支持临时证时 **不会** 打开 QR 串口。

### 日志

```
二维码串口初始化:{path},{baud},... => true/false
二维码接收到的数据: "..."
```

`=> false`：路径错误或参数不匹配。

### 数据格式

- `readData` 转 String，去除 `\r`
- 交给 `getShortPassCardID(data)`
- 乱码：调整 dataBits/parity 与扫码枪一致

### 发送侧

`SerialManage.send` 要求 **十六进制字符串**；查验场景主要是接收，一般不发指令。

## 卡号有但无卡面 / 无人员信息

| 原因 | 处理 |
|---|---|
| 本地无该 cardId | 等增量同步或重新登录 |
| 通行证 status 注销/过期 | UI 显示状态文案 |
| 图片未下载 | 查 `ImageDownloader.downloadImage` 失败日志 |

## 串口任务时序（QR）

| 任务 | 间隔 |
|---|---|
| 发送队列 | 100 ms |
| 读取轮询 | 150 ms |

高频日志可辅助确认串口存活。

## 与离线模式

`NetworkUtils.isAvailableByPing` 失败 → `isOffLine=true`

- 读卡仍可读 **本地** 长期证
- 临时证可能依赖在线校验（视 Activity 逻辑）

## 硬件冲突

- 232 多指令：QR 侧必须用 `SerialManage.send` 队列，勿_burst 直接写串口
- 同 tty 被其他进程占用：open 失败

## 关联源码

| 文档 | 内容 |
|---|---|
| `doc/09-serial/01-rfid-card-reader.md` | CardSerialConfigUtil 与 Activity |
| `doc/09-serial/02-qr-scanner.md` | SerialManage/Handle 细节 |

## 快速 ADB

```bash
# 查看串口设备
adb shell ls -l /dev/ttyS*

# 过滤串口日志
adb logcat -s YCJC | grep -iE "读卡|OpenReader|二维码|串口"
```
