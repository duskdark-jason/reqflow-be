# MCP协议握手与工具暴露修复执行计划

## 输入文件

- 需求说明：`docs/specs/active/2026-06-10-REQ-005-MCP协议握手与工具暴露修复/requirement.md`
- MCP 服务：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java`
- MCP 请求/响应对象：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpRequest.java`、`McpResponse.java`
- MCP Controller：`ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpController.java`
- 项目初始化指令：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImpl.java`、`ReqProjectInitServiceImpl.java`
- 现有测试：`ruoyi-requirement/src/test/java/com/ruoyi/requirement/mcp/McpServiceTest.java`、`ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImplTest.java`、`ReqProjectInitServiceImplTest.java`
- 后端契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 模块文档：`docs/ai-harness/modules/requirement-platform.md`
- 影响模块：需求管理、MCP 管理、项目接入初始化
- 模块知识库动作：更新
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`

## 实施步骤

1. TDD Red：在 `McpServiceTest` 增加 `initialize`、`resources/templates/list` 和工具 schema 失败用例，覆盖 AC-BE-001、AC-BE-003、AC-BE-004。
2. 指令契约 Red：在 `ReqActionTokenServiceImplTest` 和 `ReqProjectInitServiceImplTest` 增加 `mcpServer/toolName/mcpTool/actionToken` 断言，覆盖 AC-BE-008。
3. TDD Green：在 `McpService` 中实现 lifecycle、模板列表和工具元数据，覆盖 AC-BE-001、AC-BE-003、AC-BE-004。
4. Tool result：补 `tools/call` MCP 标准结果包装，保留 `structuredContent`，并修复 `longArg/intArg` 对缺省兼容参数的空值转换，覆盖 AC-BE-005。
5. Notification：在 `ReqMcpController` 中识别 `notifications/initialized` 这类无 `id` 通知，返回可接受响应并避免不支持错误，覆盖 AC-BE-002。
6. 初始化指令：更新项目创建/初始化指令内容，明确 `reqflow.publish_repository_index` 和 `arguments.actionToken`，覆盖 AC-BE-008。
7. Harness 文档：更新 `requirement-platform-api.md` 和 `requirement-platform.md`，记录 protocol lifecycle、templates、tool schema、初始化指令格式和初始化冒烟路径，覆盖 AC-BE-007。
8. 执行报告：写 `execution-report.md`，记录修改、验证、运行态证据、模块知识库动作和数据库无需更新原因。
9. 验证与提交：运行 L2、L1、L0 和本地 HTTP 冒烟，完成后直接 commit。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | 补 MCP lifecycle、资源模板、工具 schema、tool result 包装和空值参数转换 |
| 修改 | `ruoyi-admin/src/main/java/com/ruoyi/web/controller/requirement/ReqMcpController.java` | 接受初始化完成 notification |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImpl.java` | 项目初始化指令明确 MCP server/tool |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImpl.java` | 兼容迁移缺表指令明确 MCP server/tool |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/mcp/McpServiceTest.java` | 补协议握手、模板、工具 schema 和 tool result 测试 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImplTest.java` | 补初始化指令 server/tool 断言 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImplTest.java` | 补兼容指令 server/tool 断言 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md` | 同步 MCP 协议契约和初始化指令格式 |
| 修改 | `docs/ai-harness/modules/requirement-platform.md` | 同步 MCP 能力维护风险和验证建议 |
| 新增 | `docs/specs/active/2026-06-10-REQ-005-MCP协议握手与工具暴露修复/execution-report.md` | 执行证据与验证结果 |

## 模块知识库计划

- 更新 `docs/ai-harness/modules/requirement-platform.md`。
- 更新原因：MCP tools 暴露与项目接入初始化是需求平台核心长期能力，后续新增/修改 MCP tool 时必须理解 initialize、capabilities、schema、初始化指令和权限边界。

## 验证计划

- TDD Red：`mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest,ReqActionTokenServiceImplTest,ReqProjectInitServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`
- TDD Green：`mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest,ReqActionTokenServiceImplTest,ReqProjectInitServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`
- L2 单元/契约：`mvn -pl ruoyi-requirement -am test`
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`
- L0 文档/规范：`sh scripts/check-docs.sh`
- Harness 完成门禁：`sh scripts/check-harness.sh complete --spec docs/specs/active/2026-06-10-REQ-005-MCP协议握手与工具暴露修复`
- L3 运行态冒烟：当前已有本地 8080 后端时，用 `curl` 验证 `initialize`、`notifications/initialized`、`resources/templates/list`、`tools/list`；若运行中的后端未加载新代码，则启动当前分支后端再验证。
- L4 跨端/端到端：不在本次自动执行；修复后接入项目需新开或重启 Codex 会话重新加载 MCP server。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | TDD 与服务实现 | `McpServiceTest` + HTTP `initialize` |
| AC-BE-002 | Controller notification | Controller 代码检查 + HTTP notification 冒烟 |
| AC-BE-003 | 资源模板实现 | `McpServiceTest` + HTTP `resources/templates/list` |
| AC-BE-004 | 工具 schema | `McpServiceTest` + HTTP `tools/list` |
| AC-BE-005 | tool result 与权限 | `McpServiceTest` 原有发布索引/权限测试 |
| AC-BE-006 | 运行态冒烟 | `curl initialize -> notification -> resources/templates/list -> tools/list` |
| AC-BE-007 | 文档同步 | `sh scripts/check-docs.sh` + harness complete |
| AC-BE-008 | 初始化指令契约 | `ReqActionTokenServiceImplTest` + `ReqProjectInitServiceImplTest` |

## 执行约束

- 不引入新依赖，不升级 Spring/RuoYi 版本。
- 不改变 `publish_repository_index` 的业务导入 Service 和数据库写入语义。
- 不修改数据库结构和权限 SQL。
- 保留人员 `X-MCP-Key` 与动作 `actionToken` 的职责分离。
- 当前任务分支完成修改和验证后必须直接 commit；merge、push、删除分支需另行授权。
