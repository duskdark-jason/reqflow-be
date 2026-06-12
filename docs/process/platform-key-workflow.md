# 需求平台 Key 工作流规范

本规范区分需求平台对外使用时的需求设计、开发执行和项目接入初始化流程，以及当前需求平台自身建设时的本地流程。所有模式都必须先确认影响仓库、远端地址、基线分支、任务分支和当前工作区状态，再决定是否允许写入本地仓库。

## 模式一：需求平台需求设计模式

适用场景：编排人员或指定开发人员拿到需求设计 Key，在 Codex 中通过需求平台 MCP 获取需求初稿、关联项目、目标远端仓库、目标基线分支、建议任务分支、影响模块、历史需求设计版本和需求人补充调整指令，并推演详细需求设计。

硬性规则：

- 必须通过需求平台 MCP 读取 Key 对应的需求、项目、仓库远端、目标基线分支、建议任务分支、影响模块和历史需求设计版本；不得只依赖对话描述。
- 必须检查当前 workspace 内仓库的 Git 远端是否与需求平台返回的目标仓库一致；不一致时先自动切换到匹配的本地仓库或 workspace。
- 自动切换失败时必须停止，并说明需求平台期望的仓库、当前检测到的仓库、尝试过的切换路径和失败原因。
- 必须先切换到目标基线分支并本地拉取最新代码；推荐命令为 `git switch <baseline-branch>` 和 `git pull --ff-only`。拉取失败、工作区存在未说明改动或远端不一致时必须停止说明。
- 必须在需求设计阶段基于最新基线创建或切换到需求平台建议的 ASCII 任务分支；任务分支创建时机在需求设计阶段，不得推迟到开发阶段。
- 需求分析阶段必须先形成需求可行性评估和风险判断，结论类型至少区分“可继续设计、需澄清、需调整、暂不可实现”，并通过需求平台 MCP `upload_requirement_assessment` 使用需求分析 actionToken 回写评估报告；该阶段不得调用 `save_requirement_package`，不得生成最终 `requirement.md`、`plan.md`、执行报告或 Review 报告。
- 需求分析结论需要需求人补充、调整或当前不可实现时，应把结论作为反馈推送给需求人，本轮停止。结论允许继续后，需求平台流转到需求生成阶段，开发人员复制新的需求生成提示词。
- 需求生成阶段只生成或调整需求设计，只允许写入 `docs/specs/active/REQ-001-中文需求标题/meta.md` 和 `requirement.md`，并通过需求平台 MCP `save_requirement_package` 使用需求生成 actionToken 回写需求设计；该阶段不得调用 `upload_requirement_assessment`，不得生成 `plan.md`、不得改业务代码、不得写 `execution-report.md` 或 `review-report.md`。
- 需求人补充调整指令后，继续在同一任务分支更新最终 `requirement.md` 并再次回写，需求平台按 `req_package_version` 记录需求设计迭代版本；如需重新评估风险，应由平台重新进入需求分析阶段并使用新的分析 token。
- 本地任务分支保留最终版 `requirement.md` 作为后续开发输入。MCP 回写不等同于 Git push；是否提交或推送任务分支按平台指令、仓库 Git 工作流或用户授权执行。
- 需求设计阶段不得生成执行计划、不得进入开发或 Review。

## 模式二：需求平台开发模式

适用场景：开发人员拿到需求开发 Key，在 Codex 中通过需求平台 MCP 拉取已确认的最终需求设计、任务分支、目标系统、远端仓库、开发基线分支和验收要求。

硬性规则：

- 必须通过需求平台 MCP 读取 Key 对应的最终需求设计、任务分支、目标仓库、开发基线分支和验收要求。
- 必须检查当前 workspace 内仓库的 Git 远端是否与需求平台关联的系统一致；不一致时停止，除非能自动切换到明确匹配的本地仓库。
- 必须检查当前分支是否为需求平台需求设计阶段创建的任务分支；不一致时优先自动切换到同名任务分支，切换失败必须停止并说明当前分支、期望任务分支和处理建议。
- 开发阶段不得重新生成不同任务分支；如本地任务分支缺失，只能按需求平台记录的同名任务分支恢复，不能自行创造新分支名。
- 进入开发阶段后，将最终需求设计落地或校准到 `docs/specs/active/REQ-001-中文需求标题/requirement.md`，再由 Execution Agent 基于最终需求设计先判断是否适合拆分为多个 subagent 并行执行；只有职责边界清晰、无共享状态且可独立验证时才拆分，否则保持单执行路径。随后生成或更新 `plan.md`，然后按计划执行开发。
- 落地 `meta.md` 时必须记录需求平台返回的影响模块，并声明模块知识库动作。涉及菜单、页面、接口、权限、核心流程或数据口径时，必须更新 `docs/ai-harness/modules/*.md`。
- 开发阶段使用同一个开发阶段 actionToken 完成 `save_development_plan`、`upload_execution_report` 和 `upload_review_report` 回写；该 token 只在 `developing` 流程阶段有效，转入待验收后失效。
- 开发完成后进入自动 Review 循环：Execution Agent 持续追加或更新 `execution-report.md` 并通过 MCP `upload_execution_report` 回写新版本；Review Agent 只读审查、追加或更新 `review-report.md` 并通过 MCP `upload_review_report` 回写新版本。发现 `RF-*` 后自动切回执行阶段修复并回填 `execution-report.md`，再自动复审，直到最终 Review 结论为 `通过`。
- 返修阶段沿用同一任务分支和同一 spec 目录；需求人补充返修说明后，开发人员复制新的返修任务提示词，使用同一个返修阶段 actionToken 持续补充 `execution-report.md` 和 `review-report.md` 并回写平台。返修阶段不得重新生成 `requirement.md` 或 `plan.md`，需求平台按版本保留每次执行和 Review 资料。
- 需求人验收通过后，需求平台进入待合并归档阶段并给指定开发人员下发合并归档指令。开发人员必须在每个目标仓库把本地任务分支 squash merge 到需求基线分支，push 基线分支，再通过 `publish_repository_index` 使用合并归档 actionToken 发布当前完整知识库快照；平台验证所有有效仓库归档索引通过后，才允许确认完成并删除本地开发分支。

## 模式三：项目接入初始化模式

适用场景：一个新项目接入需求平台，需要由 Codex 根据平台保存的 harness 模板自动初始化目标 workspace，不再人工复制模板和手动改入口文件。

硬性规则：

- 必须从需求平台读取项目、仓库远端、默认基线分支、companion 仓库、任务分支前缀和 harness 模板版本。
- 必须检查当前 workspace 内仓库远端是否与需求平台登记一致；不一致时先自动切换到匹配 workspace，切换失败必须停止并说明期望仓库、当前仓库和失败原因。
- 写入前必须切换到需求平台登记的默认基线分支并拉取最新代码；推荐命令为 `git switch <default-branch>` 和 `git pull --ff-only`。本地存在未说明改动、拉取失败或分支受保护导致无法写入时，必须停止并说明。
- 写入前必须先判定初始化模式：
  - `fresh-init`：目标仓库没有有效 `docs/ai-harness/harness-index.json`，或索引中 `initialized` 不是 `true`，允许按模板初始化。
  - `bind-existing`：目标仓库已有 `initialized=true` 的 harness，且无需升级模板；只绑定需求平台项目、仓库、项目分支和任务分支前缀等身份信息，不覆盖既有 docs/scripts。
  - `upgrade-existing`：目标仓库已有 harness，但模板版本或脚本能力落后；只能对平台托管的流程、模板和脚本做 diff/merge 升级，不得覆盖项目沉淀文档。
- 单仓项目只下发子仓库 `AGENTS.md`、`docs/` 和 `scripts/`；多仓 workspace 必须同时下发 workspace 根目录 `AGENTS.md` 和每个子仓库的 harness。
- workspace 根目录 `AGENTS.md` 只写项目分流、通用授权语义和子仓库入口，不沉淀具体业务规则。
- 子仓库 harness 必须写入 `docs/process/platform-key-workflow.md`，确保后续编排 Key 和开发 Key 都按平台驱动流程执行。
- 子仓库初始化不能只保留 `docs/ai-harness/modules/.gitkeep`；必须至少生成一个 `docs/ai-harness/modules/*.md` 非模板模块文档。初始化阶段必须先分析前端路由、菜单、页面组件和 API 封装，按菜单目录、子菜单、隐藏页签或页面业务功能建立具体业务知识库；纯后端项目按它服务的 companion 前端菜单、MCP 能力或后台任务写清对应关系，不得只生成仓库概览。
- 如果需求平台登记或项目扫描表明子仓库承担数据库、SQL、Mapper 或统计口径职责，初始化时必须生成 `docs/db/README.md`、`docs/db/table-dictionary.md` 和 `docs/db/relationship.md`。纯前端或无数据库职责项目可以不创建 `docs/db/`。如果仓库没有 DDL、迁移脚本、schema 导出或可信数据库字典，只能生成“暂无确认表结构来源/暂无确认关系来源”的待补齐骨架，记录已扫描路径和后续补齐方式，不得虚构表、字段或关系。
- 已初始化项目禁止整包覆盖。必须保留项目自有的 `docs/ai-harness/modules/**`、`docs/ai-harness/contracts/**`、`docs/ai-harness/decisions/**`、`docs/domains/**`、`docs/db/**`、`docs/specs/**` 和 `docs/runbooks/local-run.md`；除非用户明确授权，不得删除、重命名或改写这些文件。
- 已存在 `AGENTS.md` 时只能合并 snippet，不得覆盖全文；如果自动合并有冲突，停止并列出冲突段和建议人工处理方式。
- 初始化完成后必须运行 `sh scripts/check-docs.sh` 和 `sh scripts/check-harness.sh init`；校验通过后必须提交初始化生成或升级的 harness 文件，并直接推送到当前默认基线分支。提交和推送失败时必须保留本地改动、说明失败原因，并把初始化模式、写入文件清单、保留文件清单、命令输出、commit、push 结果、失败原因和人工确认项回写需求平台。
- 初始化阶段不得创建需求开发分支，不得改业务代码，不得把初始化结果伪造成需求执行报告。

## 模式四：平台自身建设模式

适用场景：当前仓库正在建设需求管理平台本身，需求平台 MCP 能力尚未完整可用，或用户明确要求先本地维护 harness、流程和业务实现。

允许行为：

- 可以不通过需求平台 MCP 回写设计文档。
- 可以按阶段把需求、计划、执行报告和 Review 报告写入本地 `docs/specs`。
- 可以按当前项目 Git 工作流进行本地文档与代码变更；涉及 commit、push、merge 仍按 `git-workflow.md` 和用户授权执行。

限制：

- 不能把平台自身建设模式推广到其他业务项目。
- 如果任务已经给出需求平台 Key，优先按需求设计模式、开发模式或项目接入初始化模式执行，不能退回本地自由流程。
- 当前项目自身建设完成后，应把长期有效规则沉淀到 `docs/ai-harness`、`docs/process` 或模板中。

## 自举接入规则

需求平台项目后期可以接入需求平台，作为普通项目被自己管理。接入后：

- 在需求平台中登记当前项目、`reqflow-be`、`reqflow-ui`、远端仓库、默认基线分支和 companion 关系。
- 因当前项目已存在 harness，后期自举接入必须按 `bind-existing` 或 `upgrade-existing` 处理，不得按 `fresh-init` 重新覆盖本地文档、脚本和历史 spec。
- 新增需求默认通过需求平台 Key 进入需求设计模式、开发模式或项目接入初始化模式，不再把本地自由流程作为首选。
- 平台自身不可用、MCP 不可用、Key 无法解析，或平台返回的远端/分支与当前工作区冲突时，必须停止平台驱动流程，说明原因，并退回平台自身建设模式。
- 退回平台自身建设模式时，只能把文档落到本地 `docs/specs`，不得伪造需求平台回写结果。
- 平台恢复后，可以人工或通过 MCP 把本地 spec 补登记到需求平台，但必须保持需求 Key、仓库、分支和 commit 关联一致。

## meta.md 记录要求

所有落地到本地的 spec 必须在 `meta.md` 记录：

- 流程模式：`需求平台需求设计模式`、`需求平台开发模式`、`项目接入初始化模式` 或 `平台自身建设模式`。
- 需求 Key：没有 Key 时写“无，本地平台建设”。
- 平台关联远端：需求平台返回的 Git 远端；平台自身建设模式可写当前仓库远端或“未配置”。
- 平台目标分支：需求设计模式和开发模式都记录开发基线分支；同时记录需求平台任务分支。
- 当前分支、执行授权、Review 授权和 companion 仓库。
- 影响模块、模块知识库动作、模块知识库文档和无需更新原因。
