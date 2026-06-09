# 需求管理平台 MVP-lite 需求说明

## 背景

当前 workspace 已基于 RuoYi-Vue 拆分为 `reqflow-be` 和 `reqflow-ui` 两个子仓库。平台目标是提供一个轻量需求管理入口，帮助管理员、需求人员和开发人员维护项目、代码仓库、客户定制线、模块功能点、需求记录、执行包、MCP 读写入口和使用统计。

根目录已有设计文档与开发计划，本文件将本次需求落地到后端仓库 active specs，便于 harness 追踪计划、执行、验证和 review 状态。

## 目标

- 新增 `ruoyi-requirement` 后端业务模块，承载项目、仓库、客户线、模块、需求、执行包、MCP 和统计服务。
- 新增 MySQL 初始化脚本和菜单权限脚本，并可在本地 `ry-vue` 数据库执行。
- 暴露 RuoYi 风格 REST 接口，供前端页面调用。
- 使用确定性 Markdown/JSON 模板生成需求执行包，不调用大模型、不执行仓库命令。
- 提供 JSON-RPC 风格 MCP endpoint，只允许读写平台记录。

## 范围

本次包含：

- 数据库表：`req_project`、`req_repository`、`req_variant`、`req_module`、`req_demand`、`req_package_version`、`req_memory_index`、`req_activity_log`。
- 菜单与按钮权限：`req:project:*`、`req:repo:*`、`req:variant:*`、`req:module:*`、`req:demand:*`、`req:package:*`、`req:stats:view`。
- 后端模块、domain、mapper、service、controller、template、mcp、statistics。
- 本地测试配置使用 MySQL 账号 `root`、密码 `123456`，Redis 使用本地服务。

本次不包含：

- 克隆代码仓库、执行 shell、自动建分支或调用大模型。
- Markdown 富文本编辑器依赖。
- 生产部署配置和真实用户权限矩阵细化。

## 影响范围

- 接口/API：是，新增 `/requirement/**` REST 接口和 `/requirement/mcp`。
- 数据库/SQL：是，新增 8 张需求平台表和菜单权限脚本。
- 权限/菜单：是，新增需求管理菜单、统计菜单和按钮权限。
- 页面/交互：是，前端 companion spec 负责页面实现。
- 导出/异步/任务：否，MVP-lite 不实现导出和异步任务执行。

## 契约与数据口径

- 接口路径和方法：RuoYi CRUD 采用 `GET /list`、`GET /{id}`、`POST`、`PUT`、`DELETE /{ids}`；需求状态流转采用 `POST /requirement/demand/{demandId}/status/{status}`；执行包采用版本追加接口；MCP 采用 `POST /requirement/mcp`。
- 请求参数：列表查询使用 query 参数，详情和状态使用 path 参数，新增修改和保存包内容使用 JSON body。
- 响应字段：使用 RuoYi `AjaxResult` 和 `TableDataInfo`，业务对象字段与 SQL 表字段一一对应。
- 数据粒度：需求列表一行代表一个 `req_demand`；执行包列表一行代表一个 `req_package_version`；统计项目排行一行代表一个项目聚合。

## 验收标准

- AC-BE-001：本地 MySQL 可执行 `sql/req_platform_schema.sql` 和 `sql/req_platform_menu.sql`。
- AC-BE-002：`mvn -pl ruoyi-requirement -am test` 通过，覆盖需求状态流转和模板渲染。
- AC-BE-003：`mvn -pl ruoyi-admin -am -DskipTests package` 通过。
- AC-BE-004：后端提供计划中 `/requirement/**` 和 `/requirement/mcp` 接口。
- AC-BE-005：MCP 工具仅写平台表，不执行 Git、shell、clone、branch 或文件系统命令。

## Companion 关联

- companion spec：`../../../../reqflow-ui/docs/specs/active/2026-06-09-REQ-PLATFORM-MVP-lite`
- 关联分支：`feature/REQ-PLATFORM-MVP-lite`

## 客户与分支

- 目标客户：通用
- 基线分支：main
- 任务分支：feature/REQ-PLATFORM-MVP-lite

## 约束与假设

- 数据库名按当前 RuoYi 默认使用 `ry-vue`。
- 当前为内网开发环境，测试配置可直接写入本地数据库和 Redis 连接信息。
- 根目录设计和开发计划保留为源计划，子仓库 active specs 承接执行追踪。
