# 04 · 核心数据流与交互

## 1. 应用启动与登录流

```mermaid
sequenceDiagram
    participant Boot as BootReceiver
    participant Login as LoginActivity
    participant VPN as SangforSDK
    participant API as 业务 API
    participant App as ArcFaceApplication
    participant Face as FaceServer
    participant DB as YinchuanAirportDB

    Boot->>Login: 开机/startActivity
    Login->>VPN: initZeroTrust()
    VPN-->>Login: 隧道就绪
    Login->>API: vertical-client-login
    API-->>Login: accessToken + refreshToken
    Login->>Login: ApiUtils.setAccessToken()
    Login->>API: getUserDetail / getDeviceConfig
    Login->>API: page-pass（全量分页）
    API-->>Login: LongTermPass 列表
    Login->>DB: insert/update LongTermPass
    Login->>Face: registerFromFile（批量人脸注册）
    Login->>App: startUpDataToServer()
    Login->>App: startPeriodicTask()
    Login->>Login: gotoActivity() → 查验页
```

**关键数据**：

| 步骤 | 输入 | 输出 | 存储 |
|------|------|------|------|
| 登录 | 账号密码 | Token | ApiUtils 静态 + InfoStorage |
| 设备配置 | deviceId | areaName, interval | InfoStorage |
| 全量同步 | page=1..N | LongTermPass[] | Room |
| 人脸注册 | Pass.photoPath | FaceEntity | FaceDatabase + FaceServer 引擎 |

---

## 2. 查验成功流（刷卡+人脸）

以 LivenessDetectJinActivity（短距）为例，Yuan/YuanAndJin 读卡步骤不同但后续一致。

```mermaid
sequenceDiagram
    participant User as 用户
    participant Serial as SerialManage
    participant Act as LivenessDetectJinActivity
    participant DAO as LongTermPassDao
    participant Doc as Document2/3
    participant FH as FaceHelper
    participant FS as FaceServer
    participant Rec as LongTermRecordsDao
    participant App as ArcFaceApplication
    participant API as 业务 API

    User->>Serial: 刷卡
    Serial->>Act: onCardRead(cardNo)
    Act->>DAO: queryByCardNo(cardNo)
    DAO-->>Act: LongTermPass
    Act->>Act: checkCard() 校验有效期/区域/临时证
    Act->>Doc: 展示卡面（Glide 加密图）
    User->>FH: 人脸进入识别区
    FH->>FS: searchFace(feature) 1:1
    FS-->>FH: CompareResult(similarity)
    FH->>Act: onRecognized(result)
    Act->>Act: 比对 pass.nickname == face.userName
    Act->>Rec: insert LongTermRecords
    Note over App: 30s 定时任务
    App->>Rec: query 待上传记录
    App->>API: POST create-long + 图片
    API-->>App: success
    App->>Rec: delete 已上传记录
```

**临时证分支**：走 TemporaryCardRecords + create-temporary API。

**纯人脸模式**（RegisterAndRecognizeActivity）：跳过刷卡，直接 1:N searchFace → 反查 LongTermPass。

---

## 3. 通行证增量同步流

```mermaid
flowchart TD
    A[startPeriodicTask 触发] --> B{updateNext?}
    B -->|否| Z[跳过本轮]
    B -->|是| C[GET page-pass?page=N&size=20]
    C --> D{有数据?}
    D -->|是| E[upsert LongTermPass]
    E --> F[下载加密照片 ImageDownloader]
    F --> G[registerFromFile 新人脸]
    G --> H[page++]
    H --> C
    D -->|否| I[page=1, 等待 interval 分钟]
    I --> A
```

**并发控制**：`updateNext` 标志位防止重叠同步。

**关联键**：Pass.nickname → FaceEntity.userName → 引擎 faceId。

---

## 4. 通行记录上传流

```mermaid
flowchart TD
    A[30s 定时器] --> B{CAS isUploadingRecord}
    B -->|失败| Z[跳过]
    B -->|成功| C[查询 LongTermRecords 待上传]
    C --> D[查询 TemporaryCardRecords 待上传]
    D --> E{有记录?}
    E -->|否| F[释放 CAS]
    E -->|是| G[ImageUploader 上传抓拍图]
    G --> H[POST create-long / create-temporary]
    H --> I{成功?}
    I -->|是| J[delete 本地记录]
    I -->|否| K[保留待下轮重试]
    J --> F
    K --> F
```

**CAS 机制**：

```java
if (!isUploadingRecord.compareAndSet(false, true)) return;
try { /* 上传 */ } finally { isUploadingRecord.set(false); }
```

---

## 5. 人脸引擎数据流

```mermaid
flowchart LR
    subgraph input["输入"]
        RGB[RGB Camera Frame]
        IR[IR Camera Frame]
    end

    subgraph pipeline["FaceHelper 管线"]
        Detect[人脸检测 FT]
        Quality[质量过滤]
        Live[活体检测 FL]
        Feature[特征提取 FR]
        Filter[过滤器链]
    end

    subgraph engine["FaceServer"]
        Search1N[1:N Search]
        Search11[1:1 Compare]
        Register[Register]
    end

    subgraph storage["存储"]
        FaceDB[(FaceDatabase)]
        EngineMem[引擎内存特征库]
    end

    RGB --> Detect
    IR --> Live
    Detect --> Quality --> Live --> Feature --> Filter
    Filter --> Search1N
    Filter --> Search11
    Register --> FaceDB
    Register --> EngineMem
    FaceDB --> EngineMem
```

---

## 6. 加密图片加载流

```mermaid
flowchart LR
    A[Pass.photoPath 本地路径] --> B[EncryptedGlideFile]
    B --> C[EncryptedFileModelLoader]
    C --> D[EncryptedFileDecoder]
    D --> E[AESUtils 解密]
    E --> F[Bitmap → ImageView]
```

**涉及类**：`util/glide/*`、`ImageDownloader`（下载时加密存储）。

---

## 7. 施工人员查询流

```mermaid
sequenceDiagram
    participant UI as AccessRecordFragment
    participant VM as AccessRecordViewModel
    participant PS as AccessRecordPagingSource
    participant API as 业务 API

    UI->>VM: search()
    VM->>PS: Pager(flow)
    PS->>API: GET 施工通行记录（带筛选参数）
    API-->>PS: 分页数据
    PS-->>UI: PagingData → Adapter
```

**特点**：Kotlin + Coroutine + Paging3，网络调用在 PagingSource 内。

---

## 8. 配置数据流

### 查验模式切换

```
CustomDrawerPopupView
  → SPUtils.put("checkType", 0-3)
  → SPUtils.put("direction", 1/-1)
  → SPUtils.put("tipsLoc", 0-3)
  → restart LoginActivity
  → gotoActivity() 读 checkType 跳转
  → 各 Activity 读 direction/tipsLoc
```

### 识别参数

```
RecognizeSettingsActivity
  → PreferenceFragment
  → DefaultSharedPreferences
  → ConfigUtil.getRecognizeConfiguration()
  → RecognizeViewModel.init()
  → FaceHelper 应用阈值
```

---

## 9. 渠道差异数据流

| 差异点 | 影响流 |
|--------|--------|
| TENANT_PREFIX | URL 路径拼接 |
| TENANT_ID | HTTP Header |
| SUPPORTS_TEMPORARY_PASS=false | checkCard() 拒绝临时证；Document3 占位 UI |
| Document2/3 layout | 卡面展示字段/样式 |

---

## 10. 异常与离线分支

| 场景 | 行为 | 涉及模块 |
|------|------|----------|
| VPN 连接失败 | 阻塞登录，UI 提示 | M03 |
| Token 过期 | 当前无自动刷新（JobService 未启用） | M02,M10 |
| 网络不可用 | 记录本地保存，待上传队列累积 | M07 |
| 人脸引擎 init 失败 | 提示重新激活 | M04 |
| 读卡超时 | UI 提示重新刷卡 | M08 |
| 比对失败 | 音效 + 提示，不写记录 | M05 |
| 临时证渠道不支持 | checkCard() 直接拒绝 | M06,渠道 |

---

## 11. 状态机：查验页简化

```mermaid
stateDiagram-v2
    [*] --> Idle: Activity 启动
    Idle --> WaitingCard: 初始化完成
    WaitingCard --> CardRead: 刷卡成功
    CardRead --> Validating: 查库
    Validating --> CardRejected: 校验失败
    Validating --> ShowingCard: 校验通过
    ShowingCard --> WaitingFace: 展示卡面
    WaitingFace --> Recognizing: 人脸入框
    Recognizing --> MatchSuccess: 比对通过
    Recognizing --> MatchFailed: 比对失败
    MatchSuccess --> SavingRecord: 写 Room
    SavingRecord --> Idle: 重置
    MatchFailed --> WaitingFace: 重试
    CardRejected --> WaitingCard: 重新刷卡
```

**重构价值**：此状态机当前隐含在 Activity 的 if-else/flag 中，抽取后可测试、可复用。
