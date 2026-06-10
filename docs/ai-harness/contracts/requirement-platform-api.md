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

## 项目初始化接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/project/init/{projectId}` | GET | `req:project:query` | 查询一个项目的初始化上下文 |
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
  "content": "请执行项目分支初始化，调用 reqflow MCP server 的 publish_repository_index tool 发布当前仓库索引。\nmcpServer: reqflow\ntoolName: publish_repository_index\nmcpTool: reqflow.publish_repository_index\ntargetMethod: publish_repository_index\nactionToken: reqflow_action_xxx\n调用要求：\n1. 在接入项目的 Codex 会话中确认已加载名为 reqflow 的 MCP server。\n2. 通过 tools/list 查找 reqflow.publish_repository_index，只调用该 server 下的该 tool。\n3. 通过 tools/call 传入 arguments.actionToken、remoteUrl、branchName、commitHash、indexVersion 和结构化索引列表。\n4. actionToken 是 publish_repository_index 的 arguments.actionToken，不是 X-MCP-Key。",
  "copyLabel": "复制初始化指令",
  "expireTime": null
}
```

`initInstruction.content` 必须保留 `mcpServer: reqflow`、`toolName: publish_repository_index` 和 `mcpTool: reqflow.publish_repository_index` 三个机器可读字段，避免接入项目在存在多个 MCP server 或同名能力描述时无法定位到需求平台工具。`actionToken` 是项目分支动作 token，只能作为 `publish_repository_index` 的 `arguments.actionToken` 传入，不能写入 `X-MCP-Key` 请求头。

初始化保存请求 `project` 必须包含项目名称和项目编码；`repositories` 至少包含一条有效代码仓库，且仓库名称、仓库类型、Git 远端和默认分支不能为空，允许纯后端服务只维护一条 `BACKEND` 仓库；`variants` 至少包含一条项目分支，且分支中文标签 `branchLabel` 和真实分支名 `baselineBranch` 不能为空。`variantCode` 可以为空，后端会按真实分支名生成稳定兼容编码；`mcpKey` 可以为空，后端会按 `项目编码:分支编码` 生成兼容字段，前端主展示以 `initInstruction` 为准。

初始化保存必须在同一事务内完成。新增时先写 `req_project`，再写 `req_repository` 和 `req_variant`；更新时按传入 ID 更新已有仓库/分支配置、插入新增行，并删除本次维护弹窗中移除的仓库/分支配置。接口拒绝仓库地址、默认分支、真实分支名、项目说明或备注中的个人本机绝对路径。

## Harness 模板与初始化下发

需求平台内置保存一份团队统一的需求平台驱动 harness 模板，作为项目接入时的初始化资产。模板包至少包含：

- 子仓库 `AGENTS.md` 模板。
- workspace 根目录 `AGENTS.md` 模板。
- `docs/process/platform-key-workflow.md`、`new-requirement-flow.md`、`agent-workflow.md` 和 `git-workflow.md`。
- `docs/ai-harness/**` 基础索引、验证说明、契约/模块/决策模板。
- `docs/specs/**`、`docs/templates/**`、`docs/runbooks/**` 和 `scripts/check-*.sh|cmd`。

项目接入初始化时，平台根据项目仓库、companion 仓库、默认基线分支、任务分支前缀和客户分支配置生成 harness 初始化模板包。Codex 在目标 workspace 中通过需求平台 MCP 或接口获取模板包，并在校验远端仓库一致后写入目标仓库；服务端只负责存储和下发模板，不直接执行 Git、shell 或文件系统写入。

Codex 落地初始化结果时，不能只保留 `docs/ai-harness/modules/.gitkeep`；必须基于项目主菜单、子菜单、隐藏页签或主后端能力生成至少一个 `docs/ai-harness/modules/*.md` 模块知识库文档，记录功能接口、权限标识和涉及文件。纯后端服务没有前端菜单时，应按 companion 前端菜单、MCP 能力或后台任务建立对应关系。

多仓 workspace 初始化必须同时下发 workspace 根目录 `AGENTS.md` 和各子仓库 `AGENTS.md`。workspace 入口只做分流和通用护栏；业务规则、验证命令和契约仍落到对应子仓库 `docs/ai-harness`、`docs/process` 和 `docs/specs`。

Codex 完成初始化后，通过 `register_harness_init_result` 或 `/requirement/project/{projectId}/harness-init-result` 回写结果，内容包括仓库远端、当前分支、写入文件清单、校验命令、校验结果、失败原因和是否需要人工确认。

## 项目索引接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/index/batch/list` | GET | `req:index:list` | 查询仓库索引批次 |
| `/requirement/index/module/tree` | GET | `req:index:list` | 查询索引生成的模块知识列表 |
| `/requirement/index/impact/suggest` | GET | `req:index:list` | 按项目、仓库、项目分支和模块推荐影响面 |
| `/requirement/index/import` | POST | `req:index:import` | 备用 JSON 导入入口 |

索引导入只保存 Git 远端、仓库类型、分支、commit、相对路径和结构化影响面。上传内容如果包含个人本机绝对路径，服务端必须拒绝导入。

索引批次列表和模块知识只读接口用于项目接入中心展示。模块知识库需要同时关联 `projectId` 和 `variantId`；传入 `variantId` 时只返回该项目分支的模块知识，不再混入 `variant_id is null` 的项目级旧数据。部分迁移库缺少 `req_repository_index_batch` 或 `req_index_module` 时，这两个只读接口返回空列表和成功响应，避免项目接入中心整页不可用；索引导入、影响面推荐和其他表异常仍按真实错误处理。

索引导入优先支持 `actionToken + remoteUrl`：服务端按动作 token 解析目标动作、项目和项目分支，并在同项目下按 `remoteUrl` 定位代码仓库。`actionToken` 必须能解析为 `project_init` 动作且 `targetMethod` 为 `publish_repository_index`；同时兼容旧的 `mcpKey + remoteUrl` 和 `projectId + repoId + branchName`。模块和影响面 payload 可以显式携带 `variantId`；未携带时，服务端按动作 token、项目分支或 `projectId + branchName + status=0` 反查分支并沉淀到索引模块和影响面条目。每个项目分支都需要单独初始化索引，不能用主线索引代替客户分支或其他功能分支。

影响面推荐请求可传 `projectId`、`repoId`、`variantId`、`moduleId`、`moduleCode`。当传入 `variantId` 时，服务端必须校验项目分支属于当前项目，并使用该项目分支 `baselineBranch` 过滤影响面；查询只返回目标仓库或每个仓库最新 `imported` 批次的数据。返回 `pages`、`apis`、`tables`、`permissions` 和 `documents` 五类列表，每一项来自 `req_impact_item`，同类资源按 `itemKey/apiPath/permissionKey/tableName/relativePath/itemName` 去重。

## 需求接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/demand/list` | GET | `req:demand:list` | 查询需求列表 |
| `/requirement/demand/{demandId}` | GET | `req:demand:query` | 查询需求详情 |
| `/requirement/demand` | POST | `req:demand:add` | 新增并提交需求 |
| `/requirement/demand` | PUT | `req:demand:edit` | 修改需求 |
| `/requirement/demand/{demandId}/status/{status}` | POST | `req:demand:edit` | 状态流转 |

新增需求时后端生成 `demandNo`，格式为 `REQ-yyyyMMdd-序号`，并将状态设为 `submitted`。

新增和修改需求时，`projectId + variantId` 必须指向同一项目下已启用且初始化完成的项目分支；未初始化完成的分支不得作为需求提交目标。分支初始化完成口径为：项目存在有效代码仓库，所选分支存在模块知识（人工模块或索引模块），且所有有效仓库都已有该分支真实 `baselineBranch` 的 `imported` 索引批次。该校验必须在后端服务层兜底，不能只依赖前端下拉过滤。

允许的状态流转：

```text
draft -> submitted
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

不允许跳转或倒退，违反时抛出业务异常 `需求状态流转不允许`。

## 执行包接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/package/{demandId}` | GET | `req:package:list` | 查询某需求全部执行包版本 |
| `/requirement/package/{demandId}/{artifactType}/latest` | GET | `req:package:list` | 查询某类产物最新版本 |
| `/requirement/package/{demandId}/{artifactType}` | POST | `req:package:save` | 保存新版本 |
| `/requirement/package/generate/{demandId}` | POST | `req:package:save` | 生成草稿执行包 |

执行包保存永远追加 `req_package_version` 新记录，不覆盖历史版本。版本号按 `demand_id + artifact_type` 独立递增。

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

动作 token 不是人员认证 Key，不能替代 MCP 请求头 `X-MCP-Key`；人员 Key 负责认证和权限，动作 token 负责让 MCP 服务识别应该调用哪个接口以及绑定到哪个项目、分支或需求上下文。任何列表、日志或前端持久化都不得展示 `token_hash`，也不得把明文 action token 写入本地存储。

## MCP人员Key管理接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/mcp/key/list` | GET | `req:mcp:key:list` | 分页查询人员 MCP Key，列表只返回 Key 前缀和绑定人员，不返回明文或哈希 |
| `/requirement/mcp/key/config` | GET | `req:mcp:key:list`、`req:mcp:key:add` 或 `req:mcp:key:edit` | 查询 MCP 地址、请求头名和 Codex 配置模板 |
| `/requirement/mcp/key/user-options` | GET | `req:mcp:key:list`、`req:mcp:key:add` 或 `req:mcp:key:edit` | 查询 MCP Key 可绑定的启用用户，只返回 `userId`、`userName`、`nickName`，不依赖 `system:user:list` |
| `/requirement/mcp/key/{keyId}` | GET | `req:mcp:key:query` | 查询单个人员 MCP Key |
| `/requirement/mcp/key` | POST | `req:mcp:key:add` | 为启用用户创建随机唯一 MCP Key，明文只在本次响应返回 |
| `/requirement/mcp/key` | PUT | `req:mcp:key:edit` | 修改 Key 名称、状态和备注；不允许变更绑定用户，停用后不能继续用于 MCP 鉴权 |
| `/requirement/mcp/key/{keyId}/regenerate` | POST | `req:mcp:key:edit` | 重置 Key 并返回一次性明文，旧 Key 立即失效 |
| `/requirement/mcp/key/{keyIds}` | DELETE | `req:mcp:key:remove` | 删除一个或多个人员 MCP Key |

人员 Key 使用 `req_mcp_user_key` 表保存，服务端只落库 SHA-256 哈希、Key 前缀、绑定用户、状态、最近使用时间和最近使用 IP。前端和列表接口不得展示 `keyHash`，明文 `plainKey` 只允许在创建和重置响应中出现一次；创建和重置接口的操作日志必须关闭响应保存，避免明文 Key 进入 `sys_oper_log`。

`/requirement/mcp/key/config` 返回的 `mcpAddress` 与 `codexConfigTemplate.url` 优先读取后端配置项 `reqflow.mcp.public-url`。该配置项应填写完整 MCP 对外访问地址，例如 `https://reqflow.example.com/requirement/mcp`；为空时服务端才按 `X-Forwarded-Proto`、`X-Forwarded-Host`、`Host` 和 `context-path` 自动推导地址。部署在反向代理、HTTPS、非默认端口或非本机访问场景时，建议显式配置 `reqflow.mcp.public-url`，避免页面展示 `localhost` 或临时代理端口。

MCP 管理菜单权限独立于需求提交权限。提需求人员角色默认不分配 `req:mcp:key:*`，管理员或平台维护人员可通过该菜单为开发人员、管理员等已启用且未删除用户创建 Key。Key 鉴权后使用绑定用户的当前菜单权限集合；绑定用户停用或删除后，即使 Key 本身仍为启用状态也必须拒绝鉴权。即使 Key 有效，调用 MCP 工具仍受 `req:package:save`、`req:index:import`、`req:project:query` 等权限限制。

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
- `get_harness_template` 必须校验 `req:project:query`，返回项目、仓库、项目分支、workspace `AGENTS.md` 内容和每个仓库的 harness 初始化指令；该工具只读平台配置，不写仓库文件、不执行 Git 或 shell。
- `register_harness_init_result` 只更新 `req_repository` 的 harness 字段，必须校验 `req:package:save`。
- `publish_repository_index` 必须校验 `req:index:import`，优先接收 `actionToken + remoteUrl`，兼容旧 `mcpKey + remoteUrl`，只写入索引批次、模块知识、影响面条目和活动日志；上传内容不得包含个人本机绝对路径。
- 报告上传、计划保存和执行资料类工具必须校验 `req:package:save`，并且只追加 `req_package_version`。
- `artifactType` 必须属于本文列出的支持类型。
