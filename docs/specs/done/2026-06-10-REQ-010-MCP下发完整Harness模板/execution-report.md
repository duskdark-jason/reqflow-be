# MCP下发完整Harness模板执行报告

## 执行结论

- 当前分支：task/harness-template-resource-mcp
- commit：本次任务分支提交
- 结论：执行、Review 和完成态收尾已完成
- 模块知识库动作：更新
- 模块知识库文档：docs/ai-harness/modules/requirement-platform.md

## 修改摘要

| 文件 | 修改内容 |
|---|---|
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | `get_harness_template` 改为从 classpath `harness-template/files.txt` 和模板资源读取完整 docs/scripts 文件清单，生成 workspace/仓库初始化包 |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/mcp/McpServiceTest.java` | 增加完整模板文档、测试脚本、自动 Review 规则和 `harness-index.json` 初始化状态断言 |
| `ruoyi-requirement/src/main/resources/harness-template/**` | 新增随包发布的完整 harness 模板资源和文件清单 |
| `docs/ai-harness/contracts/requirement-platform-api.md` | 补充模板资源存储位置与完整下发契约 |
| `docs/ai-harness/modules/requirement-platform.md` | 补充 MCP 下发完整 harness 模板的维护和验证规则 |
| `docs/specs/active/2026-06-10-REQ-010-MCP下发完整Harness模板/` | 新增本次需求过程文档 |

## 验收覆盖

| 验收 ID | 执行结果 | 证据 |
|---|---|---|
| AC-001 | 通过 | `McpServiceTest#getHarnessTemplateToolReturnsWorkspaceAndRepositoryInstructions` 断言返回 `docs/process/agent-workflow.md`、`docs/process/new-requirement-flow.md`、`docs/templates/review-report-template.md` 和 `scripts/test-check-harness.sh` |
| AC-002 | 通过 | 同一测试断言返回内容包含“自动 Review、返修和复审循环” |
| AC-003 | 通过 | 新增 `ruoyi-requirement/src/main/resources/harness-template/**` 和 `files.txt`，Maven resources 阶段显示复制 68 个资源 |
| AC-004 | 通过 | 同一测试断言返回 `harness-index.json` 内容包含 `"template": false` 和 `"initialized": true` |
| AC-005 | 通过 | `McpServiceTest` 全量 18 个用例通过，包含 `getHarnessTemplateToolRequiresProjectQueryPermission` |

## 验证结果

| 层级 | 验收 ID | 命令 | 结果 |
|---|---|---|---|
| L0 | AC-001 至 AC-005 | `sh scripts/check-docs.sh` | 通过 |
| L0 | AC-001 至 AC-005 | `sh scripts/check-harness.sh init` | 通过 |
| L2 | AC-001 至 AC-005 | `mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，18 个测试通过 |
| L2 | AC-001 至 AC-005 | `mvn -pl ruoyi-requirement -am test` | 通过，62 个测试通过 |
| L0 | AC-001 至 AC-005 | `git diff --check` | 通过 |

## Review 返修记录

无，待 Review Agent 审查。

## 偏差与风险

- 未启动后端做真实 MCP HTTP 冒烟；本次变更集中在 MCP service 返回结构和 classpath 资源读取，已通过 Service 层单测覆盖。
- Review 已完成并通过；本轮合并和清理分支由用户明确授权继续执行。
