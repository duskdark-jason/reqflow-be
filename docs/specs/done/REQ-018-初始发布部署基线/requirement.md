# 初始发布部署基线后端需求说明

## 背景

需求管理平台已基本成型，当前仓库仍保留多轮建设过程中的历史 spec、一次性 SQL 增量脚本和根路径部署配置。初始发布前需要把仓库收敛为干净、可部署、可交接的基线，减少部署人员误用历史脚本或历史过程文档的风险。

## 目标

- 清理后端历史 spec，仅保留当前发布基线记录和占位文件。
- 清理数据库脚本目录，保留初始部署必要的 schema、菜单、Quartz 和当前发布基线脚本。
- 后端应用增加稳定 context-path，避免与同域其他应用冲突。
- 同步长期文档，说明初始发布访问路径和脚本保留口径。

## 可行性评估

- 评估结论：可继续设计。
- 主要风险：删除一次性 SQL 前必须确认最终 `req_platform_schema.sql` 已包含当前表结构；context-path 变更需要前端生产 API 前缀同步；MCP 安装地址必须保留后端 context-path 且不能混入前端项目名。
- 需需求人补充或调整：无。本次默认后端访问前缀为 `/reqflow-api`，前端访问项目名为 `/reqflow/`。
- 是否允许继续生成需求设计：是，用户已明确要求清理并调整为初始发布部署状态。

## 范围

本次包含：

- 删除 `docs/specs/active` 与 `docs/specs/done` 中历史需求目录，保留 `.gitkeep` 和当前发布基线记录。
- 删除 `docs/db/sql/req_platform_req*.sql` 中仅用于历史增量、迁移或测试设置的脚本。
- 保留 `docs/db/sql/req_platform_schema.sql`、`docs/db/sql/req_platform_menu.sql`、`docs/db/sql/quartz.sql`、`docs/db/sql/ry_20260417.sql` 作为初始部署可用脚本。
- 修改 `server.servlet.context-path` 为 `/reqflow-api`，保留发布上传目录 `/reqflow/uploadPath`。
- 补充 MCP 地址生成回归测试，确保外部 MCP endpoint 为 `/reqflow-api/requirement/mcp`。
- 更新数据库、模块和运行文档中的脚本路径、访问路径说明。

本次不包含：

- 不修改业务表结构、Mapper 查询、接口返回字段或权限模型。
- 不执行生产数据库迁移。
- 不合并、不推送、不删除远端分支。

## 影响范围

- 接口/API：是，所有后端 HTTP 接口外部访问路径增加 `/reqflow-api` 前缀。
- 数据库/SQL：是，清理脚本文件目录，但不改变最终基线 schema 的表结构。
- 权限/菜单：否，保留现有菜单与权限初始化脚本。
- 页面/交互：否，页面由 companion 前端 spec 处理访问项目名。
- 导出/异步/任务：否。

## 契约与数据口径

- 接口路径和方法：Controller 内部路径不变，外部访问路径由 `/reqflow-api` + 原路径组成。
- 请求参数：不变。
- 响应字段：不变。
- 数据粒度：不变；本次只调整部署路径和仓库交付物。

## 验收标准

- AC-001：`application.yml` 中后端 context-path 为 `/reqflow-api`，上传目录使用 Linux 发布路径 `/reqflow/uploadPath`。
- AC-002：历史 spec 目录已清理，`docs/specs/active` 不再残留历史未办结需求。
- AC-003：SQL 目录只保留初始发布需要的基线脚本、菜单脚本、Quartz 脚本和 RuoYi 基线脚本。
- AC-004：`docs/db`、模块文档和运行说明不再指向已删除的历史增量 SQL。
- AC-005：后端构建和 harness 文档检查通过。
- AC-006：MCP 安装地址生成保留后端 `/reqflow-api` context-path，且不会拼入前端 `/reqflow/` 项目名。

## Companion 关联

- companion spec：`../reqflow-ui/docs/specs/active/REQ-018-初始发布部署基线`
- 关联分支：`chore/req-018-release-baseline`

## 客户与分支

- 目标客户：通用
- 基线分支：main
- 任务分支：chore/req-018-release-baseline

## 约束与假设

- 初始发布以 `docs/db/sql/req_platform_schema.sql` 作为需求平台表结构基线；历史增量脚本不再作为新环境执行入口。
- context-path 采用 `/reqflow-api`，与前端 `/reqflow/` 形成同域部署下的清晰分工；MCP 客户端调用后端 `/reqflow-api/requirement/mcp`，不经过前端静态项目名前缀。
