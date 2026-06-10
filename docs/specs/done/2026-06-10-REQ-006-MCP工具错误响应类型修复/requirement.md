# MCP工具错误响应类型修复需求说明

## 背景

REQ-005 已让 Codex 会话能发现 `reqflow.publish_repository_index`，但接入项目真实调用三次后均返回：

```text
tool call failed for `reqflow/publish_repository_index`
Caused by:
    Unexpected response type
```

直接复现 `tools/call publish_repository_index` 的失败路径后，当前服务返回：

```json
{"error":{"message":"初始化指令、仓库远端、commit 和索引版本不能为空"},"id":99,"jsonrpc":"2.0","result":null}
```

该响应同时存在两个问题：JSON-RPC error 缺少标准 `code` 字段，并且错误响应仍包含 `result:null`；对 MCP tool 执行业务错误，也没有按 MCP tool result 返回 `content` 与 `isError=true`，导致客户端无法展示真实业务错误。

## 目标

- `tools/call` 内的业务异常返回 MCP tool error result，而不是不规范 JSON-RPC error。
- JSON-RPC 协议级错误包含标准 `code/message`，且响应不同时包含 `result` 与 `error`。
- 让接入项目再次调用 `publish_repository_index` 时，成功则拿到可确认回执，失败则看到可读业务错误，不再是 `Unexpected response type`。

## 范围

本次包含：

- 修复 `McpResponse.error` 的 JSON-RPC error shape。
- 修复 `tools/call` 业务异常包装，返回 `content + isError=true`。
- 增加单元测试和 HTTP 冒烟，覆盖错误路径响应类型。
- 同步 MCP harness 文档和执行报告。

本次不包含：

- 不修改索引导入业务写入语义。
- 不修改数据库、SQL、权限点或菜单。
- 不修改前端页面。

## 影响范围

- 接口/API：是，`POST /requirement/mcp` 错误响应格式更符合 JSON-RPC/MCP。
- 数据库/SQL：否。
- 权限/菜单：否。
- 页面/交互：否。
- 导出/异步/任务：否。

## 契约与数据口径

- MCP protocol-level error：返回 JSON-RPC error object，包含数值 `code` 和 `message`，不输出 `result:null`。
- MCP tool execution error：`tools/call` 返回 JSON-RPC success，`result.content[0].type=text`，`result.content[0].text` 为错误说明，`result.isError=true`。
- MCP tool execution success：继续返回 `content`、`structuredContent` 和 `isError=false`。

## 验收标准

- AC-BE-001：`McpResponse.error` 序列化后包含 `error.code/error.message`，不包含 `result:null`。
- AC-BE-002：`tools/call publish_repository_index` 业务失败时不返回 JSON-RPC protocol error，而返回 MCP tool result 且 `isError=true`。
- AC-BE-003：`tools/call publish_repository_index` 成功路径仍返回 `isError=false` 和 `structuredContent`。
- AC-BE-004：真实 HTTP 冒烟用无效 `actionToken` 触发业务失败时，客户端可解析到 `content` 与 `isError=true`，不再返回不规范响应。
- AC-BE-005：后端 harness 文档同步记录 MCP protocol error 与 tool execution error 的区别。

## Companion 关联

- companion spec：无
- 关联分支：无

## 客户与分支

- 目标客户：通用
- 基线分支：main
- 任务分支：fix/REQ-20260610-006-mcp-tool-error-result

## 约束与假设

- 用户提供的接入项目调用使用了正确的 MCP server 和 tool，问题集中在 reqflow server 响应类型。
- 当前没有真实 actionToken 明文可用于复现成功业务写入，本轮用无效 actionToken 验证错误响应类型。
