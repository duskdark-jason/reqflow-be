# 需求流转与填报体验调整需求说明

## 背景

前端需求流转体验调整依赖后端 companion 同步：新增需求需要由后端自动获取创建人，需求编号需要遵循 harness `REQ-001` 风格且不带日期，默认状态需要从“已提交”改为“未提交”，状态机也要支持提需、MCP 编排、开发和验收闭环。

## 目标

- 新增需求时忽略客户端传入的 `creatorId` 和 `demandNo`，由后端自动填充。
- 需求编号生成改为不带日期的稳定序号格式。
- 默认状态改为未提交，提交后进入待生成需求说明和执行计划。
- 提供审批人员可复制的 MCP 编排指令，绑定需求动作上下文。
- 提供开发人员可复制的执行开发指令，绑定执行报告回写动作上下文。
- 状态流转与前端按钮保持一致。
- 增加返修流程，记录状态事件并通过执行包版本链保留返修资料历史。
- 初始化、需求编排和执行开发 actionToken 仅可使用一次，24 小时内有效，过期或已使用后需重新生成。

## 范围

本次包含：

- `ReqDemandServiceImpl` 编号、默认状态和状态事件调整。
- `ReqDemandStatusTransition` 状态机调整。
- `ReqActionTokenServiceImpl` 增加需求编排指令内容。
- 需求相关单元测试和 API/harness 文档同步。

本次不包含：

- 新增数据库字段或菜单权限。
- 自动调用大模型生成需求说明和执行计划。
- 实现角色级按钮权限差异。

## 影响范围

- 接口/API：是，`POST /requirement/demand`、`POST /requirement/demand/{demandId}/status/{status}` 语义调整。
- 数据库/SQL：否，复用现有 `req_demand`、`req_action_token`、`req_package_version`。
- 权限/菜单：否，沿用 `req:demand:*`、`req:package:*`。
- 页面/交互：是，前端 companion 依赖新的状态语义和指令字段。
- 导出/异步/任务：否。

## 契约与数据口径

- 需求编号：后端生成 `REQ-001` 风格的递增编号，不包含日期；请求体传入编号会被覆盖。
- 创建人：`creatorId` 来自当前登录用户 ID，不使用客户端提交值。
- 默认状态：新增为 `draft`，中文语义为“未提交”。
- 主状态流转：`draft -> submitted -> plan_ready -> confirmed -> developing -> review -> completed`。
- 返修流转：`review -> repairing -> review`，返修完成后重新走验收确认。
- 兼容状态：旧 `plan_pending`、`repairing`、`archived` 可继续识别，其中 `repairing` 作为待验收返修分支。

## 验收标准

- AC-001：新增需求后编号为 `REQ-001` 风格且不包含日期。
- AC-002：新增需求后状态为 `draft`，创建人来自当前用户，忽略客户端 `creatorId`。
- AC-003：状态机允许新主路径流转并拒绝跳转和倒退。
- AC-004：审批人员可通过需求详情获取用于 MCP 更新需求说明和执行计划的复制指令。
- AC-005：后端 API、数据库关系和模块 harness 文档同步记录新契约。
- AC-006：MCP 编排指令采用初始化指令风格，明确 `mcpServer`、`mcpTool`、`toolName`、`arguments.actionToken`，并说明 actionToken 不是 `X-MCP-Key`。
- AC-007：开发阶段可获取执行开发指令，明确调用 `reqflow.upload_execution_report` 回写执行报告。
- AC-008：待验收可进入返修状态，返修提交后回到待验收，并记录返修状态事件；资料历史通过 `req_package_version` 新版本保留。
- AC-009：`project_init`、`requirement_plan`、`requirement_develop` actionToken 生成后 24 小时内有效且仅可成功解析一次；已过期或 `last_used_time` 非空时拒绝使用，重新执行需重新生成指令。

## Companion 关联

- companion spec：`../reqflow-ui/docs/specs/active/REQ-016-需求流转与填报体验调整`
- 关联分支：`feature/req-016-demand-flow-ux`

## 客户与分支

- 目标客户：通用
- 基线分支：main
- 任务分支：feature/req-016-demand-flow-ux

## 约束与假设

- 不新增 SQL 脚本；编号格式调整不需要表结构变化。
- MCP 编排指令只生成动作 token 和复制文本，不自动保存资料包。
