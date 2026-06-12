# 初始发布部署基线后端 Review 报告

## Review 结论

- 结论：通过
- Review Agent：Codex 静态复核
- Review 时间：2026-06-12

## 审查输入

- `requirement.md`
- `plan.md`
- `execution-report.md`
- 代码 diff
- Maven 单测与打包输出
- SQL 和 spec 清理清单

## 问题清单

| 严重级别 | 文件 | 问题 | 风险 | 建议 |
|---|---|---|---|---|
| 无 | 无 | 未发现阻断或重要问题 | 无 | 无需返修 |

## 验收 ID 覆盖矩阵

| 验收 ID | 需求描述 | 实现证据 | 验证证据 | Review 结论 |
|---|---|---|---|---|
| AC-001 | context-path 与上传目录发布化 | `ruoyi-admin/src/main/resources/application.yml` | Maven 打包、配置 diff | 通过 |
| AC-002 | 清理历史 spec | `docs/specs/active`、`docs/specs/done` 清理结果 | `find docs/specs -type f -print` | 通过 |
| AC-003 | SQL 目录保留发布脚本 | `docs/db/sql/` 清理结果、`req_platform_release_settings.sql` | `find docs/db/sql -maxdepth 1 -type f -print` | 通过 |
| AC-004 | 长期文档不指向已删除历史 SQL | `README.md`、`docs/db/**`、`docs/ai-harness/**` | `rg` 引用检查 | 通过 |
| AC-005 | 后端构建和 harness 文档检查 | `execution-report.md` 验证计划 | Maven 命令、`check-docs`、`check-harness complete`、`git diff --check` 已通过 | 通过 |
| AC-006 | MCP 地址不受前端项目前缀影响 | `ReqMcpKeyControllerTest`、接口契约和模块文档 | `ReqMcpKeyControllerTest` 6 个用例通过 | 通过 |

## 验收复核

- AC-001：通过，配置已将后端 context-path 固定为 `/reqflow-api`。
- AC-002：通过，历史 active 和 done spec 已清理，仅保留当前发布基线与占位文件。
- AC-003：通过，SQL 目录收敛为 RuoYi 基线、Quartz、需求平台 schema、菜单和发布设置脚本。
- AC-004：通过，长期数据库和 harness 文档已改为 schema 与发布设置脚本口径。
- AC-005：通过，Maven 单测、打包、文档检查、harness complete 和 diff 检查已通过。
- AC-006：通过，新增回归测试证明 MCP 地址为后端 context-path 加 `/requirement/mcp`。

## 返修交接清单

无。

| 修复 ID | 严重级别 | 关联验收 ID | 问题 | 修复要求 | 验证要求 |
|---|---|---|---|---|---|
| 无 | 无 | AC-001、AC-002、AC-003、AC-004、AC-005、AC-006 | 无 | 无 | 无 |

## 复审记录

| 修复 ID | 执行处理结果 | 复审结论 | 复审证据 |
|---|---|---|---|
| 无 | 无需修复 | 通过 | Maven 单测、打包和静态 diff 复核 |

- 最终结论：通过

## Review 分级说明

- 阻断：需求不可用、数据错误、安全或权限风险、迁移风险、缺少关键验收证据。
- 重要：契约不一致、关键测试缺口、计划承诺的验证未完成且影响真实流程。
- 一般：命名、文档清晰度、低风险维护建议。
