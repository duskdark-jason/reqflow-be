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
| 仓库 CRUD | `/requirement/repository/**` | GET/POST/PUT/DELETE | `req:repo:*` | 一个代码仓库 |
| 客户线 CRUD | `/requirement/variant/**` | GET/POST/PUT/DELETE | `req:variant:*` | 一个客户定制线 |
| 模块 CRUD | `/requirement/module/**` | GET/POST/PUT/DELETE | `req:module:*` | 一个模块或功能点 |

## 项目索引接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/index/batch/list` | GET | `req:index:list` | 查询仓库索引批次 |
| `/requirement/index/module/tree` | GET | `req:index:list` | 查询索引生成的模块知识列表 |
| `/requirement/index/impact/suggest` | GET | `req:index:list` | 按项目、仓库、客户线和模块推荐影响面 |
| `/requirement/index/import` | POST | `req:index:import` | 备用 JSON 导入入口 |

索引导入只保存 Git 远端、仓库类型、分支、commit、相对路径和结构化影响面。上传内容如果包含个人本机绝对路径，服务端必须拒绝导入。

索引导入时 `branchName` 必须是当前索引分支或客户基线分支。模块和影响面 payload 可以显式携带 `variantId`；未携带时，服务端按 `projectId + branchName + status=0` 反查客户线并沉淀到索引模块和影响面条目。

影响面推荐请求可传 `projectId`、`repoId`、`variantId`、`moduleId`、`moduleCode`。当传入 `variantId` 时，服务端必须校验客户线属于当前项目，并使用该客户线 `baselineBranch` 过滤影响面；查询只返回目标仓库或每个仓库最新 `imported` 批次的数据。返回 `pages`、`apis`、`tables`、`permissions` 和 `documents` 五类列表，每一项来自 `req_impact_item`，同类资源按 `itemKey/apiPath/permissionKey/tableName/relativePath/itemName` 去重。

## 需求接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/demand/list` | GET | `req:demand:list` | 查询需求列表 |
| `/requirement/demand/{demandId}` | GET | `req:demand:query` | 查询需求详情 |
| `/requirement/demand` | POST | `req:demand:add` | 新增并提交需求 |
| `/requirement/demand` | PUT | `req:demand:edit` | 修改需求 |
| `/requirement/demand/{demandId}/status/{status}` | POST | `req:demand:edit` | 状态流转 |

新增需求时后端生成 `demandNo`，格式为 `REQ-yyyyMMdd-序号`，并将状态设为 `submitted`。

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
```

## 统计接口

| 路径 | 方法 | 权限 | 说明 |
|---|---|---|---|
| `/requirement/statistics/overview` | GET | `req:stats:view` | 需求、方案、计划、报告和活跃用户总览 |
| `/requirement/statistics/project-rank` | GET | `req:stats:view` | 按项目聚合需求与包生成率 |
| `/requirement/statistics/user-usage` | GET | `req:stats:view` | 按用户聚合提交和报告上传次数 |

统计查询必须保持项目级或用户级数据粒度，避免一对多 join 放大需求数。

## MCP 接口

入口：`POST /requirement/mcp`，Controller 粗授权为 `req:package:save` 或 `req:index:import`，Service 必须继续按 tool name 做细粒度权限校验。

支持方法：

```text
resources/list
resources/read
prompts/list
prompts/get
tools/list
tools/call
```

允许工具：

```text
save_requirement_package
save_development_plan
upload_execution_report
upload_review_report
register_harness_init_result
publish_repository_index
```

MCP 安全边界：

- 只能读取平台资源或写入平台表。
- 不允许执行 Git、shell、clone、branch、文件系统写入或大模型调用。
- `register_harness_init_result` 只更新 `req_repository` 的 harness 字段，必须校验 `req:package:save`。
- `publish_repository_index` 必须校验 `req:index:import`，只写入索引批次、模块知识、影响面条目和活动日志；上传内容不得包含个人本机绝对路径。
- 报告上传、计划保存和执行资料类工具必须校验 `req:package:save`，并且只追加 `req_package_version`。
- `artifactType` 必须属于本文列出的支持类型。
