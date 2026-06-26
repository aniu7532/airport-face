# airport-face 重构架构文档

> 本文档体系用于支撑后续重构计划制定，基于源码与 `doc/` 技术文档（v2，57 篇）整理。  
> 生成日期：2026-06-26 · 版本基准：`1.0.72`（versionCode `45092621`）

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
  | LivenessDetectYuanActivity     （长距刷卡+人脸）
  | LivenessDetectYuanAndJinActivity（双读卡器）
  | RegisterAndRecognizeActivity   （纯人脸出区）
  → ConstructionWorkersActivity    （施工人员支线）
```

### 源码规模（main 包，约 200 文件）

| 包/目录 | 文件数 | 重构关注度 |
|---------|--------|------------|
| `ui/activity` | 14 | ★★★★★ 巨型 Activity |
| `util` | 69 | ★★★ 工具分散 |
| `widget` | 30 | ★★ 弹窗/自定义 View |
| `util/face` | 15+ | ★★★★ 核心引擎管线 |
| `network` + `data/http` | 12 | ★★★ 双通道网络 |
| `db` + `facedb` | 10 | ★★ 双库分离清晰 |

### 关联文档

- 实现细节：[doc/README.md](../doc/README.md)
- 分层架构：[doc/03-architecture/01-layered-architecture.md](../doc/03-architecture/01-layered-architecture.md)
- 查验核心：[doc/05-check/](../doc/05-check/)
- 网络层：[doc/11-network/01-api-utils.md](../doc/11-network/01-api-utils.md)

---

## 使用建议（制定重构计划时）

1. 先读 **01 → 03** 建立架构认知
2. 对照 **04** 确认要改的业务链路
3. 用 **05** 识别风险与约束（硬件/SDK/渠道）
4. 用 **06** 排期与拆分 PR

后续可在本目录追加：

- `07-target-architecture.md` — 目标架构草案
- `08-migration-plan.md` — 分阶段迁移计划
- `09-module-extraction/` — 各模块抽取方案
