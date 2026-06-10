# 需求管理平台后端模块 Harness

## 业务目的

本模块承接“需求管理”菜单下的后端能力，支撑项目接入、需求提交、Agent 交接资料、MCP 调用、分支知识库和使用统计。前端 companion 仓库通过 `reqflow-ui/src/api/requirement/**` 调用本模块接口。后续需求变更时，必须从菜单和子菜单定位到对应 Controller、Service、Mapper 和权限标识。

## 菜单与功能入口

| 菜单目录 | 子菜单/页面 | 功能说明 | 前端文件 | API 封装 | 后端接口与权限 | 后端核心文件 |
|---|---|---|---|---|---|---|
| 需求管理 | 项目管理 | 项目列表、项目维护入口、项目初始化状态 | `reqflow-ui/src/views/requirement/project/index.vue`、`maintain.vue` | `reqflow-ui/src/api/requirement/project.js`、`projectInit.js` | `/requirement/project/**`，`req:project:*`；`/requirement/project/init/**`，`req:project:*` | `ReqProjectController`、`ReqProjectInitController`、`ReqProjectServiceImpl`、`ReqProjectInitServiceImpl` |
| 需求管理 | 项目接入中心 | 查看项目仓库、项目分支、索引批次、模块知识和初始化指令 | `reqflow-ui/src/views/requirement/project/detail.vue` | `reqflow-ui/src/api/requirement/project.js`、`index.js` | `/requirement/project/init/{projectId}`，`req:project:query`；`/requirement/index/**`，`req:index:*` | `ReqProjectInitController`、`ReqIndexController`、`ReqRepositoryIndexServiceImpl` |
| 需求管理 | 分支知识库详情页签 | 按项目分支查看模块知识、索引批次和初始化指令 | `reqflow-ui/src/views/requirement/project/knowledge.vue` | `reqflow-ui/src/api/requirement/index.js`、`project.js` | `/requirement/index/module/tree`，`req:index:list`；`/requirement/index/batch/list`，`req:index:list` | `ReqIndexController`、`ReqRepositoryIndexServiceImpl`、`ReqIndexModuleMapper`、`ReqRepositoryIndexBatchMapper` |
| 需求管理 | 需求列表 | 需求新增、编辑、查询和影响面推荐 | `reqflow-ui/src/views/requirement/demand/index.vue`、`detail.vue` | `reqflow-ui/src/api/requirement/demand.js`、`index.js` | `/requirement/demand/**`，`req:demand:*`；`/requirement/index/impact/suggest`，`req:index:list` | `ReqDemandController`、`ReqDemandServiceImpl`、`ReqIndexController`、`ReqRepositoryIndexServiceImpl` |
| 需求管理 | 需求执行包 | 保存和读取需求、计划、执行报告、Review 报告等交接资料 | `reqflow-ui/src/views/requirement/package/index.vue` | `reqflow-ui/src/api/requirement/package.js` | `/requirement/package/**`，`req:package:*` | `ReqPackageController`、`ReqPackageServiceImpl`、`ReqPackageVersionMapper` |
| 需求管理 | MCP 管理 | 管理人员 MCP Key、MCP 地址和 Codex 配置片段 | `reqflow-ui/src/views/requirement/mcpKey/index.vue` | `reqflow-ui/src/api/requirement/mcpKey.js` | `/requirement/mcp/key/**`，`req:mcp:key:*`；`/requirement/mcp` | `ReqMcpKeyController`、`ReqMcpController`、`ReqMcpUserKeyServiceImpl`、`McpService` |
| 需求管理 | 使用统计 | 需求、项目、用户和状态统计 | `reqflow-ui/src/views/requirement/statistics/index.vue` | `reqflow-ui/src/api/requirement/statistics.js` | `/requirement/statistics/**`，`req:stats:view` | `ReqStatisticsController`、`ReqStatisticsService` |
| 需求管理 | 隐藏兼容能力 | 仓库、项目分支、人工模块兼容 CRUD，不作为左侧菜单独立入口 | `reqflow-ui/src/api/requirement/repository.js`、`variant.js`、`module.js` | 同前述 API 文件 | `/requirement/repository/**`、`/requirement/variant/**`、`/requirement/module/**`，`req:repo:*`、`req:variant:*`、`req:module:*` | `ReqRepositoryController`、`ReqVariantController`、`ReqModuleController` 及对应 Service/Mapper |

## 模块文件索引

| 类型 | 优先查看文件 | 说明 |
|---|---|---|
| 菜单与权限 SQL | `sql/req_platform_menu.sql` | 需求管理一级菜单、子菜单和按钮权限。 |
| 接口契约 | `docs/ai-harness/contracts/requirement-platform-api.md` | 后端接口、MCP resource、MCP tool、知识库和初始化契约。 |
| 领域入口 | `docs/domains/requirement-platform/README.md` | 后端业务边界和长期维护规则。 |
| 后端 Controller | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/` | HTTP 接口入口。 |
| 后端 Service | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/` | 核心业务规则。 |
| 数据访问 | `ruoyi-requirement/src/main/resources/mapper/requirement/` | Mapper XML 和 SQL。 |
| 领域对象 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/domain/` | `req_*` 表对应实体。 |
| MCP 能力 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | MCP resources、tools 和权限兜底。 |

## 接口与数据契约

- 相关契约文档：`docs/ai-harness/contracts/requirement-platform-api.md`。
- 相关领域入口：`docs/domains/requirement-platform/README.md`。
- 关键菜单脚本：`sql/req_platform_menu.sql`。
- 关键表结构：`sql/req_platform_schema.sql` 以及后续 `sql/req_platform_req*.sql` 增量脚本。

## 不变量

- 需求管理左侧菜单以“需求管理”为一级目录；项目仓库、项目分支和人工模块不再作为独立左侧菜单入口。
- 项目、仓库、项目分支和模块知识必须按项目分支隔离；不能把其他分支或旧项目级模块默认混入当前分支。
- 新增或编辑需求时，后端必须校验 `projectId + variantId` 属于同一项目，且项目分支已完成模块知识和仓库索引初始化。
- 人员 `X-MCP-Key` 只负责认证和权限；项目分支动作 `actionToken` 只负责动作上下文定位，二者不能互相替代。
- 项目初始化指令必须明确包含 `mcpServer: reqflow`、`toolName: publish_repository_index` 和 `mcpTool: reqflow.publish_repository_index`，确保接入项目能定位到指定 MCP server 的指定 tool。
- `/requirement/mcp` 必须支持 MCP `initialize -> notifications/initialized -> tools/list` lifecycle；新增 tool 时必须同步 `tools/list` 的描述和 `inputSchema`。
- `/requirement/mcp` 的协议级错误必须返回标准 JSON-RPC `error.code/error.message`，不能同时带 `result:null`；`tools/call` 内的业务错误必须返回 MCP tool result，并设置 `isError=true`。
- 项目接入初始化由平台存储和下发 harness 模板，后端不直接执行 Git、shell 或写用户本地文件。
- 用户可见系统名称统一为“统一需求流转平台”，但底层 RuoYi 包名、权限框架和通用基础能力保持兼容。

## 常见风险

- 修改项目初始化上下文时，必须同步检查前端项目管理、项目接入中心、分支知识库页签和需求表单的字段使用。
- 修改索引导入或影响面推荐时，必须确认项目分支、真实 Git 分支、索引批次和模块知识的粒度一致。
- MCP tools 新增或改名时，必须同步人员权限校验、接口契约、`tools/list` schema、前端文案和平台初始化指令。
- MCP lifecycle 或 HTTP Controller 调整时，必须用真实 HTTP 冒烟验证 `initialize`、`notifications/initialized`、`resources/templates/list` 和 `tools/list`，不能只看 Service 单测。
- MCP `tools/call` 错误响应调整时，必须覆盖成功、权限失败、参数校验失败和业务导入失败路径；接入项目侧不能再只看到 `Unexpected response type`，应能读到 `content` 中的业务错误。
- 项目接入初始化指令调整时，必须保证 agent 能先识别 reqflow MCP skill，调用 `get_harness_template` 写入本地 harness，再运行 `check-docs.sh`、`check-harness.sh init`，最后才发布索引和登记初始化结果。
- 索引表迁移不完整时，`publish_repository_index` 必须返回指向 `sql/req_platform_req007_index_tables.sql` 的友好业务错误，不能把 `Table ... doesn't exist` 原样作为最终结论。
- 菜单权限调整时，必须同时检查 `sql/req_platform_menu.sql`、Controller `@PreAuthorize` 和前端按钮权限。

## 验证建议

- 最低文档门禁：`sh scripts/check-docs.sh && sh scripts/check-harness.sh init`。
- 后端契约或 Service 变更：运行 `mvn -pl ruoyi-requirement -am test`，必要时补指定测试类。
- 后端打包验证：运行 `mvn -pl ruoyi-admin -am -DskipTests package`。
- MCP 协议变更：在后端启动后用 `curl` 或 MCP 客户端验证 `initialize -> notifications/initialized -> resources/templates/list -> tools/list`，并确认工具列表含 `publish_repository_index`。
- MCP 项目接入初始化变更：验证 `resources/templates/list` 含 `skill://reqflow/project-init`，`get_harness_template` 返回 `reqflowMcpSkill`、`workspaceFiles` 和每个仓库的 `files`，且 `files` 包含非模板模块文档与检查脚本。
- MCP tool 错误路径变更：用无效 `actionToken` 调用 `tools/call publish_repository_index`，确认 HTTP 响应没有顶层 protocol `error`，而是 `result.content` 中包含错误说明且 `result.isError=true`。
- 跨端流程变更：配合前端验证项目管理、项目接入中心、分支知识库详情、需求新增、执行包保存、MCP Key 管理和统计页面。
