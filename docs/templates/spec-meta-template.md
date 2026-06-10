# 【需求名称】元信息

- 状态：planning
- 当前角色：Plan Agent
- 流程模式：【需求平台编排模式/需求平台开发模式/项目接入初始化模式/平台自身建设模式】
- 需求 Key：【需求平台 Key / 无，本地平台建设】
- 平台关联远端：【需求平台返回的 Git 远端 / 当前仓库远端 / 未配置】
- 平台目标分支：【目标分支/开发基线分支/当前任务分支】
- 执行模式：【不适用/普通模式/任务分支模式】
- 当前分支：【分支名或未创建】
- 执行授权：【未授权/已授权/不适用】
- Review 授权：【未授权/已授权/不适用】
- 目标客户：【通用/客户标识】
- 基线分支：【main/master/客户定制分支】
- companion 仓库：【无 companion / 关联仓库 spec 路径】
- 关联 spec：【无或路径】
- 最后更新：【YYYY-MM-DD】

## 状态说明

- `planning`：Plan Agent 正在产出 `requirement.md` 和 `plan.md`。
- `executing`：Execution Agent 正在按计划实现并写 `execution-report.md`。
- `review`：Review Agent 正在只读审查并写 `review-report.md`。
- `repairing`：Execution Agent 正在处理 `RF-*` 返修项。
- `complete`：验收、执行、Review 和返修闭环均已完成。

## 授权说明

- `需求平台编排模式` 不允许在本地写入 spec、改业务代码或提交；编排设计经确认后必须通过 MCP 回写需求平台。
- `需求平台开发模式` 必须先校验平台关联远端和平台目标分支，再基于开发基线创建任务分支。
- `项目接入初始化模式` 只允许下发 workspace `AGENTS.md`、子仓库 harness、运行 init 校验并回写初始化结果，不允许改业务代码。
- `平台自身建设模式` 仅用于当前需求平台项目自身建设或平台不可用时的降级流程，可以按阶段写本地 `docs/specs`。
- `planning` 阶段默认填写 `执行授权：未授权`、`Review 授权：未授权`。
- 进入 `executing`、`repairing` 或 `complete` 前，必须已有明确执行授权，并填写 `执行授权：已授权`。
- 进入 `review` 或 `complete` 前，必须已有明确 Review 授权或独立 Review 请求，并填写 `Review 授权：已授权`。
- 新需求执行必须填写 `执行模式：任务分支模式`，并在目标基线分支上创建 ASCII 任务分支；不得使用 worktree，也不得直接在 `main` 或 `master` 上实现。
- 用户选择方案、确认方向或同意建议，只代表进入计划阶段，不等于执行授权或 Review 授权。

## 角色匹配

- `planning`：当前角色写 `Plan Agent`。
- `executing` / `repairing`：当前角色写 `Execution Agent`。
- `review`：当前角色写 `Review Agent`。
- 人工或用户接管时，当前角色可写 `人工` 或 `用户`。
