# MCP协议握手与工具暴露修复需求说明

## 背景

在另一个接入平台的项目中执行项目初始化时，Codex 会话无法调用 `publish_repository_index`。本地配置已包含 reqflow MCP server，但当前会话工具列表没有暴露 reqflow 工具，`tool_search` 查不到，MCP resources/templates 为空。

现场验证显示：直接向 `POST /requirement/mcp` 调用 `tools/list` 能看到 `publish_repository_index`，但 MCP 客户端标准第一步 `initialize` 返回“不支持的MCP方法：initialize”。因此根因不是工具业务未实现，而是后端 MCP 入口尚未实现完整 MCP lifecycle 和能力协商，导致 Codex 不会进入工具发现阶段。

用户补充要求：项目创建/初始化时复制给接入项目的 MCP 调用指令也必须准确识别指定 MCP server 和指定 MCP tool，不能只写泛化的 `targetMethod`。

## 目标

- 让 `/requirement/mcp` 成为 Codex 可握手、可发现工具的 MCP HTTP/JSON-RPC 服务。
- 保留现有 `X-MCP-Key` 人员鉴权和 tool 细粒度权限校验。
- 让接入项目会话能在重新加载 MCP server 后发现 `publish_repository_index`。
- 让项目创建/初始化指令明确指向 `reqflow` MCP server 和 `publish_repository_index` tool。
- 补齐 tools、resources、resource templates、prompts 的基础元数据，避免客户端因 schema 不完整而不展示工具。
- 更新长期 harness 文档，明确 reqflow MCP 服务的协议入口、能力声明、初始化指令格式和验证方法。

## 范围

本次包含：

- 支持 MCP `initialize`，返回协议版本、serverInfo、tools/resources/prompts capabilities。
- 支持 `notifications/initialized` 这类 JSON-RPC notification，避免客户端初始化完成通知被当成错误。
- 支持 `resources/templates/list`，返回平台当前使用的 URI 模板。
- 补齐 `tools/list` 的 `description` 和 `inputSchema`，至少覆盖 `publish_repository_index` 的关键入参。
- 将 `tools/call` 返回包装成 MCP tool result 的 `content` 结构，并保留结构化结果，兼容现有调用测试。
- 更新项目初始化 `initInstruction.content`，显式输出 `mcpServer: reqflow`、`toolName: publish_repository_index`、`mcpTool: reqflow.publish_repository_index` 和 `actionToken`。
- 更新后端契约与模块 harness 文档。
- 补充单元测试和本地 HTTP 冒烟验证。

本次不包含：

- 不引入平台服务器 clone、Git、shell、文件系统写入或大模型调用。
- 不改 `publish_repository_index` 的业务导入语义、数据库表结构或权限点。
- 不改前端页面交互。
- 不处理 Codex 当前已启动会话的热刷新能力；修复后需新开或重启接入项目会话加载 MCP server。

## 影响范围

- 接口/API：是，`POST /requirement/mcp` 支持 MCP lifecycle、resource templates 和标准 tool result；项目初始化响应中的 `initInstruction.content` 文本格式增强。
- 数据库/SQL：否，不新增或修改表、字段、SQL 口径。
- 权限/菜单：否，继续使用 `X-MCP-Key`、`req:project:query`、`req:index:import`、`req:package:save`。
- 页面/交互：否，不改前端页面。
- 导出/异步/任务：否。

## 契约与数据口径

- `initialize` 请求返回：
  - `protocolVersion`：优先回显客户端传入的 2025 系列协议版本，缺失时使用 `2025-11-25`。
  - `capabilities.tools`、`capabilities.resources`、`capabilities.prompts`：声明服务端支持对应能力。
  - `serverInfo`：返回 reqflow server 名称、标题和版本。
- `notifications/initialized` 不产生业务写入；Controller 应接受 notification，不阻断后续请求。
- `resources/templates/list` 返回与 `resources/list` 对应的 URI template 列表。
- `tools/list` 每个工具至少包含 `name`、`description`、`inputSchema`。
- `tools/call` 返回 MCP tool result：
  - `content`：包含一条 `type=text` 的 JSON 文本结果。
  - `structuredContent`：保留原有结构化结果，便于客户端解析。
  - `isError=false`。
- 项目初始化指令必须同时包含：
  - `mcpServer: reqflow`
  - `toolName: publish_repository_index`
  - `mcpTool: reqflow.publish_repository_index`
  - `targetMethod: publish_repository_index`
  - `actionToken: reqflow_action_xxx`
  - 说明 `actionToken` 是 `publish_repository_index` 的 `arguments.actionToken`，不是 `X-MCP-Key`。
- 业务权限继续由 `ReqMcpController` 粗授权和 `McpService.requirePermission` 细粒度授权共同保证。

## 验收标准

- AC-BE-001：`initialize` 请求返回成功结果，声明 tools/resources/prompts capabilities，不再返回“不支持的MCP方法：initialize”。
- AC-BE-002：`notifications/initialized` 被接受，不污染安全上下文，不返回不支持方法错误。
- AC-BE-003：`resources/templates/list` 返回资源模板列表，外部会话不再看到模板为空或方法不支持。
- AC-BE-004：`tools/list` 暴露 `publish_repository_index`，且工具包含描述和 JSON Schema 入参。
- AC-BE-005：`tools/call publish_repository_index` 仍执行原索引导入 Service，权限不足仍被拒绝；成功响应符合 MCP tool result 结构。
- AC-BE-006：本地 HTTP 冒烟能完成 `initialize -> notifications/initialized -> tools/list`，并在 tools 中看到 `publish_repository_index`。
- AC-BE-007：后端契约和模块 harness 已同步更新，说明真实 MCP 服务能力和接入项目初始化验证方式。
- AC-BE-008：项目创建/初始化返回的 `initInstruction.content` 能让接入项目准确识别并调用 `reqflow.publish_repository_index`，并明确 `actionToken` 是 tool arguments。

## Companion 关联

- companion spec：无
- 关联分支：无

## 客户与分支

- 目标客户：通用
- 基线分支：main
- 任务分支：fix/REQ-20260610-005-mcp-protocol-service

## 约束与假设

- 当前仓库没有配置 Git 远端，本次基于本地 `main` 创建任务分支。
- 当前 Codex 会话不会因为代码修复自动刷新出 reqflow MCP tool；验收以 HTTP 协议冒烟和后续新会话可加载为准。
- 不引入外部 MCP Java SDK，避免升级依赖和扩大改动面。
