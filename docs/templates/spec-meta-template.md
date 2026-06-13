# 【需求名称】元信息

- 状态：planning
- 当前角色：Plan Agent
- 流程模式：【需求平台需求设计模式/需求平台开发模式/项目接入初始化模式/本地 Harness 模式】
- 需求 Key：【需求平台 Key / 无，本地流程】
- MCP 接入状态：【已接入/未接入/不可用】
- 平台回写状态：【已回写/未回写/不适用，必须基于真实工具调用】
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
- 影响模块：【菜单目录/子菜单/隐藏页签/后端能力，多个用顿号分隔】
- 模块知识库动作：【新增/更新/无需更新】
- 模块知识库文档：【docs/ai-harness/modules/xxx.md / 无】
- 无需更新原因：【模块知识库动作为无需更新时必填，否则写不适用】
- 最后更新：【YYYY-MM-DD】

## 状态说明

- `planning`：Plan Agent 正在产出或调整需求可行性评估和 `requirement.md`。
- `executing`：Execution Agent 正在按计划实现并写 `execution-report.md`。
- `review`：Review Agent 正在只读审查并写 `review-report.md`。
- `repairing`：Execution Agent 正在处理 `RF-*` 返修项。
- `complete`：验收、执行、Review 和返修闭环均已完成。

## 授权说明

- `需求平台需求设计模式` 必须先校验平台关联远端和平台目标分支，在最新基线上创建或切换任务分支，先本地生成需求可行性评估；用户明确确认后再通过 MCP 回写。评估结论允许继续后只写或迭代 `requirement.md`，用户明确确认定稿后再通过 MCP 回写需求设计版本。
- `需求平台开发模式` 必须先校验平台关联远端、平台目标分支和任务分支，沿用需求设计阶段任务分支，不得另建不同开发分支。
- `项目接入初始化模式` 只允许下发 workspace `AGENTS.md`、子仓库 harness、运行 init 校验、提交并推送初始化文件、回写初始化结果，不允许改业务代码。
- `本地 Harness 模式` 用于没有需求平台 Key、未接入 MCP 或 MCP 不可用的一般项目，可以按阶段写本地 `docs/specs`。
- 本地 Harness 模式与 MCP 接入模式使用相同的 spec、Review、返修和完成门禁；区别只是不得声明不存在的 MCP 回写。
- 本地 Harness 模式也必须保留需求设计确认点；多轮补充调整期间保持 `planning`，不得提前生成 `plan.md`、`execution-report.md` 或 `review-report.md`。
- `planning` 阶段默认填写 `执行授权：未授权`、`Review 授权：未授权`。
- 进入 `executing`、`repairing` 或 `complete` 前，必须已有明确执行授权，并填写 `执行授权：已授权`。
- 明确执行授权默认包含执行完成后的自动 Review、自动返修和复审循环；进入 `review` 或 `complete` 前填写 `Review 授权：已授权`。用户明确要求只执行不 Review 时，执行完成后必须停在执行报告，不得写 `review-report.md`。
- 需求平台需求设计和新需求执行必须填写 `执行模式：任务分支模式`；任务分支在需求设计阶段创建，开发阶段沿用。不得使用 worktree，也不得直接在 `main` 或 `master` 上实现。
- `影响模块` 必须对齐前端菜单目录、子菜单、隐藏页签或后端能力；模块知识库动作是 `新增` 或 `更新` 时，完成态必须在执行报告记录对应 `docs/ai-harness/modules/*.md` 路径。
- 模块知识库动作是 `无需更新` 时，必须填写无需更新原因。
- 用户选择方案、确认方向或同意建议，只代表进入需求设计阶段，不等于执行授权；明确执行授权后才启用自动 Review 循环。

## 角色匹配

- `planning`：当前角色写 `Plan Agent`。
- `executing` / `repairing`：当前角色写 `Execution Agent`。
- `review`：当前角色写 `Review Agent`。
- 人工或用户接管时，当前角色可写 `人工` 或 `用户`。
