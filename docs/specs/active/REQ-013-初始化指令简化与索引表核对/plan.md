# 初始化指令简化与索引表核对执行计划

## 执行步骤

1. 核对现状：确认索引表 SQL、后端缺表 guard、MCP 业务错误和本地数据库状态。
2. TDD Red：更新默认初始化指令和兼容初始化指令测试，断言短指令字段完整且不包含 1-7 步长流程。
3. Green：修改 `ReqActionTokenServiceImpl` 和 `ReqProjectInitServiceImpl` 的指令内容生成。
4. 数据库核对：用本地配置连接 `ry-vue`，确认 `req_repository_index_batch`、`req_index_module`、`req_impact_item` 是否存在；缺失时执行 `sql/req_platform_req007_index_tables.sql` 并复查。
5. 文档同步：更新 `docs/ai-harness/modules/requirement-platform.md` 和相关契约说明，记录默认指令短化边界。
6. 写执行报告，记录 Red/Green、数据库核对、迁移结果、影响范围和验证命令。
7. 验证并提交任务分支。

## 分层验证

| 层级 | 命令 | 覆盖 |
|---|---|---|
| L1 | `mvn -pl ruoyi-requirement -am -Dtest=ReqActionTokenServiceImplTest,ReqProjectInitServiceImplTest,ReqRepositoryIndexServiceImplTest,McpServiceTest -Dsurefire.failIfNoSpecifiedTests=false test` | 初始化指令、缺表错误和 MCP tool 结果 |
| L1 | `mvn -pl ruoyi-requirement -am test` | 需求模块回归 |
| L0 | `sh scripts/check-docs.sh` | 文档结构 |
| L0 | `sh scripts/check-harness.sh init --spec docs/specs/active/REQ-013-初始化指令简化与索引表核对` | active spec 和 harness |
| L0 | `git diff --check` | 空白检查 |
| DB | `SHOW TABLES LIKE 'req\\_%'` 或等价查询 | 本地平台库索引表核对 |

## 验收映射

| 验收 ID | 验证方式 |
|---|---|
| AC-BE-001 | `ReqActionTokenServiceImplTest` |
| AC-BE-002 | `ReqProjectInitServiceImplTest` |
| AC-BE-003 | `ReqRepositoryIndexServiceImplTest`、`McpServiceTest` |
| AC-BE-004 | 本地数据库查询和迁移执行记录 |
| AC-BE-005 | Maven、文档、harness 和空白检查 |
