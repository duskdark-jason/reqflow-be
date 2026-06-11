# 项目文档与目录清理执行计划

## 执行步骤

1. 基于最新 `main` 创建 `chore/REQ-20260611-001-workspace-cleanup` 任务分支。
2. 使用 `git mv` 将后端 `sql/` 迁移到 `docs/db/sql/`。
3. 删除后端旧 `doc/若依环境使用手册.docx`。
4. 重写 root、后端和前端 README。
5. 同步 `docs/db`、`docs/ai-harness`、`docs/process`、`docs/templates` 和 `harness-template` 中的 SQL 路径。
6. 将已 complete 但仍位于 active 的历史 spec 归档到 done。
7. 运行文档、harness 和必要脚本验证。

## 验收覆盖

| 验收 ID | 执行步骤 | 分层验证 |
|---|---|---|
| AC-001 | 迁移后检查后端根目录不再存在 tracked `sql/` 和 `doc/` 目录。 | L0 |
| AC-002 | 检查 SQL 脚本和长期文档引用均指向 `docs/db/sql/` 或 `docs/db/`。 | L0 |
| AC-003 | 扫描 README，确认若依默认宣传内容已移除。 | L0 |
| AC-004 | 运行文档、harness、脚本自测和后端路径提示回归测试。 | L0、L2 |

## 分层验证计划

- `sh scripts/check-docs.sh`
- `sh scripts/check-harness.sh complete --spec docs/specs/done/REQ-002-项目文档与目录清理`
- `sh scripts/test-check-harness.sh`，覆盖 harness 模板 SQL 路径校验。
- `mvn -pl ruoyi-requirement -am -Dtest=ReqRepositoryIndexServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test`
