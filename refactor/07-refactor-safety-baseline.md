# 07 · 重构安全基线与验收护栏

> 基线版本：`1.0.75`（versionCode `45092624`）
> 建立日期：2026-07-17
> 目的：先锁定当前可接受行为和已知缺陷，再判断重构是否等价。

## 1. 使用规则

1. “当前代码如此”不等于“行为正确”。已知缺陷单独列出，不能写成重构后必须保留的契约。
2. 纯抽取 PR 不同时修业务缺陷；缺陷修复必须独立提交并附重构前后证据。
3. 无法自动化的硬件流程必须保留设备型号、SDK/AAR、串口参数、日志和结果。
4. 每阶段至少通过本文件对应矩阵，不能只以编译成功或行数下降验收。

## 2. 构建基线

| 检查 | 基线 |
|------|------|
| 工具链 | AGP 8.9.1、Kotlin 2.2.10、compileSdk 36 |
| Android | minSdk 26、targetSdk 33 |
| ABI | 仅 `arm64-v8a` |
| flavor | yinchuan、chongqing、shihezi、luoyang |
| buildType | debug、release |
| 混淆/资源压缩 | 当前均关闭 |
| 本地 SDK | `app/libs/` 通过 fileTree 全量加载 |

阶段 0 应保存 8 个构建变体的命令、结果和 APK 文件名。签名口令不得进入报告；仅记录证书指纹是否与生产包一致。

## 3. 渠道契约

| flavor | TENANT_PREFIX | TENANT_ID | 临时证 |
|--------|---------------|-----------|--------|
| yinchuan | 空 | `1` | 支持 |
| chongqing | 空 | `3` | 支持 |
| shihezi | `shf` | `1` | 支持 |
| luoyang | `fy` | `2054084946120802305` | 支持，横版定制 Document3 |

必须建立可在 JVM 或构建任务中执行的 flavor 契约检查，防止 URL、租户和业务开关被文档或复制代码改错。

## 4. 核心业务行为矩阵

### 4.1 登录与初始化

- [ ] 零信任成功后才调用后台登录。
- [ ] 登录响应写入内存 accessToken/refreshToken；userId 同时写入 InfoStorage。
- [ ] 设备、配置、用户信息任一步失败会阻止进入查验页。
- [ ] 首次/空库执行全量通行证分页、图片下载和人脸注册。
- [ ] 当前没有自动 Token 刷新；已有 Job 未注册/调度且刷新后未写回 accessToken，Token 过期按现状回登录。

### 4.2 查验规则

`CardValidator` 特征测试至少锁定：

1. 临时证开关。
2. 未生效。
3. 已过期。
4. 非正常状态。
5. 黑名单。
6. 已收回。
7. 已暂扣。
8. 分数小于等于 0。
9. 时间段不允许。
10. 区域 `areaIds` / `areaRootIds` 匹配。
11. C 类证和临时证引领人关系。
12. 1500ms 重复刷卡过滤。

四个 flavor 都要覆盖临时证成功路径，不能继续使用“洛阳拒绝临时证”的历史假设。

### 4.3 人脸与记录

- [ ] Jin/Yuan/YuanAndJin 使用目标注册照与现场特征做 1:1。
- [ ] RegisterAndRecognize 使用 1:N，`FaceEntity.userName` 按通行证 `id` 反查档案。
- [ ] 比对成功和失败均按当前规则写通行记录；失败原因为“人证不匹配”。
- [ ] 长期证、临时证、C 类引领关系字段与重构前一致。
- [ ] 注册照/证件照仍从 `register/`、`photo/` 加密目录加载。

### 4.4 离线与上传

- [ ] 断网时查验仍可读取本地长期证。
- [ ] 长期/临时记录进入各自 Room 待上传队列。
- [ ] 联网后 30 秒任务上传并在成功后删除记录及关联图片。
- [ ] 上传失败保留队列，下轮可重试。
- [ ] 并发触发时 CAS 保证同一时刻只有一个上传任务。
- [ ] Activity 中即时上传 else 分支不可达；测试不得误把该分支当作当前生产链路。

## 5. 硬件真机矩阵

| 场景 | 实现 | 必测 |
|------|------|------|
| 大屏短距 | 德卡 `BasicOper` + PSAM/ACPU | 冷启动、认证全链、反复进出、模式切换 |
| 小屏短距 | `AndroidSerialPort` + `Card` | OpenReader、UID 字节序、重连 |
| 长距 | `EC_API` | 串口枚举、38400/8E1、cardIdLong 查询 |
| 双读卡 | 德卡 + EC_API | 短距优先、失败后长距、并行无冲突 |
| 临时证 | `SerialManage` QR | 四 flavor applyId、引领关系、串口独立 |

每次记录：

```text
设备型号 / Android 版本 / flavor / APK
SDK AAR/JAR 版本与 SHA-256
串口路径、波特率、连接方式
open/start/stop/close 返回值
读卡步骤、错误码、耗时
是否有线程、文件描述符、native 内存增长
```

## 6. 已知缺陷与非契约行为

以下行为应在独立 bugfix 中修复，不能作为“重构必须保持”的基线：

| ID | 当前问题 | 修复验收 |
|----|----------|----------|
| K-01 | Yuan 会轮询 `BasicOper`，但本页面未 `dc_open` | 冷启动直达 Yuan，短距正常且有明确 open 日志 |
| K-02 | 三个查验页均未调用 `BasicOper.dc_exit()` | 退出/切换后端口释放，可立即重进 |
| K-03 | 网络全局客户端存在不安全证书信任，Manifest 允许明文流量 | 生产证书链和必要域名通过，非法证书失败 |
| K-04 | 签名口令硬编码在 Gradle | Secret 外置，仓库与日志不再出现口令 |
| K-05 | SQLCipher 仅有依赖，Room 实际未加密 | 文档保持真实；如启用需独立迁移与恢复方案 |
| K-06 | `fallbackToDestructiveMigration()` 可能清空未覆盖升级路径 | v19→v20 保留通行证与待上传记录 |
| K-07 | Activity 保留被 `if (true)` 绕过的即时上传旧代码 | 明确 Application 为唯一真实上传入口；护栏完成后删除旧分支 |

## 7. 自动化测试分层

| 层级 | 首批覆盖 |
|------|----------|
| JVM 单测 | CardValidator、AreaPermissionChecker、TimeControl、短距/长距 PassLookup、渠道契约、状态机 |
| Repository 测试 | Room 写入/查询/删除、待上传队列、Migration |
| 网络契约测试 | Header、URL、401 单飞刷新、错误映射 |
| 仪器测试 | Login 路由、Document 导航、权限拒绝/恢复 |
| 真机测试 | 相机、ArcFace、德卡/华大/EC_API/QR、Kiosk |

厂商 SDK 无法在 JVM 中运行时，使用 Fake `CardReaderStrategy` 驱动状态机；不要在单测里加载 native 库。

## 8. PR 门禁

- [ ] PR 只做一种变更：特征测试、缺陷修复、纯抽取或基础设施替换。
- [ ] 四 flavor 至少完成 Debug 构建；影响签名/更新时补 Release。
- [ ] 纯重构前后关键输入输出一致，并附测试结果。
- [ ] 涉及硬件时附本文件第 5 节记录。
- [ ] 涉及数据库时附旧版本升级和回滚结果。
- [ ] 涉及网络/Token 时覆盖离线、401、重试和证书失败。
- [ ] 更新 `doc/` 实现文档与本目录技术债/计划状态。

## 9. 关联文档

- [构建、签名与发布](../doc/01-overview/04-build-release.md)
- [刷卡校验规则](../doc/05-check/06-card-read-validate.md)
- [长短距读卡器](../doc/09-serial/01-rfid-card-reader.md)
- [德卡 SDK 归档与升级](../doc/sdk/Android_sdk_release2.56/README.md)
- [后台登录与 Token](../doc/04-auth/02-backend-login-token.md)
- [Room 业务数据库](../doc/12-data/01-room-business-db.md)
- [Android 权限](../doc/16-device/02-runtime-permissions.md)
