# 多 Agent 文件交接流程

本流程用于多个 agent 或工具协作开发同一需求。目标是让计划、执行、测试和 review 之间有稳定文件交接，避免只靠对话上下文。

## 目录结构

新需求默认使用目录式 spec：

```text
docs/specs/active/YYYY-MM-DD-需求简称/
  meta.md
  requirement.md
  plan.md
  execution-report.md
  review-report.md
```

旧的单文件需求文档可以继续作为历史参考；新需求优先使用目录式结构。

## 角色职责

### Plan Agent

- 负责澄清业务目标、影响范围、接口契约、数据口径、验证路径和风险。
- 输出 `requirement.md` 和 `plan.md`。
- `plan.md` 必须足够明确，执行 agent 不需要再决定需求范围、接口字段、数据口径或验收标准。
- 如果存在不确定项，必须在计划阶段标记为阻断或假设，不能让执行阶段自行猜测。

### Execution Agent

- 负责按 `plan.md` 实现、补测试、运行验证和更新长期 harness。
- 不自行扩展需求范围，不新增计划外接口、字段、依赖或架构。
- 如果发现计划缺失、冲突或无法执行，停止并在 `execution-report.md` 写明阻断点。
- 完成后写 `execution-report.md`，包含修改文件、验证命令、结果、未验证风险和与计划的偏差。
- L3/L4 失败或跳过时，必须记录启动命令、执行目录、profile/env 或 mode、检查命令、错误摘要和补验环境；不得把当前 agent 环境问题写成用户环境问题。
- Review Agent 产生 `RF-001` 形式的返修项后，执行 agent 必须按相同修复 ID 在 `execution-report.md` 的 `Review 返修记录` 中回填处理结果、修改文件和验证命令。

### Review Agent

- 只做审查，不直接修改代码。
- 审查输入包括 `requirement.md`、`plan.md`、`execution-report.md`、代码 diff、测试结果和相关 harness 文档。
- 输出 `review-report.md`，结论只能是：`通过`、`有条件通过`、`阻断`。
- 阻断问题必须给出文件、行为、风险和可复现验证方式。
- `review-report.md` 是唯一 Review 结论与返修交接文件；不要另建 `fix-request.md`、`review-fix.md` 等并行文件。
- 结论为 `阻断` 或 `有条件通过` 且需要执行 agent 处理时，必须填写 `返修交接清单`，每项使用 `RF-001`、`RF-002` 递增编号，并关联验收 ID、修复要求和验证要求。
- Review Agent 产生 `RF-*` 后必须停止，不得直接修改代码或回填 `execution-report.md`；除非用户明确授权切换为 Execution Agent 继续返修。
- 执行 agent 回填返修记录后，Review Agent 复审并在 `review-report.md` 的 `复审记录` 中写明每个修复 ID 的复审结论。
- 对无命令证据的“DB 不可达”“环境不可达”“后端不可用”等结论，应标记为 `阻断` 或 `有条件通过`。

## Review 分级

| 级别 | 使用场景 |
|---|---|
| 阻断 | 需求不可用、数据错误、安全/权限风险、迁移风险、缺少关键验收证据、无证据环境结论 |
| 重要 | 契约不一致、关键测试缺口、L3/L4 未验证且影响真实流程 |
| 一般 | 命名、文档清晰度、低风险维护建议 |

## 状态流转

```text
draft requirement -> approved plan -> executing -> ready for review -> reviewed -> repairing -> re-review -> done
```

- `requirement.md` 未清楚前，不进入实现。
- `plan.md` 未包含验收和验证前，不进入实现。
- `meta.md` 必须记录当前状态、当前角色、当前分支和 companion 仓库；阶段变化时同步更新。
- `meta.md` 状态必须和文件阶段匹配：`planning` 不应已有执行或 Review 报告，`review` / `repairing` / `complete` 必须已有执行和 Review 报告。
- `meta.md` 当前角色必须和状态匹配：`planning` 对应 Plan Agent，`executing` / `repairing` 对应 Execution Agent，`review` 对应 Review Agent；人工或用户接管时可写 `人工` / `用户`。
- `execution-report.md` 未列出验证证据前，不进入 review。
- `requirement.md` 中每个 `AC-*` 必须在 `plan.md`、`execution-report.md` 和 `review-report.md` 中形成闭环。
- `review-report.md` 为 `阻断` 时，必须通过 `返修交接清单` 指向计划或执行阶段。
- `review-report.md` 存在 `RF-*` 返修项时，执行 agent 必须在 `execution-report.md` 回填同 ID 的 `Review 返修记录`，再进入复审。

## Harness 检查模式

`scripts/check-harness.sh` 区分初始化、Review 阶段和完成态：

```bash
sh scripts/check-harness.sh init      # Harness 初始化或纯文档接入后使用，不检查 active spec，不启动项目
sh scripts/check-harness.sh review    # Review Agent 输出 review-report.md 后使用，允许阻断结论和未回填 RF
sh scripts/check-harness.sh complete  # 默认完成态门禁，要求 RF 已回填且阻断已复审关闭
sh scripts/check-harness.sh complete --spec docs/specs/active/YYYY-MM-DD-需求简称  # 只检查指定需求，要求 meta.md 状态为 complete
sh scripts/check-harness.sh           # 等同 complete
```

- Harness 初始化或纯文档接入时，只运行 `init` 模式和 `check-docs.sh`，不跑项目构建、测试、启动或 L3/L4 冒烟。
- Review Agent 写出 `阻断` 或 `有条件通过` 的 `review-report.md` 后，应运行 `review` 模式并停止交接。
- Execution Agent 完成 RF 返修、回填 `execution-report.md` 后，再运行默认 `complete` 模式。
- `review-report.md` 的 `Review 结论` 只能是 `通过`、`有条件通过` 或 `阻断`，否则门禁失败。
- 不要为了让默认门禁通过而让 Review Agent 越权修复代码。

## Git 配合

- 普通模式下不自动提交。
- 用户明确要求创建分支或 worktree 执行任务时，执行 agent 可在隔离分支内按阶段提交。
- merge、push、rebase、删除 worktree 或删除远端分支仍需用户明确确认。
- 多仓联调时，相关仓库应使用相同需求目录名和分支名，便于追踪。
