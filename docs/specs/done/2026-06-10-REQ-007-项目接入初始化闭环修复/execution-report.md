# 项目接入初始化闭环修复执行报告

## 执行结论

已完成本次三块修复：

- MCP 侧新增 `skill://reqflow/project-init` 资源/模板，并在 `get_harness_template` 返回 `reqflowMcpSkill`，让 agent 能识别必须调用 reqflow 指定 MCP tools。
- `get_harness_template` 返回 `workspaceFiles` 和每个仓库的 `files` 文件清单，初始化指令明确要求先写入或合并本地 harness，再运行 init 校验，然后发布索引和回写初始化结果。
- `publish_repository_index` 写入前预检 `req_repository_index_batch`、`req_index_module`、`req_impact_item`，缺表时返回“平台索引表未初始化”业务错误，并指向 `sql/req_platform_req007_index_tables.sql`。

提交：`9c6aa85 feat: 完善项目接入初始化闭环`

## 修改文件

| 文件 | 修改说明 |
|---|---|
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | 暴露 reqflow 项目接入初始化 skill；`get_harness_template` 返回 workspace/仓库文件清单；模板中包含 AGENTS、docs/ai-harness、docs/process 和检查脚本内容。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImpl.java` | 初始化指令增加 `projectId`、`variantId` 和完整调用顺序。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImpl.java` | 缺 `req_action_token` 表时的兼容指令同步说明模板读取、本地 harness 写入、init 校验和结果回写。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqRepositoryIndexServiceImpl.java`、`ReqOptionalIndexTableGuard.java` | 索引写入前预检三张索引表，缺表时转换为友好业务错误。 |
| `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mapper/*Mapper.java`、`ruoyi-requirement/src/main/resources/mapper/requirement/*Mapper.xml` | 增加索引表存在性预检查询。 |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/**` | 增加 MCP skill/template、初始化指令顺序和缺表错误单元测试。 |
| `sql/req_platform_req007_index_tables.sql` | 新增独立索引表补齐脚本。 |
| `docs/ai-harness/contracts/requirement-platform-api.md`、`docs/ai-harness/modules/requirement-platform.md`、`docs/db/relationship.md`、`docs/db/table-dictionary.md` | 同步接口、模块、数据库关系和迁移入口说明。 |

## 数据库影响

- 新增脚本：`sql/req_platform_req007_index_tables.sql`。
- 脚本包含三张索引表：`req_repository_index_batch`、`req_index_module`、`req_impact_item`。
- 本次未自动执行任何生产或本地数据库迁移；部署或维护时如遇缺表，需要人工执行该脚本或总 schema 对应建表段。

## 接口、权限和页面影响

- API/MCP：`resources/list`、`resources/templates/list` 增加 `skill://reqflow/project-init`；`get_harness_template` 返回结构增加 `reqflowMcpSkill`、`workspaceFiles` 和仓库 `files`。
- 权限：不新增权限，继续使用 `req:project:query`、`req:index:import`、`req:package:save`。
- 页面：不修改前端页面；前端如展示模板包结构，可按新增字段增强。
- 数据粒度：不改变索引成功写入粒度，仍按项目、仓库、项目分支、真实分支和 commit 记录。

## 验证结果

| 命令 | 结果 |
|---|---|
| `mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest,ReqActionTokenServiceImplTest,ReqProjectInitServiceImplTest,ReqRepositoryIndexServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，47 tests。 |
| `mvn -pl ruoyi-requirement -am test` | 通过，62 tests。 |
| `mvn -pl ruoyi-admin -am -DskipTests package` | 通过。 |
| `sh scripts/check-docs.sh` | 通过。 |
| `sh scripts/check-harness.sh init --spec docs/specs/active/2026-06-10-REQ-007-项目接入初始化闭环修复` | 通过。 |
| `git diff --check` | 通过，无输出。 |

## 验收覆盖

| 验收 ID | 覆盖结果 |
|---|---|
| AC-BE-001 | 已通过 `skill://reqflow/project-init`、`reqflowMcpSkill` 和 `McpServiceTest` 覆盖。 |
| AC-BE-002 | 已通过初始化指令顺序和 `ReqActionTokenServiceImplTest`、`ReqProjectInitServiceImplTest` 覆盖。 |
| AC-BE-003 | 已通过 `get_harness_template` 返回 `workspaceFiles`、仓库 `files` 和 `McpServiceTest` 覆盖。 |
| AC-BE-004 | 已通过三张索引表缺失测试和 `ReqRepositoryIndexServiceImplTest` 覆盖。 |
| AC-BE-005 | 已通过 `sql/req_platform_req007_index_tables.sql`、`docs/db` 和 `docs/ai-harness` 更新覆盖。 |
| AC-BE-006 | 已通过 Maven 测试、admin 打包、文档检查、harness 检查和空白检查覆盖。 |

## Review 返修记录

- 无返修项。

## 后续注意

- 接入项目初始化时，agent 仍需在目标 workspace 本地写文件；reqflow MCP 只下发模板和保存平台索引，不远程写调用方文件系统。
- IMS 平台库如果仍缺索引表，应先执行 `sql/req_platform_req007_index_tables.sql` 后重新调用 `publish_repository_index`。
