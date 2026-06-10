# MCP工具错误响应类型修复执行计划

## 输入文件

- 需求说明：`docs/specs/active/2026-06-10-REQ-006-MCP工具错误响应类型修复/requirement.md`
- MCP 响应对象：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpResponse.java`
- MCP 服务：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java`
- MCP 测试：`ruoyi-requirement/src/test/java/com/ruoyi/requirement/mcp/McpServiceTest.java`
- 后端契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 模块文档：`docs/ai-harness/modules/requirement-platform.md`

## 实施步骤

1. TDD Red：增加 `McpResponse.error` JSON 序列化测试，覆盖 AC-BE-001。
2. TDD Red：增加 `publish_repository_index` 业务异常返回 MCP tool error result 的测试，覆盖 AC-BE-002。
3. TDD Green：给 `McpResponse` 增加标准 error code/message，并省略 null 字段。
4. TDD Green：让 `tools/call` 捕获业务异常并返回 `isError=true` 的 tool result；保留成功路径 `isError=false`，覆盖 AC-BE-003。
5. 文档同步：更新 API 契约和模块 harness，说明 protocol error 与 tool execution error 边界，覆盖 AC-BE-005。
6. 验证：运行指定测试、完整后端测试、admin 打包、文档检查、harness init 和 HTTP 错误路径冒烟，覆盖 AC-BE-004。
7. 执行报告：记录根因、修改、验证和数据库无需更新原因。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpResponse.java` | 标准 JSON-RPC error shape，省略 null 字段 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | tool 执行业务异常包装为 MCP tool error result |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/mcp/McpServiceTest.java` | 补错误响应类型测试 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md` | 同步 MCP 错误响应契约 |
| 修改 | `docs/ai-harness/modules/requirement-platform.md` | 同步 MCP 常见风险和验证建议 |
| 新增 | `docs/specs/active/2026-06-10-REQ-006-MCP工具错误响应类型修复/execution-report.md` | 执行证据 |

## 模块知识库计划

- 更新 `docs/ai-harness/modules/requirement-platform.md`。
- 更新原因：MCP tool 错误响应类型会直接影响接入项目调用是否可诊断，是 MCP 管理长期维护规则。

## 验证计划

- TDD Red/Green：`mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest -Dsurefire.failIfNoSpecifiedTests=false test`
- L2 单元/契约：`mvn -pl ruoyi-requirement -am test`
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`
- L0 文档/规范：`sh scripts/check-docs.sh`
- Harness 当前阶段：`sh scripts/check-harness.sh init --spec docs/specs/active/2026-06-10-REQ-006-MCP工具错误响应类型修复`
- L3 运行态冒烟：用无效 `actionToken` 调 `tools/call publish_repository_index`，确认 HTTP 200 body 为 `result.content` + `isError=true`，且不出现 protocol `error`。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | 响应对象修复 | `McpServiceTest` JSON 序列化断言 |
| AC-BE-002 | tool 错误包装 | `McpServiceTest` mock 异常断言 |
| AC-BE-003 | 成功路径保持 | `McpServiceTest.publishRepositoryIndexToolReturnsMcpToolResultContent` |
| AC-BE-004 | 运行态冒烟 | `curl tools/call` 无效 actionToken |
| AC-BE-005 | 文档同步 | `check-docs.sh` + harness init |

## 执行约束

- 不引入新依赖，不升级框架。
- 不改变索引导入 Service 的业务规则和数据库写入语义。
- 不修改 SQL、权限点或前端页面。
