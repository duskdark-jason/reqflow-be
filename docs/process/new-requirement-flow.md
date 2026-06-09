# 新需求执行流程

本流程用于新功能、Bug 修复、接口变更、数据库变更、页面变更、权限变更和跨端联调。

## 1. 需求进入

先回答：

- 需求目标是什么，最终用户是谁。
- 属于后端、前端、数据库、权限、AI、报表、导出、任务调度还是跨端联调。
- 是否影响已有接口、字段、状态、权限或数据口径。
- 是否影响用户可见页面、导出、历史记录或异步进度。

如果需求不清晰，先补充需求说明，不要直接改代码。

## 2. 创建需求说明

新需求优先使用目录式文件交接，详见 `agent-workflow.md`：

```text
docs/specs/active/YYYY-MM-DD-需求名称/
  meta.md
  requirement.md
  plan.md
  execution-report.md
  review-report.md
```

可以从 `../templates/spec-meta-template.md`、`../templates/requirement-template.md`、`../templates/agent-plan-template.md`、`../templates/execution-report-template.md` 和 `../templates/review-report-template.md` 复制；模板选择见 `../templates/README.md`。

以下情况必须沉淀需求文档：

- 新增模块、核心页面或核心流程。
- 新增或修改接口契约。
- 新增或修改数据库表、SQL 口径或统计逻辑。
- 修改权限、导出、异步流程或高影响业务链路。
- 需要多人协作或跨端联调。

## 3. 多 Agent 协作入口

- 计划阶段：由 Plan Agent 输出 `requirement.md` 和 `plan.md`，锁定目标、契约、验证计划和不做范围。
- 执行阶段：由 Execution Agent 按 `plan.md` 实现并写 `execution-report.md`，不得自行扩大范围。
- 审查阶段：由 Review Agent 只读审查并写 `review-report.md`，结论为 `通过`、`有条件通过` 或 `阻断`。
- 返修阶段：Review Agent 产生 `RF-*` 后停止交接；Execution Agent 返修并在 `execution-report.md` 回填同 ID 的 `Review 返修记录`；Review Agent 再复审。
- 任一阶段发现计划缺失、契约冲突或验证无法执行，必须回到计划阶段补齐文件。

## 4. 阅读上下文

按影响范围读取：

| 影响范围 | 必读文档 |
|---|---|
| 任意修改 | `../ai-harness/README.md`、`../ai-harness/change-checklist.md` |
| 业务领域 | `../domains/【领域名】/README.md` |
| 接口字段 | `../ai-harness/contracts/` |
| 页面或交互 | `../ai-harness/modules/`、`../ai-harness/contracts/` |
| 数据库或统计 | `../db/`，如果项目存在 |
| 重要决策 | `../ai-harness/decisions/` |

## 5. 更新 harness

编码前判断是否需要更新：

- `ai-harness/modules/*.md`：模块业务、入口文件、不变量。
- `ai-harness/contracts/*.md`：接口字段、结果结构、前后端约定或 UI 状态。
- `ai-harness/decisions/*.md`：长期有效的业务或技术决策。
- `domains/*/README.md`：业务领域当前入口。

如果变更会影响长期理解，先更新文档再编码。若无需更新，完成说明中写明原因。

## 6. 编码实现

- 小范围修改，不跨模块改无关代码。
- 优先复用项目已有模式、组件、工具和返回结构。
- 不随意升级依赖。
- 不改变接口返回结构，除非需求明确要求。
- 涉及数据聚合时必须确认数据粒度。
- 如需创建分支、worktree、commit、merge 或 rebase，先按 `git-workflow.md` 执行确认流程。

## 7. 验证

按 `ai-harness/verification.md` 选择最小充分验证组合。编译或构建只是 L1，不能替代运行态冒烟、接口联调、权限验证或端到端验证。

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

Review Agent 刚写完 `review-report.md`、尚未返修时，运行 `sh scripts/check-harness.sh review`；完成态或准备移入 `specs/done/` 前，运行 `sh scripts/check-harness.sh complete`。

## 8. 完成沉淀

完成后检查：

- 需求说明是否需要从 `specs/active/` 移到 `specs/done/`。
- 是否有长期有效结论要沉淀到 `ai-harness/` 或 `domains/`。
- 是否有重要决策要写入 `ai-harness/decisions/`。

完成说明必须包含：

- 修改了哪些文件。
- 为什么这么修改。
- 是否影响接口、数据库、权限或页面展示。
- 已运行哪些验证命令；无法运行时说明原因。
