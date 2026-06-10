# MCP协议握手与工具暴露修复 Review 报告

## Review 结论

- 结论：通过
- Review Agent：Codex Review Agent
- Review 时间：2026-06-10

## 审查输入

- `docs/specs/active/2026-06-10-REQ-005-MCP协议握手与工具暴露修复/requirement.md`
- `docs/specs/active/2026-06-10-REQ-005-MCP协议握手与工具暴露修复/plan.md`
- `docs/specs/active/2026-06-10-REQ-005-MCP协议握手与工具暴露修复/execution-report.md`
- `main..HEAD` 代码 diff
- 执行报告记录的验证输出
- 相关 harness 文档：`docs/ai-harness/contracts/requirement-platform-api.md`、`docs/ai-harness/modules/requirement-platform.md`

## 问题清单

| 严重级别 | 文件 | 问题 | 风险 | 建议 |
|---|---|---|---|---|
| 无 | 无 | 未发现阻断、重要或一般问题 | 无 | 无需返修 |

## 验收 ID 覆盖矩阵

| 验收 ID | 需求描述 | 实现证据 | 验证证据 | Review 结论 |
|---|---|---|---|---|
| AC-BE-001 | `initialize` 返回成功结果和 capabilities | `McpService.initialize`、`serverCapabilities` | `McpServiceTest.initializeDeclaresMcpCapabilities`、HTTP 冒烟 `initialize_http=200` | 通过 |
| AC-BE-002 | `notifications/initialized` 被接受 | `ReqMcpController.isNotification`、`McpService.handle` | HTTP 冒烟 `initialized_http=202 size=0` | 通过 |
| AC-BE-003 | `resources/templates/list` 返回资源模板 | `McpService.resourceTemplatesList` | `McpServiceTest.resourceTemplatesListReturnsMcpTemplates`、HTTP 冒烟 `templates_has_resourceTemplates=yes` | 通过 |
| AC-BE-004 | `tools/list` 暴露 `publish_repository_index` 和 JSON Schema | `McpService.toolsList`、`publishRepositoryIndexSchema` | `McpServiceTest.toolListExposesRepositoryIndexPublisherSchema`、HTTP 冒烟 `tools_has_publish_repository_index=yes`、`tools_has_inputSchema=yes` | 通过 |
| AC-BE-005 | `tools/call publish_repository_index` 仍执行索引导入并返回 MCP tool result | `McpService.toolsCall`、`toIndexRequest`、`toolResult` | `McpServiceTest.publishRepositoryIndexToolDelegatesToIndexService`、`publishRepositoryIndexToolReturnsMcpToolResultContent`、`publishRepositoryIndexToolRequiresIndexImportPermission` | 通过 |
| AC-BE-006 | 真实 HTTP 冒烟完成 MCP 初始化与工具发现 | `ReqMcpController`、`McpService` | 执行报告运行态证据：`initialize -> notifications/initialized -> resources/templates/list -> tools/list` 均通过 | 通过 |
| AC-BE-007 | 后端契约和模块 harness 同步更新 | `docs/ai-harness/contracts/requirement-platform-api.md`、`docs/ai-harness/modules/requirement-platform.md` | `sh scripts/check-docs.sh`、`sh scripts/check-harness.sh init --spec ...` | 通过 |
| AC-BE-008 | 项目创建/初始化指令能识别 `reqflow.publish_repository_index` | `ReqActionTokenServiceImpl.projectInitInstructionContent`、`ReqProjectInitServiceImpl.buildMigrationPendingInstruction` | `ReqActionTokenServiceImplTest.createsProjectInitInstructionWithUniqueTokenAndHashStorage`、`ReqProjectInitServiceImplTest.keepsProjectInitUsableWhenActionTokenTableIsMissing` | 通过 |

## 验收复核

- MCP lifecycle：通过。`initialize`、`notifications/initialized`、`resources/templates/list`、`tools/list` 均有实现和运行态证据。
- Tool schema：通过。`publish_repository_index` 暴露关键入参，且单测和 HTTP 冒烟覆盖 `inputSchema` 与 `actionToken`。
- Tool call：通过。索引导入仍走原 Service，权限不足不写入，成功响应为 MCP tool result。
- 项目初始化指令：通过。指令包含 `mcpServer`、`toolName`、`mcpTool` 和 `actionToken` 职责说明。
- 文档和 harness：通过。API 契约、模块 harness、spec 和执行报告已同步。

## 返修交接清单

无。

| 修复 ID | 严重级别 | 关联验收 ID | 问题 | 修复要求 | 验证要求 |
|---|---|---|---|---|---|
| 无 | 无 | 无 | 无 | 无 | 无 |

## 复审记录

| 修复 ID | 执行处理结果 | 复审结论 | 复审证据 |
|---|---|---|---|
| 无 | 无需修复 | 通过 | 未产生 RF 返修项 |

## Review 分级说明

- 阻断：需求不可用、数据错误、安全/权限风险、迁移风险、缺少关键验收证据、无证据环境结论。
- 重要：契约不一致、关键测试缺口、计划承诺的运行态或增强验证未完成且影响真实流程。
- 一般：命名、文档清晰度、低风险维护建议。
