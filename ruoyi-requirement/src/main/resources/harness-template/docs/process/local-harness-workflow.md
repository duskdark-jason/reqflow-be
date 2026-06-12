# 本地 Harness 工作流

本流程适用于未接入 Reqflow MCP、MCP 服务不可用、没有需求平台 Key，或当前项目明确采用本地文档闭环的情况。它和 MCP 接入模式使用同一套 `docs/specs`、`docs/ai-harness`、验证命令和阶段门禁，只是不执行平台读取或回写。

## 触发条件

- 用户没有提供需求平台需求设计 Key、开发 Key 或项目接入初始化 Key。
- 当前仓库未接入 Reqflow MCP，或当前 Codex 会话没有可用 MCP 工具。
- MCP 服务暂时不可用，且用户明确允许先本地推进。
- 当前项目处于平台自身建设、自举前治理或纯本地 harness 维护阶段。

如果用户提供了有效 Key，必须优先走 `platform-key-workflow.md`。不能因为本地流程更方便而绕过平台流程。

## 启动步骤

1. 读取 `AGENTS.md`、`docs/README.md`、`docs/ai-harness/harness-index.json`、`docs/ai-harness/search-map.md` 和 `docs/ai-harness/change-checklist.md`。
2. 按 `search-map.md` 定位相关模块、契约、决策、数据库或运行手册。
3. 新需求或 Bug 修复先创建 `docs/specs/active/REQ-001-中文需求标题/`，并填写 `meta.md`、`requirement.md`。
4. `meta.md` 中 `流程模式` 写 `本地 Harness 模式`，`需求 Key` 写“无，本地流程”，`平台关联远端` 写“未配置”或当前仓库远端。当前仓库正在建设需求平台、平台类治理能力，或明确拷贝平台建设版本进行本地自举时，可写 `平台自身建设模式`，它必须按本流程执行。
5. 未获得明确执行授权前，不写 `plan.md`、不改业务代码、不写执行报告和 Review 报告。

## 需求设计确认点

- 本地 Harness 模式也必须保留需求设计确认点；用户只是在讨论、选择方案或补充调整时，只能迭代 `requirement.md`。
- 多轮需求设计时，每一轮补充都继续停留在 `planning`，直到用户明确说“开始执行”“按确认后的需求实现”或等价执行授权。
- `plan.md` 必须由 Execution Agent 在执行阶段基于最终确认的 `requirement.md` 编写；不能由 Plan Agent 在需求设计阶段提前生成。
- `scripts/check-harness.sh complete` 会拦截 `planning` 状态下提前出现的 `plan.md`、`execution-report.md` 或 `review-report.md`。

## 阶段流程

| 阶段 | 本地文件 | 要求 |
|---|---|---|
| 需求设计 | `meta.md`、`requirement.md` | 写清验收 ID、影响模块、模块知识库动作和无需更新原因，可多轮迭代 |
| 执行计划 | `plan.md` | Execution Agent 基于最终确认的需求设计生成，覆盖每个 AC 和验证层级 |
| 执行实现 | 代码、测试、harness、`execution-report.md` | 记录修改文件、验证命令、模块知识库路径、代码注释处理和 commit |
| Review | `review-report.md` | 独立 Review Agent 只读审查，结论为通过、有条件通过或阻断 |
| 返修复审 | `execution-report.md`、`review-report.md` | `RF-*` 必须同 ID 回填并复审到最终通过 |
| 完成 | `meta.md`、验证结果 | `meta.md` 切到 `complete`，运行完成态门禁 |

## 与 MCP 接入模式保持一致

- 目录结构、验收 ID、模块知识库动作、Review 返修 ID 和验证分层必须和 MCP 接入模式一致。
- 本地模式不得伪造 `upload_requirement_assessment`、`save_requirement_package`、`upload_execution_report` 或 `upload_review_report` 成功结果。
- 如后续补接入 MCP，可以把本地 spec 作为输入补登记到平台，但必须明确记录真实需求 Key、分支、commit 和回写时间。
- 本地模式下无法回写平台时，只写“未接入 MCP，本地文件闭环”，不要写“已上传需求平台”。

## 完成检查

```bash
sh scripts/check-docs.sh
sh scripts/check-harness.sh complete --spec docs/specs/active/REQ-001-中文需求标题
```

Harness 初始化或纯文档接入只运行：

```bash
sh scripts/check-docs.sh
sh scripts/check-harness.sh init
```

Review Agent 刚写完 `review-report.md` 且尚未返修时，运行：

```bash
sh scripts/check-harness.sh review
```
