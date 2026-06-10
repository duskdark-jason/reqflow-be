# MCP工具错误响应类型修复Review报告

## Review 结论

结论：通过。

本轮只读 Review 未发现阻断、重要或一般问题。代码修改范围集中在 MCP 响应对象和 `tools/call` 错误包装，未扩展索引导入业务语义、数据库结构、权限点或前端页面。用户已明确要求直接办结合并，并接手真实有效 `actionToken` 的人工测试。

## 审查输入

- 需求说明：`docs/specs/active/2026-06-10-REQ-006-MCP工具错误响应类型修复/requirement.md`
- 执行计划：`docs/specs/active/2026-06-10-REQ-006-MCP工具错误响应类型修复/plan.md`
- 执行报告：`docs/specs/active/2026-06-10-REQ-006-MCP工具错误响应类型修复/execution-report.md`
- 代码 diff：`30764bd fix: 修复 MCP tool 错误响应类型`
- 后端实现：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpResponse.java`、`ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java`
- 回归测试：`ruoyi-requirement/src/test/java/com/ruoyi/requirement/mcp/McpServiceTest.java`
- harness 文档：`docs/ai-harness/contracts/requirement-platform-api.md`、`docs/ai-harness/modules/requirement-platform.md`

## 审查发现

未发现需要返修的问题。

## 验收覆盖

| 验收 ID | Review 结论 | 证据 |
|---|---|---|
| AC-BE-001 | 通过 | `McpServiceTest.errorResponseSerializesJsonRpcErrorWithoutNullResult` 覆盖 `error.code/error.message` 和无 `result`；`successResponseSerializesWithoutNullError` 覆盖成功响应无 `error:null`。 |
| AC-BE-002 | 通过 | `publishRepositoryIndexToolBusinessErrorReturnsMcpToolErrorResult` 覆盖业务失败返回 `content` 和 `isError=true`，且 `response.getError()` 为 null。 |
| AC-BE-003 | 通过 | `publishRepositoryIndexToolReturnsMcpToolResultContent` 覆盖成功路径保留 `structuredContent` 和 `isError=false`。 |
| AC-BE-004 | 通过 | 执行报告记录 HTTP 冒烟：无效 `actionToken` 返回 `result.content[0].text=动作Token不存在或已停用` 和 `isError=true`，无顶层 protocol `error`。 |
| AC-BE-005 | 通过 | API 契约和模块 harness 已记录 protocol error 与 tool execution error 的边界。 |

## 验证证据复核

| 命令 | 复核结论 |
|---|---|
| `mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest -Dsurefire.failIfNoSpecifiedTests=false test` | TDD Red 失败点与需求一致，Green 后 17 tests 通过。 |
| `mvn -pl ruoyi-requirement -am test` | 58 tests 通过。 |
| `mvn -pl ruoyi-admin -am -DskipTests package` | 后端聚合打包通过。 |
| `curl http://localhost:8080/requirement/mcp` | 协议级错误和 tool 业务错误路径均有运行态证据。 |
| `sh scripts/check-docs.sh` | 文档检查通过。 |
| `sh scripts/check-harness.sh init --spec ...REQ-006...` | active spec init 检查通过。 |

## 残留风险

- 真实有效 `actionToken` 的成功写入索引路径未由本轮自动化复现，原因是当前执行环境没有用户真实项目的明文 actionToken。用户已声明后续由人工测试接管。
- 仓库验证说明引用 `docs/runbooks/local-run.md`，但当前仓库只有 `docs/runbooks/local-run.detected.md` 和 `docs/runbooks/local-run-template.md`。本轮已复用当前 8080 后端实例完成 L3 冒烟，该文档缺口不阻断本需求办结。

## 返修交接清单

无。

## 复审记录

无返修项，无需复审。
