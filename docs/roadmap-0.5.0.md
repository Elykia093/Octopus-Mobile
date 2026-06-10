# Octopus Mobile 0.5.0 Roadmap

## 目标

0.5.0 聚焦“发布自动化与版本迭代安全”。0.4.0 已补齐可配置签名和 CI signed release 基础，下一步把手工 GitHub Release 流程收敛成可复用工具，降低每个版本发布时的重复操作和遗漏风险。

## 本轮起点

- `v0.4.0` 已通过本地 release 验证、tag 推送和 GitHub Release 发布。
- 发布过程仍需要人工拼 release notes、选择 APK 附件名、调用 GitHub CLI。
- 本地 GitHub CLI 可能不在 PATH，需要支持显式传入 `GH_CLI_PATH` 或脚本参数。

## 本轮已落地

- 新增 `scripts/create-github-release.ps1`。
- 脚本支持从 `CHANGELOG.md` 提取指定版本的发布说明。
- 脚本支持 `-DryRun`，发布前可预览 tag、仓库、附件和 release notes。
- 脚本支持 `-Signed`，用于真实签名 APK 产物命名。

## 后续候选

1. 版本准备脚本：统一更新 `versionCode`、`versionName` 和 `CHANGELOG.md` 日期。
2. Release checklist：在脚本中检查 git 工作区、tag 是否存在、当前版本是否匹配。
3. GitHub Actions 手动发版 workflow：通过 workflow_dispatch 输入版本号，自动构建并创建 Release。
4. Signed APK 命名固化：让 Gradle 输出文件名直接带版本号和 signed/unsigned 标识。

## 验证基线

- `scripts/create-github-release.ps1 -Version 0.4.0 -DryRun`
- `git diff --check`
