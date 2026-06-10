# Octopus Mobile 0.4.0 Roadmap

## 目标

0.4.0 聚焦“正式分发与运维质量”。0.3.0 已补齐分组侧 Web 管理能力，下一阶段优先解决发布包可信度、CI 产物、操作反馈和页面状态边界，减少从功能可用到稳定分发之间的风险。

## 当前证据

- `0.3.0` 已完成分组置顶、分组预设、自动分组和健康检查等主要 Web 侧分组能力适配。
- README 在 `0.3.0` 后仍标注 release 包为默认 unsigned 输出。
- GitHub Actions 之前只上传 `app-release-unsigned.apk` 作为验证产物。
- AndroidManifest 已关闭明文流量，release build 已启用 minify 和 shrinkResources。

## 本轮已落地

- 支持通过 Gradle 属性或环境变量配置 release 签名。
- CI 支持从 `OCTOPUS_RELEASE_KEYSTORE_BASE64` 恢复临时 keystore，并配合签名 secrets 构建 release。
- CI release artifact 改为收集实际生成的 release APK，兼容 signed 和 unsigned 输出。
- `.gitignore` 增加本地签名文件忽略规则，避免误提交 keystore。
- README 和 CHANGELOG 增加 0.4.0 发布构建说明。

## 后续候选

1. GitHub Release 自动化：从 `CHANGELOG.md` 提取当前版本说明，自动创建 Release 并上传签名 APK。
2. 正式签名产物命名：统一 signed/unsigned APK 文件名，减少人工发版时的附件歧义。
3. 操作反馈收敛：统一增删改操作的 loading、成功、失败状态，优先覆盖站点、渠道、分组、API Key。
4. ViewModel 边界整理：拆分超大页面状态与动作，降低后续功能补齐时的回归风险。

## 验证基线

- `git diff --check`
- `python -c "import yaml, pathlib; yaml.safe_load(pathlib.Path('.github/workflows/android.yml').read_text(encoding='utf-8')); print('workflow yaml ok')"`
- `.\gradlew.bat testReleaseUnitTest`
- `.\gradlew.bat assembleRelease`
- `.\gradlew.bat :app:tasks -POCTOPUS_RELEASE_STORE_FILE=dummy --quiet` 应按预期失败，提示签名参数必须完整配置。
