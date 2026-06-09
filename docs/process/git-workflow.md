# Git 工作流规范

本规范用于新需求、Bug 修复、文档治理或跨端联调时的分支/worktree 管理。默认不自动创建分支、worktree、commit、merge、rebase 或删除工作区，除非用户明确要求。

本规范区分两种模式：

- 普通模式：未明确要求隔离开发时，不自动 commit；只适合只读分析、小范围文档维护或用户明确允许的当前分支修改。
- 隔离开发模式：用户明确要求“创建分支/worktree 并完成任务”后，执行开发计划时必须在该分支/worktree 内按计划阶段 commit，避免长任务失去可追踪性。

## 默认原则

- 不自动合并主分支。
- 不自动删除分支或 worktree。
- 不自动 push。
- 不在脏工作区上执行会覆盖用户改动的操作。
- 创建分支/worktree 前先说明当前状态和计划，等待用户确认。
- 普通模式下不自动提交代码。
- 用户选择方案、确认方向或回复“可以/按这个方向”，不等于授权创建分支、改代码、commit、merge、push 或进入 Review。
- 当前分支为 `main` 或 `master` 时，除只读分析和明确的小文档修正外，不得开始功能实现；如确需在主分支修改，必须获得明确授权并记录到 `docs/specs/active/.../meta.md` 的 `主分支修改授权`。
- 隔离开发模式下，执行开发计划必须在验证通过后按阶段提交。

## 何时建议隔离开发

以下情况建议创建独立分支或 worktree：

- 新功能或影响范围不小的重构。
- 跨端、跨服务或多仓联调。
- 数据库、权限、导出、异步流程等高影响链路。
- 当前工作区已有未提交改动。
- 需要并行处理多个需求。

文档小修、只读分析、一次性验证通常不需要新分支。新功能、接口/数据库/权限/页面流程修改，若当前在 `main` 或 `master`，默认必须先请求任务分支或 worktree；只有用户明确说允许主分支修改时才可继续。

## 开始前检查

在受影响仓库执行：

```bash
git status --short
git branch --show-current
```

如果涉及多个仓库，每个仓库都要检查。

如果当前分支是 `main` 或 `master`，且任务将进入实现阶段：

- 先停止在计划阶段，说明需要任务分支/worktree 或主分支修改授权。
- 未获得明确授权前，不得修改业务代码、写 `execution-report.md` 或推进到 Review。
- 若用户明确允许主分支修改，必须在 `meta.md` 写 `主分支修改授权：已授权`。

同时读取 `docs/ai-harness/harness-index.json`：

- 确认当前仓库角色和 companion 仓库，避免只改前端或只改后端。
- 如果 `customization.customerBranches` 非空，或需求明确面向某个客户，必须先确认客户基线分支。
- 未确认客户基线分支前，不要从 `main`、`master` 或当前分支直接创建任务分支。

若存在未提交改动：

- 判断是否与本次任务相关。
- 不要擅自还原或覆盖。
- 先向用户说明，再决定继续、换分支、建 worktree 或暂停。

## 分支命名建议

```text
feature/REQ-YYYYMMDD-001-ascii-task
feature/customer-code/REQ-YYYYMMDD-001-ascii-task
fix/REQ-YYYYMMDD-001-ascii-fix
docs/REQ-YYYYMMDD-001-harness-docs
chore/REQ-YYYYMMDD-001-maintenance
```

Git 任务分支必须使用 ASCII，避免 CI、Webhook、URL、Shell 和远端平台兼容问题。需求 spec 目录可使用 `YYYY-MM-DD-REQ-001-中文需求标题`，但不要直接复用为分支名。

多仓联调时，建议相关仓库使用相同 ASCII 分支名，便于追踪。客户定制需求应从对应客户基线分支创建任务分支，并在 `docs/specs/active/.../meta.md` 记录目标客户和基线分支。

## worktree 使用建议

仅在用户明确要求隔离开发，或当前工作区不适合直接修改时使用 worktree。

示例：

```bash
git worktree add ../project-req-001 -b feature/REQ-YYYYMMDD-001-ascii-task
```

多仓联调时，应分别在相关仓库创建对应分支或 worktree。

## 提交策略

### 普通模式

未明确进入隔离开发模式时：

1. 只做只读分析、小范围文档维护，或用户明确允许的当前分支修改。
2. 如果要从计划进入实现，必须先确认执行授权；当前分支为 `main` 或 `master` 时还必须确认主分支修改授权。
3. 开发和验证完成后汇总修改文件、影响范围和验证结果。
4. 给出建议 commit message。
5. 只有用户明确要求时才执行 commit。

### 隔离开发模式

当用户已经明确要求创建分支或 worktree 执行任务时，视为授权并要求在该隔离环境内进行阶段性 commit：

1. 每个 commit 必须对应一个清晰阶段，例如文档、后端、前端、接口联调、测试或修复。
2. commit 前必须运行与该阶段匹配的最小验证；无法运行时在提交说明或完成说明中写明原因。
3. commit message 使用中文，保持简洁。
4. 不要把无关改动混入同一个 commit。
5. 如果发现工作区有用户未说明的改动，停止并询问。

多 agent 协作时，阶段性 commit 不需要每次等待用户确认，但必须满足：

- 当前工作区是用户明确授权的隔离分支或 worktree。
- `docs/specs/active/.../plan.md` 已明确允许执行该阶段。
- commit 内容只覆盖当前阶段，不混入计划外改动。
- `execution-report.md` 记录 commit、验证命令和结果。
- 如果某阶段无法提交，必须在 `execution-report.md` 写明阻断原因；不得用“未提交”替代阶段性 commit 后直接进入完成态。

如果需要 merge、push、rebase、删除 worktree、删除远端分支或覆盖未提交改动，仍需用户明确确认。

## 完成后处理

开发和验证完成后：

1. 汇总修改文件、影响范围和验证结果。
2. 说明是否已在隔离开发模式下提交，以及提交列表。
3. 普通模式下给出建议 commit message。
4. 只有用户明确要求时才执行 merge、rebase、push 或删除 worktree。

## 合并前检查

执行 merge 或 rebase 前，必须确认：

- 工作区没有未说明的未提交改动。
- 必要验证已通过。
- 文档和 harness 已同步更新。
- 多仓任务中相关仓库状态一致。

如果存在冲突，应停止并说明冲突文件，不要擅自大范围改写。
