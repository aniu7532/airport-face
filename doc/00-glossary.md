# 全局术语与配置索引

本文档汇总全项目 **SPUtils**、**InfoStorage**、**VerifyFeatureSettings**、**串口配置** 及核心常量，便于跨模块检索。

---

## 1. SPUtils（`com.blankj.utilcode.util.SPUtils`）

默认 SP 文件，运维抽屉与查验页广泛使用。

| 键 | 类型 | 默认值 | 写入位置 | 读取位置 | 说明 |
|----|------|--------|----------|----------|------|
| `checkType` | int | `0` | CustomDrawerPopupView | LoginActivity.gotoActivity、各 Liveness* | 0短距 1长距 2双距 3纯人脸 |
| `direction` | int | `1` | CustomDrawerPopupView | 各查验 Activity | 1进 -1出 |
| `tipsLoc` | int | `0` | CustomDrawerPopupView | Liveness*、Register* | 0左下 1左上 2右下 3右上 |
| `mobile` | String | `""` | LoginActivity getUserDetail | CustomDrawerPopupView | 登录用户手机 |
| `wenan` | String | `""` | CustomDrawerPopupView | 查验页文案 | 自定义提示文案 |
| `Appid` | String | Constants | AppKeyPopDialog | ActivationActivity | ArcFace APP_ID |
| `Sdkkey` | String | Constants | AppKeyPopDialog | ActivationActivity | ArcFace SDK_KEY |
| `Activecode` | String | Constants | AppKeyPopDialog | ActivationActivity | ArcFace ACTIVE_KEY |
| `reboot` | boolean | `true` | ArcFaceApplication 日任务 | 凌晨2点重启 | 当日是否可重启 |
| `upload_log` | boolean | `true` | ArcFaceApplication | 上午10点 | 当日是否可上传日志 |
| `reinit_check` | boolean | `true` | ArcFaceApplication | 凌晨1点 | 当日是否可数据检查 |
| `card_serial_path` | String | `/dev/ttyS3` | CardSerialConfigPopDialog | CardSerialConfigUtil | RFID 串口路径 |
| `card_serial_baud` | int | `115200` | CardSerialConfigPopDialog | CardSerialConfigUtil | RFID 波特率 |
| `qr_serial_path` | String | `/dev/ttyS4` | QrSerialConfigPopDialog | QrSerialConfigUtil | 二维码串口路径 |
| `qr_serial_baud` | int | `115200` | QrSerialConfigPopDialog | QrSerialConfigUtil | 二维码波特率 |
| `qr_serial_data_bits` | int | `7` | QrSerialConfigPopDialog | QrSerialConfigUtil | 数据位 |
| `qr_serial_stop_bits` | int | `1` | QrSerialConfigPopDialog | QrSerialConfigUtil | 停止位 |
| `qr_serial_parity` | int | `2` | QrSerialConfigPopDialog | QrSerialConfigUtil | 校验位 |

---

## 2. InfoStorage（SP 名：`yunduanchayan`）

**类**：`util/InfoStorage.java`  
**实例**：`ArcFaceApplication` / `LoginActivity` 的 `infoStorage` 字段

| 键 | 类型 | 写入时机 | 主要读取方 | 说明 |
|----|------|----------|------------|------|
| `userId` | String | login 成功 | getUserDetail | 后台用户 ID |
| `loginName` | String | getUserDetail | 写通行记录 checkUserName | 登录名/昵称 |
| `isFirstStart` | boolean | 首次同步完成→false | login 判断是否全量拉证 | 首次启动标志 |
| `deviceId` | String | getMACDetail | 写记录、RecordsPopDialog | 设备 ID |
| `deviceName` | String | getMACDetail | 写记录 | 默认「立式查验终端」 |
| `deviceMac` | String | getMACDetail | 心跳 mac | 设备 MAC |
| `deviceAreaDetail` | String | getMACDetail | 查验页 areaName | 绑定区域 JSON/名称 |
| `devicesType` | String | getConfigInfo type=5 | — | 设备类型配置 |
| `devicesEnter` | String | getConfigInfo type=5 | — | 进区设备配置 |
| `devicesOut` | String | getConfigInfo type=5 | — | 出区设备配置 |
| `interval` | int | getConfigInfo type=6 | startPeriodicTask | 心跳/同步间隔（分钟） |
| `startDate` | String | 全量同步开始 | — | 同步开始时间 |
| `linshiID` | String | 刷长期证作引领人 | 临时证流程 | 当前引领人 userId |
| `zero_trust_username` | String | 零信任成功 | startLogin | VPN 账号 |
| `zero_trust_password` | String | 零信任成功 | startLogin | VPN 密码 |

---

## 3. VerifyFeatureSettings（SP 布尔）

| 键 | 默认 | 说明 |
|----|------|------|
| `verify_feature_enabled` | false | 核销总开关 |
| `verify_required_passage` | false | 核实必填通道 |
| `verify_required_pass_time` | false | 核实必填通行时间 |
| `verify_required_device` | false | 核实必填设备 |
| `verify_required_remark` | false | 核实必填备注 |

`needVerifyForNewRecord()` = 总开关值。

---

## 4. Constants（`common/Constants.java`）

| 常量 | 值/说明 |
|------|---------|
| `APP_ID` / `SDK_KEY` / `ACTIVE_KEY` | ArcFace 激活三要素 |
| `ACTIVE_CONFIG_FILE_NAME` | `activeConfig.txt` 外部配置激活 |
| `DEFAULT_REGISTER_FACES_DIR` | 外部注册图目录 |
| `BASE_VPN` | 零信任服务器 URL |
| `ZERO_USERNAME` / `ZERO_PASSWORD` | 默认 VPN 账号 |

---

## 5. UrlConstants 运行时

| 常量 | 来源 |
|------|------|
| `URL` | `ChannelConfig.BASE_URL` |
| `TENANT_ID` | `ChannelConfig.TENANT_ID` |
| `URL_ClIENTID` | 固定 `VERTICAL` |

---

## 6. ArcFaceApplication 静态常量

| 常量 | 值 |
|------|-----|
| `UPLOAD_LOG_TIME` | 30000 ms |
| `PING_DELAY_TIME` | 10000 ms |
| `UPDATE_DELAY_TIME` | 5（分钟，可被后台 interval 覆盖） |
| `POOL_SIZE` | 15 |
| `READ_TIME` | 1000 ms |

---

## 7. 业务枚举速查

| 名称 | 值 | 含义 |
|------|-----|------|
| 通行证 type | 0 | 长期证 |
| 通行证 type | 1 | 临时证 |
| 通行证 status | 2 | 已注销 |
| direction | 1 / -1 / 2 | 进 / 出 / 核验 |
| templateType | 1 / 2 | 蓝证 / 黄证 |
| checkType | 0~3 | 见 05-check/01-check-modes.md |

---

## 8. 本地存储路径

| 路径 | 内容 |
|------|------|
| `{externalFilesDir}/db/airportDb.db` | 业务 Room |
| `{externalFilesDir}/database/faceDB.db` | 人脸 Room |
| `{externalFilesDir}/log/` | ALog 日志 |
| `{externalFilesDir}/faceDB/` | 加密证件照 |
| `{externalFilesDir}/records/` | 通行抓拍（3天清理） |
