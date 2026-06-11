# 初始化指令简化与索引表核对执行报告

## 执行摘要

已将项目接入初始化默认复制指令从长流程说明简化为短动态上下文。新的指令只保留全局 `reqflow-mcp` skill 触发词、目标 MCP server/tool、项目分支上下文和 `actionToken` 位置要求；完整执行顺序继续由全局 skill 承接。兼容路径在缺少 `req_action_token` 表时也改为短指令，并保留 `docs/db/sql/req_platform_req003_action_token.sql` 迁移提示。

已核对用户测试中的索引发布失败：仓库 SQL 和代码 guard 均已具备，实际阻断来自本地平台库 `ry-vue` 未创建 `req_repository_index_batch`、`req_index_module` 和 `req_impact_item`。已执行既有迁移 `docs/db/sql/req_platform_req007_index_tables.sql`，复查三张表存在。

## 修改内容

| 文件 | 修改说明 |
|---|---|
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImpl.java` | 默认项目初始化指令短化，保留 `reqflow-mcp`、MCP server/tool、项目分支和 `actionToken` 字段。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImpl.java` | 兼容初始化指令短化，保留 `mcpKey` 兼容字段和 `req_action_token` 迁移提示。 |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImplTest.java` | 增加短指令断言，确保默认指令不再包含 1-7 步长流程。 |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImplTest.java` | 增加兼容短指令断言，确保缺 `req_action_token` 时仍有可操作提示。 |
| `docs/ai-harness/contracts/requirement-platform-api.md` | 更新 `initInstruction.content` 示例和职责边界，说明完整流程由全局 skill 承接。 |
| `docs/ai-harness/modules/requirement-platform.md` | 同步项目初始化指令短化和全局 skill 职责边界。 |

## TDD 记录

- Red：先更新 `ReqActionTokenServiceImplTest` 和 `ReqProjectInitServiceImplTest`，要求指令包含 `reqflow-mcp` 且不包含 `调用要求：`、`\n1.`、本地 harness 长步骤和 `register_harness_init_result` 长说明。旧实现按预期失败。
- Green：修改默认指令和兼容指令生成逻辑后，定向测试转绿。

## 数据库核对

- SQL 来源：`docs/db/sql/req_platform_req007_index_tables.sql` 已包含 `req_repository_index_batch`、`req_index_module`、`req_impact_item` 三张表；`docs/db/sql/req_platform_schema.sql` 中也包含同三段建表语句。
- 代码 guard：`ReqRepositoryIndexServiceImpl` 在索引导入前预检三张索引表；缺表时通过 `ReqOptionalIndexTableGuard` 返回 `平台索引表未初始化：<table>`，并提示执行 `docs/db/sql/req_platform_req007_index_tables.sql`。
- MCP 返回：`McpServiceTest` 保持覆盖，业务失败通过 MCP tool result 返回 `content` 和 `isError=true`，不是顶层 protocol error。
- 本地库执行前：查询 `ry-vue` 中三张索引表，结果为空。
- 已执行：`/opt/homebrew/opt/mysql-client/bin/mysql -uroot -p123456 -h127.0.0.1 -P3306 ry-vue < docs/db/sql/req_platform_req007_index_tables.sql`。
- 执行后复查：`req_impact_item`、`req_index_module`、`req_repository_index_batch` 均存在；列数分别为 24、20、21。
- 初始化登记核对：`req_repository` 中 repoId 1/2 均为 `init_uncommitted_publish_fail`，harness commit 分别为 `f78eba6788e2a7c79c28e935c921a57332f1a957` 和 `70bdb9d61872d989ad43a62bab76c2850a134d4e`。

## 未重放 publish 的原因

`actionToken` 明文只在初始化指令中返回，平台库只保存哈希和前缀，无法从数据库安全反查。当前会话没有用户测试时使用的 actionToken 明文，因此不能无损重放两个 IMS 仓库的 `publish_repository_index`。索引表已补齐后，需要使用原初始化指令中的 actionToken，或重新复制初始化指令生成新的 actionToken 后再发布。

## 影响范围

- 接口/API：`initInstruction.content` 文本变短，字段和接口路径不变。
- 数据库/SQL：不新增 SQL 文件；已在本地 `ry-vue` 执行既有 `docs/db/sql/req_platform_req007_index_tables.sql`。
- 权限：无影响。
- 页面展示：复制指令内容变短，页面结构不变。
- 接入项目：未修改 IMS 工作区文件，未提交 IMS harness 初始化文件。

## 验证结果

| 命令 | 结果 |
|---|---|
| `mvn -pl ruoyi-requirement -am -Dtest=ReqActionTokenServiceImplTest,ReqProjectInitServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | Red 阶段按预期失败，旧实现不包含 `reqflow-mcp` 短触发。 |
| `mvn -pl ruoyi-requirement -am -Dtest=ReqActionTokenServiceImplTest,ReqProjectInitServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | Green 阶段通过，18 个测试无失败。 |
| `mvn -pl ruoyi-requirement -am -Dtest=ReqActionTokenServiceImplTest,ReqProjectInitServiceImplTest,ReqRepositoryIndexServiceImplTest,McpServiceTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，47 个测试无失败。 |
| `mvn -pl ruoyi-requirement -am test` | 通过，63 个测试无失败。 |
| `sh scripts/check-docs.sh && sh scripts/check-harness.sh init --spec docs/specs/active/2026-06-10-REQ-013-初始化指令简化与索引表核对 && git diff --check` | 通过，文档检查通过，Harness 检查通过（init 模式），空白检查无输出。 |
