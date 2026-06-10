# MCP协议握手与工具暴露修复执行报告

## 执行摘要

已完成 reqflow 后端 MCP 服务协议兼容修复，并补强项目创建/初始化指令对指定 MCP tool 的识别能力：

- `/requirement/mcp` 支持 MCP `initialize`、`notifications/initialized`、`ping` 和 `resources/templates/list`。
- `tools/list` 为所有工具返回 `description` 和 `inputSchema`，`publish_repository_index` 明确暴露 `actionToken`、`remoteUrl` 和结构化索引列表等入参。
- `tools/call` 成功响应统一包装为 MCP tool result，包含 `content`、`structuredContent` 和 `isError=false`。
- 修复 `longArg/intArg` 在兼容参数缺省时的拆箱空指针，保证 `actionToken + remoteUrl` 路径不强制要求 `projectId/repoId`。
- 项目创建/初始化指令新增机器可读字段 `mcpServer: reqflow`、`toolName: publish_repository_index`、`mcpTool: reqflow.publish_repository_index`，并说明 `actionToken` 是 tool arguments，不是 `X-MCP-Key`。
- 同步后端 API 契约、模块 harness 文档和本地 REQ-005 spec。

## 文件变更

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | 补 MCP lifecycle、资源模板、工具 schema、tool result 包装和空值参数转换 |
| 修改 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpController.java` | 对 `notifications/*` JSON-RPC notification 返回 HTTP 202 且无响应体 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImpl.java` | 项目初始化指令明确 reqflow MCP server 和 publish_repository_index tool |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImpl.java` | 兼容迁移缺表指令同样明确 MCP server/tool |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/mcp/McpServiceTest.java` | 覆盖 initialize、resource templates、tool schema、tool result、actionToken 导入参数 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImplTest.java` | 覆盖初始化指令 server/tool/actionToken 字段 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImplTest.java` | 覆盖兼容初始化指令 server/tool 字段 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md` | 同步 MCP lifecycle、tool schema 和初始化指令契约 |
| 修改 | `docs/ai-harness/modules/requirement-platform.md` | 同步 MCP 不变量、常见风险和验证建议 |
| 新增 | `docs/specs/done/2026-06-10-REQ-005-MCP协议握手与工具暴露修复/` | 记录本需求的元信息、需求、计划、执行报告和 Review 报告 |

## 模块知识库沉淀

- 影响模块：需求管理、MCP 管理、项目接入初始化。
- 模块知识库动作：更新。
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`。
- 无需更新原因：不适用。

## 数据库与数据口径

- 数据库/SQL/Mapper：无结构、脚本或查询口径变更。
- `sql/`：无需新增或修改迁移脚本。
- `docs/db/`：无需更新；既有 `docs/db/relationship.md` 已记录 `actionToken` 与 `publish_repository_index` 的数据边界，本次只补 MCP 协议握手、tool schema 和初始化指令文本。

## 验收覆盖

| 验收项 | 结果 |
|---|---|
| AC-BE-001 | 通过。`McpServiceTest.initializeDeclaresMcpCapabilities` 和 HTTP 冒烟验证 `initialize` 返回 capabilities 与 `serverInfo.name=reqflow`。 |
| AC-BE-002 | 通过。`ReqMcpController` 对无 `id` 的 `notifications/initialized` 返回 HTTP 202；HTTP 冒烟返回 `initialized_http=202 size=0`。 |
| AC-BE-003 | 通过。`resources/templates/list` 返回 `resourceTemplates`，单测和 HTTP 冒烟均覆盖。 |
| AC-BE-004 | 通过。`tools/list` 暴露 `publish_repository_index`，并包含 `description`、`inputSchema` 和 `actionToken`。 |
| AC-BE-005 | 通过。`publish_repository_index` 仍调用 `IReqRepositoryIndexService.importRepositoryIndex`，权限不足仍拒绝；成功响应为 MCP tool result。 |
| AC-BE-006 | 通过。真实 HTTP 冒烟完成 `initialize -> notifications/initialized -> resources/templates/list -> tools/list`。 |
| AC-BE-007 | 通过。后端契约和模块 harness 已同步更新。 |
| AC-BE-008 | 通过。项目创建/初始化指令包含 `mcpServer: reqflow`、`toolName: publish_repository_index`、`mcpTool: reqflow.publish_repository_index`，并说明 `actionToken` 是 `arguments.actionToken`。 |

## 验证命令

| 层级 | 命令 | 结果 |
|---|---|---|
| TDD Red | `mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest,ReqActionTokenServiceImplTest,ReqProjectInitServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 按预期失败：初始化指令缺少 server/tool 字段；`actionToken + remoteUrl` 路径触发 `longArg` 空值拆箱错误 |
| TDD Green | `mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest,ReqActionTokenServiceImplTest,ReqProjectInitServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，32 tests，0 failures，0 errors |
| L2 | `mvn -pl ruoyi-requirement -am test` | 通过，55 tests，0 failures，0 errors |
| L1 | `mvn -pl ruoyi-admin -am -DskipTests package` | 通过，`ruoyi-admin` 打包成功 |
| L0 | `sh scripts/check-docs.sh` | 通过，输出“文档检查通过” |
| Harness | `sh scripts/check-harness.sh init --spec docs/specs/active/2026-06-10-REQ-005-MCP协议握手与工具暴露修复` | 通过，输出“Harness 检查通过（init 模式）” |
| 空白检查 | `git diff --check` | 通过，无输出 |
| Harness complete 尝试 | `sh scripts/check-harness.sh complete --spec docs/specs/active/2026-06-10-REQ-005-MCP协议握手与工具暴露修复` | 按流程未通过：当前 meta 状态仍为 `executing`，且 Review 未授权，不能伪造完成态 |

## 运行态证据

- 执行目录：后端仓库根目录
- 启动命令：复用本机已启动的后端服务，未在本轮重新启动进程。
- 检查命令：使用 `curl` 向 `http://127.0.0.1:8080/requirement/mcp` 发送 `initialize`、`notifications/initialized`、`resources/templates/list` 和 `tools/list` JSON-RPC 请求。
- 服务地址：`http://127.0.0.1:8080/requirement/mcp`
- 鉴权方式：复用本机 Codex 配置中的 `X-MCP-Key`，未在报告中记录明文 Key。
- 错误摘要：未出现 HTTP 错误；`notifications/initialized` 返回 HTTP 202 且响应体大小 0。
- 当前执行 agent 环境：本机后端开发环境，验证目标为当前 8080 后端实例。

| 调用 | 结果 |
|---|---|
| `initialize` | HTTP 200，响应包含 `capabilities` |
| `notifications/initialized` | HTTP 202，响应体大小 0 |
| `resources/templates/list` | HTTP 200，响应包含 `resourceTemplates` |
| `tools/list` | HTTP 200，响应包含 `publish_repository_index`、`inputSchema`、`actionToken` |

## 提交记录

- 提交：本提交 `fix: 修复 MCP 协议握手与工具暴露`，具体 hash 以当前任务分支 `git log -1` 为准。

## 未执行项

- 无。用户已授权 Review 与办结流程，Review 结论为通过，完成态 harness 将在 spec 移入 `docs/specs/done` 后执行。

## Review 返修记录

- 暂无 Review 返修项。

## Review 与办结记录

- Review 授权：已授权。
- Review 结论：通过。
- Review 报告：`docs/specs/done/2026-06-10-REQ-005-MCP协议握手与工具暴露修复/review-report.md`。
- 返修项：无。
