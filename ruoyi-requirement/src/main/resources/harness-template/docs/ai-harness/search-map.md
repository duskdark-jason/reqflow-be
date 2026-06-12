# Harness 功能导航

本文件用于让初次进入项目的模型先用关键词定位上下文，再进入模块、契约、决策或流程文档。新增模块、接口、菜单、权限、数据库口径、核心流程或长期决策时，必须同步维护本文件。

## 使用顺序

1. 先读 `docs/ai-harness/harness-index.json`，确认仓库角色和入口文件。
2. 再按本文件的关键词索引定位模块文档、契约文档、决策文档或流程文档。
3. 如果任务没有命中关键词，先读 `docs/ai-harness/README.md` 和 `docs/ai-harness/change-checklist.md`，再补充本文件。

## 关键词索引

| 关键词 | 功能/场景 | 入口文档 | 代码入口 |
|---|---|---|---|
| 本地 Harness 模式 | 未接入 MCP 时的需求设计、执行、Review、返修与完成闭环 | `docs/process/local-harness-workflow.md` | `AGENTS.md`、`docs/specs/active/` |
| MCP 接入模式 | 已接入 Reqflow MCP 后的需求读取、设计、执行和回写流程 | `docs/process/platform-key-workflow.md` | `docs/process/agent-workflow.md` |
| 项目接入初始化 | 下发 `AGENTS.md`、`docs/`、`scripts/` 并发布模块索引 | `docs/process/platform-key-workflow.md` | `docs/ai-harness/modules/*-page-functions.md` |
| 模块知识库 | 菜单、页面、接口、权限、核心流程和涉及文件索引 | `docs/ai-harness/modules/` | `docs/ai-harness/templates/module-template.md` |
| 接口契约 | 请求字段、响应字段、错误语义、UI 状态和调用方联动 | `docs/ai-harness/contracts/` | `docs/ai-harness/templates/contract-template.md` |
| 决策记录 | 长期边界、不可随意改变的技术或业务决策 | `docs/ai-harness/decisions/` | `docs/ai-harness/templates/decision-template.md` |
| 数据库口径 | 表结构、表关系、SQL、join、统计口径或分页粒度 | `docs/db/README.md` | `docs/db/table-dictionary.md`、`docs/db/relationship.md` |
| 验证入口 | 最小充分验证、L0 到 L4 分层和本地运行证据 | `docs/ai-harness/verification.md` | `scripts/check-docs.sh`、`scripts/check-harness.sh` |

## 维护触发

- 新增或重命名 `docs/ai-harness/modules/*.md`、`contracts/*.md`、`decisions/*.md`。
- 新增菜单、页面、接口、MCP 工具、后台任务、导出或异步流程。
- 新增长期关键字，例如业务别名、权限标识、核心表、路由、接口路径或任务名称。
- 拆分过大的模块文档后，必须把新旧关键词都指向新的入口文档。

## 拆分建议

- 单个模块文档超过 300 行，或一个文档同时覆盖多个菜单目录时，优先拆成多个模块文档。
- 一个模块内接口字段、错误语义或 UI 状态复杂时，抽出 `contracts/*.md`。
- 涉及长期取舍、不变量或跨模块边界时，抽出 `decisions/*.md`。
