# ViewModel 全索引

> 路径：`ui/viewmodel/`  
> 模式：Java `ViewModel` + `MutableLiveData`；Kotlin 施工模块用 `viewModelScope` + Paging

---

## 总览表

| 类 | 语言 | 主要职责 | 主要使用方 |
|----|------|----------|------------|
| `RecognizeViewModel` | Java | 相机预览识别、活体、比对结果 LiveData | 各 Liveness Activity、`RegisterAndRecognizeActivity` |
| `LivenessDetectViewModel` | Java | 活体检测 UI 状态辅助 | Liveness 系列 |
| `FacePhotoViewModel` | Java | 人脸库加载/删除/批量注册、`initFinished` | `LoginActivity`、`FaceManageActivity` |
| `HomeViewModel` | Java | 首页状态（若启用） | `HomeActivity` |
| `ActiveViewModel` | Java | ArcFace SDK 激活文件读写 | `ActivationActivity` |
| `RecognizeDebugViewModel` | Java | 识别调试信息 | 调试页 |
| `DataCalculatorViewModel` | Java | 图片宽高数据计算提示 | 设置/调试 |
| `ConstructionWorkersViewModel` | Kotlin | 施工人员主 Tab 容器 | `ConstructionWorkersActivity` |
| `AccessRecordViewModel` | Kotlin | 进出记录筛选 + Paging | 施工-进出记录 |
| `WriteOffRecordViewModel` | Kotlin | 核销记录 | 施工-核销 |
| `InOutStatisticsViewModel` | Kotlin | 进出统计筛选 | 施工-统计 |

---

## RecognizeViewModel（核心）

### LiveData 输出

| LiveData | 内容 |
|----------|------|
| `compareResultList` | `List<CompareResult>` 比对结果 |
| `faceItemEventMutableLiveData` | 人脸框增删事件 |
| `ftInitCode` / `frInitCode` / `flInitCode` / `mainInitCode` | 引擎各模块初始化码 |
| `recognizeConfiguration` | 阈值、开关配置 |
| `recognizeNotice` | 提示文案 |
| `drawRectInfoText` | 调试框文字 |

### 关键方法

| 方法 | 说明 |
|------|------|
| `init()` | 初始化 FT/FR/FL 引擎 |
| `destroy()` / `unInit()` | 释放 |
| `onRecognized(CompareResult, ...)` | 识别回调，更新 UI |
| `prepareRegister()` / `registerFace` | 单帧注册 |
| `clearLeftFace` | 清理离开画面的人脸 |
| `setRecognizeArea(Rect)` | 识别区域 |

实现 `RecognizeCallback`，由 `FaceHelper` 驱动。

---

## FacePhotoViewModel

| LiveData | 说明 |
|----------|------|
| `initFinished` | FaceServer 初始化完成 |
| `faceEntityList` | 当前页人脸列表 |
| `totalFaceCount` | 总数 |

| 方法 | 说明 |
|------|------|
| `init()` | 初始化 FaceServer + 加载计数 |
| `loadData(start, size)` | 分页读 `FaceDao` |
| `deleteFace(FaceEntity)` | 删库 + 删引擎 |
| `registerFromFile` / 批量 | 与 `LoginActivity` 共用逻辑 |

---

## LivenessDetectViewModel

查验页辅助状态（提示语、步骤），具体字段见源码；与 `RecognizeViewModel` 分工：**Recognize 管引擎，Liveness 管业务流程 UI**。

---

## Kotlin 施工模块 ViewModel

### AccessRecordViewModel

| 方法 | 说明 |
|------|------|
| `setName` / `setCardNo` / `setCompanyName` | 筛选条件 |
| `setStartTime` / `setEndTime` | `Calendar` 范围 |
| `search()` | 触发 Paging 刷新 |
| `reset()` | 清空条件 |

### WriteOffRecordViewModel / InOutStatisticsViewModel

类似模式：筛选字段 + `search()` + PagingSource 配合 `ConstructionWorkers` 各 Fragment。

详见 [08-construction/](../08-construction/)。

---

## ViewModel 与 Activity 绑定模式

```java
// Java DataBinding 常见
new ViewModelProvider(this).get(RecognizeViewModel.class);

// LoginActivity 自定义 Factory
new ViewModelProvider(this, new ViewModelProvider.Factory() {
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new FacePhotoViewModel(...);
    }
}).get(FacePhotoViewModel.class);
```

---

## 生命周期注意

| 规则 | 原因 |
|------|------|
| `onDestroy` 调 `recognizeViewModel.destroy()` | 释放 native 引擎 |
| 勿在 ViewModel 持有 Activity Context | 泄漏 |
| 相机回调转 LiveData `postValue` | 非主线程安全 |

---

## 联调检查清单

- [ ] `mainInitCode` 为 0 表示引擎 OK
- [ ] `initFinished` true 后再 `registerFromFile`
- [ ] 施工列表无数据：查 ViewModel 筛选条件是否过窄
- [ ] 旋转屏幕：ViewModel 保留，相机需 Activity 重绑
