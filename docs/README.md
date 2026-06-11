# 文档入口

本目录用于沉淀项目长期有效文档。新需求、接口变更、数据库变更、权限变更和用户可见流程调整，都应先按 `process/new-requirement-flow.md` 执行。

## 文档地图

| 目录 | 用途 | 使用场景 |
|---|---|---|
| `ai-harness/` | agent 执行护栏 | 模型或同事修改代码前必读 |
| `domains/` | 业务领域当前入口 | 处理某个业务领域需求前阅读 |
| `process/` | 新需求流程和文档治理 | 开始新需求或整理文档前必读 |
| `specs/` | 单次需求过程文档 | 查询 active/done 需求 |
| `templates/` | 可复制模板 | 新增需求、契约或验收文档时复制 |
| `../scripts/check-docs.sh` | 文档自检脚本 | 文档或 harness 变更后运行 |
| `../scripts/check-harness.sh` | harness 初始化、spec 和多 agent 交接门禁 | 初始化、需求交接、Review、返修或完成前运行 |

涉及需求平台 Key、需求编排、需求开发或 MCP 回写时，先读 `process/platform-key-workflow.md`。

涉及多 agent 协作时，先读 `process/agent-workflow.md`。

涉及分支、commit、merge 或 rebase 时，先读 `process/git-workflow.md`。

涉及代码实现细则时，先读 `process/code-guidelines.md`。

## 新需求默认流程

1. `process/new-requirement-flow.md`
2. `process/platform-key-workflow.md`，如果任务来自需求平台 Key 或需要 MCP 回写
3. `process/agent-workflow.md`，如果计划、执行和 review 由不同 agent 或工具完成
4. `ai-harness/README.md`
5. 与任务相关的 `ai-harness/modules/`、`ai-harness/contracts/`、`domains/` 文档

## 文档维护原则

- 长期有效业务规则写入 `ai-harness/` 或 `domains/`。
- 单次需求过程文档优先写入 `specs/active/REQ-001-中文需求标题/` 目录。
- 已完成但仍有参考价值的方案移入 `specs/done/`。
- 重要技术或业务决策写入 `ai-harness/decisions/`。
- 不要把个人本机路径、临时草稿和过期计划作为新需求默认入口。
- Harness 初始化完成后运行 `sh scripts/check-harness.sh init`；Review Agent 刚写完 `review-report.md`、尚未返修时运行 `sh scripts/check-harness.sh review` 作为中间交接检查；Execution Agent 完成返修、Review Agent 最终复审通过后，运行 `sh scripts/check-harness.sh complete`。
- Windows 原生命令行可通过 `scripts\check-docs.cmd` 与 `scripts\check-harness.cmd init|review|complete` 调用 Git Bash；WSL 用户进入 WSL shell 后直接运行同名 `.sh`。
