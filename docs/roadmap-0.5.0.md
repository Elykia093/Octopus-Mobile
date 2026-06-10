# Octopus Mobile 0.5.0 Roadmap

## 目标

0.5.0 聚焦“发布自动化、版本迭代安全、Web 行为对齐”。0.4.0 已补齐可配置签名和 CI signed release 基础，下一步把手工 GitHub Release 流程收敛成可复用工具，同时开始按 Web 管理端高频操作补齐移动端的筛选、排序和反馈体验。

## 本轮起点

- `v0.4.0` 已通过本地 release 验证、tag 推送和 GitHub Release 发布。
- 发布过程仍需要人工拼 release notes、选择 APK 附件名、调用 GitHub CLI。
- 本地 GitHub CLI 可能不在 PATH，需要支持显式传入 `GH_CLI_PATH` 或脚本参数。

## 本轮已落地

- 新增 `scripts/create-github-release.ps1`。
- 脚本支持从 `CHANGELOG.md` 提取指定版本的发布说明。
- 脚本支持 `-DryRun`，发布前可预览 tag、仓库、附件和 release notes。
- 脚本支持 `-Signed`，用于真实签名 APK 产物命名。
- 新增 `scripts/prepare-release.ps1`，可统一更新 `versionCode`、`versionName` 和 `CHANGELOG.md` 日期，并支持 `-DryRun`。
- `scripts/create-github-release.ps1` 增加正式发版检查：工作区必须干净、tag 必须存在、Gradle 版本必须匹配、changelog 不能仍是 Unreleased、APK 和 GitHub CLI 必须可用。
- 新增 `Android Release` 手动 workflow：输入版本号后执行 release 单元测试、构建 APK、创建缺失 tag，并调用同一个 release 脚本发布 GitHub Release。
- Gradle release APK 产物命名固化为 `Octopus-Mobile-v<version>-signed/unsigned.apk`，release 脚本默认按该命名自动发现附件。
- 新增 1.0 Web/Mobile 覆盖基线，确认当前 Mobile 已覆盖 Web 参考源码中的 `/api/v1` 路径。
- 补齐 Mobile 设置页对 Web 设置项的标题、开关识别和校验规则。
- 对齐 Web SiteChannel 投影列表行为：默认加载 `include_history=true`，新增未正确配置、有请求历史、禁用模型筛选，以及名称、模型数量、异常优先排序。

## 0.5.0 剩余收口

1. 0.5.0 正式发布前运行 `prepare-release.ps1`，更新版本号和日期后创建 tag 与 Release。

## 后续候选

1. Web 行为审计：按 Home、Site、Projection、Channel、Group、Model、API Key、Log、Setting 模块核对搜索、筛选、反馈和批量操作体验。
2. Projection 批量模型操作：Web 支持选中模型后批量切换路由、启用、禁用；Mobile 当前仍以单模型操作为主。
3. Projection 面板偏好：Web 支持 compact/table sort 等细粒度偏好；Mobile 已有列表级排序，后续继续评估是否需要账号内模型排序。

## 验证基线

- `scripts/create-github-release.ps1 -Version 0.4.0 -DryRun`
- `scripts/prepare-release.ps1 -Version 0.5.0 -DryRun`
- GitHub Actions release workflow YAML parse check
- `git diff --check`
- `.\gradlew.bat testReleaseUnitTest`
- `.\gradlew.bat assembleRelease`
