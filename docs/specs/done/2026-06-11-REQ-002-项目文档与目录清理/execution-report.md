# 项目文档与目录清理执行报告

## 执行结论

- 状态：已完成
- 分支：chore/REQ-20260611-001-workspace-cleanup
- commit：本次任务分支提交，提交哈希见 Git 记录。

## 模块知识库沉淀

- 影响模块：项目文档、数据库脚本、AI Harness
- 模块知识库动作：更新
- 模块知识库文档：docs/ai-harness/modules/requirement-platform.md
- 无需更新原因：不适用

## 数据库变更沉淀

- 数据库影响：无持久化结构变化，仅迁移脚本存放目录和文档引用。
- SQL 脚本路径：docs/db/sql/
- 数据库文档路径：docs/db/README.md、docs/db/table-dictionary.md、docs/db/relationship.md
- 数据库变更说明：无
- 无需更新原因：未新增或修改表结构、字段、索引、约束或数据口径。

## 代码注释处理

- 注释动作：无需新增
- 注释文件：无
- 处理说明：本次仅迁移脚本目录、文档和提示路径；运行时代码只替换迁移脚本提示字符串，无复杂业务分支或新数据口径，因此无需新增代码注释。

## 主要变更

- 后端 SQL 脚本从 `sql/` 迁移到 `docs/db/sql/`。
- 删除后端旧 `doc/若依环境使用手册.docx`。
- 重写 README，移除若依默认介绍和演示材料。
- 更新数据库文档、AI Harness 文档和模板 SQL 路径。
- 归档已完成但仍位于 active 的历史 spec。

## 影响说明

- 接口：否。
- 持久化结构：否，脚本路径迁移，不改变 SQL 内容。
- 权限：否。
- 页面展示：否。

## 验证结果

| 层级 | 验收 ID | 命令或方式 | 结果 |
|---|---|---|---|
| L0 | AC-001 | `find reqflow-be -maxdepth 2 -type d -name sql -o -name doc` | 通过，未发现后端根 tracked `sql/` 或 `doc/` 目录。 |
| L0 | AC-002 | `find reqflow-be/docs/db/sql -maxdepth 1 -type f -print` | 通过，SQL 脚本已位于 `docs/db/sql/`。 |
| L0 | AC-003 | `rg` 扫描 README 若依默认宣传内容 | 通过，README 不再保留若依默认宣传内容。 |
| L0 | AC-004 | `sh scripts/check-docs.sh` | 已通过，最终复验再次确认。 |
| L0 | AC-004 | `sh scripts/check-harness.sh complete --spec docs/specs/done/2026-06-11-REQ-002-项目文档与目录清理` | 通过。 |
| L0 | AC-004 | `sh scripts/test-check-harness.sh` | 通过。 |
| L2 | AC-004 | `mvn -pl ruoyi-requirement -am -Dtest=ReqRepositoryIndexServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false test` | 通过，15 tests。 |

## 运行态证据

- 执行目录：当前 `reqflow-be` 子仓库根目录。
- 启动命令：未执行。
- profile/env/mode：不适用，未改接口、页面或服务启动配置。
- 检查命令：见“验证结果”。
- 原始错误摘要：首次 complete 门禁提示执行报告缺少 AC 覆盖、分层验证和代码注释处理，已补齐后复验通过。
- screenshot/trace 路径：无。
- 是否代表用户环境：否，仅代表当前执行 agent 环境。
- 后续补验环境：无需补验。

## 计划偏差

- 原计划为纯文档清理；路径扫描发现运行时代码和内置 harness 模板仍指向旧 `sql/`，已补充运行时提示字符串和回归测试。

## Review 返修记录

无。

## 风险与后续

- 历史 done spec 中仍保留当时执行记录的旧 `sql/` 路径，仅作为历史证据；当前长期文档、模板和运行时提示已指向 `docs/db/sql/`。
