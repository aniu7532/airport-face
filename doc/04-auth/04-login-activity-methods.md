# LoginActivity 方法索引

> 源码：`ui/activity/LoginActivity.java`（约 1300+ 行）  
> 职责：**零信任 VPN → 后台登录 → 四步初始化 → 通行证同步 → 人脸注册 → 跳转查验页**

---

## 生命周期

| 方法 | 说明 |
|------|------|
| `onCreate` | DataBinding、权限、运维抽屉入口、自动 `startLogin`、初始化 `LongTermPassDao` |
| `onResume` / `onPause` / `onDestroy` | 常规；`onDestroy` 取消对话框 |
| `onGlobalLayout` | 键盘弹出时滚动登录表单 |
| `onActivityResult` | 权限/激活等回调 |

---

## 用户交互

| 方法 | 触发 | 行为 |
|------|------|------|
| `onClick(View)` | 登录/取消/测试按钮 | 登录：零信任 `AsyncTask` → `initZeroTrust` → `login()`；取消：`finish` |
| `startLogin(int time)` | 自动登录 | 延迟 `time` ms 后触发登录逻辑 |
| `showProgressDialog` / `dismissProgressDialog` | 同步进度 | 全屏或进度条提示 |

---

## 零信任（深信服）

| 方法/回调 | 说明 |
|-----------|------|
| `onClick` 内 `AsyncTask` | 后台 `SFUemSDK.getInstance().initSDK` |
| `SFSetSpaConfigListener.onSetSpaConfig` | SPA 配置结果 |
| `SFAuthResultListener.onAuthSuccess` | 成功后调 `login()` |
| `onAuthFailed` / `onAuthProgress` | 失败提示 / 进度 |

详见 [01-zero-trust-vpn.md](./01-zero-trust-vpn.md)。

---

## 后台登录

| 方法 | 说明 |
|------|------|
| `login()` | `ApiUtils.post(UrlConstants.URL_login, json)` |
| `login` onSuccess | 解析 `Login` → 写 Token、`userId`、`InfoStorage`；`ThreadUtils.executeByCached` 跑初始化链 |
| `login` onFailure | Toast / 对话框提示 |

成功后续：`getMACDetail` → `getConfigInfo` → `getUserDetail` → 条件 `getLongPassCards` → `gotoActivity`。

详见 [02-backend-login-token.md](./02-backend-login-token.md)、[03-login-init-chain.md](./03-login-init-chain.md)。

---

## 初始化链（同步 OkGo）

| 方法 | 返回 | 失败影响 |
|------|------|----------|
| `getMACDetail()` | `boolean` | 中断链，提示设备未登记 |
| `getConfigInfo()` | `boolean` | type=5/6 查验配置缺失 |
| `getUserDetail(String userId)` | `boolean` | 用户信息缺失 |
| `getCheckMethod()` | void 异步 | 获取查验方式列表（辅助） |

---

## 通行证同步

| 方法 | 说明 |
|------|------|
| `getLongPassCards()` | 分页 `PAGE_SIZE=20` GET；下载 `register`+`photo`；`CountDownLatch` 等待 |
| `insertDataToLocalDb(List)` | `AsyncTask`：`Converters` → `LongTermPass` → `longTermPassDao.insert` |
| 进度 UI | `progressBar` + `WeakHandler` 更新 `textViewMessage` |

条件：首次 `isFirstStart` 或本地库空。非首次可能跳过全量，依赖周期增量。

---

## 人脸引擎

| 方法 | 说明 |
|------|------|
| `initFaceServer()` | `ViewModelProvider` 获取 `FacePhotoViewModel`，观察 `initFinished` |
| `registerFace()` | 入口，调 `registerFromFile` |
| `registerFromFile(File dir)` | `BatchRegisterCallback` 批量注册 `register/` 下图片 |

---

## Handler 消息

`WeakHandler` 根据 `message.what` 更新：

- 同步进度百分比
- 错误提示
- 跳转前收尾

（具体 `what` 常量见源码 `switch (message.what)` 块。）

---

## 成员变量速查

| 变量 | 用途 |
|------|------|
| `page` / `PAGE_SIZE` | 通行证分页 |
| `updatePage` / `UPDATE_PAGE_SIZE` | 增量更新分页（静态） |
| `deviceId` | MAC，来自 `DeviceUtils` |
| `infoStorage` | 配置持久化 |
| `longTermPassDao` | 通行证 DAO |
| `facePhotoViewModel` | 人脸注册 |
| `latch` | 多页下载同步 |

---

## 跳转目标

初始化成功后根据 `InfoStorage` 中 `devicesType`、`checkType` 等进入：

- `LivenessDetectJinActivity` / `Yuan` / `YuanAndJin`
- `RegisterAndRecognizeActivity`

并调用 `ArcFaceApplication.startPeriodicTask()`、`startUpDataToServer()`（若尚未运行）。

---

## 异常分支速查

| 场景 | 表现 | 文档 |
|------|------|------|
| 零信任失败 | 无法到达 `login()` | [01-zero-trust-vpn.md](./01-zero-trust-vpn.md) |
| login 401 | Token 未写入 | [02-backend-login-token.md](./02-backend-login-token.md) |
| MAC 未登记 | `getMACDetail` false | [03-login-init-chain.md](./03-login-init-chain.md) |
| 同步中断 | 进度条停住 | [06-pass-card/01-pass-sync-full.md](../06-pass-card/01-pass-sync-full.md) |
| 注册失败 | `BatchRegisterCallback.onFinish` failed>0 | [10-face-engine/01-face-server.md](../10-face-engine/01-face-server.md) |

---

## 联调检查清单

- [ ] 账号密码正确且 VPN 已通
- [ ] `tenant-id` 与渠道一致
- [ ] 设备 MAC 已在后台登记
- [ ] 全量同步后 `long_term_pass` 表有数据
- [ ] `register/` 文件数 ≈ 人脸注册成功数
- [ ] `initFinished` LiveData 为 true 后再跳转
