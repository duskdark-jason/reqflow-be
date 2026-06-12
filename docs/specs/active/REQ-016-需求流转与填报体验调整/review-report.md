# 需求流转与填报体验调整 Review 报告

## Review 结论

- 结论：通过
- Review Agent：Codex 本地复核
- Review 时间：2026-06-12

## 审查输入

- `requirement.md`
- `plan.md`
- `execution-report.md`
- 代码 diff
- 验证输出

## 问题清单

| 严重级别 | 文件 | 问题 | 风险 | 建议 |
|---|---|---|---|---|
| 无 | 无 | 未发现阻断或重要问题 | 无 | 进入完成门禁 |

## 验收 ID 覆盖矩阵

| 验收 ID | 需求描述 | 实现证据 | 验证证据 | Review 结论 |
|---|---|---|---|---|
| AC-001 | 编号无日期 | `ReqDemandServiceImpl.nextDemandNo`、`ReqDemandMapper.selectDemandCount` | `mvn -pl ruoyi-requirement -am test` 覆盖 `REQ-003`、`REQ-004`、`REQ-007` | 通过 |
| AC-002 | 默认 draft 且创建人自动获取 | `ReqDemandServiceImpl.insertReqDemand`、`updateReqDemand` | 单测覆盖客户端 `creatorId` 被覆盖、默认 `draft`、非草稿/非创建人拒绝修改 | 通过 |
| AC-003 | 状态机新主路径 | `ReqDemandStatusTransition` | `ReqDemandStatusTransitionTest` 覆盖主路径和兼容路径 | 通过 |
| AC-004 | MCP 需求评估与设计指令 | `ReqDemandController`、`ReqDemandServiceImpl`、`McpService` | 单测确认指令包含可行性评估和需求设计两个回写工具、两个 token 和需求 ID | 通过 |
| AC-005 | 文档同步 | API、模块、表字典、关系文档 | `sh scripts/check-docs.sh` 通过 | 通过 |
| AC-006 | 初始化式 MCP 指令 | `ReqDemandServiceImpl.requirementPlanInstructionContent` | 单测断言 `reqflow-mcp`、`mcpServer`、`mcpTool`、`arguments.actionToken` 和非 `X-MCP-Key` 说明 | 通过 |
| AC-007 | 执行开发指令 | `ReqDemandController`、`ReqDemandServiceImpl.createRequirementDevelopInstruction` | 单测断言目标工具为 `reqflow.upload_execution_report`，浏览器接口冒烟返回成功 | 通过 |
| AC-008 | 返修流转和版本历史 | `ReqDemandServiceImpl.updateDemandStatus`、执行包版本链文档 | 单测覆盖 `demand_repairing` 和 `demand_repair_submitted` 事件，文档记录 `req_package_version` 历史版本 | 通过 |
| AC-009 | actionToken 阶段有效和 24 小时兜底 | `ReqActionTokenServiceImpl`、`ReqActionTokenMapper.xml`、`McpService` | 单测覆盖最长有效期、普通 token 已使用拒绝、开发阶段 token 复用和需求状态流转后失效 | 通过 |
| AC-010 | 角色授权 SQL | `req_platform_req016_role_permissions.sql` | `ReqPlatformRoleSqlTest` 覆盖角色边界 | 通过 |
| AC-011 | 计划阶段先评估再生成需求设计 | `ReqDemandServiceImpl`、`McpService` | 单测断言计划 token 只允许 `upload_requirement_assessment` 和 `save_requirement_package`，不允许执行计划工具 | 通过 |
| AC-012 | 执行阶段生成计划和报告 | `ReqDemandServiceImpl`、`McpService` | 单测断言执行计划、执行报告和 Review 报告共用同一个开发阶段 actionToken | 通过 |
| AC-013 | 详情嵌入资料读取权限 | `ReqPackageController` | 权限注解复核，前端构建和浏览器详情页通过 | 通过 |
| AC-014 | 来源必填、附件和 2MB 上传 | `ReqDemandServiceImpl`、`ReqDemandController`、模板和 SQL | 来源必填、字段脚本、上传限制、文本模板转义测试通过 | 通过 |
| AC-015 | 需求列表上下文权限和首页快捷入口 | `ReqProjectController`、`ReqVariantController`、`ReqModuleController`、`ReqIndexController`、首页 | 需求上下文只读接口权限复核，前端构建通过 | 通过 |
| AC-016 | 管理员删除和流程角色隔离 | `ReqDemandController`、`ReqDemandServiceImpl`、SQL 脚本 | 单测覆盖删除链路、角色状态动作拦截和 SQL 权限边界 | 通过 |
| AC-017 | 单一指定开发人员和参与人锁定 | `ReqDemandServiceImpl`、`ReqDemandMapper.xml`、`ReqPackageServiceImpl`、`McpService`、前端状态按钮过滤 | 单测覆盖指定开发人员、非参与人拒绝和 SQL 字段；前端构建通过 | 通过 |

## 验收复核

- AC-001：通过，编号不再拼接日期。
- AC-002：通过，新增需求默认 `draft`，创建人由当前用户上下文写入。
- AC-003：通过，主状态路径与前端按钮保持一致，越级跳转仍被拒绝。
- AC-004：通过，审批人员可复制 MCP 需求评估与设计指令，MCP 保存工具可用 `actionToken` 解析需求。
- AC-005：通过，长期契约文档已同步。
- AC-006：通过，MCP 需求评估与设计指令具备初始化指令式字段和 `arguments.actionToken` 说明。
- AC-007：通过，执行开发指令可指向 `upload_execution_report` 回写执行报告。
- AC-008：通过，待验收可进入返修，返修完成后重新进入待验收；历史资料通过执行包版本链保留。
- AC-009：通过，actionToken 按流程阶段有效，流转到下一流程即失效；初始化和需求设计 token 一次性消费，开发阶段 token 在当前开发阶段内可复用。
- AC-010：通过，角色授权脚本覆盖三类角色边界。
- AC-011：通过，计划阶段先回写需求可行性评估，评估允许后只生成需求设计，不包含执行计划。
- AC-012：通过，执行阶段包含执行计划、执行报告和 Review 报告回写。
- AC-013：通过，需求详情读取资料包可使用 `req:demand:query`。
- AC-014：通过，需求来源必填、需求上传 2MB 限制和执行包上下文已覆盖。
- AC-015：通过，需求列表上下文只读接口按需求权限放行，首页快捷入口按权限过滤。
- AC-016：通过，管理员删除和流程角色隔离已覆盖。
- AC-017：通过，需求只锁定一个指定开发人员，该人员同时负责需求设计、执行开发和返修；普通访问与操作限制在创建人和该开发人员之间。

## 返修交接清单

| 修复 ID | 严重级别 | 关联验收 ID | 问题 | 修复要求 | 验证要求 |
|---|---|---|---|---|---|
| RF-002 | 中 | AC-006、AC-007、AC-008 | 用户反馈 MCP 指令不够清晰，并建议增加返修流程和历史版本记录 | 补齐初始化式字段、执行开发指令、返修事件和版本链文档 | 单测、接口冒烟、文档门禁 |
| RF-003 | 中 | AC-009 | 用户补充 actionToken 应按流程阶段有效，转到下一流程即失效 | 补齐 token 过期时间、普通 token 已使用拒绝、开发阶段 token 复用、需求状态阶段校验和指令文案 | 单测、接口冒烟、文档门禁 |

## 复审记录

| 修复 ID | 执行处理结果 | 复审结论 | 复审证据 |
|---|---|---|---|
| RF-002 | 已完成 MCP 指令、执行开发指令、返修事件和版本链说明 | 通过 | `mvn -pl ruoyi-requirement -am test`、接口冒烟、`check-docs` |
| RF-003 | 已完成 actionToken 流程阶段有效、24 小时兜底和开发阶段复用限制 | 通过 | `ReqActionTokenServiceImplTest`、`McpServiceTest`、接口冒烟、`check-docs` |

- 最终结论：通过
