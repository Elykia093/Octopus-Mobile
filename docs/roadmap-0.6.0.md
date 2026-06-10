# Octopus Mobile 0.6.0 Roadmap

## 目标

0.6.0 聚焦 Projection/SiteChannel 深水区，把 Web 管理端账号面板里的批量模型操作、分组范围操作和更细的反馈状态逐步迁到移动端。

## 本轮起点

- 0.5.0 已正式发布，API 覆盖基线确认 Mobile 覆盖 Web `/api/v1` 路径。
- Mobile 已有 SiteChannel 历史加载、快速筛选、列表排序、单模型路由、单模型启停、source keys、投影渠道设置和手动模型管理。
- Web 仍领先的主要交互是：选中模型后的批量路由切换、批量启用/禁用、账号内分组过滤、表格排序和 compact 偏好。

## 本轮已落地

- 新增 Projection 分组级批量路由入口。
- 新增 Projection 分组级批量启用和批量禁用入口。
- 批量路由跳过禁用、空名称、重复名称和已是目标路由的模型。
- 批量启停只提交状态会变化的模型。
- 新增账号内分组范围切换和模型搜索。
- 搜索结果会收窄模型列表，批量操作跟随当前可见模型范围。
- 新增账号内模型排序：模型名、分组名、端点格式、最近请求。
- 新增 Projection advanced settings 的参数覆盖 JSON 对象校验。

## 后续候选

1. Site workflows 审计：站点同步、签到、导入、归档站点、代理模式、账号编辑。

## 验证基线

- `git diff --check`
- `.\gradlew.bat testReleaseUnitTest`
- `.\gradlew.bat assembleRelease`
