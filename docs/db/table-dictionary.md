# 需求管理平台表结构字典

本文件维护需求平台自有表的结构字典。可执行 DDL、迁移 SQL、菜单 SQL 和历史基线统一维护在 `docs/db/sql/`；本文件只记录长期可读的表用途、关键字段、键约束和维护注意事项。

## 维护范围

- 主要来源：`docs/db/sql/req_platform_schema.sql`、`docs/db/sql/req_platform_req*.sql`。
- RuoYi 系统表如 `sys_user`、`sys_menu` 只记录需求平台用到的关联点，不在本字典完整展开。
- 新增或修改表、字段、索引、约束、状态枚举或数据保留语义时，同步更新本文件。

## 表清单

| 表 | 业务含义 | 主键 | 关键唯一键或索引 | 维护注意事项 |
|---|---|---|---|---|
| `req_project` | 需求平台项目 | `project_id` | `uk_req_project_code(project_code)` | 项目编码是平台内稳定标识，不能随展示名称变化随意修改。 |
| `req_repository` | 项目下代码仓库 | `repo_id` | `idx_req_repo_project(project_id)` | `repo_url` 是远端匹配依据；`local_path_hint` 不能保存个人本机绝对路径。 |
| `req_variant` | 项目分支，兼容旧客户线语义 | `variant_id` | `uk_req_variant_code(project_id, variant_code)`、`uk_req_variant_mcp_key(mcp_key)` | `baseline_branch` 是真实 Git 基线分支；`mcp_key` 仅保留兼容识别。 |
| `req_module` | 人工维护模块或功能点 | `module_id` | `uk_req_module_code(project_id, variant_id, module_code)`、`idx_req_module_project_variant(project_id, variant_id)` | REQ-005 后模块按项目分支隔离，查询和写入必须带 `variant_id`。 |
| `req_demand` | 需求记录 | `demand_id` | `uk_req_demand_no(demand_no)`、`idx_req_demand_project(project_id)`、`idx_req_demand_variant(variant_id)` | 需求必须绑定项目和项目分支；编号使用 `REQ-001` 风格且不含日期；`feature_id` 仍是预留字段。 |
| `req_package_version` | 需求执行包产物版本 | `package_id` | `uk_req_package_version(demand_id, artifact_type, version_no)` | 同一需求和产物类型可有多版本，查询当前版本必须明确版本选择规则。 |
| `req_memory_index` | 项目记忆文档索引 | `memory_id` | `idx_req_memory_project(project_id)`、`idx_req_memory_repo(repo_id)`、`idx_req_memory_project_variant(project_id, variant_id)` | 记录文档路径、分支和摘要，不保存本机绝对路径。 |
| `req_repository_index_batch` | 仓库索引批次 | `batch_id` | `idx_req_index_batch_project(project_id)`、`idx_req_index_batch_repo(repo_id)`、`idx_req_index_batch_commit(repo_id, branch_name, commit_hash)` | 一行代表某仓库某分支某 commit 的一次索引上传。 |
| `req_index_module` | 仓库索引模块知识 | `index_module_id` | `idx_req_index_module_variant(project_id, variant_id, module_code)` | 与人工模块不同，是索引产物；按项目分支过滤，不能混入旧批次。 |
| `req_impact_item` | 模块影响面条目 | `impact_id` | `idx_req_impact_project_module(project_id, module_code)`、`idx_req_impact_variant_branch(variant_id, branch_name)`、`idx_req_impact_repo(repo_id)`、`idx_req_impact_type(item_type)` | 页面、接口、表、权限和文档资源统一记录在此表，推荐时必须按最新批次和分支去重。 |
| `req_mcp_user_key` | 人员 MCP 访问 Key | `key_id` | `uk_req_mcp_user_key_hash(key_hash)`、`idx_req_mcp_user_key_user(user_id)`、`idx_req_mcp_user_key_status(status)` | 只保存哈希和前缀；明文 Key 只能在创建或重置响应中出现。 |
| `req_action_token` | MCP 动作 Token | `token_id` | `uk_req_action_token_hash(token_hash)`、`idx_req_action_token_context(action_type, project_id, variant_id, demand_id, status)` | 用于项目初始化、编排和开发动作上下文，不代表人员身份。 |
| `req_activity_log` | 需求平台业务事件 | `id` | `idx_req_activity_user(user_id)`、`idx_req_activity_project(project_id)`、`idx_req_activity_demand(demand_id)`、`idx_req_activity_time(event_time)` | 审计和活动流使用；敏感明文、Key 和本机路径不得写入 `metadata_json`。 |

## 关键字段说明

### 项目与仓库

| 表 | 字段 | 含义 | 维护要求 |
|---|---|---|---|
| `req_project` | `project_code` | 平台内项目编码 | 保持唯一；作为需求、索引和 MCP 上下文的稳定业务键。 |
| `req_project` | `workspace_agents_template_version` | workspace AGENTS 模板版本 | 项目接入初始化或升级时更新。 |
| `req_repository` | `repo_type` | 仓库类型 | 常见值为后端、前端或文档仓，前后端 companion 关系依赖它辅助识别。 |
| `req_repository` | `repo_url` | Git 远端地址 | 开发 Key 和初始化 Key 必须用它校验当前 workspace。 |
| `req_repository` | `harness_status`、`harness_commit` | harness 初始化状态和提交 | 初始化或升级完成后同步，不能伪造未执行结果。 |

### 项目分支与模块

| 表 | 字段 | 含义 | 维护要求 |
|---|---|---|---|
| `req_variant` | `variant_code` | 项目分支编码 | 同一项目内唯一；用于平台数据分组，不等同真实 Git 分支名。 |
| `req_variant` | `baseline_branch` | 真实 Git 基线分支 | 开发 Key 必须检查当前分支与该字段一致，再创建 ASCII 任务分支。 |
| `req_variant` | `mcp_key` | 兼容旧 MCP 识别 key | 新流程优先使用 action token；保留该字段用于兼容。 |
| `req_module` | `variant_id` | 项目分支 ID | 查询、保存和下拉必须带该字段，避免不同分支同名模块互相污染。 |
| `req_module` | `module_type`、`repo_scope` | 模块类型和仓库范围 | 模块知识库需要和前端菜单、后端能力或 MCP 能力对齐。 |

### 需求与执行包

| 表 | 字段 | 含义 | 维护要求 |
|---|---|---|---|
| `req_demand` | `demand_no` | 稳定需求编号 | 与本地 spec 目录中的 `REQ-001` 类编号保持可追踪关系。 |
| `req_demand` | `status` | 需求状态 | 新增默认 `draft`；主流程为 `draft -> submitted -> plan_ready -> confirmed -> developing -> review -> completed`，兼容 `plan_pending`、`repairing`、`archived`。 |
| `req_demand` | `creator_id` | 需求创建人用户 ID | 新增时由服务端当前登录用户写入；普通编辑只允许创建人在 `draft` 状态修改。 |
| `req_demand` | `project_id`、`variant_id`、`module_id` | 需求归属 | 保存前必须校验项目分支归属和仓库索引证据；新功能提需可以没有既有模块知识。 |
| `req_demand` | `impact_page`、`impact_api`、`impact_data`、`impact_permission` | 影响面摘要 | 需求编排和开发计划使用，不能替代详细设计文档。 |
| `req_package_version` | `artifact_type`、`version_no` | 产物类型和版本号 | 多版本并存，查询最新版本时不能直接 join 后分页。 |
| `req_package_version` | `content` | 需求设计、计划或报告内容 | 内容可能较大，列表查询避免直接加载。 |

### 索引与知识库

| 表 | 字段 | 含义 | 维护要求 |
|---|---|---|---|
| `req_memory_index` | `doc_type`、`doc_path`、`doc_title` | 文档类型、路径和标题 | 文档路径使用仓库相对路径或平台路径，不保存个人本机目录。 |
| `req_repository_index_batch` | `branch_name`、`commit_hash`、`index_version` | 索引来源版本 | 影响面推荐必须限定最新 `imported` 批次。 |
| `req_repository_index_batch` | `module_count`、`page_count`、`api_count`、`table_count`、`permission_count`、`document_count` | 索引统计 | 仅代表批次摘要，不能当作真实业务数量。 |
| `req_index_module` | `batch_id`、`variant_id`、`module_code` | 索引模块定位 | 按批次、项目分支和模块编码定位；不能跨分支混用。 |
| `req_impact_item` | `item_type`、`item_key`、`relative_path` | 影响资源类型、业务键和相对路径 | 推荐和展示时按资源键去重；路径必须是仓库相对路径。 |

### MCP 与审计

| 表 | 字段 | 含义 | 维护要求 |
|---|---|---|---|
| `req_mcp_user_key` | `user_id` | 绑定系统用户 | 关联 RuoYi `sys_user.user_id`；权限仍按用户菜单权限判断。 |
| `req_mcp_user_key` | `key_prefix`、`key_hash` | Key 前缀和哈希 | 明文不得落库、不得写日志、不得进入活动记录。 |
| `req_action_token` | `action_type`、`target_method` | 动作类型和目标 MCP 方法 | 用于限定初始化、编排或开发动作，不替代 `X-MCP-Key` 认证。 |
| `req_action_token` | `project_id`、`variant_id`、`demand_id` | 动作上下文 | 必须和平台返回的项目、分支和需求一致。 |
| `req_activity_log` | `event_type`、`metadata_json` | 事件类型和扩展信息 | 扩展 JSON 只存可审计摘要，不写敏感明文。 |

## 系统表关联点

| 系统表 | 使用位置 | 注意事项 |
|---|---|---|
| `sys_user` | `req_mcp_user_key.user_id`、需求创建人、活动用户 | 查询用户显示信息时避免因角色表 join 放大行数。 |
| `sys_menu` | 需求平台菜单、按钮权限和 MCP Key 菜单 | 菜单 SQL 保留在 `docs/db/sql/req_platform_menu.sql` 和增量 SQL 中，权限标识需与 Controller 注解一致。 |

## 更新检查

- 表、字段、索引或约束变化：更新本文件。
- 关系、join、聚合、分页粒度或过滤条件变化：更新 `relationship.md`。
- 可执行 DDL、DML、迁移或修复脚本：保留在 `docs/db/sql/`，并在当前 spec 的 `execution-report.md` 记录路径。索引表缺失时先执行 `docs/db/sql/req_platform_req007_index_tables.sql` 补齐 `req_repository_index_batch`、`req_index_module` 和 `req_impact_item`。
- 只改文档不改 SQL 时：在 `execution-report.md` 写明数据库影响为文档更新，无执行脚本。
