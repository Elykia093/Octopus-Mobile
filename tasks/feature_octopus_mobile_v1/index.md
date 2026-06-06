# Task: Octopus Mobile V1

> **Type**: feature
> **Priority**: P0
> **Owner**: Codex
> **Status**: in_progress
> **Started**: 2026-04-13

> **Latest Update**: 2026-06-06 开始数据层第一阶段重构，按统计、渠道、分组、模型、日志、API Key、更新与数据导入导出拆分 Repository，验证口径调整为 release-only。

## Goal
Build the first Android native management app in `D:\Website\Octopus-Mobile` with Kotlin, Compose and miuix, targeting the current local `D:\Website\octopus` API.

## Progress

| Item | Status | Detail |
|------|--------|--------|
| API validation | done | `user/login`, `user/status`, main tabs, CRUD handlers confirmed |
| Android bootstrap | done | `miuix`、Gradle 9.1.0、AGP 8.9.1、Kotlin 2.3.20、Hilt 2.58 编译链已打通 |
| Core infrastructure | done | Theme、auth、storage、networking 已连通，并补强动态 base path / 401 / HTTP 错误消息 / 响应 code 判定 / 空响应泛型处理 |
| Feature screens | in_progress | Home、Channel、Group、Model、Log、Setting 已可进入，后续补完交互深度 |
| Verification | done | 本地 `assembleRelease` 已通过，`gradlew.bat -version` 在未配置 `JAVA_HOME` 时可自动探测 JDK，GitHub Actions 已切换为 release 构建与 release 产物上传 |
