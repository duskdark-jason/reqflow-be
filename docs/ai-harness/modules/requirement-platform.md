# 需求管理平台后端模块 Harness

## 业务目的

本模块承接“需求管理”菜单下的后端能力，支撑项目接入、需求提交、Agent 交接资料、MCP 调用、分支知识库和使用统计。前端 companion 仓库通过 `reqflow-ui/src/api/requirement/**` 调用本模块接口。后续需求变更时，必须从菜单和子菜单定位到对应 Controller、Service、Mapper 和权限标识。

## 菜单与功能入口

| 菜单目录 | 子菜单/页面 | 功能说明 | 前端文件 | API 封装 | 后端接口与权限 | 后端核心文件 |
|---|---|---|---|---|---|---|
| 需求管理 | 项目管理 | 项目列表、项目维护入口、项目初始化状态 | `reqflow-ui/src/views/requirement/project/index.vue`、`maintain.vue` | `reqflow-ui/src/api/requirement/project.js`、`projectInit.js` | `/requirement/project/**`，`req:project:*`；需求表单上下文只读可使用 `req:demand:*` 读取项目列表和初始化上下文 | `ReqProjectController`、`ReqProjectInitController`、`ReqProjectServiceImpl`、`ReqProjectInitServiceImpl` |
| 需求管理 | 分支知识库详情页签 | 按项目分支查看模块知识、索引批次和初始化指令 | `reqflow-ui/src/views/requirement/project/knowledge.vue` | `reqflow-ui/src/api/requirement/index.js`、`project.js` | `/requirement/index/module/tree`，管理页使用 `req:index:list`，需求表单只读可使用 `req:demand:*`；`/requirement/index/batch/list`，`req:index:list` | `ReqIndexController`、`ReqRepositoryIndexServiceImpl`、`ReqIndexModuleMapper`、`ReqRepositoryIndexBatchMapper` |
| 需求管理 | 需求列表 | 需求维护页签、新增、编辑、查询、删除、状态流转、返修流转、生成需求设计指令、执行任务指令和合并归档指令 | `reqflow-ui/src/views/requirement/demand/index.vue`、`maintain.vue`、`detail.vue` | `reqflow-ui/src/api/requirement/demand.js`、`index.js` | `/requirement/demand/**`，`req:demand:*`；管理员删除使用 `req:demand:remove`；`/requirement/index/impact/suggest` 需求表单可使用 `req:demand:add/edit/query` | `ReqDemandController`、`ReqDemandServiceImpl`、`ReqDemandStatusTransition`、`ReqIndexController`、`ReqRepositoryIndexServiceImpl` |
| 需求管理 | 需求执行包 | 保存和读取需求可行性评估、需求设计、执行计划、执行报告、Review 报告等交接资料；需求详情嵌入读取可使用 `req:demand:query` | `reqflow-ui/src/views/requirement/package/index.vue`、`detail.vue` | `reqflow-ui/src/api/requirement/package.js` | `/requirement/package/**`，读取为 `req:package:list` 或 `req:demand:query`，保存为 `req:package:save` | `ReqPackageController`、`ReqPackageServiceImpl`、`ReqPackageVersionMapper` |
| 需求管理 | MCP 管理 | 管理人员 MCP Key 和管理员 MCP 请求地址配置；普通用户新增默认绑定自己且不可改绑，管理员可指定用户；后端持久保存明文 Key 用于后续安装命令渲染，页面不单独展示明文 Key 或 Key 前缀；创建后返回 Codex、Claude Code、Trae、Qoder、CodeBuddy、OpenCode 多客户端 MCP 与全局 skill 统一安装指令，执行后由用户选择安装工具 | `reqflow-ui/src/views/requirement/mcpKey/index.vue` | `reqflow-ui/src/api/requirement/mcpKey.js` | `/requirement/mcp/key/**`，`/requirement/codex/install.*`，`req:mcp:key:*`；`/requirement/mcp` | `ReqMcpKeyController`、`ReqCodexInstallController`、`ReqMcpController`、`ReqMcpUserKeyServiceImpl`、`McpService` |
| 需求管理 | 使用统计 | 需求、项目、用户和状态统计 | `reqflow-ui/src/views/requirement/statistics/index.vue` | `reqflow-ui/src/api/requirement/statistics.js` | `/requirement/statistics/**`，`req:stats:view` | `ReqStatisticsController`、`ReqStatisticsService` |
| 需求管理 | 隐藏兼容能力 | 仓库、项目分支、人工模块兼容 CRUD，不作为左侧菜单独立入口 | `reqflow-ui/src/api/requirement/repository.js`、`variant.js`、`module.js` | 同前述 API 文件 | `/requirement/repository/**`、`/requirement/variant/**`、`/requirement/module/**`，`req:repo:*`、`req:variant:*`、`req:module:*` | `ReqRepositoryController`、`ReqVariantController`、`ReqModuleController` 及对应 Service/Mapper |

## 模块文件索引

| 类型 | 优先查看文件 | 说明 |
|---|---|---|
| 菜单与权限 SQL | `docs/db/sql/req_platform_menu.sql`、`docs/db/sql/req_platform_release_settings.sql` | 需求管理一级菜单、子菜单、按钮权限、系统参数和三类角色授权。 |
| 接口契约 | `docs/ai-harness/contracts/requirement-platform-api.md` | 后端接口、MCP resource、MCP tool、知识库和初始化契约。 |
| 领域入口 | `docs/domains/requirement-platform/README.md` | 后端业务边界和长期维护规则。 |
| 后端 Controller | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/controller/` | HTTP 接口入口，随需求业务模块发布。 |
| 后端 Service | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/` | 核心业务规则。 |
| 数据访问 | `ruoyi-requirement/src/main/resources/mapper/requirement/` | Mapper XML 和 SQL。 |
| 领域对象 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/domain/` | `req_*` 表对应实体。 |
| MCP 能力 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | MCP resources、tools 和权限兜底。 |
| Harness 导航 | `docs/ai-harness/search-map.md`、`docs/process/local-harness-workflow.md` | 初次接触模型的关键词索引，以及未接入 MCP 时的本地闭环流程。 |
| Harness 模板源 | `ruoyi-requirement/src/main/resources/harness-template/` | 项目接入初始化下发的 AGENTS、docs、scripts 和检查脚本。 |

## 接口与数据契约

- 相关契约文档：`docs/ai-harness/contracts/requirement-platform-api.md`。
- 相关领域入口：`docs/domains/requirement-platform/README.md`。
- 关键菜单脚本：`docs/db/sql/req_platform_menu.sql`。
- 关键角色脚本：`docs/db/sql/req_platform_release_settings.sql`。
- 关键表结构：`docs/db/sql/req_platform_schema.sql`。
- 初始发布设置脚本：`docs/db/sql/req_platform_release_settings.sql`。

## 不变量

- 需求管理左侧菜单以“需求管理”为一级目录；项目仓库、项目分支和人工模块不再作为独立左侧菜单入口。
- 项目、仓库、项目分支和模块知识必须按项目分支隔离；不能把其他分支或旧项目级模块默认混入当前分支。
- 新增或编辑需求时，后端必须校验 `projectId + variantId` 属于同一项目，且项目分支已有仓库索引证据；新功能提需允许分支暂时没有既有模块知识。
- 新增需求时后端必须覆盖请求体中的需求编号并生成 `REQ-001` 风格编号，不包含日期；后端必须覆盖客户端 `creatorId`，以当前登录用户作为创建人，并将新需求状态置为 `draft`。
- 新增和修改需求必须提供 `demandSource`；该字段是自由文本来源说明。业务背景保存普通文本内容，图片或文件不写入 `business_background`，统一通过 `/requirement/demand/upload` 上传后追加到 `attachments`，服务端单文件最大 2MB。
- 新增和修改草稿需求必须提供 `developerUserId`，且该用户必须是启用的 `requirement_developer`；该字段就是后续需求设计、执行开发和返修的同一个指定开发人员，不拆分对接人与实际开发人。
- 普通需求编辑只允许 `draft` 状态且创建人匹配；状态变化必须通过状态流转接口，不得通过通用编辑接口绕过状态机。
- 普通用户的需求列表、详情、资料包读取和 MCP 需求资源读取必须按参与人锁定：创建人可见自己创建的需求，指定开发人员仅在需求提交后可见分配给自己的需求；管理员不受参与人限制。
- 删除需求只开放给管理员按钮权限 `req:demand:remove`，会同步删除该需求的资料包版本和动作 token；需求人员和开发人员角色脚本不得分配该权限。
- 需求主状态流转为 `draft -> submitted -> plan_pending -> plan_ready -> confirmed -> developing -> review -> closeout_pending -> completed`；需求分析或需求设计阶段可由指定开发人员流转到 `supplement_required` 或 `rejected`，需求创建人在 `supplement_required` 提交补充说明后回到 `plan_pending`；需求设计待确认 `plan_ready` 时，需求创建人也可提交“需求设计调整说明”回到 `plan_pending`，指定开发人员必须重新回写新的 `requirement` 设计版本后才能再次提交 `plan_ready`；验收阶段可走 `review -> repairing -> review` 返修分支，其中 `review -> repairing` 必须由需求创建人或管理员通过返修问题说明接口提交，不允许普通状态接口直接流转；需求创建人确认验收后进入 `closeout_pending` 等待指定开发人员合并归档，平台验证知识库归档结果后才能进入 `completed`，`archived` 仅作为历史归档状态保留；`submitted` 表示待需求分析，`supplement_required` 表示待需求补充，`plan_pending` 表示待生成需求设计，`plan_ready` 表示需求设计待确认，`confirmed` 表示待执行开发，`closeout_pending` 表示待合并归档，`rejected` 表示当前需求无法实现。
- 状态流转不仅校验 `req:demand:edit` 和状态机，还必须按角色和参与人隔离：需求创建人执行提交需求、补充说明、确认需求设计、带问题说明的返修和验收，指定开发人员执行需求分析结论、需求设计结论、开始开发、提交验收和返修验收，`admin` 可执行全部合法动作。
- 指定开发人员可通过需求详情获取 `requirement_plan` 动作 token 指令；`submitted` 状态只生成需求分析指令，使用 `target_method=requirement_analysis` 和 `upload_requirement_assessment` 回写 `requirement_assessment`；`plan_pending` 状态只生成需求生成指令，使用 `target_method=requirement_generate` 和 `save_requirement_package` 保存 `requirement`；`plan_ready` 为需求设计待确认阶段，不生成需求设计指令。需求分析阶段必须先给出可行性结论和风险，需求生成阶段只写 `requirement.md`，不能替代人员 `X-MCP-Key`。
- 指定开发人员只能在点击“开始开发”进入 `developing` 后，通过需求详情获取 `requirement_develop` 开发阶段动作 token 指令；`confirmed` 待执行开发阶段不展示也不生成执行指令。该指令只给出一个开发阶段 actionToken，可在当前开发阶段内用于 MCP `save_development_plan`、`upload_execution_report` 和 `upload_review_report`，生成 `plan.md` 前必须先分析需求是否可拆分为多个 subagent 并行执行，只有职责边界清晰、无共享状态且可独立验证时才拆分；不能替代人员 `X-MCP-Key`。
- 指定开发人员可在 `repairing` 状态通过需求详情获取 `requirement_repair` 返修阶段动作 token 指令；该指令只给出一个返修阶段 actionToken，可在当前返修阶段内用于 MCP `upload_execution_report` 和 `upload_review_report`，必须读取 Review 报告和需求人返修问题说明，不得重新生成需求设计或执行计划。返修阶段 `upload_review_report` 回写成功后，MCP 服务端必须自动尝试提交返修验收并把需求从 `repairing` 流转回 `review`；流转前必须校验最新“需求人返修问题说明”之后已经回写新的 `execution_report` 和 `review_report`，历史脏数据缺少返修说明版本时，以同一需求已存在执行报告和 Review 报告作为兼容条件。
- 指定开发人员可在 `closeout_pending` 状态通过需求详情获取 `requirement_closeout` 合并归档指令；该指令按项目分支下每个有效仓库生成一次性 `publish_repository_index` actionToken，引导本地先在任务分支完成 `docs/specs/active/REQ-*` 到 `docs/specs/done/` 的归档迁移，再把任务分支 squash merge 到需求基线分支并 push，然后按当前完整代码快照发布知识库索引，最后删除本地开发分支。需求流转到 `completed` 前，服务端必须逐仓校验本需求生成的合并归档 token 已被使用，且对应仓库产生带本需求归档上下文的 imported 索引批次；`/requirement/demand/{demandId}/closeout-verification` 复用同一验证口径返回只读验证结果，供前端在生成合并归档指令和确认归档完成之间互斥展示。
- 项目初始化、需求分析、需求生成和合并归档动作 token 生成后在当前流程阶段内有效，最长保留 24 小时，且仅可使用一次；开发阶段动作 token 在 `developing` 阶段内可重复用于执行计划、执行报告和 Review 报告回写，返修阶段动作 token 在 `repairing` 阶段内可重复用于执行报告和 Review 报告回写；需求流转到下一阶段后旧 token 立即失效，超过 24 小时也需重新生成。
- 需求提交时服务端自动追加 `requirement_draft` 和 `context_manifest` 版本，供 MCP `requirement://{demandNo}/draft-package` 和 `context-manifest` 读取；需求创建人在 `supplement_required` 状态提交补充说明、在 `plan_ready` 状态提交需求设计调整说明、或在 `review` 状态提交返修问题说明时，均追加 `requirement_supplement`，MCP 可通过 `requirement://{demandNo}/supplement` 读取最新补充、调整或返修问题内容。
- 需求资料包通过 `req_package_version` 追加版本记录，需求设计阶段保留需求草稿、需求补充、需求可行性评估和需求设计版本，返修流程依赖同一需求下执行计划、执行报告、Review 报告和需求人返修问题说明的历史版本链，不新增覆盖式更新；通用保存和 MCP 回写只允许指定开发人员或管理员，需求人补充说明和返修问题说明仅能通过专用接口写入。前端默认一级标签不展示 `requirement_supplement`，补充/调整/返修问题版本应嵌入需求可行性评估、需求设计或 Review 报告标签内作为折叠历史记录。
- 管理员角色沿用 `role_key='admin'` 超级管理员全部权限；需求人员角色 `requirement_user` 只分配需求列表和使用统计菜单权限；开发人员角色 `requirement_developer` 分配需求列表、MCP 管理、使用统计和隐藏 `req:package:save` 权限，供 MCP 回写资料。
- 需求未选择既有模块时，可以用备注承载新功能名称；执行包模块名解析顺序为人工模块、索引模块、备注。
- 人员 `X-MCP-Key` 只负责认证和权限；项目分支动作 `actionToken` 只负责动作上下文定位，二者不能互相替代。
- 发布默认后端 context-path 为 `/reqflow-api`，MCP 客户端入口为 `/reqflow-api/requirement/mcp`。前端访问项目名 `/reqflow/` 只用于静态资源和页面路由，不参与 MCP endpoint 拼接。
- 项目初始化默认复制指令只保留短动态上下文，必须包含 `reqflow-mcp`、`mcpServer: reqflow`、`toolName: publish_repository_index` 和 `mcpTool: reqflow.publish_repository_index`，确保接入项目能触发全局 skill 并定位到指定 MCP server 的指定 tool。
- `/requirement/mcp` 必须支持 MCP `initialize -> notifications/initialized -> tools/list` lifecycle；新增 tool 时必须同步 `tools/list` 的描述和 `inputSchema`。
- `/requirement/mcp` 的协议级错误必须返回标准 JSON-RPC `error.code/error.message`，不能同时带 `result:null`；`tools/call` 内的业务错误必须返回 MCP tool result，并设置 `isError=true`。
- 项目接入初始化由平台存储和下发 harness 模板，后端不直接执行 Git、shell 或写用户本地文件；执行初始化的 agent 必须在目标仓库先拉取默认基线分支最新代码，初始化校验通过后提交并推送 harness 文件，再登记初始化结果。
- 项目接入初始化下发的 harness 必须包含 `docs/ai-harness/search-map.md`、`docs/process/local-harness-workflow.md`，并在 `harness-index.json` 登记 `searchMap` 和 `localHarnessWorkflow` 入口。
- 本地 Harness 模式和 MCP 接入模式必须共享需求设计确认点：`planning` 阶段只允许迭代 `meta.md` 和 `requirement.md`；`plan.md`、`execution-report.md`、`review-report.md` 必须等明确执行授权后由 Execution Agent/Review Agent 按阶段生成。
- 项目接入初始化的模块知识库必须按前端页面业务功能优先生成：初始化 agent 先扫描前端路由、菜单、页面组件和 API 封装，再用 `publish_repository_index.modules` 按菜单目录、子菜单、隐藏页签或页面业务功能发布；纯后端仓库按 companion 前端菜单、MCP 能力或后台任务发布。不得把仓库概览、技术层目录或空数组当作模块知识库。重复发布同一仓库分支索引是快照同步，旧模块和旧影响面会失效；前后端项目给需求人员提需求时应优先选择前端页面/菜单模块。
- 用户可见系统名称统一为“统一需求流转平台”，但底层 RuoYi 包名、权限框架和通用基础能力保持兼容。

## 常见风险

- 修改项目初始化上下文时，必须同步检查前端项目管理、项目维护、分支知识库页签和需求表单的字段使用。
- 修改索引导入、模块知识查询或影响面推荐时，必须确认项目分支、真实 Git 分支、索引批次和模块知识的粒度一致，并保持活动知识库只取最新 imported 批次。
- MCP tools 新增、改名或 actionToken 解析调整时，必须同步人员权限校验、接口契约、流程阶段有效期语义、`tools/list` schema、前端文案和平台初始化指令。
- MCP 管理 Key 创建、使用指令或请求地址配置调整时，必须同步前端 MCP 管理页。页面只允许管理员通过“配置请求地址”入口打开弹窗查看和保存 MCP 请求地址配置；普通开发人员不展示配置入口。普通用户新增 Key 默认绑定自己且不可改绑，管理员才可指定绑定用户。后端保存并返回 `plainKey` 用于创建结果和后续使用指令渲染，前端下次打开使用指令时也必须从顶层 `plainKey` 或 `key.plainKey` 读取真实明文并替换安装命令占位符，但前端列表和弹窗不得单独展示明文 Key 或 Key 前缀字段，也不得展示 `keyHash`。`codexSetupPackage.packageName=reqflow-mcp-multi-client-setup`，`supportedClients` 只包含 `codex`、`claude-code`、`trae`、`qoder`、`codebuddy`、`opencode`；`installCommands[]` 是前端主展示入口，只展示一组统一安装指令，用户执行后由脚本交互选择 Codex、Claude Code、Trae、Qoder、CodeBuddy、OpenCode 或全部工具。`clientInstructions[]` 仅保留在高级 JSON 中作为按客户端排障和手工配置材料。通用安装脚本在未传 `--client`/`-Client` 时交互选择工具，传 `all` 或单个客户端时执行对应安装流程，并通过 `npx skills add ... -g -a <agent> --copy -y` 安装全局 `reqflow-mcp` skill；脚本输出必须区分 `Reqflow automatic MCP configuration completed` 和 `Manual MCP import required`，不能把仅生成 JSON 片段的客户端描述成 MCP 已安装。OpenCode 的 MCP 配置使用 `opencode.json` 的 `mcp.reqflow`，`type=remote`，并携带 `headers.X-MCP-Key`；已有可解析 JSON 配置应自动合并，无法解析时才输出片段。模板使用 `${REQFLOW_MCP_KEY}` 占位，前端仅在渲染安装命令时用响应中的 `plainKey` 替换。长 JSON 安装包仅作为高级配置/调试信息保留。MCP 服务对外 host 不得写在项目 yml 中，管理员在 MCP 管理页弹窗维护 `reqflow.mcp.public-host`，仅填写 `IP:端口` 或域名端口；后端按请求协议和后端 context-path 拼出 `/requirement/mcp`。发布默认 endpoint 为 `/reqflow-api/requirement/mcp`，不得使用前端静态项目名作为 MCP 前缀；为空时按请求头自动推导。
- `/requirement/codex/install.sh` 和 `/requirement/codex/install.ps1` 是匿名可读的多客户端通用安装脚本端点，路径名保留 Codex 仅用于兼容旧入口。脚本内容不得内置人员 Key，不得自动调用 reqflow MCP tools；脚本从 `REQFLOW_MCP_KEY` 或入参读取 Key，不带 `--client`/`-Client` 时执行后提示用户选择安装工具，带参数时执行全部或指定客户端安装流程。Codex 直接写 `~/.codex/config.toml`；Claude Code 和 CodeBuddy 优先用各自 CLI 写入；CodeBuddy CLI 不可用时按官方优先级写入或合并用户级 MCP 配置；OpenCode 写入或合并全局 `opencode.json`；Trae、Qoder 以及无法自动合并的配置输出 `Manual MCP import required`，用户完成设置页导入后才算 MCP 安装完成。`/requirement/codex/skill/SKILL.md` 是匿名可读的全局 skill 内容端点，供 `npx skills add` 命令先下载到临时 skill 目录后安装到目标客户端。
- 全局 `reqflow-mcp` skill 模板的 `SKILL.md` frontmatter 必须保持合法 YAML；`name` 和 `description` 使用双引号包裹，描述中不得出现未转义的 `: `，避免 Codex、Claude Code、Trae、Qoder、CodeBuddy 或 OpenCode 启动扫描时跳过该 skill。
- MCP lifecycle 或 HTTP Controller 调整时，必须用真实 HTTP 冒烟验证 `initialize`、`notifications/initialized`、`resources/templates/list` 和 `tools/list`，不能只看 Service 单测。
- MCP `tools/call` 错误响应调整时，必须覆盖成功、权限失败、参数校验失败和业务导入失败路径；接入项目侧不能再只看到 `Unexpected response type`，应能读到 `content` 中的业务错误。
- 项目接入初始化指令调整时，默认复制内容不得重复完整 1-7 步流程；完整顺序由全局 `reqflow-mcp` skill 承接，必须保证 agent 能先调用 `get_harness_template` 写入本地 harness，再运行 `check-docs.sh`、`check-harness.sh init`，最后才发布索引和登记初始化结果。
- Harness 模板或脚本调整时，必须同步后端模板源、当前后端 harness、前端 harness 和 `search-map.md`；确认点门禁不能只写在文档里，必须由 `scripts/check-harness.sh` 测试覆盖。`--spec` 只允许检查 `docs/specs/active/` 下执行中的需求，完成态门禁通过后才可按需归档到 `docs/specs/done/`；项目接入初始化模板也必须包含同样约束，避免新项目初始化后继续在 `done/` 中执行。
- 需求分析和需求生成指令调整时，必须保持阶段收敛：需求分析阶段只给 `upload_requirement_assessment` 和需求分析 actionToken；需求生成阶段只给 `save_requirement_package` 和需求生成 actionToken。结论允许继续后，才在需求生成阶段落地 `requirement.md`、通过 `save_requirement_package` 回写平台版本；开发阶段只能沿用该分支生成 `plan.md` 和实现。
- 返修指令调整时，必须保持同一任务分支和同一 spec 目录，只给 `upload_execution_report`、`upload_review_report` 和同一个返修阶段 actionToken，持续追加 `execution-report.md` 与 `review-report.md`，不得携带执行计划或需求设计生成要求。
- 合并归档指令调整时，必须保持“任务分支先完成 active spec 到 done 的归档迁移、squash merge 到需求基线分支、push、发布完整知识库快照、平台验证通过、删除本地开发分支”的顺序；平台未验证归档结果前不得允许需求办结。
- 项目接入初始化索引调整时，必须防止“已发布索引但没有具体业务模块”的假阳性；`actionToken` 或 `mcpKey` 项目初始化上下文下，`modules` 至少包含一个带 `moduleCode` 和 `moduleName` 的页面业务功能或后端主能力。
- MCP 下发的完整 harness 模板由后端 `ruoyi-requirement/src/main/resources/harness-template/` 保存并随包发布；`files.txt` 是下发清单。该目录是项目接入初始化模板的唯一维护源，workspace 根目录不再保留离线模板副本。
- 索引表初始化不完整时，`publish_repository_index` 必须返回指向 `docs/db/sql/req_platform_schema.sql` 对应建表段的友好业务错误，不能把 `Table ... doesn't exist` 原样作为最终结论。
- 菜单权限调整时，必须同时检查 `docs/db/sql/req_platform_menu.sql`、Controller `@PreAuthorize` 和前端按钮权限。

## 验证建议

- 最低文档门禁：`sh scripts/check-docs.sh && sh scripts/check-harness.sh init`。
- 后端契约或 Service 变更：运行 `mvn -pl ruoyi-requirement -am test`，必要时补指定测试类。
- 后端打包验证：运行 `mvn -pl ruoyi-admin -am -DskipTests package`。
- MCP 协议变更：在后端启动后用 `curl` 或 MCP 客户端验证 `initialize -> notifications/initialized -> resources/templates/list -> tools/list`，并确认工具列表含 `publish_repository_index`。
- MCP 项目接入初始化变更：验证 `resources/templates/list` 含 `skill://reqflow/project-init`，`get_harness_template` 返回 `reqflowMcpSkill`、`workspaceFiles` 和每个仓库的 `files`，且 `files` 包含 `docs/ai-harness/modules/*-page-functions.md` 非模板页面功能索引文档、完整 `docs/process/**`、完整 `docs/templates/**`、检查脚本和测试脚本。
- MCP tool 错误路径变更：用无效 `actionToken` 调用 `tools/call publish_repository_index`，确认 HTTP 响应没有顶层 protocol `error`，而是 `result.content` 中包含错误说明且 `result.isError=true`。
- 跨端流程变更：配合前端验证项目管理、项目维护、分支知识库详情、需求新增、执行包保存、MCP Key 管理和统计页面。
