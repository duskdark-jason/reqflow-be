# AGENTS.md 工作空间入口

## Workspace 说明

这是一个多仓 workspace，包含以下子项目：

```text
【backend-repo/】   # 【后端/服务端说明】
【frontend-repo/】  # 【前端/客户端说明】
【other-repo/】     # 【可选：其他子项目说明】
```

根目录只作为 workspace 导航。共享业务上下文、代码规则、验证命令和长期维护规则，应写入对应子仓库并随代码提交。

## 任务分流

开始任务前先判断影响范围：

| 任务类型 | 进入目录 | 先读 |
|---|---|---|
| 【后端/API/数据库/任务】 | `【backend-repo/】` | `AGENTS.md`、`docs/README.md`、`docs/process/new-requirement-flow.md` |
| 【前端/页面/组件/交互】 | `【frontend-repo/】` | `AGENTS.md`、`docs/README.md`、`docs/process/new-requirement-flow.md` |
| 【跨端联调/接口字段/权限联动/导出一致性】 | 相关子仓库都要读 | 各自的 `AGENTS.md`、`docs/process/agent-workflow.md` 和 `docs/ai-harness/README.md` |
| 【需求平台 Key 编排、开发或接入初始化】 | 需求平台返回的目标子仓库 | 各自的 `AGENTS.md`、`docs/process/platform-key-workflow.md`、`docs/process/git-workflow.md` |

如果实际目录变化，请先根据构建文件判断子项目类型。

## Workspace 原则

1. 不要在根目录沉淀具体业务规则；业务规则写入对应子仓库 `docs/ai-harness` 或 `docs/domains`。
2. 不要跨仓库修改无关代码。
3. 接口字段变化时，必须检查调用方和展示方。
4. 请求参数或展示字段变化时，必须检查提供方接口契约。
5. 涉及数据库、SQL、join 或统计口径时，优先查看数据所属仓库的 `docs/db`。
6. 涉及权限时，同时关注服务端权限和客户端路由/按钮权限。
7. 不要随意升级依赖。
8. 普通模式下不要自动提交代码；新需求执行时必须创建 ASCII 任务分支，完成修改和验证后直接提交。
9. 涉及分支、commit、merge、push 或 rebase 时，先阅读受影响子仓库的 `docs/process/git-workflow.md`。
10. 多 agent 协作时，按受影响子仓库的 `docs/process/agent-workflow.md` 使用 `requirement.md`、`plan.md`、`execution-report.md`、`review-report.md` 文件交接。
11. 多仓 workspace 只维护 workspace 级启动提示词；不要在子仓库单独沉淀独立启动提示词。子仓库规则以各自 `AGENTS.md`、`docs/process/` 和 `docs/ai-harness/` 为准。
12. 各类落地文档必须使用中文描述；必要英文术语、命令、接口名和工具名可以保留，但标题和说明必须给出中文语义。
13. 需求 spec 目录必须使用 `REQ-001-中文需求标题` 形式，包含稳定需求编号和中文标题；Git 任务分支必须使用 ASCII。
14. 用户选择方案、确认方向或同意建议，只代表进入计划阶段；不得据此自动创建分支、改代码、写执行报告或写 Review 报告；明确执行授权后，默认自动进入 Review、返修和复审循环，直到最终 Review 通过。
15. 当前分支为 `main` 或 `master` 时，除只读分析和明确的小文档修正外，不得开始功能实现；需要执行新需求时，先按子仓库 Git 工作流创建 ASCII 任务分支。
16. Plan、Execution、Review 是独立阶段：Plan 写完计划后停止；Execution 不自我 Review；Review 只读审查并产出 `RF-*`；返修自动回到 Execution，复审自动回到 Review，直到最终 Review 通过。
17. 出现需求平台编排 Key、开发 Key 或项目接入初始化 Key 时，必须优先按受影响子仓库的 `docs/process/platform-key-workflow.md` 执行；当前需求平台自身建设阶段允许使用平台自身建设模式，把阶段文档写入本地 `docs/specs`。

## Harness 维护

新增或修改以下内容时，必须同步更新对应子仓库 `docs/ai-harness`；如果不需要更新，必须在对应 spec 的 `meta.md` 和完成说明中写明原因：

- 新模块、新页面、新接口或新导出。
- 接口请求字段、响应字段、分页字段或错误语义。
- 数据库表、字段、join、统计口径或分页粒度。
- 权限、菜单、路由或按钮权限。
- 核心业务流程、不变量或验收路径。

功能模块文档应优先和前端菜单对应，记录菜单目录、子菜单或隐藏页签、功能接口、权限标识和涉及文件。项目接入初始化时，对应子仓库必须至少生成一个非模板 `docs/ai-harness/modules/*.md`。

Harness 初始化或纯文档接入后，在受影响子仓库运行：

```bash
sh scripts/check-docs.sh
sh scripts/check-harness.sh init
```

Windows 原生命令行可在受影响子仓库通过 `.cmd` 包装入口调用 Git Bash；WSL 用户进入 WSL shell 后直接运行同名 `.sh`：

```bat
scripts\check-docs.cmd
scripts\check-harness.cmd init
```

真实需求执行、Review、返修和最终复审通过后才运行 `sh scripts/check-harness.sh complete`。

Review Agent 刚写完 `review-report.md`、尚未由 Execution Agent 返修时，运行 `sh scripts/check-harness.sh review`。

## 验证入口

验证命令以受影响子仓库的 `docs/ai-harness/verification.md` 为准。

如果任务影响多个子仓库，相关子仓库都要按各自验证说明执行。

## 输出要求

每次完成任务后，请说明：

1. 修改了哪些文件。
2. 为什么这么修改。
3. 是否影响接口、数据库、权限或页面展示。
4. 是否已运行验证命令；如果没有运行，说明原因。
