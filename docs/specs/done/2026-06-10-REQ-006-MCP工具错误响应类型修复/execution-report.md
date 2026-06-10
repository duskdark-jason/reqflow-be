# MCP工具错误响应类型修复执行报告

## 执行摘要

已完成 MCP `tools/call` 错误响应类型修复：

- `McpResponse` 序列化时省略 null 字段，成功响应不再输出 `error:null`，错误响应不再输出 `result:null`。
- JSON-RPC 协议级错误补充标准 `error.code/error.message`。
- `tools/call` 内的权限不足、参数校验、动作 token 无效、索引导入失败等业务异常返回 MCP tool result，包含 `content` 和 `isError=true`，不再包装成 protocol error。
- `publish_repository_index` 成功路径保持 `content`、`structuredContent` 和 `isError=false`。
- 同步后端 API 契约和模块 harness，沉淀 protocol error 与 tool execution error 的边界。

## 根因

接入项目真实调用已能发现 `reqflow.publish_repository_index`，失败发生在 `tools/call` 响应解析阶段。直接复现旧实现可见：

- 业务异常被 `McpService.handle` 外层 catch 包成 JSON-RPC `error`。
- `error` 只有 `message`，缺少 JSON-RPC 标准数值 `code`。
- 错误响应仍序列化出 `result:null`。
- 成功响应也序列化出 `error:null`。

这些响应形态容易被 MCP 客户端判定为非预期响应类型，导致调用侧只看到 `Unexpected response type`，看不到真实业务错误。

## 文件变更

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpResponse.java` | 增加 null 字段省略和标准 JSON-RPC error code/message |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | `tools/call` 业务异常转为 MCP tool error result |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/mcp/McpServiceTest.java` | 增加响应序列化、权限失败和业务失败的回归测试 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md` | 补充 MCP protocol error 与 tool execution error 契约 |
| 修改 | `docs/ai-harness/modules/requirement-platform.md` | 补充 MCP 错误响应不变量、风险和验证建议 |
| 新增 | `docs/specs/active/2026-06-10-REQ-006-MCP工具错误响应类型修复/execution-report.md` | 记录执行、验证和交接证据 |

## 模块知识库沉淀

- 影响模块：需求管理、MCP 管理、项目接入初始化。
- 模块知识库动作：更新。
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`。
- 无需更新原因：不适用。

## 数据库与数据口径

- 数据库/SQL/Mapper：无结构、脚本、Mapper 或查询口径变更。
- `sql/`：无需新增或修改迁移脚本。
- `docs/db/`：无需更新；本次只调整 MCP 响应协议语义，不改变表关系、字段、join、统计口径或分页粒度。
- 运行态验证时临时插入 1 条 `req_mcp_user_key` 测试 Key 绑定本地 admin 用户，调用后已立即删除；验证后查询 `remaining_temp_keys=0`，无持久测试 Key 留存。

## 验收覆盖

| 验收 ID | 结果 |
|---|---|
| AC-BE-001 | 通过。`McpResponse.error` 序列化后包含 `error.code/error.message`，不包含 `result`；`McpResponse.success` 不包含 `error`。 |
| AC-BE-002 | 通过。`tools/call publish_repository_index` 业务失败返回 `result.content` 和 `isError=true`，`response.getError()` 为 null。 |
| AC-BE-003 | 通过。`publishRepositoryIndexToolReturnsMcpToolResultContent` 继续覆盖成功路径 `isError=false` 和 `structuredContent`。 |
| AC-BE-004 | 通过。真实 HTTP 冒烟用无效 `actionToken` 触发业务失败，响应为 `result.content[0].text=动作Token不存在或已停用`、`isError=true`，无顶层 protocol `error`。 |
| AC-BE-005 | 通过。后端 API 契约和模块 harness 已同步记录两类错误响应边界。 |

## 验证命令

| 层级 | 命令 | 结果 |
|---|---|---|
| TDD Red | `mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest -Dsurefire.failIfNoSpecifiedTests=false test` | 按预期失败：17 tests，6 failures，失败点集中在 null 字段、缺少 error code、tool 业务错误被包装成 protocol error |
| TDD Green | `mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过：17 tests，0 failures，0 errors |
| L2 | `mvn -pl ruoyi-requirement -am test` | 通过：58 tests，0 failures，0 errors |
| L1 | `mvn -pl ruoyi-admin -am -DskipTests package` | 通过：`ruoyi-admin` 打包成功 |
| L3 协议级错误 | `curl -H 'Content-Type: application/json' -X POST http://localhost:8080/requirement/mcp --data '{"jsonrpc":"2.0","id":99,"method":"tools/call",...}'` | 通过：未带 `X-MCP-Key` 时返回标准 JSON-RPC `error.code=-32603`，不包含 `result:null` |
| L3 tool 业务错误 | `curl -H 'Content-Type: application/json' -H 'X-MCP-Key: 临时测试Key' -X POST http://localhost:8080/requirement/mcp --data '{"jsonrpc":"2.0","id":100,"method":"tools/call","params":{"name":"publish_repository_index",...}}'` | 通过：HTTP 200，响应为 `result.content` + `isError=true`，无顶层 protocol `error` |

## 运行态证据

- 执行目录：后端仓库根目录
- 启动命令：复用本机已启动的后端服务，进程 `PID=89264`，类路径指向当前仓库 `ruoyi-admin/target/classes` 与 `ruoyi-requirement/target/classes`。
- 检查命令：`lsof -nP -iTCP:8080 -sTCP:LISTEN`、`ps -p 89264 -o pid=,command=`、`curl http://localhost:8080/requirement/mcp`。
- profile/env 或 mode：当前本机后端开发实例；仓库缺少验证说明指定的 `docs/runbooks/local-run.md`，实际只存在 `docs/runbooks/local-run.detected.md` 和 `docs/runbooks/local-run-template.md`。
- 错误摘要：未出现 HTTP 调用错误。无鉴权请求验证 protocol error 形态；临时 MCP Key 请求验证 tool execution error 形态。
- 当前执行 agent 环境：本机 Codex 执行环境，仅证明当前 8080 后端实例和当前代码输出符合预期。

### HTTP 响应摘要

未带 MCP Key 的协议级错误：

```json
{"error":{"code":-32603,"message":"调用MCP需要权限：req:package:save、req:index:import 或 req:project:query"},"id":99,"jsonrpc":"2.0"}
```

带临时 MCP Key 的 tool 业务错误：

```json
{"id":100,"jsonrpc":"2.0","result":{"content":[{"type":"text","text":"动作Token不存在或已停用"}],"isError":true}}
```

临时 Key 清理确认：

```text
remaining_temp_keys
0
```

## 计划偏差

- 计划要求先读取 `docs/runbooks/local-run.md` 再执行 L3；当前仓库没有该文件，仅存在 `docs/runbooks/local-run.detected.md`。本轮已读取 detected 初稿并复用当前运行实例完成 HTTP 冒烟，后续可单独补齐正式 `local-run.md`。
- 本轮没有真实有效 `actionToken`，因此不做成功写入索引的 HTTP 冒烟；成功路径由单元测试 mock `IReqRepositoryIndexService.importRepositoryIndex` 覆盖。

## 提交记录

- commit：`30764bd fix: 修复 MCP tool 错误响应类型`
- 任务分支：`fix/REQ-20260610-006-mcp-tool-error-result`

## Review 返修记录

- 暂无 Review 返修项。

## 后续交接

- 当前阶段：用户已要求直接办结合并，Review 已授权并完成。
- 人工测试交接：真实有效 `actionToken` 的成功写入索引路径由用户在目标接入项目中人工测试；本轮自动验证覆盖响应类型和错误路径。
