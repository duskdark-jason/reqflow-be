# AGENTS.md 项目入口

## 项目说明

本仓库是需求管理平台后端，基于 RuoYi-Vue 后端工程改造。系统面向管理员、需求人员和开发人员，负责项目、仓库、客户定制线、需求执行包、MCP 和使用统计等能力。技术栈以 Spring Boot、Spring Security、MyBatis、MySQL、Redis 和 RuoYi 权限体系为基础。当前初始化阶段只建立 harness 和运行说明初稿，不修改业务代码。

## 工作前阅读规则

基础必读：

- `docs/README.md`
- `docs/process/new-requirement-flow.md`
- `docs/process/platform-key-workflow.md`，如果任务包含需求平台 Key、需求编排、需求开发、项目接入初始化或 MCP 回写
- `docs/process/agent-workflow.md`
- `docs/process/code-guidelines.md`，如果涉及代码实现
- `docs/process/git-workflow.md`，如果涉及分支、commit、merge 或 rebase
- `docs/ai-harness/README.md`
- `docs/ai-harness/change-checklist.md`

按任务类型追加阅读：

- 构建、依赖或模块结构：`pom.xml`
- 配置或启动：`ruoyi-admin/src/main/resources/application.yml`、`ruoyi-admin/src/main/resources/application-druid.yml`、`ruoyi-admin/src/main/resources/application-test.yml`
- 接口或业务逻辑修改：先查看同模块同类代码和 `docs/ai-harness/contracts/`
- 数据库、SQL、统计口径：`docs/db/`，如果项目存在
- 页面、组件或交互：`docs/ai-harness/modules/` 和 `docs/ai-harness/contracts/`
- 权限、菜单或安全：权限相关代码、路由/菜单配置和对应 harness 文档

## AI Harness 维护

`docs/ai-harness` 是给 vibecoding / agentic coding 工具长期理解项目的共享上下文。修改代码前先读 `docs/ai-harness/README.md`，再按任务类型阅读对应模块、契约或决策文档。

各类落地文档必须使用中文描述；必要英文术语、命令、接口名和工具名可以保留，但标题和说明必须给出中文语义。

需求 spec 目录必须使用 `YYYY-MM-DD-REQ-001-中文需求标题` 形式，包含稳定需求编号和中文标题；Git 任务分支必须使用 ASCII，详见 `docs/specs/README.md` 和 `docs/process/git-workflow.md`。

新需求、接口变更、数据库变更或跨端联调开始前，必须先按 `docs/process/new-requirement-flow.md` 判断影响范围、补充需求说明和确认文档联动。

如果任务由多个 agent 或工具协作完成，必须按 `docs/process/agent-workflow.md` 使用文件交接：计划 agent 写清 `requirement.md` 和 `plan.md`，执行 agent 只按计划实现并写 `execution-report.md`，review agent 只审查并写 `review-report.md`。

阶段授权必须明确记录：用户选择方案、确认方向或同意建议，只代表进入计划阶段；不得据此自动创建分支、改业务代码、写 `execution-report.md` 或写 `review-report.md`。开始实现必须得到明确的执行授权，例如“开始执行”“按计划实现”“允许改代码”或“创建任务分支执行”。执行授权默认包含“执行完成后自动进入 Review、发现问题自动返修并复审直到最终 Review 通过”的闭环；用户明确要求只执行不 Review 时除外。新需求执行不使用 worktree，必须从目标基线分支创建 ASCII 任务分支；当前分支为 `main` 或 `master` 时，除只读分析和明确的小文档修正外，不得开始功能实现。Execution Agent 不得自我 Review；Review Agent 只读审查并产出 `RF-*`，返修必须回到 Execution Agent。

需求平台 Key 流程必须按 `docs/process/platform-key-workflow.md` 执行：编排模式只通过 MCP 获取/回写设计文档，不在本地仓库落地或提交；开发模式必须校验需求平台关联远端和目标分支，基于开发基线创建任务分支后再落地 spec、开发和自动 Review；项目接入初始化模式由平台下发 harness 模板和 workspace `AGENTS.md` 给 Codex 落地。当前需求平台自身建设阶段可使用平台自身建设模式，不强制 MCP 回写，按阶段写本地 `docs/specs`。

新增或修改以下内容时，必须同步更新 `docs/ai-harness`；如果不需要更新，请在 `meta.md` 和完成说明中写明原因：

1. 接口路径、请求参数、响应字段或错误语义。
2. 数据库表、字段、SQL、join、统计口径或分页粒度。
3. 核心业务流程、不变量、导出内容或异步流程。
4. 权限、菜单、路由或按钮权限。
5. 验证命令、联调方式或验收路径。

功能模块文档应优先和前端菜单对应：写清菜单目录、子菜单或隐藏页签、功能说明、前端文件、API 封装、后端接口、权限标识和后端核心文件。新需求的 `meta.md` 必须记录影响模块、模块知识库动作和模块文档路径；项目接入初始化时必须至少生成一个非模板 `docs/ai-harness/modules/*.md`。

Harness 初始化或纯文档接入只运行 L0/init 检查，不启动项目、不跑业务测试：

```bash
sh scripts/check-docs.sh
sh scripts/check-harness.sh init
```

Windows 原生命令行可通过 `.cmd` 包装入口调用 Git Bash；WSL 用户进入 WSL shell 后直接运行同名 `.sh`：

```bat
scripts\check-docs.cmd
scripts\check-harness.cmd init
```

真实需求执行、Review、返修和最终复审通过后才运行 `sh scripts/check-harness.sh complete`。

Review Agent 刚写完 `review-report.md`、尚未由 Execution Agent 返修时，运行 `sh scripts/check-harness.sh review`。

涉及分支、commit、merge、push 或 rebase 时，必须先阅读 `docs/process/git-workflow.md`。新需求执行必须创建任务分支，完成修改和验证后直接提交；merge、push、rebase 仍需用户明确确认。

完成验证时，必须按 `docs/ai-harness/verification.md` 选择最小充分验证组合。编译或构建只是最低门槛，不能替代必要的运行态冒烟、接口联调或权限验证；端到端测试按风险选择，不作为默认强制项。

## 输出要求

完成任务后说明：

1. 修改了哪些文件。
2. 为什么这么修改。
3. 是否影响接口、数据库、权限或页面展示。
4. 是否已运行验证命令；如果没有运行，说明原因。
