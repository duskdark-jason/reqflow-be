# 项目接入初始化闭环修复需求说明

## 背景

REQ-005 和 REQ-006 已修复 reqflow MCP 的工具发现、协议响应和错误响应类型。用户在 IMS 项目中再次调用 MCP 后确认：

- 当前 Codex 会话已加载 reqflow MCP server。
- 已调用 `mcp__reqflow.publish_repository_index`。
- `actionToken` 作为 `arguments.actionToken` 传入，不是 `X-MCP-Key`。
- 平台返回 `Table 'ry-vue.req_repository_index_batch' doesn't exist`，说明平台库缺少索引表。
- IMS 本地目录没有生成 harness 文件，说明 agent 只执行了索引上传，没有先执行本地 harness 初始化。

## 目标

- 让平台下发类似 Context7 的 reqflow MCP skill/snippet，帮助 agent 识别何时必须进入 reqflow 项目接入初始化流程。
- 让 `get_harness_template` 返回可写入目标 workspace 的 harness 文件清单，避免 agent 只有自然语言指令却没有本地文件内容。
- 让项目初始化指令明确要求先获取模板、落地本地 harness、运行 init 校验，再发布仓库索引并回写初始化结果。
- 让 `publish_repository_index` 写入链路在索引表缺失时返回友好错误，指向 `sql/req_platform_schema.sql` 中的 `req_repository_index_batch`、`req_index_module`、`req_impact_item` 表，而不是原始数据库异常。

## 范围

本次包含：

- MCP resource/template 或工具返回中暴露 reqflow 项目接入初始化 skill 文本。
- `get_harness_template` 返回 workspace 文件和仓库文件清单，至少覆盖 `AGENTS.md`、`docs/ai-harness/harness-index.json`、非模板模块文档、流程文档和检查脚本。
- 项目初始化指令和兼容初始化指令补充完整调用顺序。
- 索引导入写入前或写入失败时识别缺表，返回平台库未初始化的可操作错误。
- 增加单元测试和文档同步。

本次不包含：

- MCP server 直接写入调用方本地文件系统。
- 修改 IMS 或其他接入项目仓库。
- 自动执行生产数据库迁移。
- 改变索引导入成功路径的数据粒度、权限点或前端页面。

## 影响范围

- 接口/API：是，`get_harness_template` 返回结构增加，MCP resources 增加 skill 说明。
- 数据库/SQL：是，新增独立索引表迁移脚本或预检说明；不改现有表结构。
- 权限/菜单：否，沿用 `req:project:query`、`req:index:import`、`req:package:save`。
- 页面/交互：否。
- 本地 harness：是，平台模板包包含可落地文件清单。

## 契约与数据口径

- `publish_repository_index` 仍只负责把 agent 扫描后的结构化索引发布到平台，不负责写调用方本地文件。
- 本地 harness 文件由 agent 根据 `get_harness_template` 返回的 `workspaceFiles` 和 `repositoryHarnessInstructions[].files` 写入目标 workspace。
- 多仓 workspace 需要先写 workspace 根 `AGENTS.md`，再分别写后端、前端子仓库 harness，并分别发布索引。
- 索引写入依赖 `req_repository_index_batch`、`req_index_module`、`req_impact_item` 三张表；缺任一表时不得写入部分数据。

## 验收标准

- AC-BE-001：MCP resources/templates 或 `get_harness_template` 结果中包含 reqflow MCP skill 文本，触发条件覆盖 `actionToken`、`mcpServer: reqflow`、`mcpTool: reqflow.publish_repository_index` 和项目接入初始化。
- AC-BE-002：项目初始化指令明确调用顺序：确认 reqflow MCP -> 调 `get_harness_template` -> 写入/合并本地 harness -> 运行 `check-docs.sh` 和 `check-harness.sh init` -> 调 `publish_repository_index` -> 调 `register_harness_init_result`。
- AC-BE-003：`get_harness_template` 返回 workspace 文件和仓库文件清单，仓库文件包含至少一个非模板 `docs/ai-harness/modules/*.md`。
- AC-BE-004：`publish_repository_index` 遇到 `req_repository_index_batch`、`req_index_module` 或 `req_impact_item` 缺表时返回友好业务错误，提示执行平台索引表迁移，不暴露原始 `Table ... doesn't exist` 作为最终错误。
- AC-BE-005：新增独立 SQL 迁移脚本或文档入口，可用于补齐索引表；`docs/db` 和 `docs/ai-harness` 同步记录依赖关系和故障处理。
- AC-BE-006：原成功路径和已有权限校验不退化，相关单元测试、构建和 harness 检查通过。

## Companion 关联

- companion spec：无
- 关联分支：无

## 客户与分支

- 目标客户：通用
- 基线分支：main
- 任务分支：feature/REQ-20260610-007-project-init-closure

## 约束与假设

- 当前需求平台自身建设阶段允许本地 spec，不伪造平台回写。
- 接入项目本地文件写入必须由 agent 在目标 workspace 执行，不由 reqflow 服务端远程写文件。
- 平台数据库迁移由部署/维护人员执行；本需求只提供脚本和友好错误，不直接改生产库。
