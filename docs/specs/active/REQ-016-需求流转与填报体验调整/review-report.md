# 需求流转与填报体验调整 Review 报告

## Review 结论

- 结论：通过
- Review Agent：Codex 本地复核
- Review 时间：2026-06-11

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
| AC-004 | MCP 编排指令 | `ReqDemandController`、`ReqDemandServiceImpl`、`McpService` | 单测和浏览器接口冒烟确认指令包含两个保存工具和需求 ID | 通过 |
| AC-005 | 文档同步 | API、模块、表字典、关系文档 | `sh scripts/check-docs.sh` 通过 | 通过 |
| AC-006 | 初始化式 MCP 指令 | `ReqDemandServiceImpl.requirementPlanInstructionContent` | 单测断言 `reqflow-mcp`、`mcpServer`、`mcpTool`、`arguments.actionToken` 和非 `X-MCP-Key` 说明 | 通过 |
| AC-007 | 执行开发指令 | `ReqDemandController`、`ReqDemandServiceImpl.createRequirementDevelopInstruction` | 单测断言目标工具为 `reqflow.upload_execution_report`，浏览器接口冒烟返回成功 | 通过 |
| AC-008 | 返修流转和版本历史 | `ReqDemandServiceImpl.updateDemandStatus`、执行包版本链文档 | 单测覆盖 `demand_repairing` 和 `demand_repair_submitted` 事件，文档记录 `req_package_version` 历史版本 | 通过 |
| AC-009 | actionToken 一次性和 24 小时有效 | `ReqActionTokenServiceImpl`、`ReqActionTokenMapper.xml` | `ReqActionTokenServiceImplTest` 覆盖有效期、过期拒绝、已使用拒绝和条件更新失败拒绝 | 通过 |

## 验收复核

- AC-001：通过，编号不再拼接日期。
- AC-002：通过，新增需求默认 `draft`，创建人由当前用户上下文写入。
- AC-003：通过，主状态路径与前端按钮保持一致，越级跳转仍被拒绝。
- AC-004：通过，审批人员可复制 MCP 编排指令，MCP 保存工具可用 `actionToken` 解析需求。
- AC-005：通过，长期契约文档已同步。
- AC-006：通过，MCP 编排指令具备初始化指令式字段和 `arguments.actionToken` 说明。
- AC-007：通过，执行开发指令可指向 `upload_execution_report` 回写执行报告。
- AC-008：通过，待验收可进入返修，返修完成后重新进入待验收；历史资料通过执行包版本链保留。
- AC-009：通过，初始化、需求编排和执行开发 actionToken 统一 24 小时内有效且仅可消费一次。

## 返修交接清单

| 修复 ID | 严重级别 | 关联验收 ID | 问题 | 修复要求 | 验证要求 |
|---|---|---|---|---|---|
| RF-002 | 中 | AC-006、AC-007、AC-008 | 用户反馈 MCP 指令不够清晰，并建议增加返修流程和历史版本记录 | 补齐初始化式字段、执行开发指令、返修事件和版本链文档 | 单测、接口冒烟、文档门禁 |
| RF-003 | 中 | AC-009 | 用户补充 actionToken 仅可使用一次且 24 小时内有效 | 补齐 token 过期时间、已使用拒绝、并发条件更新和指令文案 | 单测、接口冒烟、文档门禁 |

## 复审记录

| 修复 ID | 执行处理结果 | 复审结论 | 复审证据 |
|---|---|---|---|
| RF-002 | 已完成 MCP 指令、执行开发指令、返修事件和版本链说明 | 通过 | `mvn -pl ruoyi-requirement -am test`、接口冒烟、`check-docs` |
| RF-003 | 已完成 actionToken 24 小时有效和一次性消费限制 | 通过 | `ReqActionTokenServiceImplTest`、接口冒烟、`check-docs` |

- 最终结论：通过
