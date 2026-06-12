# Harness 功能导航

本文件是初次进入 `reqflow-be` 时的第一层搜索索引。先用关键词定位模块、契约、决策、数据库或流程文档，再进入代码搜索。新增、拆分或重命名模块文档、契约文档、决策文档、菜单、接口、MCP 工具、权限、数据库口径或核心流程时，必须同步维护本文件。

## 使用顺序

1. 先读 `docs/ai-harness/harness-index.json`，确认本仓库角色、companion 前端仓库和入口文件。
2. 再按下方关键词索引定位长期文档和代码入口。
3. 如果关键词没有命中，先读 `docs/ai-harness/README.md` 和 `docs/ai-harness/change-checklist.md`，再补充本文件。

## 关键词索引

| 关键词 | 功能/场景 | 入口文档 | 代码入口 |
|---|---|---|---|
| 需求管理 | 项目、需求、执行包、MCP、统计的后端总入口 | `docs/ai-harness/modules/requirement-platform.md` | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/` |
| 项目管理 | 项目列表、维护、初始化状态、项目接入上下文 | `docs/ai-harness/modules/requirement-platform.md` | `ReqProjectController`、`ReqProjectInitController`、`ReqProjectInitServiceImpl` |
| 分支知识库 | 项目分支模块树、索引批次、影响面推荐 | `docs/ai-harness/modules/requirement-platform.md` | `ReqIndexController`、`ReqRepositoryIndexServiceImpl`、`ReqIndexModuleMapper` |
| 需求列表 | 需求新增、编辑、查询、状态流转、需求指令生成 | `docs/ai-harness/modules/requirement-platform.md` | `ReqDemandController`、`ReqDemandServiceImpl`、`ReqDemandStatusTransition` |
| 需求执行包 | 需求可行性评估、需求设计、执行计划、执行报告和 Review 报告 | `docs/ai-harness/contracts/requirement-platform-api.md` | `ReqPackageController`、`ReqPackageServiceImpl`、`ReqPackageVersionMapper` |
| MCP 管理 | 人员 MCP Key、安装脚本、MCP lifecycle 和 tools/list | `docs/ai-harness/modules/requirement-platform.md` | `ReqMcpKeyController`、`ReqCodexInstallController`、`McpService` |
| Reqflow MCP | resources、tools、actionToken、JSON-RPC 错误语义 | `docs/ai-harness/contracts/requirement-platform-api.md` | `ruoyi-requirement/src/main/java/com/ruoyi/requirement/mcp/McpService.java` |
| 项目接入初始化 | 下发 harness 模板、发布仓库索引、登记初始化结果 | `docs/process/platform-key-workflow.md`、`docs/ai-harness/modules/requirement-platform.md` | `McpService#getHarnessTemplate`、`publish_repository_index` |
| harness 模板 | 后端保存并下发的 AGENTS、docs、scripts 模板 | `docs/ai-harness/modules/requirement-platform.md` | `ruoyi-requirement/src/main/resources/harness-template/` |
| 本地 Harness 模式 | 未接入 MCP 或无 Key 时的本地需求、执行、Review、返修闭环 | `docs/process/local-harness-workflow.md` | `docs/specs/active/`、`scripts/check-harness.sh` |
| MCP 接入模式 | 已接入需求平台后的需求设计、开发、回写和合并归档 | `docs/process/platform-key-workflow.md` | `McpService`、`ReqflowCodexGlobalSkillTemplate` |
| search-map | 面向模型的关键词导航和拆分触发 | `docs/ai-harness/search-map.md` | `scripts/check-harness.sh`、`harness-template/docs/ai-harness/search-map.md` |
| 检查脚本 | 文档占位符、harness 初始化、Review、完成态门禁 | `docs/process/agent-workflow.md` | `scripts/check-docs.sh`、`scripts/check-harness.sh` |
| 数据库表结构 | `req_*` 表、菜单权限、系统参数、索引表和迁移脚本 | `docs/db/README.md` | `docs/db/sql/req_platform_schema.sql`、`docs/db/table-dictionary.md` |
| 菜单权限 | 需求管理菜单、按钮权限、角色授权和后端 `@PreAuthorize` | `docs/ai-harness/modules/requirement-platform.md` | `docs/db/sql/req_platform_menu.sql`、Controller 权限注解 |

## 维护触发

- 新增或重命名 `docs/ai-harness/modules/*.md`、`contracts/*.md`、`decisions/*.md`。
- 新增或调整需求管理菜单、接口、MCP tool、后台任务、权限标识、数据库口径或核心流程。
- 调整 `ruoyi-requirement/src/main/resources/harness-template/`、`McpService` 或 `ReqflowCodexGlobalSkillTemplate` 的模板/触发机制。
- 单个模块文档超过 300 行，或一个文档同时覆盖多个菜单目录时，拆分文档后必须迁移关键词。

## 拆分建议

- 需求管理模块继续变大时，优先按“项目接入初始化”“需求执行包/MCP 回写”“MCP Key 与安装脚本”“分支知识库索引”拆出独立模块文档。
- 接口字段、错误语义或 UI 状态复杂时，抽到 `docs/ai-harness/contracts/`。
- 长期边界、不可逆流程或安全策略变化时，抽到 `docs/ai-harness/decisions/`。
