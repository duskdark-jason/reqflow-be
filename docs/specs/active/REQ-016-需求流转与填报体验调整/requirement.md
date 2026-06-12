# 需求流转与填报体验调整需求说明

## 背景

前端需求流转体验调整依赖后端 companion 同步：新增需求需要由后端自动获取创建人，需求编号需要遵循 harness `REQ-001` 风格且不带日期，默认状态需要从“已提交”改为“未提交”，状态机也要支持提需、生成需求设计、执行开发和验收闭环。

## 目标

- 新增需求时忽略客户端传入的 `creatorId` 和 `demandNo`，由后端自动填充。
- 需求编号生成改为不带日期的稳定序号格式。
- 默认状态改为未提交，提交后进入待生成需求设计。
- 提供开发人员可复制的生成需求设计指令，绑定需求设计回写动作上下文。
- 提供开发人员可复制的执行任务指令，绑定执行计划和执行报告回写动作上下文。
- 状态流转与前端按钮保持一致。
- 增加返修流程，记录状态事件并通过执行包版本链保留返修资料历史。
- 初始化、需求编排和执行开发 actionToken 仅可使用一次，24 小时内有效，过期或已使用后需重新生成。
- 增加平台角色授权脚本：管理员拥有全部功能，需求人员仅开放首页、需求列表和使用统计，开发人员开放首页、需求列表、MCP 管理和使用统计，并具备 MCP 回写资料所需的隐藏保存权限。
- 调整系统 MCP 工具链边界：计划阶段只生成需求设计，执行阶段生成执行计划和执行报告。
- 需求创建或草稿修改时指定一个开发人员；提交后普通访问、流程动作、MCP 指令和资料包回写锁定在需求创建人与该指定开发人员之间，管理员不受参与人限制。

## 范围

本次包含：

- `ReqDemandServiceImpl` 编号、默认状态和状态事件调整。
- `ReqDemandStatusTransition` 状态机调整。
- `ReqActionTokenServiceImpl` 增加需求编排指令内容。
- 需求相关单元测试和 API/harness 文档同步。
- 角色菜单权限 SQL 和数据库关系文档同步。
- `req_demand.developer_user_id` 字段、开发人员候选接口和参与人锁定校验。
- MCP `save_requirement_package`、`save_development_plan`、`upload_execution_report` 的 actionToken 适配和指令文案调整。

本次不包含：

- 新增独立需求参与人关系表或拆分对接开发人与实际开发人。
- 平台后端自动调用大模型生成需求设计、执行计划或执行报告。
- 在需求确认后再次选择其他实际开发人员。

## 影响范围

- 接口/API：是，`POST /requirement/demand`、`POST /requirement/demand/{demandId}/status/{status}` 语义调整。
- 数据库/SQL：是，新增幂等角色授权脚本，并新增需求来源、附件和指定开发人员字段升级脚本。
- 权限/菜单：是，新增需求人员和开发人员角色菜单授权边界，管理员角色继续按 RuoYi 超级管理员规则拥有全部权限。
- 页面/交互：是，前端 companion 依赖新的状态语义和指令字段。
- 导出/异步/任务：否。

## 契约与数据口径

- 需求编号：后端生成 `REQ-001` 风格的递增编号，不包含日期；请求体传入编号会被覆盖。
- 创建人：`creatorId` 来自当前登录用户 ID，不使用客户端提交值。
- 指定开发人员：`developerUserId` 必须指向启用的 `requirement_developer` 用户；该字段同时表示需求设计负责人和实际开发执行人，不再拆分两段开发人员。
- 需求来源：`demandSource` 为新增和修改需求必填字段，历史数据默认归类为 `BUSINESS`。
- 业务背景与附件：`businessBackground` 支持富文本 HTML 和粘贴图片；`attachments` 保存通过需求上传接口返回的文件路径，单文件不超过 2MB。
- 默认状态：新增为 `draft`，中文语义为“未提交”。
- 主状态流转：`draft -> submitted -> plan_ready -> confirmed -> developing -> review -> completed`，其中 `submitted` 表示待开发人员生成需求设计，`plan_ready` 表示需求设计待需求人员确认，`confirmed` 表示待开发人员生成执行计划并开始开发。
- 返修流转：`review -> repairing -> review`，返修完成后重新走验收确认。
- 兼容状态：旧 `plan_pending`、`repairing`、`archived` 可继续识别，其中 `repairing` 作为待验收返修分支。
- 计划阶段：`plan-instruction` 只指向 `save_requirement_package`，只能回写需求设计。
- 执行阶段：`develop-instruction` 同时给出执行计划和执行报告两个 24 小时内有效、仅可使用一次的 actionToken，分别指向 `save_development_plan` 和 `upload_execution_report`。
- 资料包读取：需求详情内嵌资料包读取可使用 `req:demand:query`，独立 Agent 资料包页面仍使用 `req:package:list` 菜单权限。
- 管理员删除：管理员拥有 `req:demand:remove` 删除需求按钮权限，删除时清理需求资料包版本和动作 token；需求人员、开发人员不展示也不能调用删除。
- 权限隔离：需求人员进入需求列表所需的项目、分支、模块和索引模块只读上下文接口可使用需求权限访问，但不展示项目管理、MCP 管理或 Agent 交接资料独立入口。
- 参与人锁定：非管理员只能看到自己创建的需求，以及提交后指定给自己的需求；详情、状态动作、MCP 指令和资料包读写均按创建人与指定开发人员校验。

## 验收标准

- AC-001：新增需求后编号为 `REQ-001` 风格且不包含日期。
- AC-002：新增需求后状态为 `draft`，创建人来自当前用户，忽略客户端 `creatorId`。
- AC-003：状态机允许新主路径流转并拒绝跳转和倒退。
- AC-004：开发人员可通过需求详情获取用于 MCP 生成并回写需求设计的复制指令。
- AC-005：后端 API、数据库关系和模块 harness 文档同步记录新契约。
- AC-006：生成需求设计指令采用初始化指令风格，明确 `mcpServer`、`mcpTool`、`toolName`、`arguments.actionToken`，并说明 actionToken 不是 `X-MCP-Key`。
- AC-007：开发阶段可获取执行任务指令，明确调用 `reqflow.save_development_plan` 回写执行计划、调用 `reqflow.upload_execution_report` 回写执行报告。
- AC-008：待验收可进入返修状态，返修提交后回到待验收，并记录返修状态事件；资料历史通过 `req_package_version` 新版本保留。
- AC-009：`project_init`、`requirement_plan`、`requirement_develop` actionToken 生成后 24 小时内有效且仅可成功解析一次；已过期或 `last_used_time` 非空时拒绝使用，重新执行需重新生成指令。
- AC-010：角色授权 SQL 幂等创建或维护 `requirement_user`、`requirement_developer` 角色；需求人员只分配需求列表和使用统计相关权限，开发人员分配需求列表、MCP 管理、使用统计和隐藏 `req:package:save` 权限，管理员角色保留全部功能。
- AC-011：`plan-instruction` 指令文案和 MCP token 只允许调用 `save_requirement_package` 保存需求设计，不再包含 `save_development_plan`。
- AC-012：`develop-instruction` 指令文案包含执行计划和执行报告两个目标工具，并分别使用一次性 actionToken 回写 `save_development_plan` 和 `upload_execution_report`。
- AC-013：需求详情读取交接资料包不要求角色拥有独立 Agent 资料包菜单权限，具备 `req:demand:query` 即可查看当前需求资料。
- AC-014：新增和修改需求时 `demandSource` 必填；`businessBackground` 可保存富文本图片；需求图片和附件上传接口单文件不超过 2MB；执行包上下文包含需求来源、业务背景和附件。
- AC-015：需求人员账号进入需求列表不出现项目、分支、模块、索引模块四类上下文接口权限不足；首页快捷入口不得展示无权限的 MCP 管理。
- AC-016：管理员可删除需求；需求人员和开发人员不可见删除按钮，服务端状态流转按角色隔离具体动作。
- AC-017：新增和修改草稿需求必须指定开发人员；提交后只有创建人、指定开发人员和管理员可查看或操作该需求，开发人员不能看到他人未提交草稿。

## Companion 关联

- companion spec：`../reqflow-ui/docs/specs/active/REQ-016-需求流转与填报体验调整`
- 关联分支：`feature/req-016-demand-flow-ux`

## 客户与分支

- 目标客户：通用
- 基线分支：main
- 任务分支：feature/req-016-demand-flow-ux

## 约束与假设

- 新增业务表字段仅限 `demand_source`、`attachments` 和 `developer_user_id`，通过幂等 SQL 脚本维护；角色权限通过幂等 SQL 脚本维护。
- 生成需求设计指令和执行任务指令只生成动作 token 和复制文本，不自动保存资料包。
