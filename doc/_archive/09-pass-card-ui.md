# 通行证卡面 UI

## 类与布局体系

```
AbstractDocument2（长期证逻辑基类）
  └── Document2（各 flavor 数据绑定）
        └── res/layout/document2.xml（各 flavor 布局）

AbstractDocument3（临时证逻辑基类）
  └── Document3（各 flavor）
        └── res/layout/document3.xml

DocumentCardSupport   — 状态 overlay、Glide 加密图、多引领人
DocumentCardUiHelper  — 二维码、区域标签、日期格式化
```

## 证件类型与 Fragment

| type 值 | 含义 | Fragment | 洛阳 |
|---------|------|----------|------|
| 0 | 长期证 | Document2 | ✅ |
| 1 | 临时证 | Document3 | ❌ 禁用 |

## templateType 模板色

| 值 | 含义 | 常见 UI |
|----|------|---------|
| 1 | 蓝色模板 | 长期证默认 |
| 2 | 黄色模板 | 部分临时/施工证 |

## 状态章（DocumentCardSupport）

根据通行证字段叠加半透明状态章：

| 条件 | 展示 |
|------|------|
| `status == 2` | 已注销 |
| 超过 `expiryDate` | 已过期 |
| `isWithhold == true` | 暂扣 |
| `isWithdraw == true` | 已撤回 |
| `isBlacklist == true` | 黑名单 |

## 区域标签

- 数据源：`areaDisplayCode`、`areaCodes`、`areaIds`
- `DocumentCardUiHelper` 生成多行 `TextView` + badge drawable
- 石河子/洛阳有渠道专属 badge 样式（`shihezi_area_badge_*`、`luoyang_area_badge_*`）

## 二维码

- 内容通常为 `idCode` 或业务约定字符串
- `DocumentCardUiHelper` 使用 ZXing `MultiFormatWriter` 生成 `Bitmap`
- 显示在卡面指定 `ImageView`

## 加密头像加载

```java
// 典型用法
Glide.with(context)
    .load(new EncryptedGlideFile(localEncryptedPath))
    .into(imageView);
```

链路：

1. `ImageDownloader` 从 `fileStreamUrl(photo)` 下载
2. AES 加密存 `{externalFilesDir}/faceDB/{id}.jpg`
3. `SecureGlideModule` 注册 `EncryptedFileModelLoader`
4. `EncryptedFileDecoder` 解密渲染

## tipsLoc 提示位置

`SPUtils.tipsLoc`（0~3）控制卡面 **状态提示条** 在屏幕四角的位置，在 `LivenessDetect*Activity` 内 `switch(tipsLoc)` 设置 `Gravity`。

## 渠道 layout 差异要点

| 渠道 | 长期证 layout 特点 |
|------|-------------------|
| yinchuan | 标准横版字段排列 |
| chongqing | 江北机场 Logo、字段 label |
| shihezi | 定制背景图、区域 badge、长期证专用 label |
| luoyang | **竖版**长期证、`luoyang_pass_long_term_bg` |

洛阳 `document3.xml` 可能存在但运行时不加载 Document3。

## Idle 页

| Fragment | 布局 | 使用场景 |
|----------|------|----------|
| `Document1` | document1.xml | 刷卡模式等待 |
| `Document11` | document11.xml | 纯人脸模式等待 |

## 多引领人展示

`LongTermPass.leadingPeople` JSON → `LeadingPeople[]`：

- `AbstractDocument3` 展示引领人列表（姓名、卡号）
- 临时证刷卡流程需逐个校验引领人长期证

## 相关文档

- 刷卡流程 → [07-liveness-detect-flow.md](./07-liveness-detect-flow.md)
- 渠道资源 → [02-product-flavors.md](./02-product-flavors.md)
- 实体字段 → [17-entity-models.md](./17-entity-models.md)
