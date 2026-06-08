# Octopus Mobile

Octopus Mobile 是 `octopus` 的 Android 原生管理端，使用 Kotlin、Jetpack Compose 和 Miuix 构建。它面向手机端管理场景，提供服务连接、登录、统计看板、渠道、分组、模型价格、API Key、日志和设置等常用管理能力。

## 功能范围

- 服务地址配置与登录鉴权
- 首页统计概览、趋势图和排行
- 渠道列表、创建、编辑、启停、删除和模型拉取
- 分组列表、创建、编辑、删除和渠道成员配置
- 模型价格列表、创建、编辑、删除和服务端价格刷新
- API Key 列表、创建、编辑、删除和复制
- 日志列表、搜索、分页加载和清空
- 设置页、版本信息、模型价格刷新和渠道同步
- 浅色/深色/跟随系统主题

## 技术栈

- Kotlin + Jetpack Compose
- Hilt
- Retrofit + OkHttp + Kotlinx Serialization
- DataStore + AndroidX Security
- Miuix UI / Preference / Icons
- Coil

## 项目结构

```text
app/src/main/java/com/elykia/octopus
├── core
│   ├── common          # 通用结果类型、Dispatcher 抽象
│   ├── data
│   │   ├── local       # DataStore、加密会话、会话状态
│   │   ├── model       # API 与页面共享数据模型
│   │   ├── remote      # Retrofit API、网络执行器、URL 解析
│   │   └── repository  # 应用与管理端数据仓库
│   ├── designsystem    # 主题、组件、品牌、图标和格式化工具
│   └── di              # Hilt 依赖注入
├── feature
│   ├── app             # 应用壳、启动状态、主导航
│   ├── apikey          # API Key 页面
│   ├── auth            # 登录页面与登录状态
│   ├── channel         # 渠道页面与状态
│   ├── group           # 分组页面、状态和请求构建逻辑
│   ├── home            # 首页看板状态
│   ├── log             # 日志页面与状态
│   ├── model           # 模型价格页面与状态
│   └── setting         # 设置页面与状态
└── navigation          # 顶层路由和主 Tab 定义
```

## 本地验证

Windows:

```powershell
.\gradlew.bat testReleaseUnitTest
.\gradlew.bat assembleRelease
```

Linux/macOS:

```bash
./gradlew testReleaseUnitTest
./gradlew assembleRelease
```

Release APK 输出位置：

```text
app/build/outputs/apk/release/app-release-unsigned.apk
```

## 发布构建

```powershell
.\gradlew.bat assembleRelease
```

当前 release 包仍使用默认未签名输出，正式发布前需要补充签名配置、release 日志策略和混淆压缩策略。

## 开发说明

- 业务页面放在各自的 `feature/*` 包中，`feature/app` 只保留应用壳和导航。
- UI 颜色优先从 `core/designsystem/OctopusTones.kt` 和 Miuix theme 获取，避免页面内散落硬编码色值。
- 分组更新请求由 `feature/group/GroupUpdateRequestBuilder.kt` 统一构建，并有单元测试覆盖。
- 网络请求通过 `NetworkExecutor` 统一转换为 `AppResult`，页面层应优先展示明确的错误反馈。

## 后续优化方向

- 继续收窄各页面 ViewModel 的状态与操作边界。
- 收紧 release 签名、混淆压缩和明文流量策略。
- 完善增删改操作的 loading、错误提示和成功反馈。

## CI

仓库包含 Android GitHub Actions workflow，会在推送和 PR 时执行 release 单元测试、release 构建并上传 release 构建产物。
