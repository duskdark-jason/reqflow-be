# 需求管理平台后端接口契约

## 适用范围

本契约覆盖 `reqflow-be` 中新增的需求管理平台后端接口、权限标识、响应结构和关键业务不变量。前端 companion 仓库 `reqflow-ui` 必须以本文和 active spec 为接口依据。

## 通用响应

- 列表接口返回 RuoYi `TableDataInfo`，关键字段为 `code`、`msg`、`rows`、`total`。
- 详情、保存、生成和统计接口返回 RuoYi `AjaxResult`，关键字段为 `code`、`msg`、`data`。
- 新增、修改、删除类接口沿用 RuoYi 成功码和失败码。

## 基础 CRUD 接口

| 资源 | 路径 | 方法 | 权限 | 数据粒度 |
|---|---|---|---|---|
| 项目列表 | `/requirement/project/list` | GET | `req:project:list` | 一行一个 `req_project` |
| 项目详情 | `/requirement/project/{projectId}` | GET | `req:project:query` | 一个项目 |
| 项目新增 | `/requirement/project` | POST | `req:project:add` | 一个项目 |
| 项目修改 | `/requirement/project` | PUT | `req:project:edit` | 一个项目 |
| 项目删除 | `/requirement/project/{projectIds}` | DELETE | `req:project:remove` | 一个或多个项目 |
| 项目仓库兼容接口 | `/requirement/repository/**` | GET/POST/PUT/DELETE | `req:repo:*` | 一个代码仓库，左侧菜单不再暴露 |
| 项目分支兼容接口 | `/requirement/variant/**` | GET/POST/PUT/DELETE | `req:variant:*` | 一个项目分支，左侧菜单不再暴露 |
| 人工模块兼容接口 | `/requirement/module/**` | GET/POST/PUT/DELETE | `req:module:*` | 一个项目分支下的人工模块或功能点，左侧菜单不再暴露 |

需求列表、详情和维护页需要读取项目、分支、人工模块和索引模块作为表单上下文。为避免给需求人员分配项目/MCP 管理菜单，`/requirement/project/list`、`/requirement/project/{projectId}`、`/requirement/project/init/{projectId}`、`/requirement/variant/list`、`/requirement/variant/{variantId}`、`/requirement/module/list`、`/requirement/module/{moduleId}` 可在对应管理权限之外接受 `req:demand:list`、`req:demand:add`、`req:demand:edit` 或 `req:demand:query` 的只读访问；新增、修改和删除仍只接受各自管理权限。

## 项目初始化接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/project/init/{projectId}` | GET | `req:project:query` 或需求上下文只读权限 | 查询一个项目的初始化上下文 |
| `/requirement/project/init` | POST | `req:project:add` | 新增项目并同步保存代码仓库和项目分支 |
| `/requirement/project/init` | PUT | `req:project:edit` | 更新项目并同步保存代码仓库和项目分支 |
| `/requirement/project/{projectId}/harness-template` | GET | `req:project:query` | 查询项目接入所需 harness 初始化模板包 |
| `/requirement/project/{projectId}/harness-init-result` | POST | `req:package:save` | 登记 Codex 在目标 workspace 的 harness 初始化结果 |

初始化上下文响应 `data` 包含：

- `project`：`req_project` 项目基础信息。
- `repositories`：项目下团队共享仓库列表，一行代表一个 Git 远端仓库，接口返回时不回传个人本机路径。
- `variants`：项目下分支配置列表，一行代表一个可供需求人员选择的项目分支。`branchLabel` 是需求人员可见中文标签，`baselineBranch` 是真实 Git 分支名，`initInstruction` 是复制给 MCP 的项目分支初始化指令；`mcpKey`、`variantName`、`variantCode`、`customerName`、`scopeType`、`branchPolicy` 继续作为 `req_variant` 兼容字段返回。每行同时返回该分支独立的 `totalModules`、`indexedModules`、`manualModules`、`indexedRepositoryCount`、`unindexedRepositoryCount`、`latestIndexedAt` 和 `latestCommit`。
- `moduleSummary`：`totalModules`、`indexedModules`、`manualModules`，分别表示项目级模块总数、索引模块数和人工维护模块数；分支级状态以 `variants` 每行字段为准。
- `indexSummary`：`latestIndexedAt`、`latestCommit`、`indexedRepositoryCount`、`unindexedRepositoryCount`。
- `initChecklist`：`projectReady`、`repositoryReady`、`variantReady`、`moduleReady`、`indexReady`。

在本地库处于部分迁移状态、尚未创建可选索引表 `req_index_module` 或 `req_repository_index_batch` 时，初始化上下文仍必须稳定返回项目、仓库和项目分支数据；模块摘要和索引摘要按空集合计算，`moduleReady` 与 `indexReady` 为 `false`。除这两个可选索引表缺失外，其他数据库异常不得被吞掉。

`initInstruction` 结构如下：

```json
{
  "actionType": "project_init",
  "targetMethod": "publish_repository_index",
  "token": "reqflow_action_xxx",
  "tokenPrefix": "reqflow_action_xxx",
  "prompt": "请执行项目分支初始化，调用 reqflow MCP server 的 publish_repository_index tool 发布当前仓库索引。",
  "content": "请执行项目分支初始化，调用 reqflow MCP server 的 publish_repository_index tool 发布当前仓库索引。\n请按全局 skill `reqflow-mcp` 执行 Reqflow 项目接入初始化。\nmcpServer: reqflow\ntoolName: publish_repository_index\nmcpTool: reqflow.publish_repository_index\ntargetMethod: publish_repository_index\nprojectId: 1\nvariantId: 2\nactionToken: reqflow_action_xxx\n有效期：24小时内有效，仅可使用一次；过期或已使用后需重新生成。\n要求：actionToken 是 publish_repository_index 的 arguments.actionToken，不是 X-MCP-Key。",
  "copyLabel": "复制初始化指令",
  "expireTime": "2026-06-12 10:00:00"
}
```

`initInstruction.content` 是给接入项目复制的短动态上下文，不再重复完整执行步骤。内容必须保留 `reqflow-mcp`、`mcpServer: reqflow`、`toolName: publish_repository_index` 和 `mcpTool: reqflow.publish_repository_index`，避免接入项目在存在多个 MCP server 或同名能力描述时无法定位到需求平台工具。`projectId` 用于先读取 `get_harness_template`，`actionToken` 是项目分支动作 token，只能作为 `publish_repository_index` 的 `arguments.actionToken` 传入，不能写入 `X-MCP-Key` 请求头；完整初始化顺序由全局 `reqflow-mcp` skill 承接。初始化 actionToken 生成后 24 小时内有效且仅可使用一次，过期或已使用后必须重新生成初始化指令。

初始化保存请求 `project` 必须包含项目名称和项目编码；`repositories` 至少包含一条有效代码仓库，且仓库名称、仓库类型、Git 远端和默认分支不能为空，允许纯后端服务只维护一条 `BACKEND` 仓库；`variants` 至少包含一条项目分支，且分支中文标签 `branchLabel` 和真实分支名 `baselineBranch` 不能为空。`variantCode` 可以为空，后端会按真实分支名生成稳定兼容编码；`mcpKey` 可以为空，后端会按 `项目编码:分支编码` 生成兼容字段，前端主展示以 `initInstruction` 为准。

初始化保存必须在同一事务内完成。新增时先写 `req_project`，再写 `req_repository` 和 `req_variant`；更新时按传入 ID 更新已有仓库/分支配置、插入新增行，并删除本次维护弹窗中移除的仓库/分支配置。接口拒绝仓库地址、默认分支、真实分支名、项目说明或备注中的个人本机绝对路径。

## Harness 模板与初始化下发

需求平台内置保存一份团队统一的需求平台驱动 harness 模板，作为项目接入时的初始化资产。后端可发布模板存储在 `ruoyi-requirement/src/main/resources/harness-template/`，`files.txt` 是 MCP 下发文件清单；workspace 根目录不再保留离线模板副本。模板包至少包含：

- 子仓库 `AGENTS.md` 模板。
- workspace 根目录 `AGENTS.md` 模板。
- `docs/process/platform-key-workflow.md`、`new-requirement-flow.md`、`agent-workflow.md` 和 `git-workflow.md`。
- `docs/ai-harness/**` 基础索引、验证说明、契约/模块/决策模板。
- `docs/specs/**`、`docs/templates/**`、`docs/runbooks/**` 和 `scripts/check-*.sh|cmd`。

项目接入初始化时，平台根据项目仓库、companion 仓库、默认基线分支、任务分支前缀和客户分支配置生成 harness 初始化模板包。Codex 在目标 workspace 中通过需求平台 MCP 或接口获取模板包，并在校验远端仓库一致后写入目标仓库；服务端只负责存储和下发模板，不直接执行 Git、shell 或文件系统写入。

`get_harness_template` 响应 `data` 必须包含：

- `reqflowMcpSkill`：可嵌入 `AGENTS.md` 的 reqflow MCP 项目接入初始化技能文本，触发条件覆盖 `actionToken`、`mcpServer: reqflow`、`mcpTool: reqflow.publish_repository_index` 和项目接入初始化。
- `workspaceFiles`：workspace 根目录文件清单，当前至少包含根 `AGENTS.md`，写入模式为合并已有规则。
- `repositoryHarnessInstructions`：每个仓库一项，包含 `repository`、自然语言 `content` 和可落地的 `files`。`files` 必须基于 `src/main/resources/harness-template/files.txt` 下发完整 `docs/**` 和 `scripts/**` 模板，至少包含子仓库 `AGENTS.md`、完整 `docs/process/**`、`docs/templates/**`、`docs/ai-harness/**`、一个命名为 `docs/ai-harness/modules/*-page-functions.md` 的非模板页面功能索引文档、`scripts/check-docs.sh`、`scripts/check-harness.sh` 和对应测试脚本。

Codex 落地初始化结果时，不能只保留 `docs/ai-harness/modules/.gitkeep`，也不能把仓库概览、技术层目录或空模块当作模块知识库。必须先分析前端路由、菜单、页面组件和 API 封装，基于项目主菜单、子菜单、隐藏页签或页面业务功能生成至少一个 `docs/ai-harness/modules/*.md` 具体业务知识库文档，记录功能接口、权限标识和涉及文件。纯后端服务没有前端菜单时，应按 companion 前端菜单、MCP 能力或后台任务建立对应关系。

多仓 workspace 初始化必须同时下发 workspace 根目录 `AGENTS.md` 和各子仓库 `AGENTS.md`。workspace 入口只做分流和通用护栏；业务规则、验证命令和契约仍落到对应子仓库 `docs/ai-harness`、`docs/process` 和 `docs/specs`。

全局 `reqflow-mcp` skill 的执行顺序必须是：确认 reqflow MCP 已加载 -> 调用 `get_harness_template` -> 写入或合并本地 harness 文件 -> 在每个目标子仓库运行 `sh scripts/check-docs.sh` 和 `sh scripts/check-harness.sh init` -> 分析前端路由、菜单、页面组件和 API 封装并生成页面业务功能粒度的 `modules` -> 调用 `publish_repository_index` -> 调用 `register_harness_init_result`。`publish_repository_index` 不负责写调用方本地文件，不能作为初始化第一步。

Codex 完成初始化后，通过 `register_harness_init_result` 或 `/requirement/project/{projectId}/harness-init-result` 回写结果，内容包括仓库远端、当前分支、写入文件清单、校验命令、校验结果、失败原因和是否需要人工确认。

## 项目索引接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/index/batch/list` | GET | `req:index:list` | 查询仓库索引批次 |
| `/requirement/index/module/tree` | GET | `req:index:list` 或需求上下文只读权限 | 查询索引生成的模块知识列表 |
| `/requirement/index/impact/suggest` | GET | `req:index:list`、`req:demand:add`、`req:demand:edit` 或 `req:demand:query` | 按项目、仓库、项目分支和模块推荐影响面 |
| `/requirement/index/import` | POST | `req:index:import` | 备用 JSON 导入入口 |

索引导入只保存 Git 远端、仓库类型、分支、commit、相对路径和结构化影响面。上传内容如果包含个人本机绝对路径，服务端必须拒绝导入。

索引批次列表和模块知识只读接口用于项目维护和分支知识库展示。模块知识库需要同时关联 `projectId` 和 `variantId`；传入 `variantId` 时只返回该项目分支的模块知识，不再混入 `variant_id is null` 的项目级旧数据。部分迁移库缺少 `req_repository_index_batch` 或 `req_index_module` 时，这两个只读接口返回空列表和成功响应，避免项目维护或分支知识库整页不可用；索引导入、影响面推荐和其他表异常仍按真实错误处理。

索引导入优先支持 `actionToken + remoteUrl`：服务端按动作 token 解析目标动作、项目和项目分支，并在同项目下按 `remoteUrl` 定位代码仓库。`actionToken` 必须能解析为 `project_init` 动作且 `targetMethod` 为 `publish_repository_index`；同时兼容旧的 `mcpKey + remoteUrl` 和 `projectId + repoId + branchName`。项目初始化上下文中的 `modules` 不能为空，且每一项必须有稳定 `moduleCode` 和业务 `moduleName`；推荐按前端页面业务功能、菜单目录、子菜单或隐藏页签生成，一行代表一个具体业务知识库模块。模块和影响面 payload 可以显式携带 `variantId`；未携带时，服务端按动作 token、项目分支或 `projectId + branchName + status=0` 反查分支并沉淀到索引模块和影响面条目。每个项目分支都需要单独初始化索引，不能用主线索引代替客户分支或其他功能分支。

索引导入写入前会预检 `req_repository_index_batch`、`req_index_module` 和 `req_impact_item`。缺任一表时返回业务错误 `平台索引表未初始化：<table>`，并提示执行 `docs/db/sql/req_platform_req007_index_tables.sql` 或总 schema 中对应建表段；不得让调用方只看到数据库原始 `Table ... doesn't exist` 作为最终结论。

影响面推荐请求可传 `projectId`、`repoId`、`variantId`、`moduleId`、`moduleCode`。当传入 `variantId` 时，服务端必须校验项目分支属于当前项目，并使用该项目分支 `baselineBranch` 过滤影响面；查询只返回目标仓库或每个仓库最新 `imported` 批次的数据。返回 `pages`、`apis`、`tables`、`permissions` 和 `documents` 五类列表，每一项来自 `req_impact_item`，同类资源按 `itemKey/apiPath/permissionKey/tableName/relativePath/itemName` 去重。

## 需求接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/demand/list` | GET | `req:demand:list` | 查询需求列表 |
| `/requirement/demand/{demandId}` | GET | `req:demand:query` | 查询需求详情 |
| `/requirement/demand` | POST | `req:demand:add` | 新增未提交需求 |
| `/requirement/demand` | PUT | `req:demand:edit` | 修改未提交需求正文 |
| `/requirement/demand/{demandIds}` | DELETE | `req:demand:remove` | 管理员删除一个或多个需求，并删除关联资料包版本和动作 token |
| `/requirement/demand/upload` | POST | `req:demand:add` 或 `req:demand:edit` | 上传需求背景图片或附件，单文件不超过 2MB |
| `/requirement/demand/{demandId}/status/{status}` | POST | `req:demand:edit` | 状态流转 |
| `/requirement/demand/{demandId}/plan-instruction` | GET | `req:demand:query` | 获取生成需求设计并回写需求设计的 MCP 指令 |
| `/requirement/demand/{demandId}/develop-instruction` | GET | `req:demand:query` | 获取生成执行计划、执行开发并回写执行报告的 MCP 指令 |

新增需求时后端始终生成 `demandNo`，格式为 `REQ-001` 风格递增编号，不包含日期；即使请求体传入编号也会被覆盖。创建人 ID 由当前登录用户获取，即使请求体传入 `creatorId` 也会被覆盖。`demandSource` 必填，用于记录需求来源。新增后状态设为 `draft`，中文语义为“未提交”。

`businessBackground` 可保存富文本 HTML，用于承载粘贴图片后的 `<img>` 内容；图片和 `attachments` 附件均通过 `/requirement/demand/upload` 获取上传路径。该上传接口服务端硬限制单文件最大 2MB，返回字段沿用 RuoYi 上传结构：`url`、`fileName`、`newFileName`、`originalFilename`。`attachments` 保存多个文件路径时使用英文逗号分隔。

新增和修改需求时，`projectId + variantId` 必须指向同一项目下已启用且初始化完成的项目分支；未初始化完成的分支不得作为需求提交目标。分支初始化完成口径为：项目存在有效代码仓库，且所有有效仓库都已有该分支真实 `baselineBranch` 的 `imported` 索引批次。新功能提需允许当前分支暂时没有既有模块知识，该校验必须在后端服务层兜底，不能只依赖前端下拉过滤。

普通修改接口只允许修改 `draft` 状态需求，且请求操作者必须是需求创建人；普通修改接口会忽略状态字段，状态变化只能通过 `/status/{status}` 接口进入状态机。

删除需求是管理员运维能力，不属于需求人员或开发人员主流程。删除接口要求 `req:demand:remove`，角色初始化脚本不得给 `requirement_user` 或 `requirement_developer` 分配该权限；删除会物理删除 `req_demand`，并同步清理该需求的 `req_package_version` 和 `req_action_token` 记录。

需求可以选择既有模块，也可以通过备注字段承载“新功能名称”。新功能名称用于执行包上下文和前端展示，不写入项目分支知识库；选择既有模块时，`moduleId` 可以对应人工模块，也可以对应索引模块标识，执行包生成时按人工模块、索引模块、备注的顺序解析模块名。

允许的状态流转：

```text
draft -> submitted
submitted -> plan_ready
submitted -> plan_pending
plan_pending -> plan_ready
plan_ready -> confirmed
confirmed -> developing
developing -> review
review -> repairing
repairing -> review
review -> completed
completed -> archived
```

其中 `draft -> submitted -> plan_ready -> confirmed -> developing -> review -> completed` 是新主流程；`submitted` 表示待生成需求设计，`plan_ready` 表示需求设计待需求人员确认，`confirmed` 表示待执行开发；`review -> repairing -> review` 是验收返修分支；`plan_pending`、`archived` 用于兼容历史或归档场景。不允许跳转或倒退，违反时抛出业务异常 `需求状态流转不允许`。

`plan-instruction` 接口返回 `ReqActionInstruction`，`actionType=requirement_plan`，复制内容必须包含 `reqflow-mcp`、`mcpServer: reqflow`、`toolName: save_requirement_package`、`mcpTool: reqflow.save_requirement_package`、`demandId`、`demandNo`、`actionToken`、`arguments.actionToken` 使用说明，以及 24 小时内有效、仅可使用一次、过期或已使用后重新生成的提示。该 actionToken 只用于生成需求设计上下文定位，只允许调用 `save_requirement_package`，不能替代人员 `X-MCP-Key`。

`develop-instruction` 接口返回 `ReqActionInstruction`，`actionType=requirement_develop`，复制内容必须包含 `reqflow-mcp`、`mcpServer: reqflow`、`mcpTool: reqflow.save_development_plan`、`mcpTool: reqflow.upload_execution_report`、`demandId`、`demandNo`、执行计划 `actionToken`、执行报告 `actionToken`、`arguments.actionToken` 使用说明，以及 24 小时内有效、仅可使用一次、过期或已使用后重新生成的提示。两个 actionToken 分别只允许调用 `save_development_plan` 和 `upload_execution_report`，不能替代人员 `X-MCP-Key`。

## 执行包接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/package/{demandId}` | GET | `req:package:list` 或 `req:demand:query` | 查询某需求全部执行包版本 |
| `/requirement/package/{demandId}/{artifactType}/latest` | GET | `req:package:list` 或 `req:demand:query` | 查询某类产物最新版本 |
| `/requirement/package/{demandId}/{artifactType}` | POST | `req:package:save` | 保存新版本 |
| `/requirement/package/generate/{demandId}` | POST | `req:package:save` | 生成草稿执行包 |

执行包保存永远追加 `req_package_version` 新记录，不覆盖历史版本。版本号按 `demand_id + artifact_type` 独立递增。验收返修不新建需求，继续在同一需求下追加需求设计、执行计划、执行报告或 Review 报告版本，用版本链表达返修轮次。

MCP `save_requirement_package` 可显式传 `demandId`，也可传生成需求设计指令里的 `actionToken` 由服务端解析到绑定需求；MCP `save_development_plan` 和 `upload_execution_report` 可传执行任务指令里的对应 `actionToken` 由服务端解析到绑定需求；这些调用仍必须通过人员 `X-MCP-Key` 或登录态权限校验。actionToken 被成功解析后立即写入 `last_used_time`，后续重复使用必须失败。

生成草稿执行包时，`context_manifest` 和需求草稿中的任务分支使用 `fix-功能模块-编号-标题` 语义，并将各片段转换为命令行友好的 ASCII slug。模块片段优先来自人工模块名，其次来自索引模块名，最后使用备注中的新功能名称。

支持的产物类型：

```text
requirement_draft
requirement
plan
context_manifest
branch_execution_brief
execution_prompt
review_prompt
execution_report
review_report
harness_template
harness_init_result
```

## 统计接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/statistics/overview` | GET | `req:stats:view` | 需求、方案、计划、报告和活跃用户总览 |
| `/requirement/statistics/project-rank` | GET | `req:stats:view` | 按项目聚合需求与包生成率 |
| `/requirement/statistics/user-usage` | GET | `req:stats:view` | 按用户聚合提交和报告上传次数 |

统计查询必须保持项目级或用户级数据粒度，避免一对多 join 放大需求数。

## MCP动作Token接口与数据模型

项目分支初始化、后续需求编排 token 和开发执行 token 统一使用 `req_action_token` 表保存动作上下文。复制给 MCP 的明文 token 只在指令响应中出现，服务端落库字段为 SHA-256 哈希、token 前缀、动作类型、目标 MCP 方法、项目、分支、需求、状态、过期时间和最近使用时间。

`req_action_token.action_type` 当前支持：

```text
project_init
requirement_plan
requirement_develop
```

动作 token 不是人员认证 Key，不能替代 MCP 请求头 `X-MCP-Key`；人员 Key 负责认证和权限，动作 token 负责让 MCP 服务识别应该调用哪个接口以及绑定到哪个项目、分支或需求上下文。所有动作 token 生成后默认 24 小时内有效，且只能成功解析一次；`expire_time` 早于当前时间或 `last_used_time` 非空时必须拒绝使用，调用方需要重新打开页面生成新指令。任何列表、日志或前端持久化都不得展示 `token_hash`，也不得把明文 action token 写入本地存储。

## MCP人员Key管理接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/mcp/key/list` | GET | `req:mcp:key:list` | 分页查询人员 MCP Key，列表只返回 Key 前缀和绑定人员，不返回明文或哈希 |
| `/requirement/mcp/key/user-options` | GET | `req:mcp:key:list`、`req:mcp:key:add` 或 `req:mcp:key:edit` | 查询 MCP Key 可绑定的启用用户，只返回 `userId`、`userName`、`nickName`，不依赖 `system:user:list` |
| `/requirement/mcp/key/{keyId}` | GET | `req:mcp:key:query` | 查询单个人员 MCP Key |
| `/requirement/mcp/key` | POST | `req:mcp:key:add` | 为启用用户创建随机唯一 MCP Key，明文只在本次响应返回 |
| `/requirement/mcp/key` | PUT | `req:mcp:key:edit` | 修改 Key 名称、状态和备注；不允许变更绑定用户，停用后不能继续用于 MCP 鉴权 |
| `/requirement/mcp/key/{keyId}/regenerate` | POST | `req:mcp:key:edit` | 重置 Key 并返回一次性明文，旧 Key 立即失效 |
| `/requirement/mcp/key/{keyIds}` | DELETE | `req:mcp:key:remove` | 删除一个或多个人员 MCP Key |

人员 Key 使用 `req_mcp_user_key` 表保存，服务端只落库 SHA-256 哈希、Key 前缀、绑定用户、状态、最近使用时间和最近使用 IP。前端和列表接口不得展示 `keyHash`，明文 `plainKey` 只允许在创建和重置响应中出现一次；创建和重置接口的操作日志必须关闭响应保存，避免明文 Key 进入 `sys_oper_log`。

`/requirement/mcp/key/config` 已删除，MCP 管理页不再常驻展示 MCP 地址、请求头名、Codex 配置模板、全局 skill 包或 Codex 安装指令包。

创建 Key 和重置 Key 响应只单独返回 `key`、`plainKey` 和 `codexSetupPackage`。其中 `plainKey` 是一次性明文；`codexSetupPackage` 是推荐复制给 Codex 的安装指令包，包含 `packageName=reqflow-codex-setup`、`installScope=global`、`mcpServer`、`codexConfigTemplate`、`installScripts`、`installCommands`、`skillPackage`、`installPrompt` 和 `serverMetadata`。`installCommands[]` 至少包含 `macos-linux` 与 `windows-powershell` 两个平台，每项包含 `platform`、`label`、`language` 和 markdown 代码块使用的 `command` 模板；`command` 使用 `${REQFLOW_MCP_KEY}` 作为 Key 占位符，不得直接包含人员明文 Key 或一次性 `actionToken`。`mcpServer` 必须包含 `name=reqflow`、`transport=streamable-http`、`url` 和 `headerName=X-MCP-Key`；`serverMetadata` 参考 MCP registry/server.json 风格，描述远程 MCP 地址、鉴权 header、`project-init`、`index-publish`、`package-handoff` 工具组和安全提示。配置完成后不得自动调用 `publish_repository_index` 或其他工具。

安装脚本端点：

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/codex/install.sh` | GET | 匿名可读 | 返回 macOS/Linux 安装脚本，脚本从 `REQFLOW_MCP_KEY` 或 `--key` 读取人员 Key，并写入 Codex MCP 配置和全局 `reqflow-mcp` skill |
| `/requirement/codex/install.ps1` | GET | 匿名可读 | 返回 Windows PowerShell 安装脚本，脚本从 `REQFLOW_MCP_KEY` 或 `-McpKey` 读取人员 Key，并写入 Codex MCP 配置和全局 `reqflow-mcp` skill |

安装脚本不得内置人员明文 Key，不得自动调用 reqflow MCP tool；脚本执行后只提示用户重启或刷新 Codex。

`codexSetupPackage` 内的 MCP 地址优先读取后端配置项 `reqflow.mcp.public-url`。该配置项应填写完整 MCP 对外访问地址，例如 `https://reqflow.example.com/requirement/mcp`；为空时服务端才按 `X-Forwarded-Proto`、`X-Forwarded-Host`、`Host` 和 `context-path` 自动推导地址。部署在反向代理、HTTPS、非默认端口或非本机访问场景时，建议显式配置 `reqflow.mcp.public-url`，避免安装包包含 `localhost` 或临时代理端口。

平台角色授权脚本为 `docs/db/sql/req_platform_req016_role_permissions.sql`。管理员角色使用 `role_key='admin'`，沿用 RuoYi 超级管理员全部权限；需求人员角色 `requirement_user` 只分配需求列表和使用统计菜单权限；开发人员角色 `requirement_developer` 分配需求列表、MCP 管理和使用统计菜单权限，并额外分配隐藏 `req:package:save`，用于通过 MCP 回写需求设计、执行计划和执行报告。

需求删除按钮权限 `req:demand:remove` 只随菜单脚本注册给管理员使用，不能加入需求人员或开发人员角色集合。状态流转接口虽然共用 `req:demand:edit`，服务层还必须按角色校验具体动作：需求人员只能执行提需、需求设计确认、返修和验收，开发人员只能执行提交需求设计、开始开发、提交验收和返修验收。

MCP 管理菜单权限独立于需求提交权限。需求人员角色默认不分配 `req:mcp:key:*`，管理员或开发人员可通过该菜单为已启用且未删除用户创建 Key。Key 鉴权后使用绑定用户的当前菜单权限集合；绑定用户停用或删除后，即使 Key 本身仍为启用状态也必须拒绝鉴权。即使 Key 有效，调用 MCP 工具仍受 `req:package:save`、`req:index:import`、`req:project:query` 等权限限制。

## MCP 接口

入口：`POST /requirement/mcp`。该路径允许匿名进入安全过滤链，Controller 内部按两种方式鉴权：

- 已登录 Web 用户：沿用当前 Spring Security 登录态和菜单权限。
- 外部 Codex/MCP 客户端：在请求头 `X-MCP-Key` 传入人员 Key，服务端按 Key 哈希查找绑定用户，临时构建登录上下文。

Controller 粗授权为 `req:package:save`、`req:index:import` 或 `req:project:query` 任一权限，Service 必须继续按 tool name 做细粒度权限校验。

支持方法：

```text
initialize
notifications/initialized
ping
resources/list
resources/read
resources/templates/list
prompts/list
prompts/get
tools/list
tools/call
```

MCP 客户端必须先调用 `initialize` 完成协议协商。服务端返回 `protocolVersion`、`serverInfo.name=reqflow`，并声明 `tools`、`resources`、`prompts` capabilities；随后接受 `notifications/initialized`，该通知不产生业务写入。`tools/list` 返回的每个工具必须包含 `name`、中文 `description` 和 JSON `inputSchema`，其中 `publish_repository_index` 至少声明 `actionToken`、`remoteUrl`、`projectId`、`repoId`、`mcpKey`、`repoType`、`branchName`、`commitHash`、`indexVersion` 以及 `modules/pages/apis/tables/permissions/documents` 等结构化索引列表。`tools/call` 成功响应必须是 MCP tool result，包含 `content`、`structuredContent` 和 `isError=false`。

MCP 响应错误边界：

- 协议级错误用于未知方法、非法请求或非 tool 调用阶段异常，返回 JSON-RPC `error` object，必须包含数值 `code` 和 `message`，且不能同时输出 `result:null`。
- tool 执行业务错误用于 `tools/call` 内的权限不足、参数校验、动作 token 无效、导入失败等业务异常，返回 JSON-RPC success，`result.content[0].type=text`、`result.content[0].text` 为可读错误说明，并设置 `result.isError=true`。
- tool 执行成功继续返回 `result.content`、`result.structuredContent` 和 `result.isError=false`。

资源读取：

| URI | 内容 | 粒度/过滤 |
|---|---|---|
| `requirement://{demandNo}` | 需求详情 | 按稳定需求编号读取 |
| `requirement://{demandNo}/draft-package` | 最新需求草稿包 | 按需求读取最新 `requirement_draft` 版本 |
| `requirement://{demandNo}/context-manifest` | 最新上下文清单 | 按需求读取最新 `context_manifest` 版本 |
| `project://{projectId}/overview` | 项目、仓库清单、项目分支清单 | 一个项目全貌 |
| `project://{projectId}/repositories` | 项目仓库清单 | 按 `projectId` 过滤 |
| `variant://{variantId}/overview` | 项目分支详情 | 一个项目分支 |
| `variant://{variantId}/branch-policy` | 项目分支策略 | 一个项目分支的 `branchPolicy` |
| `memory://{projectId}/modules?variantId={variantId}` | 模块知识库文档 | `projectId + variantId + docType=module` |
| `memory://{projectId}/contracts?variantId={variantId}` | 接口契约知识库文档 | `projectId + variantId + docType=contract` |
| `memory://{projectId}/decisions?variantId={variantId}` | 决策记录知识库文档 | `projectId + variantId + docType=decision` |
| `memory://{projectId}/runbooks?variantId={variantId}` | 运行手册知识库文档 | `projectId + variantId + docType=runbook` |
| `memory://{projectId}/specs/done?variantId={variantId}` | 已完成需求知识库文档 | `projectId + variantId + docType=spec` |
| `skill://reqflow/project-init` | Reqflow MCP 项目接入初始化技能 | 固定资源，用于让 agent 识别指定 MCP server 和初始化顺序 |
| `workspace://{projectId}/agents` | 工作空间 AGENTS 内容与项目上下文 | 按项目返回仓库、分支和生成内容 |

知识库类 MCP resource 必须优先带 `variantId`，用于区分同一项目下不同长期分支的独有模块、契约和文档；不允许把其他分支或旧项目级索引结果混入选中分支的上下文。

允许工具：

```text
save_requirement_package
save_development_plan
upload_execution_report
upload_review_report
register_harness_init_result
get_harness_template
publish_repository_index
```

MCP 安全边界：

- 只能读取平台资源或写入平台表。
- `X-MCP-Key` 只用于 MCP 入口，不替代 Web 登录 token，也不能访问 MCP 管理接口。
- 不允许执行 Git、shell、clone、branch、文件系统写入或大模型调用。
- `get_harness_template` 必须校验 `req:project:query`，返回项目、仓库、项目分支、`reqflowMcpSkill`、`workspaceFiles`、workspace `AGENTS.md` 内容和每个仓库的 harness 初始化指令及文件清单；该工具只读平台配置，不写仓库文件、不执行 Git 或 shell。
- `register_harness_init_result` 只更新 `req_repository` 的 harness 字段，必须校验 `req:package:save`。
- `publish_repository_index` 必须校验 `req:index:import`，优先接收 `actionToken + remoteUrl`，兼容旧 `mcpKey + remoteUrl`，只写入索引批次、模块知识、影响面条目和活动日志；上传内容不得包含个人本机绝对路径。
- 报告上传、计划保存和执行资料类工具必须校验 `req:package:save`，并且只追加 `req_package_version`。`save_requirement_package` 支持生成需求设计 actionToken 定位需求；`save_development_plan` 和 `upload_execution_report` 支持执行任务 actionToken 定位需求；actionToken 不能替代人员鉴权。
- `artifactType` 必须属于本文列出的支持类型。
