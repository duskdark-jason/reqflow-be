# 需求管理平台后端模块 Harness

## 业务目的

本模块承接“需求管理”菜单下的后端能力，支撑项目接入、需求提交、Agent 交接资料、MCP 调用、分支知识库和使用统计。前端 companion 仓库通过 `reqflow-ui/src/api/requirement/**` 调用本模块接口。后续需求变更时，必须从菜单和子菜单定位到对应 Controller、Service、Mapper 和权限标识。

## 菜单与功能入口

| 菜单目录 | 子菜单/页面 | 功能说明 | 前端文件 | API 封装 | 后端接口与权限 | 后端核心文件 |
|---|---|---|---|---|---|---|
| 需求管理 | 项目管理 | 项目列表、项目维护入口、项目初始化状态 | `reqflow-ui/src/views/requirement/project/index.vue`、`maintain.vue` | `reqflow-ui/src/api/requirement/project.js`、`projectInit.js` | `/requirement/project/**`，`req:project:*`；需求表单上下文只读可使用 `req:demand:*` 读取项目列表和初始化上下文 | `ReqProjectController`、`ReqProjectInitController`、`ReqProjectServiceImpl`、`ReqProjectInitServiceImpl` |
| 需求管理 | 分支知识库详情页签 | 按项目分支查看模块知识、索引批次和初始化指令 | `reqflow-ui/src/views/requirement/project/knowledge.vue` | `reqflow-ui/src/api/requirement/index.js`、`project.js` | `/requirement/index/module/tree`，管理页使用 `req:index:list`，需求表单只读可使用 `req:demand:*`；`/requirement/index/batch/list`，`req:index:list` | `ReqIndexController`、`ReqRepositoryIndexServiceImpl`、`ReqIndexModuleMapper`、`ReqRepositoryIndexBatchMapper` |
| 需求管理 | 需求列表 | 需求维护页签、新增、编辑、查询、删除、状态流转、返修流转、生成需求设计指令和执行任务指令 | `reqflow-ui/src/views/requirement/demand/index.vue`、`maintain.vue`、`detail.vue` | `reqflow-ui/src/api/requirement/demand.js`、`index.js` | `/requirement/demand/**`，`req:demand:*`；管理员删除使用 `req:demand:remove`；`/requirement/index/impact/suggest` 需求表单可使用 `req:demand:add/edit/query` | `ReqDemandController`、`ReqDemandServiceImpl`、`ReqDemandStatusTransition`、`ReqIndexController`、`ReqRepositoryIndexServiceImpl` |
| 需求管理 | 需求执行包 | 保存和读取需求设计、执行计划、执行报告、Review 报告等交接资料；需求详情嵌入读取可使用 `req:demand:query` | `reqflow-ui/src/views/requirement/package/index.vue` | `reqflow-ui/src/api/requirement/package.js` | `/requirement/package/**`，读取为 `req:package:list` 或 `req:demand:query`，保存为 `req:package:save` | `ReqPackageController`、`ReqPackageServiceImpl`、`ReqPackageVersionMapper` |
| 需求管理 | MCP 管理 | 管理人员 MCP Key，创建或重置后返回一次性 Key、Codex 多平台安装命令和高级安装包 | `reqflow-ui/src/views/requirement/mcpKey/index.vue` | `reqflow-ui/src/api/requirement/mcpKey.js` | `/requirement/mcp/key/**`，`/requirement/codex/install.*`，`req:mcp:key:*`；`/requirement/mcp` | `ReqMcpKeyController`、`ReqCodexInstallController`、`ReqMcpController`、`ReqMcpUserKeyServiceImpl`、`McpService` |
| 需求管理 | 使用统计 | 需求、项目、用户和状态统计 | `reqflow-ui/src/views/requirement/statistics/index.vue` | `reqflow-ui/src/api/requirement/statistics.js` | `/requirement/statistics/**`，`req:stats:view` | `ReqStatisticsController`、`ReqStatisticsService` |
| 需求管理 | 隐藏兼容能力 | 仓库、项目分支、人工模块兼容 CRUD，不作为左侧菜单独立入口 | `reqflow-ui/src/api/requirement/repository.js`、`variant.js`、`module.js` | 同前述 API 文件 | `/requirement/repository/**`、`/requirement/variant/**`、`/requirement/module/**`，`req:repo:*`、`req:variant:*`、`req:module:*` | `ReqRepositoryController`、`ReqVariantController`、`ReqModuleController` 及对应 Service/Mapper |

## 模块文件索引

| 类型 | 优先查看文件 | 说明 |
|---|---|---|
| 菜单与权限 SQL | `docs/db/sql/req_platform_menu.sql`、`docs/db/sql/req_platform_req016_role_permissions.sql` | 需求管理一级菜单、子菜单、按钮权限和三类角色授权。 |
| 接口契约 | `docs/ai-harness/contracts/requirement-platform-api.md` | 后端接口、MCP resource、MCP tool、知识库和初始化契约。 |
| 领域入口 | `docs/domains/requirement-platform/README.md` | 后端业务边界和长期维护规则。 |
| 后端 Controller | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/controller/` | HTTP 接口入口，随需求业务模块发布。 |
| 后端 Service | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/` | 核心业务规则。 |
| 数据访问 | `ruoyi-requirement/src/main/resources/mapper/requirement/` | Mapper XML 和 SQL。 |
| 领域对象 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/domain/` | `req_*` 表对应实体。 |
| MCP 能力 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | MCP resources、tools 和权限兜底。 |

## 接口与数据契约

- 相关契约文档：`docs/ai-harness/contracts/requirement-platform-api.md`。
- 相关领域入口：`docs/domains/requirement-platform/README.md`。
- 关键菜单脚本：`docs/db/sql/req_platform_menu.sql`。
- 关键角色脚本：`docs/db/sql/req_platform_req016_role_permissions.sql`。
- 关键表结构：`docs/db/sql/req_platform_schema.sql` 以及后续 `docs/db/sql/req_platform_req*.sql` 增量脚本。

## 不变量

- 需求管理左侧菜单以“需求管理”为一级目录；项目仓库、项目分支和人工模块不再作为独立左侧菜单入口。
- 项目、仓库、项目分支和模块知识必须按项目分支隔离；不能把其他分支或旧项目级模块默认混入当前分支。
- 新增或编辑需求时，后端必须校验 `projectId + variantId` 属于同一项目，且项目分支已有仓库索引证据；新功能提需允许分支暂时没有既有模块知识。
- 新增需求时后端必须覆盖请求体中的需求编号并生成 `REQ-001` 风格编号，不包含日期；后端必须覆盖客户端 `creatorId`，以当前登录用户作为创建人，并将新需求状态置为 `draft`。
- 新增和修改需求必须提供 `demandSource`；业务背景允许保存富文本 HTML 和粘贴图片，背景图片及需求附件统一通过 `/requirement/demand/upload` 上传，服务端单文件最大 2MB。
- 普通需求编辑只允许 `draft` 状态且创建人匹配；状态变化必须通过状态流转接口，不得通过通用编辑接口绕过状态机。
- 删除需求只开放给管理员按钮权限 `req:demand:remove`，会同步删除该需求的资料包版本和动作 token；需求人员和开发人员角色脚本不得分配该权限。
- 需求主状态流转为 `draft -> submitted -> plan_ready -> confirmed -> developing -> review -> completed`，验收阶段可走 `review -> repairing -> review` 返修分支，旧 `plan_pending`、`archived` 仅作为兼容状态保留；`submitted` 表示待生成需求设计，`plan_ready` 表示需求设计待确认，`confirmed` 表示待执行开发。
- 状态流转不仅校验 `req:demand:edit` 和状态机，还必须按角色隔离：`requirement_user` 执行提交需求、确认需求设计、返修和验收，`requirement_developer` 执行提交需求设计、开始开发、提交验收和返修验收，`admin` 可执行全部合法动作。
- 开发人员可通过需求详情获取 `requirement_plan` 动作 token 指令；该指令只能用于 MCP `save_requirement_package` 保存 `requirement` 需求设计，不能替代人员 `X-MCP-Key`。
- 开发人员可通过需求详情获取 `requirement_develop` 动作 token 指令；该指令包含执行计划和执行报告两个一次性 actionToken，分别用于 MCP `save_development_plan` 和 `upload_execution_report`，不能替代人员 `X-MCP-Key`。
- 项目初始化、需求编排和开发执行动作 token 生成后 24 小时内有效且仅可使用一次；`last_used_time` 非空或 `expire_time` 过期时必须拒绝，重新执行需重新生成指令。
- 需求资料包通过 `req_package_version` 追加版本记录，返修流程依赖同一需求下需求设计、执行计划、执行报告和 Review 报告的历史版本链，不新增覆盖式更新。
- 管理员角色沿用 `role_key='admin'` 超级管理员全部权限；需求人员角色 `requirement_user` 只分配需求列表和使用统计菜单权限；开发人员角色 `requirement_developer` 分配需求列表、MCP 管理、使用统计和隐藏 `req:package:save` 权限，供 MCP 回写资料。
- 需求未选择既有模块时，可以用备注承载新功能名称；执行包模块名解析顺序为人工模块、索引模块、备注。
- 人员 `X-MCP-Key` 只负责认证和权限；项目分支动作 `actionToken` 只负责动作上下文定位，二者不能互相替代。
- 项目初始化默认复制指令只保留短动态上下文，必须包含 `reqflow-mcp`、`mcpServer: reqflow`、`toolName: publish_repository_index` 和 `mcpTool: reqflow.publish_repository_index`，确保接入项目能触发全局 skill 并定位到指定 MCP server 的指定 tool。
- `/requirement/mcp` 必须支持 MCP `initialize -> notifications/initialized -> tools/list` lifecycle；新增 tool 时必须同步 `tools/list` 的描述和 `inputSchema`。
- `/requirement/mcp` 的协议级错误必须返回标准 JSON-RPC `error.code/error.message`，不能同时带 `result:null`；`tools/call` 内的业务错误必须返回 MCP tool result，并设置 `isError=true`。
- 项目接入初始化由平台存储和下发 harness 模板，后端不直接执行 Git、shell 或写用户本地文件。
- 项目接入初始化的模块知识库必须按前端页面业务功能优先生成：初始化 agent 先扫描前端路由、菜单、页面组件和 API 封装，再用 `publish_repository_index.modules` 按菜单目录、子菜单、隐藏页签或页面业务功能发布；纯后端仓库按 companion 前端菜单、MCP 能力或后台任务发布。不得把仓库概览、技术层目录或空数组当作模块知识库。
- 用户可见系统名称统一为“统一需求流转平台”，但底层 RuoYi 包名、权限框架和通用基础能力保持兼容。

## 常见风险

- 修改项目初始化上下文时，必须同步检查前端项目管理、项目维护、分支知识库页签和需求表单的字段使用。
- 修改索引导入或影响面推荐时，必须确认项目分支、真实 Git 分支、索引批次和模块知识的粒度一致。
- MCP tools 新增、改名或 actionToken 解析调整时，必须同步人员权限校验、接口契约、一次性和 24 小时有效期语义、`tools/list` schema、前端文案和平台初始化指令。
- MCP 管理 Key 创建或重置结果调整时，必须同步前端 MCP 管理页。页面不再提供配置查询入口；创建和重置响应只单独返回一次性 `plainKey` 与 `codexSetupPackage`。`codexSetupPackage.installCommands` 是主复制入口，提供 macOS/Linux 和 Windows PowerShell 代码块命令模板；模板使用 `${REQFLOW_MCP_KEY}` 占位，前端只在当前结果弹窗中用一次性 `plainKey` 渲染。长 JSON 安装包仅作为高级配置/调试信息保留。
- `/requirement/codex/install.sh` 和 `/requirement/codex/install.ps1` 是匿名可读安装脚本端点，脚本内容不得内置人员 Key，不得自动调用 reqflow MCP tools，只写入本机 Codex MCP 配置和全局 `reqflow-mcp` skill。
- 全局 `reqflow-mcp` skill 模板的 `SKILL.md` frontmatter 必须保持合法 YAML；`name` 和 `description` 使用双引号包裹，描述中不得出现未转义的 `: `，避免 Codex 启动扫描时跳过该 skill。
- MCP lifecycle 或 HTTP Controller 调整时，必须用真实 HTTP 冒烟验证 `initialize`、`notifications/initialized`、`resources/templates/list` 和 `tools/list`，不能只看 Service 单测。
- MCP `tools/call` 错误响应调整时，必须覆盖成功、权限失败、参数校验失败和业务导入失败路径；接入项目侧不能再只看到 `Unexpected response type`，应能读到 `content` 中的业务错误。
- 项目接入初始化指令调整时，默认复制内容不得重复完整 1-7 步流程；完整顺序由全局 `reqflow-mcp` skill 承接，必须保证 agent 能先调用 `get_harness_template` 写入本地 harness，再运行 `check-docs.sh`、`check-harness.sh init`，最后才发布索引和登记初始化结果。
- 项目接入初始化索引调整时，必须防止“已发布索引但没有具体业务模块”的假阳性；`actionToken` 或 `mcpKey` 项目初始化上下文下，`modules` 至少包含一个带 `moduleCode` 和 `moduleName` 的页面业务功能或后端主能力。
- MCP 下发的完整 harness 模板由后端 `ruoyi-requirement/src/main/resources/harness-template/` 保存并随包发布；`files.txt` 是下发清单。该目录是项目接入初始化模板的唯一维护源，workspace 根目录不再保留离线模板副本。
- 索引表迁移不完整时，`publish_repository_index` 必须返回指向 `docs/db/sql/req_platform_req007_index_tables.sql` 的友好业务错误，不能把 `Table ... doesn't exist` 原样作为最终结论。
- 菜单权限调整时，必须同时检查 `docs/db/sql/req_platform_menu.sql`、Controller `@PreAuthorize` 和前端按钮权限。

## 验证建议

- 最低文档门禁：`sh scripts/check-docs.sh && sh scripts/check-harness.sh init`。
- 后端契约或 Service 变更：运行 `mvn -pl ruoyi-requirement -am test`，必要时补指定测试类。
- 后端打包验证：运行 `mvn -pl ruoyi-admin -am -DskipTests package`。
- MCP 协议变更：在后端启动后用 `curl` 或 MCP 客户端验证 `initialize -> notifications/initialized -> resources/templates/list -> tools/list`，并确认工具列表含 `publish_repository_index`。
- MCP 项目接入初始化变更：验证 `resources/templates/list` 含 `skill://reqflow/project-init`，`get_harness_template` 返回 `reqflowMcpSkill`、`workspaceFiles` 和每个仓库的 `files`，且 `files` 包含 `docs/ai-harness/modules/*-page-functions.md` 非模板页面功能索引文档、完整 `docs/process/**`、完整 `docs/templates/**`、检查脚本和测试脚本。
- MCP tool 错误路径变更：用无效 `actionToken` 调用 `tools/call publish_repository_index`，确认 HTTP 响应没有顶层 protocol `error`，而是 `result.content` 中包含错误说明且 `result.isError=true`。
- 跨端流程变更：配合前端验证项目管理、项目维护、分支知识库详情、需求新增、执行包保存、MCP Key 管理和统计页面。
