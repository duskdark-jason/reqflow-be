# 需求管理平台数据库关系说明

## 表与业务含义

| 表 | 业务含义 | 数据粒度 |
|---|---|---|
| `req_project` | 需求平台项目 | 一行一个项目 |
| `req_repository` | 项目下的代码仓库 | 一行一个仓库 |
| `req_variant` | 项目下的客户定制线或主线范围 | 一行一个客户线 |
| `req_module` | 项目下模块或功能点 | 一行一个模块或功能点 |
| `req_demand` | 需求记录 | 一行一个需求 |
| `req_package_version` | 需求执行包产物版本 | 一行一个产物版本 |
| `req_memory_index` | 项目记忆文档索引 | 一行一个可检索文档 |
| `req_repository_index_batch` | 仓库索引批次 | 一行代表某仓库某分支某 commit 的一次索引上传 |
| `req_index_module` | 仓库索引模块知识 | 一行代表索引得到的一个模块或功能点 |
| `req_impact_item` | 模块影响面条目 | 一行代表某索引批次、客户线或基线分支下的一个页面、接口、数据表、权限或文档资源 |
| `req_activity_log` | 业务事件 | 一行一次用户或 MCP 事件 |

## 关系与证据

确认关系：

- `req_repository.project_id -> req_project.project_id`，证据：DDL 索引 `idx_req_repo_project`，仓库列表按项目筛选。
- `req_variant.project_id -> req_project.project_id`，证据：唯一键 `uk_req_variant_code(project_id, variant_code)`。
- `req_module.project_id -> req_project.project_id`，证据：唯一键 `uk_req_module_code(project_id, module_code)`。
- `req_module.parent_id -> req_module.module_id`，证据：模块树形字段。
- `req_demand.project_id -> req_project.project_id`，证据：DDL 索引 `idx_req_demand_project`。
- `req_demand.variant_id -> req_variant.variant_id`，证据：DDL 索引 `idx_req_demand_variant`。
- `req_demand.module_id -> req_module.module_id`，证据：模板上下文加载模块名称。
- `req_package_version.demand_id -> req_demand.demand_id`，证据：唯一键 `uk_req_package_version(demand_id, artifact_type, version_no)`。
- `req_memory_index.project_id -> req_project.project_id`，证据：DDL 索引 `idx_req_memory_project`。
- `req_memory_index.repo_id -> req_repository.repo_id`，证据：DDL 索引 `idx_req_memory_repo`。
- `req_repository_index_batch.project_id -> req_project.project_id`，证据：DDL 索引 `idx_req_index_batch_project`，索引批次查询按项目筛选。
- `req_repository_index_batch.repo_id -> req_repository.repo_id`，证据：DDL 索引 `idx_req_index_batch_repo`，MCP 导入前校验仓库属于项目。
- `req_index_module.batch_id -> req_repository_index_batch.batch_id`，证据：Mapper 插入时使用同一导入批次 ID。
- `req_index_module.project_id -> req_project.project_id`，证据：DDL 索引 `idx_req_index_module_project`。
- `req_index_module.repo_id -> req_repository.repo_id`，证据：DDL 索引 `idx_req_index_module_repo`。
- `req_impact_item.batch_id -> req_repository_index_batch.batch_id`，证据：Mapper 插入时使用同一导入批次 ID。
- `req_impact_item.project_id -> req_project.project_id`，证据：DDL 索引 `idx_req_impact_project_module`。
- `req_impact_item.repo_id -> req_repository.repo_id`，证据：DDL 索引 `idx_req_impact_repo`。
- `req_impact_item.variant_id -> req_variant.variant_id`，证据：DDL 索引 `idx_req_impact_variant_branch`，导入时由 payload 或 `project_id + branch_name` 反查客户线。
- `req_activity_log.project_id -> req_project.project_id`，证据：统计按项目聚合。
- `req_activity_log.demand_id -> req_demand.demand_id`，证据：事件记录需求创建、执行包生成和报告上传。

待确认关系：

- `req_demand.feature_id` 当前预留为更细功能点字段，MVP-lite 暂未建立独立表，使用前必须确认是否仍映射 `req_module.module_id`。

## 聚合风险

- `req_project` 同时 join `req_demand`、`req_repository`、`req_package_version` 时存在一对多放大风险，统计必须使用 `count(distinct ...)` 或先聚合后 join。
- `req_package_version` 对同一需求和产物类型存在多版本，查询最新版本必须按 `version_no desc limit 1`，列表页不能把历史版本误当作需求数量。
- `req_activity_log` 一名用户可能关联多个角色，用户使用统计 join `sys_user_role` 和 `sys_role` 时可能出现角色维度拆分；如果后续需要用户唯一粒度，应先按用户聚合。
- `req_impact_item` 对同一模块可跨客户线、分支和多个索引批次保留历史记录。影响面推荐必须先按所选 `variant_id` 解析基线分支，再限定每个仓库最新 `imported` 批次，最后按资源键去重，不能把其他地区客户线或旧批次混入推荐。

## 必要过滤

- 基础表均含 `status` 字段，当前列表默认展示所有状态；如后续做停用隐藏，需要前后端统一过滤口径。
- 当前需求表没有逻辑删除字段，删除操作会物理删除基础数据；需求主流程暂不提供需求删除接口。
- 菜单权限依赖 RuoYi `sys_menu.perms`，按钮权限必须与 Controller `@PreAuthorize` 保持一致。
- 索引导入必须拒绝个人本机绝对路径，平台只保存 Git 远端、分支、commit、客户线或基线分支、相对路径和结构化影响面。
- 影响面推荐接收到 `variant_id` 时必须校验客户线属于当前项目，并使用客户线 `baseline_branch` 作为索引分支过滤条件。

## 开发指导

- 新增统计时先写清输出行的数据粒度，再决定 join 顺序。
- 对 `req_package_version` 只做追加，不做覆盖更新。
- 对状态流转只能通过 Service 的状态机方法，不要在 Mapper 外直接更新状态。
- MCP 工具只能写平台表，不能扩大到仓库文件、Git 或 shell。
- `publish_repository_index` 的输入来自本地 agent 扫描结果，服务端必须再次校验路径和仓库身份，不能信任客户端。
- `publish_repository_index` 必须校验 `req:index:import`；执行资料保存类 MCP tool 和 harness 登记 tool 继续校验 `req:package:save`。
