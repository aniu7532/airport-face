# 刷卡 / 串口排查

## 三类硬件通道

| 用途 | 配置方式 | 默认参数 | 管理 |
|---|---|---|---|
| 短距长期证读卡 | `CardSerialConfigUtil` | `/dev/ttyS3`、115200 | Activity 内德卡/华大 SDK |
| 长距长期证读卡 | `EC_API` 枚举串口 | 38400、8E1 | Activity 内 `EC_API` |
| 临时证二维码 | `QrSerialConfigUtil` | `/dev/ttyS4` | `SerialManage` 单例 |

**不可混用配置**：改 QR 串口不影响短距读卡，`CardSerialConfigUtil` 也不控制长距 `EC_API`。

## 短距读卡无反应

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

注意：`LivenessDetectYuanActivity` 当前只初始化 `EC_API` 长距设备，没有在本页面调用 `BasicOper.dc_open()`，但轮询仍会先执行短距 `BasicOper` 方法。因此 Yuan 模式出现“长距正常、短距无反应”时，优先检查这一初始化缺口，不要只改串口 SP。

### 排查步骤

1. 日志搜 `读卡串口[` 或 `OpenReader`
2. 确认 tty 节点存在：`adb shell ls -l /dev/ttyS*`
3. 权限：串口读写、厂商 ROM 授权
4. 改 SP 后需 **重新进入查验页** 触发 `initReadCard`
5. 波特率与读卡器说明书一致（默认 115200）

### 有读卡日志但无业务响应

- 查 `startReadLongPassCardID` / `startReadCarIDMini` 循环是否启动
- 卡号解析后是否匹配 `longTermPassDao` 本地记录

### 德卡 SDK 分步定位

大屏短距链路应按阶段看返回值：

1. `dc_open`：串口与权限。
2. `dc_cpureset_hex`：PSAM 卡座与复位。
3. `dc_cpuapduInt_hex`：终端编号、PSAM 应用与加密。
4. `dc_reset` / `dc_card_n_hex`：寻卡和 RFID。
5. `dc_pro_resetInt_hex` / `dc_procommandInt_hex`：通行证随机数与外部认证。
6. 最终响应是否包含 `9000`。

SDK 字符串接口通常返回 `错误码|数据`。若返回值为空、缺少分隔符或数据段，不应只按“卡不存在”排查；这属于 SDK/设备通信异常。

### 替换 AAR 后异常

| 现象 | 检查 |
|---|---|
| `Duplicate class` | `app/libs/` 同时保留新旧德卡 AAR；只留一个版本 |
| `UnsatisfiedLinkError` | APK 的 `lib/arm64-v8a/` 是否包含新版 so；设备 ABI 是否匹配 |
| 编译正常、真机认证失败 | 新版移除了部分旧 native so；回归 PSAM 外部认证全链 |
| 页面重进后串口占用 | 当前 Activity 停止轮询但未调用 `BasicOper.dc_exit()` |
| Demo 正常、项目异常 | Demo 内置 AAR 与交付 AAR 文件名/构建时间不同，需以实际替换文件为准 |

当前 App 使用 `dc_reader_release_V1.0.0_20230516162946.aar`；待升级包及 SHA-256 见 [德卡 SDK 接入文档](../sdk/Android_sdk_release2.56/README.md)。

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
| `doc/sdk/Android_sdk_release2.56/README.md` | 德卡 SDK 版本、升级风险与验收 |

## 快速 ADB

```bash
# 查看串口设备
adb shell ls -l /dev/ttyS*

# 过滤串口日志
adb logcat -s YCJC | grep -iE "读卡|OpenReader|二维码|串口"
```
