# 需求流转与填报体验调整需求说明

## 背景

前端需求流转体验调整依赖后端 companion 同步：新增需求需要由后端自动获取创建人，需求编号需要遵循 harness `REQ-001` 风格且不带日期，默认状态需要从“已提交”改为“未提交”，状态机也要支持提需、生成需求设计、执行开发和验收闭环。

## 目标

- 新增需求时忽略客户端传入的 `creatorId` 和 `demandNo`，由后端自动填充。
- 需求编号生成改为不带日期的稳定序号格式。
- 默认状态改为未提交，提交后进入待需求分析，分析通过后再进入待生成需求设计。
- 提供开发人员可复制的需求分析指令，只绑定需求可行性评估回写动作上下文。
- 提供开发人员可复制的需求生成指令，只绑定需求设计回写动作上下文。
- 提供开发人员可复制的执行任务指令，使用同一个开发阶段 actionToken 绑定执行计划、执行报告和 Review 报告回写动作上下文。
- 提供开发人员可复制的返修任务指令，使用同一个返修阶段 actionToken 绑定执行报告和 Review 报告回写动作上下文，不包含执行计划或需求设计生成要求。
- 状态流转与前端按钮保持一致。
- 增加返修流程，记录状态事件并通过执行包版本链保留返修资料历史。
- actionToken 有效期以流程阶段为准，流转到下一流程即失效，最长保留 24 小时；项目初始化、需求分析、需求生成和合并归档 token 仅可使用一次，开发阶段 token 在当前开发阶段内可用于执行计划、执行报告和 Review 报告回写，返修阶段 token 在当前返修阶段内可用于执行报告和 Review 报告回写。
- 增加平台角色授权脚本：管理员拥有全部功能，需求人员仅开放首页、需求列表和使用统计，开发人员开放首页、需求列表、MCP 管理和使用统计，并具备 MCP 回写资料所需的隐藏保存权限。
- 调整系统 MCP 工具链边界：计划阶段先生成并回写需求可行性评估，结论允许后只生成需求设计；执行阶段生成执行计划、执行报告和 Review 报告。
- 需求创建或草稿修改时指定一个开发人员；提交后普通访问、流程动作、MCP 指令和资料包回写锁定在需求创建人与该指定开发人员之间，管理员不受参与人限制。
- 调整 MCP 项目知识库同步语义：同仓库同分支重复发布索引视为当前快照，旧模块和旧影响面失效；前后端项目提需求时以前端页面/菜单模块作为需求人员主选择。
- 需求人确认验收后不直接办结，先流转到待合并归档，由指定开发人员按指令 squash merge 本地任务分支到需求基线分支、push、更新平台知识库并删除本地开发分支。
- 平台必须验证合并归档结果：每个有效仓库都必须使用自身绑定的本需求合并归档 token 发布需求基线分支完整索引，并产生带本需求归档上下文的 imported 批次后，才允许结束任务流。
- 生成执行计划前必须先分析需求是否能拆分给多个 subagent 并行执行，只有边界清晰、无共享状态且可独立验证时才拆分。
- MCP 服务请求地址不再配置在项目 yml 中，改由系统管理员登录系统参数维护 `reqflow.mcp.public-host`，仅填写 `IP:端口`。

## 范围

本次包含：

- `ReqDemandServiceImpl` 编号、默认状态和状态事件调整。
- `ReqDemandStatusTransition` 状态机调整。
- `ReqActionTokenServiceImpl` 增加需求设计指令内容。
- 需求相关单元测试和 API/harness 文档同步。
- 角色菜单权限 SQL 和数据库关系文档同步。
- `req_demand.developer_user_id` 字段、开发人员候选接口和参与人锁定校验。
- MCP `upload_requirement_assessment`、`save_requirement_package`、`save_development_plan`、`upload_execution_report`、`upload_review_report` 的 actionToken 适配和指令文案调整。
- `publish_repository_index` 导入快照同步、索引模块最新批次查询和 MCP schema/初始化指令文案调整。
- `review -> closeout_pending -> completed` 合并归档流程、合并归档指令和平台归档验证。
- MCP Key 安装指令中的服务地址来源改为系统参数 `reqflow.mcp.public-host`，并新增系统参数初始化脚本。

本次不包含：

- 新增独立需求参与人关系表或拆分对接开发人与实际开发人。
- 平台后端自动调用大模型生成需求设计、执行计划或执行报告。
- 在需求确认后再次选择其他实际开发人员。

## 影响范围

- 接口/API：是，`POST /requirement/demand`、`POST /requirement/demand/{demandId}/status/{status}` 语义调整。
- 数据库/SQL：是，新增幂等角色授权脚本，新增需求来源、附件和指定开发人员字段升级脚本，并新增 MCP 服务 IP 端口系统参数初始化脚本。
- 权限/菜单：是，新增需求人员和开发人员角色菜单授权边界，管理员角色继续按 RuoYi 超级管理员规则拥有全部权限。
- 页面/交互：是，前端 companion 依赖新的状态语义和指令字段。
- 导出/异步/任务：否。

## 契约与数据口径

- 需求编号：后端生成 `REQ-001` 风格的递增编号，不包含日期；请求体传入编号会被覆盖。
- 创建人：`creatorId` 来自当前登录用户 ID，不使用客户端提交值。
- 指定开发人员：`developerUserId` 必须指向启用的 `requirement_developer` 用户；该字段同时表示需求设计负责人和实际开发执行人，不再拆分两段开发人员。
- 需求来源：`demandSource` 为新增和修改需求必填字段，历史数据默认归类为 `BUSINESS`。
- 业务背景与附件：`businessBackground` 保存普通文本业务背景；图片和文件通过需求上传接口返回路径后写入 `attachments`，单文件不超过 2MB。
- 默认状态：新增为 `draft`，中文语义为“未提交”。
- 主状态流转：`draft -> submitted -> plan_pending -> plan_ready -> confirmed -> developing -> review -> closeout_pending -> completed`，其中 `submitted` 表示待需求分析，`plan_pending` 表示待开发人员生成需求设计，`plan_ready` 表示需求设计待需求人员确认，`confirmed` 表示待开发人员开始开发，`closeout_pending` 表示待指定开发人员合并归档。
- 结论分支：`submitted` 和 `plan_pending` 可由指定开发人员选择退回 `supplement_required` 或结束到 `rejected`；需求创建人在 `supplement_required` 提交补充说明后回到 `plan_pending`；需求创建人在 `plan_ready` 需求设计待确认阶段可提交需求设计调整说明并回到 `plan_pending`，指定开发人员必须重新生成并回写新的需求设计后，才能再次提交给需求人确认，支持多轮迭代。
- 自动草稿：需求从 `draft` 提交到 `submitted` 后，后端自动生成 `requirement_draft` 和 `context_manifest`，供 MCP 读取基础需求和上下文清单。
- 返修流转：`review -> repairing -> review`，返修完成后重新走验收确认。
- 兼容状态：`repairing`、`archived` 可继续识别，其中 `repairing` 作为待验收返修分支。
- 需求分析阶段：`submitted` 状态下 `plan-instruction` 只指向 `upload_requirement_assessment` 回写需求可行性评估，并要求先给出可实现结论、风险和需求人补充项；该阶段不生成最终 `requirement.md`、`plan.md`、执行报告或 Review 报告。
- 需求生成阶段：`plan_pending/plan_ready` 状态下 `plan-instruction` 只指向 `save_requirement_package` 回写需求设计，并要求沿用需求分析阶段任务分支，本地只生成或调整 `requirement.md`。
- 执行阶段：`confirmed` 状态只允许指定开发人员点击开始开发；进入 `developing` 后，`develop-instruction` 才给出一个当前开发阶段有效的 actionToken，可用于 `save_development_plan`、`upload_execution_report` 和 `upload_review_report`，流转到 `review` 后立即失效；该指令不包含返修说明。
- 执行计划：生成 `plan.md` 前必须先判断需求是否适合拆分为多个 subagent 并行执行；只有职责边界清晰、无共享状态且可独立验证时才拆分，否则记录不拆分原因并保持单执行路径。
- 返修阶段：`develop-instruction` 在 `repairing` 状态只给出一个当前返修阶段有效的 actionToken，可用于 `upload_execution_report` 和 `upload_review_report`，流转回 `review` 后立即失效；该指令不包含 `save_development_plan`。
- 合并归档阶段：需求创建人在 `review` 确认验收后进入 `closeout_pending`；指定开发人员通过 `develop-instruction` 获取合并归档指令，按仓库使用一次性 `requirement_closeout/publish_repository_index` token 发布需求基线分支完整索引。平台确认每个有效仓库的归档 token 已使用，且对应仓库产生带本需求归档上下文的 imported 批次后，才允许流转到 `completed`。
- MCP 服务地址：`codexSetupPackage` 内 MCP 地址优先使用系统参数 `reqflow.mcp.public-host`，管理员只填写 `IP:端口`；后端按请求协议和 context-path 拼接 `/requirement/mcp`，系统参数为空时按请求头推导。
- 资料包读取：需求详情内嵌资料包读取可使用 `req:demand:query`，独立 Agent 资料包页面仍使用 `req:package:list` 菜单权限。
- 管理员删除：管理员拥有 `req:demand:remove` 删除需求按钮权限，删除时清理需求资料包版本和动作 token；需求人员、开发人员不展示也不能调用删除。
- 权限隔离：需求人员进入需求列表所需的项目、分支、模块和索引模块只读上下文接口可使用需求权限访问，但不展示项目管理、MCP 管理或 Agent 交接资料独立入口。
- 参与人锁定：非管理员只能看到自己创建的需求，以及提交后指定给自己的需求；详情、状态动作、MCP 指令和资料包读写均按创建人与指定开发人员校验。
- 知识库快照：`publish_repository_index` 每次发布同一仓库同一分支时，都代表当前完整知识库快照。服务端先让该仓库分支旧的活动模块和影响面失效，再写入新批次；模块知识查询只返回最新 `imported` 批次，避免已删除页面继续作为提需选项。

## 验收标准

- AC-001：新增需求后编号为 `REQ-001` 风格且不包含日期。
- AC-002：新增需求后状态为 `draft`，创建人来自当前用户，忽略客户端 `creatorId`。
- AC-003：状态机允许新主路径流转并拒绝跳转和倒退。
- AC-004：开发人员可通过需求详情获取用于 MCP 回写需求可行性评估的需求分析复制指令。
- AC-005：后端 API、数据库关系和模块 harness 文档同步记录新契约。
- AC-006：需求分析指令和需求生成指令采用初始化指令风格，分别明确当前阶段唯一 MCP 工具、`mcpServer`、`mcpTool`、`toolName`、`arguments.actionToken`，并说明 actionToken 不是 `X-MCP-Key`。
- AC-007：开发阶段可获取执行任务指令，明确调用 `reqflow.save_development_plan` 回写执行计划、调用 `reqflow.upload_execution_report` 回写执行报告、调用 `reqflow.upload_review_report` 回写 Review 报告。
- AC-008：待验收可进入返修状态，返修提交后回到待验收，并记录返修状态事件；资料历史通过 `req_package_version` 新版本保留。
- AC-009：`project_init`、`requirement_plan`、`requirement_develop`、`requirement_closeout` actionToken 以流程阶段为有效边界，流转到下一流程即失效，最长 24 小时兜底；项目初始化、需求分析、需求生成和合并归档 token 一次性消费，开发阶段 token 在 `developing` 内可多次回写本阶段产物，返修阶段 token 在 `repairing` 内可多次回写返修报告产物。
- AC-010：角色授权 SQL 幂等创建或维护 `requirement_user`、`requirement_developer` 角色；需求人员只分配需求列表和使用统计相关权限，开发人员分配需求列表、MCP 管理、使用统计和隐藏 `req:package:save` 权限，管理员角色保留全部功能。
- AC-011：`plan-instruction` 在 `submitted` 状态只允许调用 `upload_requirement_assessment`，在 `plan_pending/plan_ready` 状态只允许调用 `save_requirement_package`，两个阶段都不包含 `save_development_plan`。
- AC-012：`develop-instruction` 在 `developing` 状态包含执行计划、执行报告和 Review 报告三个目标工具，并使用同一个开发阶段 actionToken 回写；`confirmed` 状态不能生成执行指令；在 `repairing` 状态只包含执行报告和 Review 报告两个目标工具，并使用同一个返修阶段 actionToken 回写。
- AC-013：需求详情读取交接资料包不要求角色拥有独立 Agent 资料包菜单权限，具备 `req:demand:query` 即可查看当前需求资料。
- AC-014：新增和修改需求时 `demandSource` 必填；`businessBackground` 保存纯文本业务背景；图片和文件作为需求附件上传，接口单文件不超过 2MB；执行包上下文包含需求来源、业务背景和附件。
- AC-015：需求人员账号进入需求列表不出现项目、分支、模块、索引模块四类上下文接口权限不足；首页快捷入口不得展示无权限的 MCP 管理。
- AC-016：管理员可删除需求；需求人员和开发人员不可见删除按钮，服务端状态流转按角色隔离具体动作。
- AC-017：新增和修改草稿需求必须指定开发人员；提交后只有创建人、指定开发人员和管理员可查看或操作该需求，开发人员不能看到他人未提交草稿。
- AC-018：需求提交后自动生成需求草稿和上下文清单；需求分析或需求设计阶段支持“需要补充说明”和“需求无法实现”结论；需求人在待补充说明状态可提交补充说明并回到待生成需求设计。
- AC-019：需求设计待确认阶段，需求人除确认需求设计外还可提交补充调整说明；服务端追加 `requirement_supplement` 版本并把状态改回 `plan_pending`，并要求开发人员重新回写晚于补充说明的新 `requirement` 版本后才能提交 `plan_ready`。
- AC-020：MCP Key 新增时普通用户只能绑定自己，管理员才可指定绑定用户；MCP Key 不提供修改和重置接口，列表操作改为打开安装指令，创建结果必须明文展示 Key 并渲染到安装命令。
- AC-021：`publish_repository_index` 对同仓库同分支重复发布时按快照同步，旧活动模块和影响面失效；`/requirement/index/module/tree` 只返回最新 `imported` 批次活动模块，并支持前端页面模块范围过滤。
- AC-022：需求人确认验收后状态进入 `closeout_pending`；指定开发人员可生成合并归档指令，指令包含 squash merge、push、`publish_repository_index`、平台验证和删除本地开发分支步骤。
- AC-023：平台未验证所有有效仓库的归档索引结果前，`closeout_pending -> completed` 必须失败；验证通过后指定开发人员可结束任务流。
- AC-024：开发执行指令和全局 `reqflow-mcp` skill 要求在生成执行计划前先分析是否可拆分为多个 subagent 并行执行。
- AC-025：MCP 服务请求地址不再读取项目 yml；MCP Key 安装指令优先读取系统参数 `reqflow.mcp.public-host`，管理员仅需填写 `IP:端口`。

## Companion 关联

- companion spec：`../reqflow-ui/docs/specs/active/REQ-016-需求流转与填报体验调整`
- 关联分支：`feature/req-016-demand-flow-ux`

## 客户与分支

- 目标客户：通用
- 基线分支：main
- 任务分支：feature/req-016-demand-flow-ux

## 约束与假设

- 新增业务表字段仅限 `demand_source`、`attachments` 和 `developer_user_id`，通过幂等 SQL 脚本维护；角色权限和 MCP 服务 IP 端口系统参数通过幂等 SQL 脚本维护。
- 生成需求设计指令和执行任务指令只生成动作 token 和复制文本，不自动保存资料包。
