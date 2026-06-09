# 【需求名称】元信息

- 状态：planning
- 当前角色：Plan Agent
- 当前分支：【分支名或未创建】
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

## 角色匹配

- `planning`：当前角色写 `Plan Agent`。
- `executing` / `repairing`：当前角色写 `Execution Agent`。
- `review`：当前角色写 `Review Agent`。
- 人工或用户接管时，当前角色可写 `人工` 或 `用户`。
