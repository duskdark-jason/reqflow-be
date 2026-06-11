# 初始化指令简化与索引表核对需求说明

## 背景

平台已提供全局 `reqflow-mcp` skill，安装后 Codex 可以通过 skill 学到项目接入初始化流程：调用 `get_harness_template`、写入 harness、运行 init 校验、发布索引并登记初始化结果。当前平台生成的项目初始化指令仍重复输出完整 1-7 步调用要求，导致默认复制内容过长，也容易和 skill 内容漂移。

用户测试真实项目初始化时，后端和前端仓库都已按要求调用 `mcp__reqflow.publish_repository_index`，且 `actionToken` 位于 tool arguments 中。两次调用均被平台库缺表阻断：`req_repository_index_batch` 未初始化。随后 `register_harness_init_result` 已把两个仓库登记为 `init_uncommitted_publish_fail`。

## 目标

- 默认项目初始化指令短化，只保留触发 skill 和定位上下文所需字段。
- 兼容初始化指令同步短化，并保留缺少 `req_action_token` 时的 SQL 迁移提示。
- 完整核对索引表缺失链路：SQL 文件、代码错误转换、MCP 返回、数据库状态和后续重跑路径。
- 如果本地平台库确实缺少索引表，按既有 `sql/req_platform_req007_index_tables.sql` 补齐本地库。

## 不做范围

- 不改变 `publish_repository_index` 的入参结构、权限和索引写入语义。
- 不改变 `register_harness_init_result` 的工具名和权限。
- 不改前端页面。
- 不自动提交接入项目中的 harness 初始化文件。

## 验收标准

- AC-BE-001：`ReqActionTokenServiceImpl` 生成的默认初始化指令包含 `reqflow-mcp`、`mcpServer`、`mcpTool`、`projectId`、`variantId` 和 `actionToken`，不再包含 1-7 步长调用要求。
- AC-BE-002：兼容初始化指令包含 `reqflow-mcp`、`mcpServer`、`mcpTool` 和 `mcpKey`，不再包含 1-7 步长调用要求，并保留 `req_platform_req003_action_token.sql` 迁移提示。
- AC-BE-003：现有 `publish_repository_index` 缺索引表路径继续返回 `平台索引表未初始化`，并指向 `sql/req_platform_req007_index_tables.sql`。
- AC-BE-004：本地平台库索引表状态已核对；缺表时执行既有迁移脚本并复查三张表存在。
- AC-BE-005：后端定向测试、需求模块测试、文档检查、harness 检查和空白检查通过。

## 影响范围

- 接口/API：返回的 `initInstruction.content` 文本变短，字段不变。
- 数据库/SQL：不新增 SQL 文件；本地库可能执行既有 `sql/req_platform_req007_index_tables.sql`。
- 权限：无影响。
- 页面展示：文案长度变短，页面结构不变。
- 模块知识库：更新项目接入初始化和缺表处理说明。
