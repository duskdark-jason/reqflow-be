# 项目接入初始化闭环修复执行计划

## 输入文件

- 需求说明：`docs/specs/active/2026-06-10-REQ-007-项目接入初始化闭环修复/requirement.md`
- MCP 服务：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java`
- 动作 token 指令：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImpl.java`
- 项目初始化兼容指令：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImpl.java`
- 索引导入服务：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqRepositoryIndexServiceImpl.java`
- 索引表 guard：`ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqOptionalIndexTableGuard.java`
- MCP 测试：`ruoyi-requirement/src/test/java/com/ruoyi/requirement/mcp/McpServiceTest.java`
- 索引服务测试：`ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqRepositoryIndexServiceImplTest.java`
- 动作 token 测试：`ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImplTest.java`
- 项目初始化测试：`ruoyi-requirement/src/test/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImplTest.java`

## 实施步骤

1. TDD Red：补 MCP skill/template 测试，断言 `get_harness_template` 返回 `reqflowMcpSkill`、`workspaceFiles` 和仓库 `files`，覆盖 AC-BE-001、AC-BE-003。
2. TDD Red：补初始化指令测试，断言指令包含 `get_harness_template`、本地 harness 写入、`check-harness.sh init`、`publish_repository_index` 和 `register_harness_init_result` 顺序，覆盖 AC-BE-002。
3. TDD Red：补索引缺表写入测试，模拟 `req_repository_index_batch`、`req_index_module` 和 `req_impact_item` 缺表时返回友好错误且不继续写后续表，覆盖 AC-BE-004。
4. TDD Green：在 MCP 模板包中增加 reqflow MCP skill 文本、workspace 文件清单和仓库 harness 文件清单。
5. TDD Green：更新项目初始化指令和兼容指令，明确完整 agent 执行顺序。
6. TDD Green：在索引导入写入链路中捕获索引表缺失并转换为平台库初始化错误。
7. SQL 与文档：新增或补充索引表迁移脚本，更新 `docs/db`、API 契约和模块 harness，覆盖 AC-BE-005。
8. 验证：运行指定测试、完整后端测试、admin 打包、文档检查、harness init 和 diff 检查，覆盖 AC-BE-006。
9. 执行报告：记录修改、验证、数据库影响和未执行的生产迁移说明。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` | 下发 reqflow MCP skill 和 harness 文件模板 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqActionTokenServiceImpl.java` | 初始化指令补完整调用顺序 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqProjectInitServiceImpl.java` | 缺 action_token 表时的兼容指令补完整调用顺序 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqRepositoryIndexServiceImpl.java` | 索引表缺失友好错误 |
| 修改 | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/service/impl/ReqOptionalIndexTableGuard.java` | 复用缺表识别和错误文案 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/**` | 补单元测试 |
| 新增 | `sql/req_platform_req007_index_tables.sql` | 独立索引表迁移脚本 |
| 修改 | `docs/ai-harness/contracts/requirement-platform-api.md` | 同步模板包和缺表错误契约 |
| 修改 | `docs/ai-harness/modules/requirement-platform.md` | 同步项目接入初始化闭环规则 |
| 修改 | `docs/db/relationship.md`、`docs/db/table-dictionary.md` | 同步索引表缺失处理和迁移入口 |
| 新增 | `docs/specs/active/2026-06-10-REQ-007-项目接入初始化闭环修复/execution-report.md` | 执行证据 |

## 验证计划

- TDD Red/Green：`mvn -pl ruoyi-requirement -am -Dtest=McpServiceTest,ReqActionTokenServiceImplTest,ReqProjectInitServiceImplTest,ReqRepositoryIndexServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`
- L2 单元/契约：`mvn -pl ruoyi-requirement -am test`
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`
- L0 文档/规范：`sh scripts/check-docs.sh`
- Harness 当前阶段：`sh scripts/check-harness.sh init --spec docs/specs/active/2026-06-10-REQ-007-项目接入初始化闭环修复`
- 空白检查：`git diff --check`

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-BE-001 | MCP skill 下发 | `McpServiceTest` |
| AC-BE-002 | 初始化指令顺序 | `ReqActionTokenServiceImplTest`、`ReqProjectInitServiceImplTest` |
| AC-BE-003 | harness 文件清单 | `McpServiceTest` |
| AC-BE-004 | 缺表友好错误 | `ReqRepositoryIndexServiceImplTest` |
| AC-BE-005 | SQL 和文档同步 | `check-docs.sh` + harness init |
| AC-BE-006 | 回归验证 | Maven 测试、admin 打包 |

## 执行约束

- 不让平台服务端直接写接入项目本地文件。
- 不自动执行生产数据库迁移。
- 不修改前端页面和权限点。
- 不改变索引成功写入的数据粒度。
