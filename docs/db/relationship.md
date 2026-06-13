# 需求管理平台数据库关系说明

表结构字典、主键、索引和关键字段说明维护在 `table-dictionary.md`；本文件只维护关系、数据粒度、必要过滤、聚合风险和开发指导。可执行 SQL、迁移脚本和菜单脚本统一维护在 `docs/db/sql/`。

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
| `req_mcp_user_key` | 人员 MCP 访问 Key | 一行代表一个绑定到系统用户的 MCP Key，保存明文、哈希和前缀 |
| `req_action_token` | MCP 动作 Token | 一行代表一个项目初始化、需求分析、需求生成、开发执行、返修或合并归档动作上下文，只保存哈希和前缀 |
| `req_activity_log` | 业务事件 | 一行一次用户或 MCP 事件 |
| `sys_role` / `sys_role_menu` | RuoYi 角色和菜单权限关系 | 一行一个角色 / 一行一个角色菜单授权 |

## 关系与证据

确认关系：

- `req_repository.project_id -> req_project.project_id`，证据：DDL 索引 `idx_req_repo_project`，仓库列表按项目筛选。
- `req_variant.project_id -> req_project.project_id`，证据：唯一键 `uk_req_variant_code(project_id, variant_code)`，`mcp_key` 仅保留为 MCP 识别项目分支的兼容 key。
- `req_module.project_id -> req_project.project_id`，证据：索引 `idx_req_module_project_variant`。
- `req_module.variant_id -> req_variant.variant_id`，证据：发布基线 schema 中 `req_module` 包含 `variant_id`，唯一键为 `uk_req_module_code(project_id, variant_id, module_code)`，人工模块按项目分支独立维护。
- `req_module.parent_id -> req_module.module_id`，证据：模块树形字段。
- `req_demand.project_id -> req_project.project_id`，证据：DDL 索引 `idx_req_demand_project`。
- `req_demand.variant_id -> req_variant.variant_id`，证据：DDL 索引 `idx_req_demand_variant`。
- `req_demand.module_id -> req_module.module_id`，证据：模板上下文加载模块名称。
- `req_demand.developer_user_id -> sys_user.user_id`，证据：DDL 索引 `idx_req_demand_developer`，需求列表和详情左连接用户表回显指定开发人员。
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
- `sys_role_menu.role_id -> sys_role.role_id`，证据：RuoYi 系统表和 `docs/db/sql/req_platform_release_settings.sql` 为 `requirement_user`、`requirement_developer` 分配需求平台菜单权限。
- `sys_role_menu.menu_id -> sys_menu.menu_id`，证据：RuoYi 系统表和 `docs/db/sql/req_platform_menu.sql` 创建需求管理菜单及按钮权限。
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
- `requirement_plan` 动作 token 可绑定 `req_action_token.demand_id`，`target_method=requirement_analysis` 时只供 MCP `upload_requirement_assessment` 定位需求并回写需求可行性评估，`target_method=requirement_generate` 时只供 MCP `save_requirement_package` 定位需求并回写需求设计；`requirement_develop` 动作 token 可绑定同一字段，`target_method=requirement_develop` 时以一个开发阶段 token 供 MCP `save_development_plan`、`upload_execution_report` 和 `upload_review_report` 定位需求并回写执行计划、执行报告和 Review 报告，`target_method=requirement_repair` 时以一个返修阶段 token 供 MCP `upload_execution_report` 和 `upload_review_report` 定位需求并回写返修执行报告和复审报告；`requirement_closeout` 动作 token 绑定同一需求，并通过 `req_action_token.remark=closeoutRepoId={repoId}` 绑定目标仓库，`target_method=publish_repository_index` 时只供指定开发人员在 `closeout_pending` 阶段发布对应仓库在需求基线分支的完整知识库快照；人员权限仍由登录态或 `X-MCP-Key` 决定。
- `req_action_token.expire_time` 和 `last_used_time` 是安全边界字段：动作 token 以需求流程阶段为有效边界，流转到下一流程即失效；`expire_time` 作为最长 24 小时兜底，不能作为长期项目、分支或需求缓存键。项目初始化、需求分析、需求生成和合并归档 token 一次性消费，开发阶段 token 在 `developing` 内可重复用于本阶段回写并刷新 `last_used_time`，返修阶段 token 在 `repairing` 内可重复用于本阶段报告回写并刷新 `last_used_time`。
- `req_demand.developer_user_id` 与 `sys_user` 是多对一展示关系；列表只允许左连接回显开发人员账号和昵称，不能因角色表 join 放大需求行数。开发人员候选列表可 join `sys_user_role` 和 `sys_role`，但必须使用 `distinct u.user_id`。
- `sys_role_menu` 与菜单权限是多对多关系，角色授权脚本会重置 `requirement_user` 和 `requirement_developer` 的菜单集合；开发人员角色包含隐藏 `req:package:save` 以允许 MCP 回写资料，但不分配独立“需求执行包”菜单。
- 项目初始化上下文一次返回一个项目全貌，不能直接把 `req_repository`、`req_variant`、`req_module`、`req_index_module`、`req_repository_index_batch` 做一条 SQL join 后分页，否则会因多组一对多关系放大行数；当前实现使用分表查询后在 Service 层聚合。
- 在 REQ-004 后，`req_variant.variant_name` 兼容承载需求人员可见中文标签，`req_variant.baseline_branch` 承载真实 Git 分支名，`req_variant.mcp_key` 保留 MCP 项目分支兼容 key；REQ-003 后前端主展示和 MCP 新指令使用 `req_action_token` 生成的 `actionToken`。`variant_code` 保持唯一键需要，允许由后端根据真实分支名兜底生成。

## 必要过滤

- 基础表均含 `status` 字段，当前列表默认展示所有状态；如后续做停用隐藏，需要前后端统一过滤口径。
- 当前需求表没有逻辑删除字段，管理员删除需求会物理删除基础数据，并同步删除该需求的资料包版本和动作 token；需求人员和开发人员主流程不分配删除权限。
- 菜单权限依赖 RuoYi `sys_menu.perms`，按钮权限必须与 Controller `@PreAuthorize` 保持一致。
- 角色初始化脚本 `docs/db/sql/req_platform_release_settings.sql` 必须在菜单脚本之后执行；需求人员角色不得分配 `req:mcp:key:*`、`req:package:*` 或 `req:project:*`，开发人员角色不得分配 `req:demand:add`、`req:project:*` 或 `req:index:*`。
- 索引导入必须拒绝个人本机绝对路径，平台只保存 Git 远端、分支、commit、项目分支或真实分支、相对路径和结构化影响面。
- 索引导入写入前必须确认 `req_repository_index_batch`、`req_index_module` 和 `req_impact_item` 三张索引表已初始化；缺表时返回平台库初始化错误并指向 `docs/db/sql/req_platform_schema.sql` 中对应建表段，不得写入部分批次或影响面。
- 项目初始化保存同样必须拒绝个人本机绝对路径；`req_repository.local_path_hint` 不属于初始化向导保存内容，初始化接口会清空该字段。
- 影响面推荐接收到 `variant_id` 时必须校验项目分支属于当前项目，并使用项目分支 `baseline_branch` 作为索引分支过滤条件。
- 模块知识库接收到 `variant_id` 时必须严格过滤该项目分支，不再兼容混入 `variant_id is null` 的旧项目级索引模块。
- `publish_repository_index` 重复发布同一仓库同一分支时视为当前快照同步：服务端会把该仓库分支旧的 `req_index_module` 和 `req_impact_item` 活动数据置为停用，再写入新批次；模块知识查询还必须限制为每个仓库和真实分支最新 `imported` 批次，避免已删除页面继续出现在提需模块中。
- 新增或修改需求时必须校验 `req_demand.project_id + req_demand.variant_id` 指向已初始化完成的项目分支：`req_variant.project_id` 必须等于需求项目，分支未停用，项目有效仓库不能为空，并且每个有效仓库都有该分支 `baseline_branch` 对应的 `req_repository_index_batch.status='imported'` 批次；新功能提需允许暂时没有既有 `req_module` 或 `req_index_module` 知识。
- 普通编辑需求只能处理 `status='draft'` 且 `creator_id` 为当前用户的记录；状态字段必须通过状态接口按状态机流转，不得通过普通编辑绕过。
- 新增或修改草稿需求必须指定启用的 `requirement_developer` 用户作为 `developer_user_id`。非管理员查询需求列表时只返回当前用户创建的需求，以及已提交后指定给当前用户开发的需求；开发人员不能看到他人尚未提交的草稿。
- 需求详情、资料包读取、MCP 需求资源读取和状态流转必须校验参与人：创建人负责提交需求、补充说明、确认需求设计、提交返修和确认验收；指定开发人员负责需求分析结论、需求设计结论、开始开发、提交验收和返修提交验收，并可生成需求设计/执行任务指令和回写资料包；管理员不受参与人限制。
- 需求填报字段由发布基线 schema 维护；`demand_source` 是必填业务字段，`attachments` 是逗号分隔的上传路径串，不建立独立附件表。
- 指定开发人员字段由发布基线 schema 维护；历史需求允许 `developer_user_id` 为空，但新建和草稿编辑由服务层强制补齐。
- MCP 读取 `memory://{projectId}/...?...variantId={variantId}` 时必须按 `req_memory_index.project_id + req_memory_index.variant_id + doc_type` 查询；分支知识库缺少 `variant_id` 会导致同项目不同长期分支的模块、契约或决策文档混用。
- MCP 人员 Key 只允许匹配 `status='0'` 且绑定用户也为启用、未删除状态；停用 Key、停用用户或已删除用户都不能继续鉴权。`plain_key` 持久保存用于后续安装命令渲染，但页面列表不得展示明文 Key、Key 前缀或哈希，操作日志和活动记录不得写入明文。
- MCP 动作 Token 只允许匹配 `status='0'` 且未过期记录；明文只出现在本次指令响应中，服务端落库和列表只能保存哈希、前缀和上下文。`project_init` 动作必须校验 `target_method='publish_repository_index'` 后才能用于项目接入索引导入；`requirement_closeout` 动作也使用 `publish_repository_index`，但还必须校验需求处于 `closeout_pending`、token 绑定项目分支与仓库一致，并在需求办结前形成每个有效仓库对应的本需求归档 `imported` 批次。批次上下文写入 `req_repository_index_batch.remark=closeoutDemandId={demandId};repoId={repoId}`，旧批次不能作为本轮归档证据。

## 开发指导

- 新增统计时先写清输出行的数据粒度，再决定 join 顺序。
- 对 `req_package_version` 只做追加，不做覆盖更新。
- 对状态流转只能通过 Service 的状态机方法，不要在 Mapper 外直接更新状态。
- 需求设计阶段先通过 `req_package_version` 追加需求可行性评估版本，评估允许继续后再追加需求设计版本；验收返修不创建新需求，状态按 `review -> repairing -> review` 循环，返修版本通过 `req_package_version` 追加需求设计、执行方案、执行报告和 Review 报告版本体现。
- 需求编号由服务端生成 `REQ-001` 风格序号，不包含日期；新增请求中的 `demand_no` 和 `creator_id` 只作为客户端输入噪声处理，不参与最终落库值。
- 需求参与人锁定只使用一个指定开发人员字段，不再拆分需求对接人和实际开发人；用户需求中提到的“开发人员”均映射为 `req_demand.developer_user_id`。
- MCP 工具只能写平台表，不能扩大到仓库文件、Git 或 shell。
- MCP resource 可以读取 `req_project`、`req_repository`、`req_variant` 和 `req_memory_index`，但只能按项目和项目分支返回结构化上下文，不能代替执行器访问仓库文件系统。
- 项目初始化编辑会同步删除本次维护弹窗移除的仓库和分支配置；如果未来为索引批次增加物理外键或软删除语义，必须重新评估删除策略，避免留下不可访问的历史索引。
- `publish_repository_index` 的输入来自本地 agent 扫描结果，服务端必须再次校验路径和仓库身份，不能信任客户端；优先用 `actionToken + remoteUrl` 解析项目分支和仓库，旧 `mcpKey + remoteUrl` 只作为兼容路径。
- `publish_repository_index` 只保存平台索引，不写接入项目本地文件；项目接入初始化必须先通过 `get_harness_template` 下发文件清单，由 agent 在目标 workspace 拉取最新默认基线、写入本地 harness、运行 init 校验、提交并推送初始化文件后再登记结果。
- `publish_repository_index` 无 actionToken 时必须校验 `req:index:import`；带项目初始化或合并归档 actionToken 时由索引服务按 token 范围校验项目、分支、仓库、需求状态和有效期。执行资料保存类 MCP tool 和 harness 登记 tool 继续校验 `req:package:save`。
- 新增 MCP 管理功能时必须区分 `req_action_token`、`req_variant.mcp_key` 和 `req_mcp_user_key`：动作 token 识别目标动作和上下文，分支 mcp key 仅保留兼容，人员 MCP Key 认证人员身份，三者不能混用。
- 调整需求人员或开发人员菜单范围时，必须同步 `docs/db/sql/req_platform_release_settings.sql`、前端菜单文档和 Controller 权限，避免“菜单不可见但详情资料不可读”或“MCP 可见但无法回写资料”的断裂。
