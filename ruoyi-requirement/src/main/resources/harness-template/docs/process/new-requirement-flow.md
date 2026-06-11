# 新需求执行流程

本流程用于新功能、Bug 修复、接口变更、数据库变更、页面变更、权限变更和跨端联调。

如果任务包含需求平台编排 Key、开发 Key、项目接入初始化 Key、MCP 拉取或 MCP 回写，先按 `platform-key-workflow.md` 判断属于编排模式、开发模式、项目接入初始化模式还是平台自身建设模式，再进入本流程。

## 1. 需求进入

先回答：

- 需求目标是什么，最终用户是谁。
- 属于后端、前端、数据库、权限、AI、报表、导出、任务调度还是跨端联调。
- 是否影响已有接口、字段、状态、权限或数据口径。
- 是否影响用户可见页面、导出、历史记录或异步进度。
- 是否影响已有菜单目录、子菜单、隐藏页签、功能模块或模块知识库。

如果需求不清晰，先补充需求说明，不要直接改代码。

### 授权语义

- 用户描述问题、选择方案、确认方向或同意建议，只代表允许进入需求说明和计划阶段。
- Plan Agent 可以据此完善 `requirement.md` 和 `plan.md`，但必须在计划完成后停止，不得自动开始实现。
- 开始实现必须有明确执行授权，例如“开始执行”“按计划实现”“允许改代码”或“创建任务分支执行”。
- 明确执行授权默认包含执行完成后的自动 Review、自动返修和复审循环；用户明确要求只执行不 Review 时除外。
- Execution Agent 不得把自己的实现直接写成 Review 完成；自动 Review 必须切换到 Review Agent 或等价的独立审查角色。
- 新需求执行不使用 worktree，必须从目标基线分支创建 ASCII 任务分支；当前分支为 `main` 或 `master` 时，除只读分析和明确的小文档修正外，不得开始功能实现。
- 当前需求平台自身建设阶段，可以不通过 MCP 回写设计文档，但必须在本地 `docs/specs` 按阶段记录；后期平台自举接入后，默认改走需求平台 Key 流程。

## 2. 创建需求说明

新需求优先使用目录式文件交接，详见 `agent-workflow.md`：

```text
docs/specs/active/REQ-001-中文需求标题/
  meta.md
  requirement.md
  plan.md
  execution-report.md
  review-report.md
```

可以从 `../templates/spec-meta-template.md`、`../templates/requirement-template.md`、`../templates/agent-plan-template.md`、`../templates/execution-report-template.md` 和 `../templates/review-report-template.md` 复制；模板选择见 `../templates/README.md`。

`meta.md` 必须在计划阶段记录影响模块和模块知识库动作：

- 影响模块：填写菜单目录、子菜单、隐藏页签或后端能力名称，多个用顿号分隔。
- 模块知识库动作：填写 `新增`、`更新` 或 `无需更新`。
- 模块知识库文档：填写预计维护的 `docs/ai-harness/modules/*.md` 路径；无需更新时写“无”。
- 无需更新原因：模块知识库动作是 `无需更新` 时必须填写。

如果需求涉及数据库、SQL、Mapper、join、聚合、统计口径或分页粒度，`requirement.md` 或 `plan.md` 必须写清数据库影响范围；实现完成后，`execution-report.md` 必须记录相关 `sql/` 脚本或 `docs/db/` 文档路径。现有 `sql/` 目录保留为可执行 SQL 和迁移脚本目录，`docs/db/` 只维护表结构字典、表关系和数据口径说明。

以下情况必须沉淀需求文档：

- 新增模块、核心页面或核心流程。
- 新增或修改接口契约。
- 新增或修改数据库表、SQL 口径或统计逻辑。
- 修改权限、导出、异步流程或高影响业务链路。
- 需要多人协作或跨端联调。

## 3. 多 Agent 协作入口

- 计划阶段：由 Plan Agent 输出 `requirement.md` 和 `plan.md`，锁定目标、契约、验证计划和不做范围。
- 执行阶段：由 Execution Agent 按 `plan.md` 实现并写 `execution-report.md`，不得自行扩大范围；完成验证后自动把 `meta.md` 切到 `review`，填写 `Review 授权：已授权`，交给 Review Agent。
- 审查阶段：由 Review Agent 只读审查并写 `review-report.md`，结论为 `通过`、`有条件通过` 或 `阻断`。
- 返修阶段：Review Agent 产生 `RF-*` 后自动回到 Execution Agent；Execution Agent 返修并在 `execution-report.md` 回填同 ID 的 `Review 返修记录`；Review Agent 再复审。循环持续到最终 Review 结论为 `通过`。
- 任一阶段发现计划缺失、契约冲突或验证无法执行，必须回到计划阶段补齐文件。
- 阶段切换必须更新 `meta.md`，并记录执行模式、执行授权和 Review 授权状态。

## 4. 阅读上下文

按影响范围读取：

| 影响范围 | 必读文档 |
|---|---|
| 任意修改 | `../ai-harness/README.md`、`../ai-harness/change-checklist.md` |
| 业务领域 | `../domains/【领域名】/README.md` |
| 接口字段 | `../ai-harness/contracts/` |
| 页面或交互 | `../ai-harness/modules/`、`../ai-harness/contracts/` |
| 数据库或统计 | `../db/README.md`、`../db/table-dictionary.md`、`../db/relationship.md`，如果项目存在 |
| 重要决策 | `../ai-harness/decisions/` |

## 5. 更新 harness

编码前判断是否需要更新：

- `ai-harness/modules/*.md`：模块业务、入口文件、不变量。
- `ai-harness/contracts/*.md`：接口字段、结果结构、前后端约定或 UI 状态。
- `ai-harness/decisions/*.md`：长期有效的业务或技术决策。
- `db/README.md`、`db/table-dictionary.md` 和 `db/relationship.md`：数据库表、字段、索引、SQL、Mapper、join、统计口径、分页粒度或数据迁移。
- `domains/*/README.md`：业务领域当前入口。

模块文档必须优先按前端菜单对齐，写清一级菜单、子菜单或隐藏页签、功能说明、前端文件、API 封装、后端接口、权限标识和后端核心文件。纯后端能力也要说明它服务哪个菜单、MCP 能力或后台流程。

数据库变更必须优先复用 `../templates/db-change-template.md`，在当前 spec 目录落地数据库变更说明；DDL、DML、迁移、清理脚本优先沉淀到稳定 `sql/` 路径，不能只写在对话或临时命令里。新增或修改表、字段、索引或约束时更新 `docs/db/table-dictionary.md`；仅查询口径、Mapper 或统计逻辑变更时，也要在 `execution-report.md` 说明是否更新 `docs/db/relationship.md`，以及无需 SQL 脚本的原因。若当前项目没有确认表结构来源，数据库相关需求必须先补齐证据或把字典标为推断/待确认，不能凭字段名相似直接当作确认结构开发。

如果变更会影响长期理解，先更新文档再编码。若无需更新，必须在 `meta.md` 填写 `模块知识库动作：无需更新` 和无需更新原因，并在完成说明中写明原因。

## 6. 编码实现

- 小范围修改，不跨模块改无关代码。
- 优先复用项目已有模式、组件、工具和返回结构。
- 不随意升级依赖。
- 不改变接口返回结构，除非需求明确要求。
- 涉及数据聚合时必须确认数据粒度。
- 未获得执行授权时，不得改业务代码、写执行报告或推进到 Review。
- 如需创建分支、commit、merge 或 rebase，先按 `git-workflow.md` 执行确认流程。

## 7. 验证

按 `ai-harness/verification.md` 选择最小充分验证组合。编译或构建只是 L1，不能替代必要的运行态冒烟、接口联调或权限验证；L4 端到端测试按影响和风险选择，不作为完成态默认强制项。

Harness 初始化或纯文档接入时运行：

```bash
sh scripts/check-docs.sh
sh scripts/check-harness.sh init
```

真实需求完成态或准备移入 `specs/done/` 前运行：

```bash
sh scripts/check-docs.sh
sh scripts/check-harness.sh complete
```

Review Agent 刚写完 `review-report.md`、尚未返修时，运行 `sh scripts/check-harness.sh review` 作为中间交接检查；完成态或准备移入 `specs/done/` 前，运行 `sh scripts/check-harness.sh complete`，此时最终 Review 必须为 `通过`。

## 8. 完成沉淀

完成后检查：

- 需求说明是否需要从 `specs/active/` 移到 `specs/done/`。
- 是否有长期有效结论要沉淀到 `ai-harness/` 或 `domains/`。
- 是否有重要决策要写入 `ai-harness/decisions/`。
- 是否涉及数据库、SQL、Mapper 或数据口径；如涉及，`execution-report.md` 必须记录 `sql/` 或 `docs/db/` 路径。
- `execution-report.md` 是否记录模块知识库动作、更新的模块文档路径和无需更新原因。

完成说明必须包含：

- 修改了哪些文件。
- 为什么这么修改。
- 是否影响接口、数据库、权限或页面展示。
- 已运行哪些验证命令；无法运行时说明原因。
