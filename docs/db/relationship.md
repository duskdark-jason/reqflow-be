# 需求管理平台数据库关系说明

表结构字典、主键、索引和关键字段说明维护在 `table-dictionary.md`；本文件只维护关系、数据粒度、必要过滤、聚合风险和开发指导。可执行 SQL、迁移脚本和菜单脚本仍保留在仓库根目录 `sql/`。

## 表与业务含义

| 表 | 业务含义 | 数据粒度 |
|---|---|---|
| `req_project` | 需求平台项目 | 一行一个项目 |
| `req_repository` | 项目下的代码仓库 | 一行一个仓库 |
| `req_variant` | 项目下的项目分支，兼容旧客户线字段 | 一行一个可展示项目分支 |
| `req_module` | 项目分支下人工模块兼容表，模块知识主来源为索引结果 | 一行一个项目分支下的人工模块或功能点 |
| `req_demand` | 需求记录 | 一行一个需求 |
| `req_package_version` | 需求执行包产物版本 | 一行一个产物版本 |
| `req_memory_index` | 项目记忆文档索引 | 一行一个可检索文档 |
| `req_repository_index_batch` | 仓库索引批次 | 一行代表某仓库某分支某 commit 的一次索引上传 |
| `req_index_module` | 仓库索引模块知识 | 一行代表索引得到的一个模块或功能点 |
| `req_impact_item` | 模块影响面条目 | 一行代表某索引批次、项目分支或真实分支下的一个页面、接口、数据表、权限或文档资源 |
| `req_mcp_user_key` | 人员 MCP 访问 Key | 一行代表一个绑定到系统用户的 MCP Key，只保存哈希和前缀 |
| `req_action_token` | MCP 动作 Token | 一行代表一个项目初始化、需求编排或开发执行动作上下文，只保存哈希和前缀 |
| `req_activity_log` | 业务事件 | 一行一次用户或 MCP 事件 |

## 关系与证据

确认关系：

- `req_repository.project_id -> req_project.project_id`，证据：DDL 索引 `idx_req_repo_project`，仓库列表按项目筛选。
- `req_variant.project_id -> req_project.project_id`，证据：唯一键 `uk_req_variant_code(project_id, variant_code)`，`mcp_key` 仅保留为 MCP 识别项目分支的兼容 key。
- `req_module.project_id -> req_project.project_id`，证据：索引 `idx_req_module_project_variant`。
- `req_module.variant_id -> req_variant.variant_id`，证据：`req_platform_req005_branch_module_migration.sql` 增加 `variant_id`，唯一键调整为 `uk_req_module_code(project_id, variant_id, module_code)`，人工模块按项目分支独立维护。
- `req_module.parent_id -> req_module.module_id`，证据：模块树形字段。
- `req_demand.project_id -> req_project.project_id`，证据：DDL 索引 `idx_req_demand_project`。
- `req_demand.variant_id -> req_variant.variant_id`，证据：DDL 索引 `idx_req_demand_variant`。
- `req_demand.module_id -> req_module.module_id`，证据：模板上下文加载模块名称。
- `req_package_version.demand_id -> req_demand.demand_id`，证据：唯一键 `uk_req_package_version(demand_id, artifact_type, version_no)`。
- `req_memory_index.project_id -> req_project.project_id`，证据：DDL 索引 `idx_req_memory_project`。
- `req_memory_index.repo_id -> req_repository.repo_id`，证据：DDL 索引 `idx_req_memory_repo`。
- `req_memory_index.variant_id -> req_variant.variant_id`，证据：字段 `variant_id` 和索引 `idx_req_memory_project_variant`；项目记忆需要同时按项目和项目分支定位。
- `req_repository_index_batch.project_id -> req_project.project_id`，证据：DDL 索引 `idx_req_index_batch_project`，索引批次查询按项目筛选。
- `req_repository_index_batch.repo_id -> req_repository.repo_id`，证据：DDL 索引 `idx_req_index_batch_repo`，MCP 导入前校验仓库属于项目。
- `req_index_module.batch_id -> req_repository_index_batch.batch_id`，证据：Mapper 插入时使用同一导入批次 ID。
- `req_index_module.project_id -> req_project.project_id`，证据：DDL 索引 `idx_req_index_module_project`。
- `req_index_module.repo_id -> req_repository.repo_id`，证据：DDL 索引 `idx_req_index_module_repo`。
- `req_index_module.variant_id -> req_variant.variant_id`，证据：字段 `variant_id` 和索引 `idx_req_index_module_variant`；项目接入中心选择项目分支时只展示该分支模块知识。
- `req_impact_item.batch_id -> req_repository_index_batch.batch_id`，证据：Mapper 插入时使用同一导入批次 ID。
- `req_impact_item.project_id -> req_project.project_id`，证据：DDL 索引 `idx_req_impact_project_module`。
- `req_impact_item.repo_id -> req_repository.repo_id`，证据：DDL 索引 `idx_req_impact_repo`。
- `req_impact_item.variant_id -> req_variant.variant_id`，证据：DDL 索引 `idx_req_impact_variant_branch`，导入时由 payload、动作 token、`mcp_key` 或 `project_id + branch_name` 反查项目分支。
- `req_mcp_user_key.user_id -> sys_user.user_id`，证据：`ReqMcpUserKeyMapper.selectReqMcpUserKeyVo` 左连接 `sys_user` 回显账号和昵称，`ReqMcpUserKeyServiceImpl.authenticate` 按绑定用户加载当前菜单权限。
- `req_action_token.project_id -> req_project.project_id`，证据：DDL 索引 `idx_req_action_token_context`，项目初始化和后续需求动作必须绑定项目上下文。
- `req_action_token.variant_id -> req_variant.variant_id`，证据：`ReqActionTokenServiceImpl.createProjectInitInstruction` 为项目分支生成动作 token，`ReqRepositoryIndexServiceImpl` 可按 `actionToken + remoteUrl` 解析项目分支。
- `req_action_token.demand_id -> req_demand.demand_id`，证据：字段预留用于 `requirement_plan` 和 `requirement_develop` 动作上下文。
- `req_activity_log.project_id -> req_project.project_id`，证据：统计按项目聚合。
- `req_activity_log.demand_id -> req_demand.demand_id`，证据：事件记录需求创建、执行包生成和报告上传。
- 项目初始化聚合关系：`req_project` 为主表，按 `project_id` 读取并同步 `req_repository`、`req_variant`、`req_module`、`req_index_module` 和 `req_repository_index_batch`，证据：`ReqProjectInitServiceImpl.selectProjectInit` 和 `ReqProjectInitServiceImpl.updateProjectInit`。

待确认关系：

- `req_demand.feature_id` 当前预留为更细功能点字段，MVP-lite 暂未建立独立表，使用前必须确认是否仍映射 `req_module.module_id`。

## 聚合风险

- `req_project` 同时 join `req_demand`、`req_repository`、`req_package_version` 时存在一对多放大风险，统计必须使用 `count(distinct ...)` 或先聚合后 join。
- `req_package_version` 对同一需求和产物类型存在多版本，查询最新版本必须按 `version_no desc limit 1`，列表页不能把历史版本误当作需求数量。
- `req_activity_log` 一名用户可能关联多个角色，用户使用统计 join `sys_user_role` 和 `sys_role` 时可能出现角色维度拆分；如果后续需要用户唯一粒度，应先按用户聚合。
- `req_impact_item` 对同一模块可跨项目分支、真实分支和多个索引批次保留历史记录。影响面推荐必须先按所选 `variant_id` 解析真实分支，再限定每个仓库最新 `imported` 批次，最后按资源键去重，不能把其他项目分支或旧批次混入推荐。
- `req_module` 在 REQ-005 后按 `project_id + variant_id + module_code` 保持唯一；新增人工模块、需求表单模块下拉和父级模块选择都必须带 `variant_id`，否则不同分支的同名模块会互相污染。
- `req_mcp_user_key` 与 `sys_user` 是多对一关系，一个用户可拥有多个 Key。列表页只能 join 用户表展示账号和昵称，不能因一个用户多个 Key 反向放大用户数量或权限判断。
- `req_action_token` 与项目、项目分支和需求都是上下文定位关系，不代表人员身份。统计或审计时不能把 action token 数量当作用户数量，也不能用 action token 绕过 `X-MCP-Key` 认证和菜单权限。
- 项目初始化上下文一次返回一个项目全貌，不能直接把 `req_repository`、`req_variant`、`req_module`、`req_index_module`、`req_repository_index_batch` 做一条 SQL join 后分页，否则会因多组一对多关系放大行数；当前实现使用分表查询后在 Service 层聚合。
- 在 REQ-004 后，`req_variant.variant_name` 兼容承载需求人员可见中文标签，`req_variant.baseline_branch` 承载真实 Git 分支名，`req_variant.mcp_key` 保留 MCP 项目分支兼容 key；REQ-003 后前端主展示和 MCP 新指令使用 `req_action_token` 生成的 `actionToken`。`variant_code` 保持唯一键需要，允许由后端根据真实分支名兜底生成。

## 必要过滤

- 基础表均含 `status` 字段，当前列表默认展示所有状态；如后续做停用隐藏，需要前后端统一过滤口径。
- 当前需求表没有逻辑删除字段，删除操作会物理删除基础数据；需求主流程暂不提供需求删除接口。
- 菜单权限依赖 RuoYi `sys_menu.perms`，按钮权限必须与 Controller `@PreAuthorize` 保持一致。
- 索引导入必须拒绝个人本机绝对路径，平台只保存 Git 远端、分支、commit、项目分支或真实分支、相对路径和结构化影响面。
- 索引导入写入前必须确认 `req_repository_index_batch`、`req_index_module` 和 `req_impact_item` 三张索引表已初始化；缺表时返回平台库初始化错误并指向 `sql/req_platform_req007_index_tables.sql`，不得写入部分批次或影响面。
- 项目初始化保存同样必须拒绝个人本机绝对路径；`req_repository.local_path_hint` 不属于初始化向导保存内容，初始化接口会清空该字段。
- 影响面推荐接收到 `variant_id` 时必须校验项目分支属于当前项目，并使用项目分支 `baseline_branch` 作为索引分支过滤条件。
- 模块知识库接收到 `variant_id` 时必须严格过滤该项目分支，不再兼容混入 `variant_id is null` 的旧项目级索引模块。
- 新增或修改需求时必须校验 `req_demand.project_id + req_demand.variant_id` 指向已初始化完成的项目分支：`req_variant.project_id` 必须等于需求项目，分支未停用，项目有效仓库不能为空，所选分支至少有一条 `req_module` 或 `req_index_module` 知识，并且每个有效仓库都有该分支 `baseline_branch` 对应的 `req_repository_index_batch.status='imported'` 批次。
- MCP 读取 `memory://{projectId}/...?...variantId={variantId}` 时必须按 `req_memory_index.project_id + req_memory_index.variant_id + doc_type` 查询；分支知识库缺少 `variant_id` 会导致同项目不同长期分支的模块、契约或决策文档混用。
- MCP 人员 Key 只允许匹配 `status='0'` 且绑定用户也为启用、未删除状态；停用 Key、停用用户或已删除用户都不能继续鉴权。Key 明文不得落库、不得出现在列表响应或操作日志中。
- MCP 动作 Token 只允许匹配 `status='0'` 且未过期记录；明文只出现在本次初始化指令响应中，服务端落库和列表只能保存哈希、前缀和上下文。`project_init` 动作必须校验 `target_method='publish_repository_index'` 后才能用于索引导入。

## 开发指导

- 新增统计时先写清输出行的数据粒度，再决定 join 顺序。
- 对 `req_package_version` 只做追加，不做覆盖更新。
- 对状态流转只能通过 Service 的状态机方法，不要在 Mapper 外直接更新状态。
- MCP 工具只能写平台表，不能扩大到仓库文件、Git 或 shell。
- MCP resource 可以读取 `req_project`、`req_repository`、`req_variant` 和 `req_memory_index`，但只能按项目和项目分支返回结构化上下文，不能代替执行器访问仓库文件系统。
- 项目初始化编辑会同步删除本次维护弹窗移除的仓库和分支配置；如果未来为索引批次增加物理外键或软删除语义，必须重新评估删除策略，避免留下不可访问的历史索引。
- `publish_repository_index` 的输入来自本地 agent 扫描结果，服务端必须再次校验路径和仓库身份，不能信任客户端；优先用 `actionToken + remoteUrl` 解析项目分支和仓库，旧 `mcpKey + remoteUrl` 只作为兼容路径。
- `publish_repository_index` 只保存平台索引，不写接入项目本地文件；项目接入初始化必须先通过 `get_harness_template` 下发文件清单，由 agent 在目标 workspace 写入本地 harness 并运行 init 校验。
- `publish_repository_index` 必须校验 `req:index:import`；执行资料保存类 MCP tool 和 harness 登记 tool 继续校验 `req:package:save`。
- 新增 MCP 管理功能时必须区分 `req_action_token`、`req_variant.mcp_key` 和 `req_mcp_user_key`：动作 token 识别目标动作和上下文，分支 mcp key 仅保留兼容，人员 MCP Key 认证人员身份，三者不能混用。
