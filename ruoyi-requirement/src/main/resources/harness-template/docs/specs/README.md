# 需求文档目录

本目录用于存放单次需求的过程文档。长期有效的业务规则、接口契约和技术决策，应在需求完成后沉淀到 `../ai-harness/` 或 `../domains/`。

## 目录说明

| 目录 | 用途 |
|---|---|
| `active/` | 正在开发、待开发或尚未验收的需求 |
| `done/` | 已完成且仍有参考价值的需求方案 |

## 命名规范

新需求优先使用目录式结构：

```text
active/REQ-001-中文需求标题/
  meta.md
  requirement.md
  plan.md
  execution-report.md
  review-report.md
```

目录名必须同时包含稳定需求编号和中文标题，例如 `REQ-001-审核结果抽屉优化`。中文标题便于团队阅读；任务分支名不复用中文目录，必须按 `../process/git-workflow.md` 使用 ASCII。

目录内文件职责：

| 文件 | 维护者 | 用途 |
|---|---|---|
| `meta.md` | 当前阶段负责人 | 状态、流程模式、需求 Key、平台远端、目标分支、当前分支、授权状态、companion 仓库和关联 spec |
| `requirement.md` | Plan Agent | 业务目标、范围、影响面、验收标准 |
| `plan.md` | Execution Agent | 基于最终确认需求生成的可执行开发计划、验证路径、风险假设 |
| `execution-report.md` | Execution Agent | 实现摘要、验证结果、偏差和阻断 |
| `review-report.md` | Review Agent | 只读审查结论、验收覆盖矩阵和返修交接清单 |

目录内必须记录 companion 仓库或关联 spec；单仓需求也要写明“无 companion”。跨仓需求完成时，相关仓库的 `execution-report.md` 应互相记录对方分支和 commit。

历史单文件需求仍可保留：

```text
REQ-001-中文需求标题.md
```

## 使用流程

1. 新需求开始前，在 `active/` 下创建需求目录，并复制 `meta.md`。
2. 如果任务来自需求平台 Key，先按 `../process/platform-key-workflow.md` 校验模式；需求设计模式必须在最新基线创建或切换任务分支，先回写需求可行性评估，评估允许继续后只生成/调整 `requirement.md` 并回写平台版本；开发模式沿用该任务分支。
3. Plan Agent 复制模板并填写 `requirement.md`；MCP 接入模式和本地 Harness 模式在需求设计确认前都不得生成 `plan.md`。
4. Execution Agent 基于最终 `requirement.md` 先分析是否适合拆分为多个 subagent 并行执行，再生成或更新 `plan.md`，按 `plan.md` 实现，并填写 `execution-report.md`；除用户明确要求“只执行不 Review”外，执行结束后自动进入 Review 阶段。
5. Review Agent 只读审查，并填写 `review-report.md`；需要返修时使用 `RF-001` 形式编号。
6. Review Agent 产生 `RF-*` 后自动回到 Execution Agent；Execution Agent 修复 Review 问题后，在 `execution-report.md` 的 `Review 返修记录` 中用同一批 `RF-*` 编号回填处理结果。
7. Execution Agent 回填返修记录后自动回到 Review Agent 复审；Review Agent 在 `review-report.md` 的 `复审记录` 中更新每个 `RF-*` 的复审结论。
8. 自动 Review、返修和复审循环持续到最终 Review 结论为 `通过`。
9. 完成后，将长期有效内容沉淀到 `ai-harness/` 或 `domains/`。
10. 需求完成且仍有参考价值时，将目录移动到 `done/`。
11. `review-report.md` 最终结论为 `通过` 后，才能从 `active/` 移入 `done/`；`有条件通过` 和 `阻断` 只能作为中间交接结论。

## 检查命令

- `meta.md` 的状态必须是 `planning`、`executing`、`review`、`repairing` 或 `complete`。
- `meta.md` 必须记录流程模式、需求 Key、平台关联远端、平台目标分支、执行模式、执行授权和 Review 授权。
- `meta.md` 状态必须和文件阶段匹配；准备关闭指定需求时，先把状态更新为 `complete`。
- `meta.md` 当前角色必须和状态匹配：Plan Agent 只负责 `planning`，Execution Agent 负责 `executing` / `repairing`，Review Agent 负责 `review`；人工接管时写 `人工` 或 `用户`。
- `requirement.md` 中的每个 `AC-*` 验收 ID 必须在 `plan.md` 中出现；当 `execution-report.md` 或 `review-report.md` 存在时，也必须覆盖同一批验收 ID。
- Review Agent 刚写完 `review-report.md`、尚未返修时运行：`sh scripts/check-harness.sh review`。该模式允许 `阻断` 或 `有条件通过` 结论和未回填的 `RF-*`，用于验证 Review 中间交接文件本身是否合格。
- Execution Agent 完成返修并回填后自动交回 Review Agent 复审；最终 Review 通过后运行：`sh scripts/check-harness.sh` 或 `sh scripts/check-harness.sh complete`。该模式要求阻断已关闭、`RF-*` 已在 `execution-report.md` 回填，且最终 Review 结论为 `通过`。
- 关闭单个需求前可运行：`sh scripts/check-harness.sh complete --spec docs/specs/active/REQ-001-中文需求标题`。
- `check-harness.sh` 会检查 active spec 目录名是否包含稳定 `REQ-001` 编号和中文需求标题。
- `review-report.md` 的 `Review 结论` 必须是 `通过`、`有条件通过` 或 `阻断`；完成态只接受最终 `通过`。
- Review Agent 产生 `RF-*` 后应自动交回 Execution Agent，不直接修代码，也不回填 `execution-report.md`，除非用户明确授权切换角色。
