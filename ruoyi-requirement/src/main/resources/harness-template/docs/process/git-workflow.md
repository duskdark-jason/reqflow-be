# Git 工作流规范

本规范用于新需求、Bug 修复、文档治理或跨端联调时的分支、commit、merge 和 rebase 管理。新需求执行不使用 worktree；需要实现时，只能基于目标基线分支创建 ASCII 任务分支。

## 默认原则

- 不自动合并主分支。
- 不自动 push。
- 不自动 rebase。
- 不在脏工作区上执行会覆盖用户改动的操作。
- 用户选择方案、确认方向或回复“可以/按这个方向”，不等于授权创建分支、改代码、commit、merge 或 push；明确执行授权后自动 Review 循环随执行阶段启用。
- 当前分支为 `main` 或 `master` 时，除只读分析和明确的小文档修正外，不得开始功能实现。
- 新需求开始执行前，必须确认目标基线分支，并从该分支创建 ASCII 任务分支。
- 任务分支上的修改完成并通过验证后，必须直接提交。
- merge、push、rebase 或删除远端分支仍需用户明确确认。

## 何时创建任务分支

以下情况必须创建独立任务分支：

- 新功能或 Bug 修复。
- 跨端、跨服务或多仓联调。
- 数据库、权限、导出、异步流程等高影响链路。
- 页面、接口、契约或用户可见流程调整。
- 当前任务需要写 `execution-report.md` 或进入 Review。

文档小修、只读分析、一次性验证通常不需要新分支；但只要进入新需求执行阶段，就必须创建任务分支。

## 开始前检查

在受影响仓库执行：

```bash
git status --short
git branch --show-current
git remote -v
```

如果涉及多个仓库，每个仓库都要检查。

同时读取 `docs/ai-harness/harness-index.json`：

- 确认当前仓库角色和 companion 仓库，避免只改前端或只改后端。
- 如果 `customization.customerBranches` 非空，或需求明确面向某个客户，必须先确认客户基线分支。
- 如果任务来自需求平台 Key，必须确认当前仓库远端和当前分支与需求平台返回的目标仓库、目标基线分支一致。

若存在未提交改动：

- 判断是否与本次任务相关。
- 不要擅自还原或覆盖。
- 先向用户说明，再决定继续、换分支或暂停。

## 分支命名建议

```text
feature/REQ-YYYYMMDD-001-ascii-task
feature/customer-code/REQ-YYYYMMDD-001-ascii-task
fix/REQ-YYYYMMDD-001-ascii-fix
docs/REQ-YYYYMMDD-001-harness-docs
chore/REQ-YYYYMMDD-001-maintenance
```

Git 任务分支必须使用 ASCII，避免 CI、Webhook、URL、Shell 和远端平台兼容问题。需求 spec 目录可使用 `YYYY-MM-DD-REQ-001-中文需求标题`，但不要直接复用为分支名。

多仓联调时，相关仓库使用相同 ASCII 分支名，便于追踪。客户定制需求应从对应客户基线分支创建任务分支，并在 `docs/specs/active/.../meta.md` 记录目标客户和基线分支。

## 创建任务分支

确认目标基线分支后，在受影响仓库执行：

```bash
git switch <baseline-branch>
git pull --ff-only
git switch -c feature/REQ-YYYYMMDD-001-ascii-task
```

如本地不允许拉取远端，必须说明原因，并确认当前基线分支已经是可接受的最新代码。

多仓联调时，应分别在相关仓库基于各自目标基线分支创建对应任务分支。

## 提交策略

### 普通模式

未进入新需求执行阶段时：

1. 只做只读分析、小范围文档维护，或用户明确允许的当前分支修改。
2. 开发和验证完成后汇总修改文件、影响范围和验证结果。
3. 只有用户明确要求时才执行 commit。

### 任务分支模式

当用户已经明确要求执行新需求，或需求平台开发 Key 已进入开发模式时，视为授权创建任务分支并在完成后提交：

1. 必须先从目标基线分支创建 ASCII 任务分支。
2. `meta.md` 写 `执行模式：任务分支模式`、`执行授权：已授权` 和当前任务分支名。
3. 只按 `plan.md` 修改，不混入计划外改动。
4. commit 前必须运行与本次改动匹配的最小验证；无法运行时在 `execution-report.md` 和完成说明中写明原因。
5. 修改完成后必须直接 commit，并在 `execution-report.md` 记录 commit、验证命令和结果。
6. 如果某阶段无法提交，必须在 `execution-report.md` 写明阻断原因；不得用“未提交”替代 commit 后直接进入完成态。

如果需要 merge、push、rebase、删除远端分支或覆盖未提交改动，仍需用户明确确认。

## 完成后处理

开发和验证完成后：

1. 汇总修改文件、影响范围和验证结果。
2. 说明任务分支和提交列表。
3. 只有用户明确要求时才执行 merge、rebase、push 或删除远端分支。

## 合并前检查

执行 merge 或 rebase 前，必须确认：

- 工作区没有未说明的未提交改动。
- 必要验证已通过。
- 文档和 harness 已同步更新。
- 多仓任务中相关仓库状态一致。

如果存在冲突，应停止并说明冲突文件，不要擅自大范围改写。
