# 通行证卡面 UI

## 类体系

```
AbstractDocument2（长期证基类）
  └── Document2（各渠道实现）
        └── document2.xml（各渠道布局）

AbstractDocument3（临时证基类）
  └── Document3（各渠道实现）
        └── document3.xml（各渠道布局）

DocumentCardSupport    — 状态章 overlay、Glide 加密图
DocumentCardUiHelper   — 二维码生成、区域标签排版
```

## 渠道定制文件

| 渠道 | Document2 | Document3 | 布局 |
|------|-----------|-----------|------|
| yinchuan | `app/src/yinchuan/java/.../Document2.java` | `Document3.java` | `document2.xml`、`document3.xml` |
| chongqing | `app/src/chongqing/java/.../` | 同上 | 同上 |
| shihezi | `app/src/shihezi/java/.../` | 同上 | 同上 |
| luoyang | `app/src/luoyang/java/.../` | 同上 | 仅长期证布局 |

## AbstractDocument2 职责

- 绑定 `LongPassCard` / `LongTermPass` 数据到 View
- 加载 AES 加密头像（Glide + `EncryptedFileDecoder`）
- 显示证件状态章（正常/过期/注销等）
- 区域标签渲染（`DocumentCardUiHelper`）
- 二维码生成（证件编号）

## AbstractDocument3 职责

- 绑定临时证数据
- 显示引领人信息（`LeadingPeople` 列表）
- 临时证有效期、通行区域
- 洛阳渠道不加载此 Fragment

## DocumentCardSupport

| 功能 | 说明 |
|------|------|
| 状态 overlay | 根据 `status`、`expiryDate` 显示「已过期」「已注销」等章 |
| 加密图加载 | `Glide.with().load(EncryptedGlideFile)` |
| 多引领人 | 支持 `LeadingPeople` 列表展示 |

## 证件提示位置

`SPUtils.tipsLoc` 控制卡面提示文字位置：

| 值 | 位置 |
|----|------|
| 0 | 左下 |
| 1 | 左上 |
| 2 | 右下 |
| 3 | 右上 |

在 `CustomDrawerPopupView` 中配置。

## 渠道 UI 差异示例

| 渠道 | 差异 |
|------|------|
| 银川 | 默认 Logo `airport_logo.xml` |
| 重庆 | PNG Logo `airport_logo.png` |
| 石河子 | 定制背景 `shihezi_pass_*_bg.png`、区域 badge 样式 |
| 洛阳 | 竖版长期证 `luoyang_pass_long_term_bg.png`，无临时证 |

## 加密图片加载链路

```
服务端 path → UrlConstants.fileStreamUrl(path)
→ ImageDownloader 下载 + AES 加密存本地
→ EncryptedGlideFile 封装本地路径
→ SecureGlideModule 注册 EncryptedFileModelLoader
→ EncryptedFileDecoder 解密 → Bitmap 显示
```

相关类位于 `util/glide/` 包。

## Idle 页面

| Fragment | 布局 | 场景 |
|----------|------|------|
| `Document1` | `document1.xml` | 刷卡模式等待刷卡 |
| `Document11` | `document11.xml` | 纯人脸模式等待识别 |
