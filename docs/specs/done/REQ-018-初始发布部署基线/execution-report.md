# 初始发布部署基线后端执行报告

## 执行结论

- 状态：已完成
- 分支：chore/req-018-release-baseline
- commit：self-contained-in-this-commit

## 修改摘要

| 路径 | 修改说明 |
|---|---|
| `ruoyi-admin/src/main/resources/application.yml` | 设置 `server.servlet.context-path=/reqflow-api`，上传目录使用 `/reqflow/uploadPath`，覆盖 AC-001。 |
| `ruoyi-requirement/src/test/java/com/ruoyi/requirement/controller/ReqMcpKeyControllerTest.java` | 新增 MCP 地址回归用例，验证公网 host 场景保留后端 `/reqflow-api` context-path，覆盖 AC-006。 |
| `docs/db/sql/req_platform_menu.sql` | 删除不再使用的 `req:mcp:key:edit` 初始化项并调整删除按钮顺序，覆盖 AC-003。 |
| `docs/db/sql/req_platform_release_settings.sql` | 新增初始发布系统参数、角色授权和品牌清理脚本，覆盖 AC-003、AC-004。 |
| `docs/db/sql/req_platform_req*.sql`、历史 spec 目录 | 删除历史增量、迁移、测试设置脚本和历史过程 spec，覆盖 AC-002、AC-003。 |
| `README.md`、`docs/db/**`、`docs/ai-harness/**`、`docs/runbooks/local-run.detected.md` | 同步初始发布 SQL 保留口径、后端 context-path 与 MCP endpoint 说明，覆盖 AC-004、AC-006。 |

## 模块知识库沉淀

- 影响模块：后端发布配置、数据库脚本、需求过程文档
- 模块知识库动作：更新
- 模块知识库文档：`docs/ai-harness/modules/requirement-platform.md`
- 无需更新原因：不适用

## 数据库变更沉淀

- 数据库影响：有
- SQL 脚本路径：`docs/db/sql/req_platform_menu.sql`、`docs/db/sql/req_platform_release_settings.sql`
- 数据库文档路径：`docs/db/README.md`、`docs/db/table-dictionary.md`、`docs/db/relationship.md`
- 数据库变更说明：本次不改变业务表结构，仅整理初始发布脚本入口并移除历史过程 SQL。
- 无需更新原因：不适用

## 代码注释处理

- 注释动作：无需新增
- 注释文件：无
- 处理说明：本次只调整配置、发布脚本和文档，并用单元测试锁定 MCP 地址规则；未新增复杂业务分支、权限边界或数据口径逻辑。

## 验证结果

| 层级 | 验收 ID | 命令或方式 | 结果 |
|---|---|---|---|
| L0 | AC-002、AC-003、AC-004、AC-006 | `find docs/specs -type f -print`、`find docs/db/sql -maxdepth 1 -type f -print`、`rg` 引用检查 | 通过；历史 spec 仅剩当前发布基线，SQL 目录仅剩发布基线脚本集合，长期文档未指向已删除历史 SQL。 |
| L1 | AC-001、AC-005 | `mvn -pl ruoyi-admin -am -DskipTests package` | 通过；后端启动模块打包成功。 |
| L2 | AC-006 | `mvn -pl ruoyi-requirement -am -Dtest=ReqMcpKeyControllerTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过；`ReqMcpKeyControllerTest` 6 个用例通过，新增用例覆盖 `/reqflow-api/requirement/mcp`。 |
| L3 | AC-001、AC-006 | 配置检查和 companion 前端构建产物检查 | 通过；本次不启动服务，发布环境部署后可补做真实 HTTP 冒烟。 |
| L4（可选） | AC-005 | 本次无业务流程、保存、导出或异步任务变更 | 不适用。 |
| L0 | AC-005 | `sh scripts/check-docs.sh`、`sh scripts/check-harness.sh complete --spec docs/specs/active/REQ-018-初始发布部署基线` | 通过；文档检查通过，harness complete 模式通过。 |
| L0 | AC-005 | `git diff --check` | 通过；无 whitespace 问题。 |

## 运行态证据

- 执行目录：当前后端子仓库根目录
- 启动命令：本次未启动服务
- profile/env/mode：Maven 单测和打包验证
- 检查命令：`mvn -pl ruoyi-requirement -am -Dtest=ReqMcpKeyControllerTest -Dsurefire.failIfNoSpecifiedTests=false test`、`mvn -pl ruoyi-admin -am -DskipTests package`
- 原始错误摘要：无
- screenshot/trace 路径：无
- 是否代表用户环境：否，仅代表当前执行 agent 环境
- 后续补验环境：测试环境或发布环境

## 计划偏差

- 无偏差；L3 运行态 HTTP 冒烟按计划留待发布环境补验。

## Review 返修记录

Review Agent 未产生返修项：无。

| 修复 ID | 关联验收 ID | 处理结果 | 修改文件 | 验证命令 | 结果 |
|---|---|---|---|---|---|
| 无 | AC-001、AC-002、AC-003、AC-004、AC-005、AC-006 | 无需修复 | 无 | 无 | 通过 |

## 风险与后续

- 发布环境需由部署人员按最终域名和反向代理配置补验 `/reqflow-api/requirement/mcp` 的真实 HTTP MCP lifecycle。
