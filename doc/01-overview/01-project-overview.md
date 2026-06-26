# 项目概览

## 产品定义

机场控制区 **竖屏通行证查验终端**：部署在闸机/立式设备，完成身份核验（人脸 + 证卡）、电子卡面展示、通行记录采集与后台同步。

## 技术栈摘要

| 层 | 技术 |
|----|------|
| UI | DataBinding、ViewBinding、XPopup、Paging3 |
| 人脸 | ArcSoft ArcFace（RGB+IR、活体、1:N，上限 30000） |
| 存储 | Room 2.4.3 + SQLCipher |
| 网络 | OkGo 3.0.4 + OkHttp 4.9.1 |
| 安全接入 | 深信服 SFUemSDK 零信任 + Bearer Token |
| 更新 | XUpdate（:xupdate-lib） |

## 版本与构建

- versionName `1.0.72`，versionCode `45092621`
- ABI `arm64-v8a`，minSdk 26
- APK：`YCJC_{flavor}_v{version}_{code}_{date}.apk`

## 源码规模（main）

| 包 | 文件数 | 文档目录 |
|----|--------|----------|
| util | 69 | 10-face-engine, 06-pass-card, 13-ops |
| ui | 50 | 04-auth, 05-check, 08-construction |
| widget | 30 | 13-ops, 09-serial |
| entity | 17 | 12-data |
| network | 4 | 11-network |

## 生产主路径 Activity

```
BootReceiver → LoginActivity
  → LivenessDetectJinActivity | LivenessDetectYuanActivity
  | LivenessDetectYuanAndJinActivity | RegisterAndRecognizeActivity
  → ConstructionWorkersActivity（支线）
```

## 非生产路径

`HomeActivity`、`FaceCompareActivity`、`RecognizeDebugActivity`、`FaceManageActivity`、`ActivationActivity`

## 相关文档

- [02-tech-stack-and-deps.md](./02-tech-stack-and-deps.md)
- [03-repo-layout.md](./03-repo-layout.md)
- [../03-architecture/02-arcface-application.md](../03-architecture/02-arcface-application.md)
