# 四渠道 Document2 / Document3 与 layout 差异

基类：

- `AbstractDocument2` — 长期证
- `AbstractDocument3` — 临时证

各渠道在 `app/src/{flavor}/` 下提供 **同名** `Document2.java`、`Document3.java` 及 `res/layout/document2.xml`、`document3.xml`。

---

## 总览矩阵

| 渠道 | 长期证 Document2 | 临时证 Document3 | 临时证支持 |
|------|------------------|------------------|------------|
| 洛阳 luoyang | 竖版 220×380，区域徽章网格 | 横版 380×295 定制临时证 | ✅ |
| 银川 yinchuan | 竖版 220×380，经典机场头图 | 竖版完整临时证 | ✅ |
| 重庆 chongqing | 同银川代码结构 | 同银川代码结构 | ✅ |
| 石河子 shihezi | 横版 380×240，ConstraintLayout | 横版 380×240 临时证 | ✅ |

---

## Document2（长期证）

### 洛阳 luoyang

**Java 特点**

- `AREA_BADGE_TEXT_COLOR = #8B2332`，`AREA_COLUMNS = 4`
- `DocumentCardUiHelper.bindAreaBadgeGrid(access_area_badges, areaDisplayCode, luoyang_area_badge_outline, ...)`
- 有效期：`formatValidityPeriod(startDate, expiryDate)`

**layout `document2.xml`**

- 尺寸：**220dp × 380dp** 竖版
- 背景：`@drawable/luoyang_pass_long_term_bg`
- 左照片 88×118 + 右侧姓名/编号/单位/期限（期限色 `#8B2332`）
- **`access_area_badges`**：纵向 `LinearLayout` 动态徽章
- `img_color`、`access_area`、`faceSimilarLayout`：**visibility=gone**（占位兼容）

### 银川 yinchuan & 重庆 chongqing

**Java**：二者 **代码相同**（仅 flavor 资源中机场名等不同）。

- 控件：`access_area`（TextView）、`img_color`、`faceSimilar`
- 区域：`access_area.setText(areaDisplayCode)`
- 色条：`templateType=="2"` 或编号以 `C`/`B` 开头 → `yellow_stripes`
- 期限：直接 `expiryDate`（非 formatValidityPeriod）
- 相似度：空/0/0.0 则隐藏文案

**layout `document2.xml`**

- 尺寸：**220dp × 380dp**
- 背景：`card_bacground`
- 顶栏：机场 logo + `@string/channel_airport_name` / `_en`
- **`img_color`**：默认 `img_blue_stripes`，可换黄条
- 左 **`access_area`** 竖排区域字母 + 右 **`card_img`** 99×132
- **`faceSimilarLayout`**：显示「相似：」+ 值
- 表格式字段：姓名、编号、单位、期限（带中文标签）

> 银川与重庆 layout 文件结构一致；差异在 flavor `strings.xml` 等资源。

### 石河子 shihezi

**Java**

- `access_area_badges` + `bindAreaBadges(..., shihezi_area_badge_blue)`
- 有效期：`formatValidityPeriod`

**layout `document2.xml`**

- 尺寸：**380dp × 240dp** 横版
- 背景图：`shihezi_pass_long_term_bg`（底层 ImageView fitXY）
- **ConstraintLayout**：照片左下 104×138；右侧 label+字段（单位/编号/期限带 `@string/label_*`）
- **`access_area_badges`**：底部横向徽章区

---

## Document3（临时证）

### 洛阳 luoyang

**Java**

- 完整实现 `bindCardContent()`，展示编号、姓名、区域、单位、引领人及有效期。
- `updatePage()` 应用状态盖章并调用 `loadTemporaryCardPhoto` 加载照片。
- 二维码 `or_code` 隐藏，右侧仅展示人像。

**layout `document3.xml`**

- **380×295** 横版，背景 `luoyang_pass_temp_bg`。
- 左侧分行展示证件字段，右侧为照片框。
- `or_code`、`img_color`、`lead_people2`、`faceSimilarLayout` 隐藏。

### 银川 yinchuan & 重庆 chongqing

**Java**：二者相同。

- 字段：陪同单位、lead_people1/2、img_color（templateType==2 → yellow_stripes）
- 通行人、区域、单位、期限 `startDate + "-" + expiryDate`
- `faceSimilarLayout`：无相似度时 **GONE**

**layout `document3.xml`**

- 竖版结构与 document2 同系列（220×380、`card_bacground`、机场头图）
- 临时证字段：passers_by、access_area、unit、expiryDate、lead_people1/2、leading_person_unit
- `or_code` 二维码、`card_img` 照片、`faceSimilarLayout`

### 石河子 shihezi

**Java**

- 通行人/陪同：`formatPersonWithIdCode(name, idCode)`
- 第二陪同：`label_escort2` / `lead_people2` 按 `leadingPeople2` 是否为空显隐
- 陪同单位：`formatEscortUnits`
- 区域：`bindAreaBadges(..., shihezi_area_badge_orange)`

**layout `document3.xml`**

- **380×240** 横版，`shihezi_pass_temp_bg`
- 右上 `or_code` 46×46
- ConstraintLayout：照片、多 label 字段、底部 `access_area_badges`

---

## 抽象基类公共行为

### AbstractDocument2

- `readArguments`：idCode、passid、photo、nickname、companyName、startDate、expiryDate、templateType、areaDisplayCode、status、faceSimilar
- `updatePage`：`bindCardContent` → `DocumentCardSupport.applyStatusSeal` → `loadLongTermCardPhoto`

### AbstractDocument3

- 额外：applyId、leadingPeople*、leadingPeopleUnit 等
- 同样状态盖章 + 照片加载逻辑（见 `DocumentCardSupport`）

---

## UI 风格分类

```text
A 类（洛阳）     : 长期证竖版徽章网格 + 临时证横版定制卡面
B 类（银川/重庆）: 竖版经典卡片 + 竖排 access_area + 相似度 + 黄蓝条
C 类（石河子）   : 横版新证 + ConstraintLayout + 横向徽章 + 完整临时证
```

---

## 代码复用说明

| 关系 | 说明 |
|------|------|
| 银川 Document2 ≡ 重庆 Document2 | Java 源码逻辑一致 |
| 银川 Document3 ≡ 重庆 Document3 | Java 源码逻辑一致 |
| 洛阳 Document3 | 独立横版实现，隐藏二维码与第二引领人，展示定制字段 |
| 石河子 | Document2/3 均横版，徽章 API 与洛阳不同（`bindAreaBadges` vs `bindAreaBadgeGrid`） |

---

## 布局尺寸对照

| layout | 洛阳 | 银川/重庆 | 石河子 |
|--------|------|-----------|--------|
| document2 | 220×380 竖 | 220×380 竖 | 380×240 横 |
| document3 | 380×295 横 | 220×380 竖 | 380×240 横 |
