# 多 Agent 文件交接流程

本流程用于多个 agent 或工具协作开发同一需求。目标是让计划、执行、测试和 review 之间有稳定文件交接，避免只靠对话上下文。

## 目录结构

新需求默认使用目录式 spec：

```text
docs/specs/active/REQ-001-中文需求标题/
  meta.md
  requirement.md
  plan.md
  execution-report.md
  review-report.md
```

旧的单文件需求文档可以继续作为历史参考；新需求优先使用目录式结构。

## 角色职责

### 需求设计阶段（Plan Agent）

- 负责澄清业务目标、影响范围、接口契约、数据口径、验证路径和风险。
- 需求平台模式下必须先输出需求可行性评估和风险判断，结论类型至少包含“可继续设计、需澄清、需调整、暂不可实现”，并通过 MCP `upload_requirement_assessment` 回写平台；结论需要需求人补充、调整或暂不可实现时，停止在反馈阶段。
- 未接入 MCP 或没有需求平台 Key 时，按 `local-harness-workflow.md` 使用本地 Harness 模式；本地模式仍使用相同的 `meta.md`、`requirement.md` 和阶段授权，只是不执行 MCP 回写。
- 评估结论允许继续后，只输出或更新 `requirement.md`；`plan.md` 由 Execution Agent 在执行阶段基于最终确认的需求设计生成。
- 平台自身建设或本地普通流程确需先写执行计划时，也必须把需求设计和执行计划分清，不得把用户选择方案直接当成执行授权。
- 用户选择方案、确认方向或同意建议时，只代表允许进入计划阶段；不得自动切换到执行阶段。
- `requirement.md` 必须足够明确，执行 agent 不需要再决定需求范围、接口字段、数据口径或验收标准。
- `meta.md` 必须记录影响模块和模块知识库动作；影响模块应对齐前端菜单目录、子菜单、隐藏页签或后端能力。
- 如果需求涉及菜单、页面、接口、权限、核心流程或数据口径，必须在 `requirement.md` 中声明执行阶段需要更新 `docs/ai-harness/modules/*.md`。
- 如果存在不确定项，必须在计划阶段标记为阻断或假设，不能让执行阶段自行猜测。
- 需求平台需求设计 Key 视为授权 Plan Agent 在最新基线创建或切换到平台建议的 ASCII 任务分支，并在本地保留最终 `requirement.md`；同一需求的补充调整必须继续使用同一任务分支和同一 spec 目录。
- Plan Agent 写完或调整可行性评估和 `requirement.md` 并通过 MCP 回写后必须停止，等待明确执行授权；不得修改业务代码、写 `plan.md`、写 `execution-report.md` 或写 `review-report.md`。
- 本地多轮需求设计也必须停在 `planning`；用户补充调整时继续迭代 `requirement.md`，不得提前写 `plan.md`。

### 执行阶段（Execution Agent）

- 负责按 `plan.md` 实现、补测试、运行验证和更新长期 harness。
- 开始改代码前必须确认 `meta.md` 已记录 `执行授权：已授权`，并确认当前分支是需求设计阶段创建的任务分支，不是未授权的 `main` 或 `master`。
- 开始实现前必须读取 `docs/ai-harness/search-map.md`，定位相关模块、契约、决策、数据库或流程入口。
- 如果 `plan.md` 尚不存在，Execution Agent 必须先基于最终 `requirement.md` 生成执行计划，再开始实现；生成计划仍属于执行阶段，不得回退成 Plan Agent 自动开发。
- 不自行扩展需求范围，不新增计划外接口、字段、依赖或架构。
- 如果发现计划缺失、冲突或无法执行，停止并在 `execution-report.md` 写明阻断点。
- 完成后写 `execution-report.md`，包含修改文件、验证命令、结果、未验证风险和与计划的偏差。
- 需求平台模式下，每次执行、返修或补验后都要更新同一个 `execution-report.md`，并通过 MCP `upload_execution_report` 回写新版本。
- 本地 Harness 模式下，每次执行、返修或补验后也要更新同一个 `execution-report.md`，但只能记录“未接入 MCP，本地文件闭环”，不得伪造平台回写成功。
- 完成后必须在 `execution-report.md` 记录模块知识库动作、模块文档路径和无需更新原因；新增或更新动作必须对应实际 `docs/ai-harness/modules/*.md` 文件。
- L3 失败或跳过，或已选择 L4 但失败/跳过时，必须记录启动命令、执行目录、profile/env 或 mode、检查命令、错误摘要和补验环境；不得把当前 agent 环境问题写成用户环境问题。
- 执行完成并具备验证证据后，必须自动把 `meta.md` 切到 `review`、填写 `Review 授权：已授权`，交给 Review Agent；用户明确要求只执行不 Review 时除外。
- Review Agent 产生 `RF-001` 形式的返修项后，执行 agent 必须按相同修复 ID 在 `execution-report.md` 的 `Review 返修记录` 中回填处理结果、修改文件和验证命令。
- Execution Agent 不得把自己的实现直接写成 Review 完成，不得代替 Review Agent 输出 `review-report.md`。

### 审查阶段（Review Agent）

- 基于自动 Review 阶段、明确 Review 授权或独立 Review 请求开始；不要把执行后的自检当成 Review 阶段。
- 只做审查，不直接修改代码。
- 审查输入包括 `requirement.md`、`plan.md`、`execution-report.md`、代码 diff、测试结果和相关 harness 文档。
- 输出 `review-report.md`，结论只能是：`通过`、`有条件通过`、`阻断`。
- 需求平台模式下，每次 Review、返修复审或补充验收后都要更新同一个 `review-report.md`，并通过 MCP `upload_review_report` 回写新版本。
- 本地 Harness 模式下，每次 Review、返修复审或补充验收后也要更新同一个 `review-report.md`，但不执行 MCP 回写。
- 阻断问题必须给出文件、行为、风险和可复现验证方式。
- `review-report.md` 是唯一 Review 结论与返修交接文件；不要另建 `fix-request.md`、`review-fix.md` 等并行文件。
- 结论为 `阻断` 或 `有条件通过` 且需要执行 agent 处理时，必须填写 `返修交接清单`，每项使用 `RF-001`、`RF-002` 递增编号，并关联验收 ID、修复要求和验证要求。
- Review Agent 产生 `RF-*` 后必须停止当前 Review 并自动交回 Execution Agent，不得直接修改代码或回填 `execution-report.md`。
- 执行 agent 回填返修记录后，Review Agent 自动复审并在 `review-report.md` 的 `复审记录` 中写明每个修复 ID 的复审结论；循环持续到最终 Review 结论为 `通过`。
- 对无命令证据的“DB 不可达”“环境不可达”“后端不可用”等结论，应标记为 `阻断` 或 `有条件通过`。

## Review 分级

| 级别 | 使用场景 |
|---|---|
| 阻断 | 需求不可用、数据错误、安全/权限风险、迁移风险、缺少关键验收证据、无证据环境结论 |
| 重要 | 契约不一致、关键测试缺口、计划承诺的运行态或增强验证未完成且影响真实流程 |
| 一般 | 命名、文档清晰度、低风险维护建议 |

## 状态流转

```text
draft requirement -> requirement design branch -> requirement design version -> requirement confirmed -> execution plan -> executing -> automatic review -> repairing -> automatic re-review -> final review passed -> done
```

- `requirement.md` 未清楚前，不进入实现。
- `docs/ai-harness/search-map.md` 未能定位到相关入口时，先补充搜索导航或在需求文档写明无需更新原因。
- `plan.md` 未包含验收和验证前，不进入实现；需求平台模式和本地 Harness 模式下，`plan.md` 都由 Execution Agent 在执行阶段生成或更新。
- `meta.md` 必须记录当前状态、当前角色、执行模式、当前分支、执行授权、Review 授权、companion 仓库、影响模块、模块知识库动作、模块知识库文档和无需更新原因；阶段变化时同步更新。
- `meta.md` 状态必须和文件阶段匹配：`planning` 不应已有 `plan.md`、执行报告或 Review 报告，`review` / `repairing` / `complete` 必须已有执行和 Review 报告。
- `meta.md` 当前角色必须和状态匹配：`planning` 对应 Plan Agent，`executing` / `repairing` 对应 Execution Agent，`review` 对应 Review Agent；人工或用户接管时可写 `人工` / `用户`。
- `executing` / `repairing` / `complete` 必须有 `执行授权：已授权`；执行授权默认包含后续自动 Review 循环，`review` / `complete` 必须有 `Review 授权：已授权`。
- `planning` / `executing` / `repairing` / `complete` 在需求平台模式下必须使用同一个任务分支；开发和返修阶段不得再创建不同任务分支，当前分支不得是 `main` 或 `master`。
- `execution-report.md` 未列出验证证据前，不进入 review。
- 模块知识库动作是 `新增` 或 `更新` 时，完成态前必须在 `execution-report.md` 记录 `docs/ai-harness/modules/*.md` 路径。
- 模块知识库动作是 `无需更新` 时，完成态前必须在 `meta.md` 或 `execution-report.md` 说明原因。
- `requirement.md` 中每个 `AC-*` 必须在 `plan.md`、`execution-report.md` 和 `review-report.md` 中形成闭环。
- `review-report.md` 为 `阻断` 或 `有条件通过` 时，必须通过 `返修交接清单` 指向计划或执行阶段。
- `review-report.md` 存在 `RF-*` 返修项时，执行 agent 必须在 `execution-report.md` 回填同 ID 的 `Review 返修记录`，再进入复审；完成态要求最终 Review 结论为 `通过`。

## Harness 检查模式

`scripts/check-harness.sh` 区分初始化、Review 阶段和完成态：

```bash
sh scripts/check-harness.sh init      # Harness 初始化或纯文档接入后使用，不检查 active spec，不启动项目
sh scripts/check-harness.sh review    # Review Agent 输出 review-report.md 后使用，允许阻断结论和未回填 RF
sh scripts/check-harness.sh complete  # 默认完成态门禁，要求 RF 已回填且最终 Review 通过
sh scripts/check-harness.sh complete --spec docs/specs/active/REQ-001-中文需求标题  # 只检查指定需求，要求 meta.md 状态为 complete
sh scripts/check-harness.sh           # 等同 complete
```

- Harness 初始化或纯文档接入时，只运行 `init` 模式和 `check-docs.sh`，不跑项目构建、测试、启动或 L3/L4 冒烟。
- `init` 模式要求存在 `docs/ai-harness/search-map.md` 和 `docs/process/local-harness-workflow.md`，且至少有一个非模板模块文档。
- Review Agent 写出 `阻断` 或 `有条件通过` 的 `review-report.md` 后，应运行 `review` 模式作为中间检查，并自动交回 Execution Agent 返修。
- Execution Agent 完成 RF 返修、回填 `execution-report.md` 后，自动交回 Review Agent 复审；复审最终通过后再运行默认 `complete` 模式。
- `review-report.md` 的 `Review 结论` 只能是 `通过`、`有条件通过` 或 `阻断`，否则门禁失败。
- `complete` 模式不接受仍停留在 `阻断` 或 `有条件通过` 的 Review；必须有最终 `通过` 结论。
- 不要为了让默认门禁通过而让 Review Agent 越权修复代码。

## Git 配合

- 普通模式下不自动提交。
- 普通模式不等于允许在主分支实现；当前分支为 `main` 或 `master` 时，除只读分析和明确的小文档修正外，不得开始功能开发。
- “选这个方案”“可以”“按这个方向”不等于改代码或 commit；只有需求平台需求设计 Key、明确创建需求设计分支指令或明确执行授权，才允许创建任务分支。
- 需求平台模式下，Plan Agent 在需求设计阶段先从目标基线分支创建 ASCII 任务分支；Execution Agent 只切换并沿用该分支，不再创建不同任务分支。不使用 worktree。
- 任务分支上的修改完成并通过验证后直接 commit；merge、push、rebase 或删除远端分支仍需用户明确确认。
- 多仓联调时，相关仓库应使用相同中文 spec 目录名，并使用相同 ASCII 任务分支名，便于追踪。
