# airport-face 重构架构文档

> 本文档体系用于支撑后续重构计划制定，基于源码与 `doc/` 技术文档（v2，60 篇）整理。
> 最近复核：2026-07-17 · 版本基准：`1.0.75`（versionCode `45092624`）
> 涉及当前运行时行为时，以源码和 `doc/` v2 为准；本目录用于架构分析、风险约束与重构排期。

---

## 文档目的

| 目标 | 说明 |
|------|------|
| **架构全景** | 梳理模块边界、分层、依赖关系，形成可查阅的架构视图 |
| **功能地图** | 按业务域划分功能模块，明确入口类与核心链路 |
| **重构输入** | 识别技术债、耦合热点、重复代码，为重构优先级提供依据 |
| **与 doc/ 分工** | `doc/` 偏实现细节与联调；`refactor/` 偏架构分析与重构决策 |

---

## 文档目录

| 序号 | 文档 | 内容 |
|------|------|------|
| 01 | [01-architecture-overview.md](./01-architecture-overview.md) | 系统定位、Gradle 模块、技术栈、生产主路径 |
| 02 | [02-functional-modules.md](./02-functional-modules.md) | 12 个功能域模块详解（入口、核心类、边界） |
| 03 | [03-layer-and-dependencies.md](./03-layer-and-dependencies.md) | 四层架构、包结构、模块依赖矩阵 |
| 04 | [04-data-flow-and-interactions.md](./04-data-flow-and-interactions.md) | 登录、查验、同步、上传等核心数据流 |
| 05 | [05-technical-debt-assessment.md](./05-technical-debt-assessment.md) | 技术债清单、风险等级、重构候选区 |
| 06 | [06-refactor-priority-matrix.md](./06-refactor-priority-matrix.md) | 重构优先级矩阵与建议阶段划分 |
| 07 | [07-refactor-safety-baseline.md](./07-refactor-safety-baseline.md) | 重构前行为基线、构建矩阵、硬件与离线验收清单 |

---

## 项目一句话定义

**机场控制区竖屏通行证查验终端**：部署在闸机/立式 Android 设备，完成 **人脸 + 证卡** 核验、电子卡面展示、通行记录本地采集与后台同步。基于 ArcSoft ArcFace SDK，支持 RGB+IR 活体、1:N 识别（上限 30,000 人脸）。

---

## 快速参考

### Gradle 模块

```
:app          主应用（生产）
:xupdate-lib  应用内更新库
:update       独立更新测试包（未被 app 依赖）
```

### 生产主路径

```
BootReceiver → LoginActivity
  → LivenessDetectJinActivity      （短距刷卡+人脸）
  | LivenessDetectYuanActivity     （长距+短距轮询+人脸）
  | LivenessDetectYuanAndJinActivity（双读卡器）
  | RegisterAndRecognizeActivity   （纯人脸出区）
  → ConstructionWorkersActivity    （施工人员支线）
```

### 源码规模（main 包，约 200 文件）

| 包/目录 | 文件数 | 重构关注度 |
|---------|--------|------------|
| `ui/activity` | 15 | ★★★★★ 巨型 Activity |
| `util` | 70 | ★★★ 工具分散 |
| `widget` | 30 | ★★ 弹窗/自定义 View |
| `util/face` | 15+ | ★★★★ 核心引擎管线 |
| `network` + `data/http` | 12 | ★★★ 双通道网络 |
| `db` + `facedb` | 10 | ★★ 双库分离清晰 |

### 关联文档

- 实现细节：[doc/README.md](../doc/README.md)
- 分层架构：[doc/03-architecture/01-layered-architecture.md](../doc/03-architecture/01-layered-architecture.md)
- 查验核心：[doc/05-check/](../doc/05-check/)
- 网络层：[doc/11-network/01-api-utils.md](../doc/11-network/01-api-utils.md)
- 构建发布：[doc/01-overview/04-build-release.md](../doc/01-overview/04-build-release.md)
- 运行时权限：[doc/16-device/02-runtime-permissions.md](../doc/16-device/02-runtime-permissions.md)
- 读卡与 SDK：[doc/09-serial/01-rfid-card-reader.md](../doc/09-serial/01-rfid-card-reader.md)、[doc/sdk/Android_sdk_release2.56/README.md](../doc/sdk/Android_sdk_release2.56/README.md)
- 重构安全基线：[07-refactor-safety-baseline.md](./07-refactor-safety-baseline.md)

---

## 使用建议（制定重构计划时）

1. 先读 **01 → 03** 建立架构认知
2. 对照 **04** 确认要改的业务链路
3. 用 **05** 识别风险与约束（硬件/SDK/渠道）
4. 用 **06** 排期与拆分 PR

后续可在本目录追加各阶段 ADR、接口草案与模块抽取记录；目标架构和迁移顺序以 06 篇为主，实际行为护栏以 07 篇为准。
