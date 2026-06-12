# 初始发布部署基线后端执行计划

## 输入文件

- 需求说明：`requirement.md`
- 相关契约：`docs/ai-harness/contracts/requirement-platform-api.md`
- 相关模块文档：`docs/ai-harness/modules/requirement-platform.md`、`docs/db/README.md`、`docs/db/table-dictionary.md`、`docs/db/relationship.md`
- 目标客户与基线分支：通用/main
- 影响模块：后端发布配置、数据库脚本、需求过程文档
- 模块知识库动作：更新
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`

## 实施步骤

1. 发布配置：修改 `ruoyi-admin/src/main/resources/application.yml`，设置 `/reqflow-api` 和 Linux 上传目录，覆盖 AC-001。
2. MCP 回归保护：补充 `ReqMcpKeyControllerTest`，确保配置公网 host 时仍拼接后端 context-path `/reqflow-api`，覆盖 AC-006。
3. 过程文档清理：删除历史 `docs/specs/active/*` 与 `docs/specs/done/*` 需求目录，仅保留发布基线记录和占位文件，覆盖 AC-002。
4. SQL 脚本清理：删除历史增量和测试设置脚本，保留初始部署基线脚本，覆盖 AC-003。
5. 长期文档同步：更新 `docs/db`、模块文档和运行说明中的脚本路径、访问路径说明，覆盖 AC-004。
6. 验证与提交：执行 Maven 构建、文档检查、harness 检查和 diff 检查，覆盖 AC-005。

## 文件改动范围

| 类型 | 路径 | 说明 |
|---|---|---|
| 修改 | `ruoyi-admin/src/main/resources/application.yml` | 初始发布 context-path 和上传目录 |
| 修改 | `ruoyi-requirement/src/test/java/com/ruoyi/requirement/controller/ReqMcpKeyControllerTest.java` | 锁定 MCP 地址保留后端 context-path |
| 删除 | `docs/specs/active/*`、`docs/specs/done/*` 历史目录 | 清理历史过程 spec |
| 删除 | `docs/db/sql/req_platform_req*.sql` 历史增量脚本 | 避免初始部署误执行增量脚本 |
| 修改 | `docs/db/README.md`、`docs/db/table-dictionary.md`、`docs/db/relationship.md` | 同步 SQL 保留口径 |
| 修改 | `docs/ai-harness/modules/requirement-platform.md`、`docs/runbooks/*.md` | 同步发布路径 |

## 模块知识库计划

- 更新 `docs/ai-harness/modules/requirement-platform.md`，记录初始发布访问路径和 SQL 基线脚本。

## 代码注释计划

- 本次只改配置和文档，不新增复杂业务分支、权限边界、SQL 查询或兼容逻辑，预计无需新增代码注释。

## 验证计划

- L0 文档/规范：`sh scripts/check-docs.sh`、`sh scripts/check-harness.sh complete --spec docs/specs/active/REQ-018-初始发布部署基线`
- L1 编译/构建：`mvn -pl ruoyi-admin -am -DskipTests package`
- L2 单元/契约：`mvn -pl ruoyi-requirement -am -Dtest=ReqMcpKeyControllerTest -Dsurefire.failIfNoSpecifiedTests=false test`
- L3 运行态冒烟：本次不启动服务；context-path 通过配置检查和前端 companion 构建验证，发布环境部署后补验。
- L4 跨端/端到端：不适用，本次无保存、导出、异步任务或核心业务流程变更。

## 验收 ID 覆盖

| 验收 ID | 计划阶段 | 验证方式 |
|---|---|---|
| AC-001 | 发布配置 | `rg` 检查配置和 Maven 构建 |
| AC-002 | 过程文档清理 | `find docs/specs` 和 harness 指定 spec 检查 |
| AC-003 | SQL 脚本清理 | `find docs/db/sql` |
| AC-004 | 长期文档同步 | `sh scripts/check-docs.sh` 和引用检查 |
| AC-005 | 验证与提交 | Maven 构建、harness 检查、`git diff --check` |
| AC-006 | MCP 回归保护 | `ReqMcpKeyControllerTest` |

## 执行约束

- 任务分支模式下完成修改和验证后直接 commit；merge、push、rebase 仍需用户确认。
- 不执行数据库变更，只整理初始发布脚本入口。
- 不扩大到业务接口、权限模型或 Mapper 逻辑修改。
