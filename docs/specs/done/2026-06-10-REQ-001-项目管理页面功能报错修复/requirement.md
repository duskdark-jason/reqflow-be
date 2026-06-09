# 项目管理页面功能报错修复后端需求说明

## 背景

用户反馈当前项目“项目管理”页面还有较多功能报错。项目管理页面依赖后端 `/requirement/project/**` 与 `/requirement/project/init/**`，若接口响应结构、权限、参数校验、事务保存或数据回显存在问题，前端会在列表加载、初始化状态、维护弹窗保存、编辑回显、删除和接入中心跳转中出现功能错误。

本后端 companion 需求用于支撑前端修复：先复现页面触发的接口错误，再按项目 CRUD 与初始化聚合链路修复后端问题，并用服务测试、admin 打包和运行态接口冒烟给出证据。

## 目标

- 修复项目管理页面依赖的项目列表、详情、删除、初始化查询、新增初始化和更新初始化接口错误。
- 确保初始化保存事务一致：项目、仓库、项目分支在同一次保存中新增、更新和同步删除，失败时整体回滚。
- 确保接口返回字段与前端契约一致，尤其是 `initChecklist`、`moduleSummary`、`indexSummary`、`branchLabel`、`baselineBranch` 和 `mcpKey`。
- 确保权限标识与前端按钮一致，避免页面按钮可见但接口因权限配置错误不可用。

## 范围

本次包含：

- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqProjectController.java`
- `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqProjectInitController.java`
- `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImpl.java`
- 项目、仓库、项目分支相关 DTO、Mapper XML 和单元测试。
- `docs/ai-harness/contracts/requirement-platform-api.md` 与相关模块/领域文档的必要同步。

本次不包含：

- 不新增项目管理之外的新业务模块。
- 不重做 MCP 索引导入、需求影响面推荐、执行包或统计接口。
- 不改变 RuoYi 通用响应结构。
- 不保存个人本机绝对路径。
- 不进行数据库表结构重设计；如果执行时发现必须新增字段或迁移，先回到计划阶段补充 DDL、数据兼容和回滚方案。

## 影响范围

- 接口/API：是，涉及 `/requirement/project/**` 与 `/requirement/project/init/**`。
- 数据库/SQL：是，涉及 `req_project`、`req_repository`、`req_variant`、`req_module`、`req_repository_index_batch` 的读取、写入或统计摘要。
- 权限/菜单：是，涉及 `req:project:list`、`req:project:query`、`req:project:add`、`req:project:edit`、`req:project:remove`。
- 页面/交互：是，直接支撑前端项目管理页面与项目维护弹窗。
- 导出/异步/任务：否，本需求不涉及导出、异步任务或后台调度。

## 契约与数据口径

- 接口路径和方法：
  - `GET /requirement/project/list`：项目列表。
  - `GET /requirement/project/{projectId}`：项目详情。
  - `DELETE /requirement/project/{projectIds}`：删除项目。
  - `GET /requirement/project/init/{projectId}`：初始化上下文。
  - `POST /requirement/project/init`：新增项目初始化信息。
  - `PUT /requirement/project/init`：更新项目初始化信息。
- 请求参数：
  - 项目列表支持 `pageNum`、`pageSize`、`projectName`、`projectCode`、`status`。
  - 初始化保存请求包含 `project`、`repositories`、`variants`、`remark`。
  - 仓库行必须有仓库名称、仓库类型、Git 远端、默认分支和状态。
  - 分支行必须有中文标签 `branchLabel`、真实分支名 `baselineBranch` 和状态。
- 响应字段：
  - 列表返回 RuoYi `TableDataInfo`，包含 `rows` 和 `total`。
  - 初始化查询和保存返回 RuoYi `AjaxResult.data`，包含 `project`、`repositories`、`variants`、`moduleSummary`、`indexSummary`、`initChecklist`。
  - 分支响应必须包含 `branchLabel`、`baselineBranch`、`mcpKey`，并兼容 `variantName`、`variantCode`、`customerName`。
- 数据粒度：
  - `req_project` 一行对应一个项目。
  - `req_repository` 一行对应一个团队共享 Git 远端仓库。
  - `req_variant` 一行对应一个项目分支。
  - `initChecklist` 是单项目初始化状态摘要，不代表单仓库或单分支状态。

## 验收标准

- AC-BE-001：项目列表、详情和删除接口在认证态下返回稳定 RuoYi 结构；列表分页总数、查询条件和删除后数据变化正确。
- AC-BE-002：`GET /requirement/project/init/{projectId}` 返回项目、仓库、分支、模块摘要、索引摘要和初始化检查项；字段名与前端契约一致，空集合返回空数组或空摘要对象，不导致前端空指针。
- AC-BE-003：`POST /requirement/project/init` 可新增项目、至少一条代码仓库和至少一条项目分支；后端生成稳定 `variantCode` 和 `mcpKey`，并拒绝个人本机绝对路径。
- AC-BE-004：`PUT /requirement/project/init` 可更新项目、更新已有仓库/分支、新增仓库/分支并同步删除弹窗中移除的仓库/分支；任一子项失败时整体事务回滚。
- AC-BE-005：项目管理相关接口权限与前端按钮一致；新增使用 `req:project:add`，更新使用 `req:project:edit`，查询使用 `req:project:query` 或 `req:project:list`，删除使用 `req:project:remove`。
- AC-BE-006：后端 harness 文档同步本次修复后的接口字段、权限、数据口径、验证命令或风险点。

## Companion 关联

- companion spec：`../../../../../reqflow-ui/docs/specs/active/2026-06-10-REQ-001-项目管理页面功能报错修复`
- 关联分支：未创建

## 客户与分支

- 目标客户：通用
- 基线分支：main
- 任务分支：未创建

## 约束与假设

- 本需求来源是用户的页面报错反馈，尚未收到逐条后端异常堆栈；执行阶段必须先结合前端冒烟和后端日志建立接口错误清单。
- 执行阶段不得把未复现、未验证的问题写成已修复；超出项目管理接口范围的问题需要记录为后续需求。
- 当前仓库分支为 `main`，开始实现前必须获得任务分支/worktree 授权，或获得明确主分支修改授权并写入 `meta.md`。
