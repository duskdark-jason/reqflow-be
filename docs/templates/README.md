# 模板使用说明

本目录只放可复制模板。新需求默认使用目录式 spec，不再默认使用单文件需求模板。

## 新需求主流程

在 `docs/specs/active/YYYY-MM-DD-REQ-001-中文需求标题/` 下复制并填写：

| 模板 | 目标文件 | 用途 |
|---|---|---|
| `spec-meta-template.md` | `meta.md` | 状态、当前角色、分支和 companion 仓库 |
| `requirement-template.md` | `requirement.md` | 背景、范围、契约和验收 ID |
| `agent-plan-template.md` | `plan.md` | 分阶段实现计划、文件范围和验证计划 |
| `execution-report-template.md` | `execution-report.md` | 执行结果、验证证据、偏差和返修记录 |
| `review-report-template.md` | `review-report.md` | Review 结论、验收覆盖矩阵和返修交接 |

## 专项模板

| 模板 | 使用场景 |
|---|---|
| `db-change-template.md` | 新增 DDL、字段、索引或迁移脚本 |
| `e2e-smoke-template.md` | 可选沉淀 Playwright smoke 流程 |

## 辅助或旧版模板

| 模板 | 说明 |
|---|---|
| `acceptance-template.md` | 人工验收时的辅助清单，不替代 `requirement.md` 和 `review-report.md` |
