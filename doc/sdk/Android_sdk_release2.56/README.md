# 德卡读卡器 SDK（Android_sdk_release2.56）接入与开发流程

## 1. 资源说明

本目录保存厂商交付的原始资源，不直接参与 App 编译。原始包约 55 MB，共 6 个顶层文件：

```text
Android_sdk_release2.56/
├── DCardReader操作手册.pdf
└── Android_sdk_release2.56/
    ├── SDK/
    │   ├── dc_reader_release_V1.0.0_20231121115913.aar
    │   ├── MobileDevSLExample.apk
    │   ├── 德卡移动开发包接口文档通用.pdf
    │   └── 新永久居住证调用示例.png
    └── demo/
        └── BaseLibraryDemo.zip
```

关键文件 SHA-256：

```text
a43af39d7e64951ff7b0f361afef6a9b51a3e8ff8693ced9aacd9e0c10fd2bbd  dc_reader_release_V1.0.0_20231121115913.aar
3e3dadc0a879d63f2122335040420348c053b693f1dff764afb04131ba80b8c4  BaseLibraryDemo.zip
3f30afd9151411f1c031018fe5911ad08f269124d4d1e25a20c8f10325ad3291  MobileDevSLExample.apk
```

版本信息存在多套编号：

- 交付目录名：`Android_sdk_release2.56`
- AAR 构建时间：`2023-11-21 11:59:13`
- 接口文档封面：Java Developers `v1.5.6`
- 接口文档修订记录最新项：`v1.5.7（2023-11-21）`

升级和问题反馈时应同时提供目录名、AAR 文件名及 SHA-256，避免只使用“2.56”造成版本误判。

## 2. 当前项目接入现状

项目当前编译版本：

```text
app/libs/dc_reader_release_V1.0.0_20231121115913.aar
```

旧版备份：

```text
doc/sdk/Android_sdk_release2.56/dc_reader_release_V1.0.0_20230516162946.aar.bak
```

`app/build.gradle` 通过 `implementation(fileTree("libs"))` 自动加载 `app/libs` 下的本地依赖。现有业务已经使用 `com.decard.NDKMethod.BasicOper`：

- `LivenessDetectJinActivity`：短距读卡。
- `LivenessDetectYuanActivity`：短距读卡 + 长距 RFID。
- `LivenessDetectYuanAndJinActivity`：短距读卡 + 长距 RFID。

当前初始化差异：

| Activity | `BasicOper.dc_open` | `BasicOper` 轮询 | 长距 `EC_API` |
|---|---:|---:|---:|
| Jin | 有 | 有 | 无 |
| Yuan | **无** | 有 | 有 |
| YuanAndJin（大屏） | 有 | 有 | 有 |
| YuanAndJin（小屏） | 不使用，改走 `AndroidSerialPort` | 无 | 无 |

Yuan 页面会调用短距 `BasicOper` 认证方法，却没有在本页面打开德卡端口。这不是新版 SDK 的变化，而是现有代码的初始化边界；升级回归必须覆盖“应用冷启动后直接进入 Yuan”场景。

串口路径和波特率由 `CardSerialConfigUtil` 管理，默认值为：

```text
path = /dev/ttyS3
baud = 115200
```

该配置目前用于 Jin 和 YuanAndJin 的短距初始化；Yuan 的长距 `EC_API` 由 SDK 自行枚举串口。

新版 AAR 同时包含 `arm64-v8a` 和 `armeabi-v7a` so，项目当前只打包 `arm64-v8a`，架构可以匹配。

## 3. 升级前检查

### 3.1 不要同时放置新旧 AAR

新旧 AAR 都包含 `BasicOper` 等同名类。由于项目使用 `fileTree("libs")`，同时放入两个版本会导致 duplicate class 或 native so 冲突。

正确做法：

1. 在独立分支完成升级。
2. 删除或移出旧 AAR。
3. 将 `dc_reader_release_V1.0.0_20231121115913.aar` 复制到 `app/libs/`。
4. 不需要额外增加 Gradle 依赖语句。
5. 执行 clean build，确认依赖和 native so 打包结果。

### 3.2 已知兼容性风险

- 新版 AAR 与旧版不是简单的同文件替换：新版不再包含 `com.android.DecardPowerApi`，同时调整了 USB、蓝牙驱动类。
- 厂商 Demo 压缩包内自带的是另一个 AAR：`dc_reader_release_V1.0.0_20231120173924.aar`，不能用 Demo 能否编译直接证明交付 AAR 可编译。
- 当前项目没有调用 `DecardPowerApi`，现有 `BasicOper` 主链路不受该类删除的直接影响；如果后续需要设备上下电，必须向厂商确认目标机型的电源控制方式。
- 新版移除了旧 AAR 中的 `libHandleDecrypt.so`、`libjnidispatch.so`，需要在真机验证现有 PSAM 外部认证流程。
- 串口节点权限取决于设备 ROM。普通 Android Manifest 权限不能替代 `/dev/tty*` 的读写权限。

### 3.3 权限

当前项目已经声明 `INTERNET`、存储、`READ_PHONE_STATE` 等基础权限。现有业务使用串口 `COM` 模式，不需要照搬 Demo 的全部蓝牙权限。

只有接入对应连接方式时才补充：

- Android USB：调用 `BasicOper.dc_AUSB_ReqPermission(activity)`，等待异步授权完成后再打开。
- 经典蓝牙/BLE：补充 Android 12+ 的 `BLUETOOTH_SCAN`、`BLUETOOTH_CONNECT` 等权限及运行时授权。
- LibUSB：厂商文档要求设备 root 并授予相应权限，不建议作为通用方案。

## 4. 开发流程

### 阶段一：确认硬件参数

1. 确认设备型号和连接方式：串口、Android USB、LibUSB、经典蓝牙或 BLE。
2. 确认串口节点和波特率，不要只依赖项目默认值。
3. 通过运维抽屉的“读卡串口配置”保存实际参数。
4. 常见厂商参数：
   - Z90N、P18F、M200-Q3：`/dev/ttyHSL1`，`115200`
   - P18Q：`/dev/dc_spi32765.0`，`115200`
   - f10_x1：`/dev/ttyS3`，`921600`
   - f11_x1：`/dev/ttyUSB0`，`115200`
   - rk3568_r、F11PC：`/dev/ttyS4`，`115200`

最终以目标设备、线材和厂商确认结果为准。

### 阶段二：替换 SDK 并编译验证

1. 用新版 AAR 替换 `app/libs` 中的旧版 AAR。
2. 保持项目 `arm64-v8a` 配置，确认目标设备确实为 64 位 ARM。
3. 执行：

```bash
./gradlew clean
./gradlew assembleYinchuanDebug
```

4. 重点检查：
   - 是否存在 duplicate class。
   - 是否出现 `UnsatisfiedLinkError`。
   - APK 是否包含 `lib/arm64-v8a/libdcrf32.so`、`libreadcard.so`、`libwlt2bmp.so` 等文件。
   - R8/混淆开启时是否需要补充 keep 规则；当前项目 debug/release 均未开启混淆。

### 阶段三：打开设备

设备初始化必须放在工作线程，避免串口或 USB 操作阻塞主线程。

现有串口方案：

```java
String path = CardSerialConfigUtil.getCardSerialPath();
int baudrate = CardSerialConfigUtil.getCardSerialBaudRate();
int handle = BasicOper.dc_open("COM", activity, path, baudrate);
if (handle >= 0) {
    BasicOper.dc_beep(5);
}
```

厂商文档将“返回设备句柄号且大于 0”描述为成功，Demo 使用 `>= 0` 判断。项目当前也使用 `>= 0`。升级联调时应记录实际返回值，并向厂商确认 `0` 在该版本中的语义。

其他连接方式：

```java
// Android USB：先异步申请权限
BasicOper.dc_AUSB_ReqPermission(activity);
int usbHandle = BasicOper.dc_open("AUSB", activity, "", 0);

// 串口
int comHandle = BasicOper.dc_open("COM", null, "/dev/ttyS3", 115200);

// 经典蓝牙 / BLE，参数为设备 MAC
int btHandle = BasicOper.dc_open("BT", activity, macAddress, 0);
int bleHandle = BasicOper.dc_open("BLE", activity, macAddress, 0);
```

同一物理串口不要被多个页面、进程或 SDK 重复打开。

### 阶段四：执行项目现有读卡链路

当前长期证短距读卡不是简单读取身份证信息，而是 PSAM + ACPU 外部认证流程：

```text
打开端口
  → 选择 PSAM 卡座并复位
  → 读取终端机编号、选择 PSAM 应用
  → 配置 Type A 卡
  → 射频复位并获取 RFID
  → 卡片取随机数
  → PSAM 加密初始化（RFID）
  → PSAM 对随机数加密并取结果
  → ACPU 外部认证
  → 返回 9000 后按 RFID 查询本地长期证
  → 有效期、状态、黑名单、区域、引领关系校验
  → 进入人脸比对和通行记录流程
```

SDK 字符串接口通常返回：

```text
0000|业务数据
错误码|错误信息
```

每一步都应先安全拆分并判断错误码，不能在返回格式异常时直接访问 `resultArr[1]`。

如果需求是读取居民身份证，可参考 Demo 的独立接口：

```java
IDCard idCard = BasicOper.dc_SamAReadCardInfo(1);
```

该接口不是当前长期通行证 PSAM 认证链路的替代方案。新版外国人永久居留证可通过 `IDCard.getType()` 区分：

- `1`：2017 版外国人永久居留身份证。
- `2`：港澳台居民居住证。
- `3`：新版外国人永久居留身份证。
- 其他：普通居民身份证。

### 阶段五：停止轮询并释放资源

1. 页面不可见或销毁时先停止读卡循环。
2. 等待读卡工作线程退出，避免关闭端口时仍在调用 native 接口。
3. 调用 `BasicOper.dc_exit()` 关闭端口并释放 SDK 资源。
4. 清理 Handler、线程和页面引用。
5. 页面重新进入时重新读取串口配置并初始化，避免配置修改后仍使用旧连接。

现有三个查验 Activity 已有 `stopReadLongPassCardID()`，但未发现对应的 `BasicOper.dc_exit()` 调用。升级开发时应将“线程停止”和“端口释放”作为同一个生命周期改造项，并验证不会影响 Activity 间快速切换。

## 5. 联调与验收

### 5.1 基础验收

- 冷启动首次打开读卡器成功，返回值和串口参数有日志。
- 退出页面后端口释放，再次进入页面可正常重连。
- 连续刷同一张卡不会产生重复记录。
- PSAM 复位、射频复位、随机数、加密、外部认证每一步均能定位错误码。
- 长期证 RFID 与旧 SDK 结果一致，字节序没有变化。
- Jin、Yuan、YuanAndJin 三个页面分别真机验证。
- Yuan 长距 RFID 与新版短距 SDK 并行时无串口、线程或 native so 冲突。

### 5.2 异常验收

- 串口路径错误、串口被占用、设备未上电。
- PSAM 不存在、卡片移开、认证失败。
- 快速进出页面、息屏恢复、应用切后台再前台。
- 连续运行至少 2 小时，检查线程、文件描述符和 native 内存。
- 覆盖未生效、过期、注销、黑名单、区域不匹配及引领关系失败。

### 5.3 日志建议

每次初始化至少记录：

```text
SDK AAR 文件名
设备 Build.MODEL
连接方式、串口路径、波特率
dc_open 返回值
读卡步骤名、错误码、耗时
脱敏后的卡号或卡号摘要
dc_exit 返回值
```

不得在生产日志输出身份证完整号码、姓名、照片原始数据、PSAM 密钥或完整认证报文。

## 6. 相关项目文档

- [长短距读卡器、SDK 与串口配置](../../09-serial/01-rfid-card-reader.md)：设备分支、认证链和生命周期。
- [刷卡校验规则](../../05-check/06-card-read-validate.md)：读卡后的长期证、临时证及引领人校验。
- [刷卡/串口排查](../../17-troubleshooting/03-card-serial.md)：串口与 SDK 分步排查。
- [Jin 短距查验](../../05-check/02-liveness-jin-activity.md)：Jin 短距读卡业务。
- [Yuan 长短距查验](../../05-check/03-liveness-yuan-activity.md)：Yuan 短距 + 长距读卡业务。
- [YuanAndJin 合并查验](../../05-check/04-liveness-yuan-jin-activity.md)：合并查验业务。

## 7. 本次交付边界

厂商资源已归档；`app/libs` 已替换为 `dc_reader_release_V1.0.0_20231121115913.aar`（`assembleYinchuanDebug` 编译通过）。旧版 AAR 备份在本目录 `dc_reader_release_V1.0.0_20230516162946.aar.bak`。真机仍需回归 PSAM 外部认证全链路后再合入。
