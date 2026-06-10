# 前端页面功能知识库初始化需求说明

## 背景

需求平台已经通过 MCP service 支持项目接入初始化：Codex 可以读取 `get_harness_template`，写入目标 workspace harness，并通过 `publish_repository_index` 发布仓库索引。当前初始化下发的模块知识库更偏“仓库概览”和后续待补齐说明，不符合预期的业务知识库沉淀方式。用户希望初始化阶段就按前端页面分析功能，沉淀成可用于后续需求选择和影响面推荐的具体业务知识库。

## 目标

- 让 MCP 项目接入初始化指令、全局 `reqflow-mcp` skill 和 tool schema 明确要求先分析前端路由、菜单、页面组件和 API 封装，再生成业务功能粒度的 `modules`。
- 让 `get_harness_template` 下发的非模板模块文档从“仓库概览”改为“前端页面功能索引骨架”，引导接入项目按菜单/页面/接口/权限沉淀模块知识。
- 保持平台服务端不直接扫描接入项目文件系统，只负责下发规则、接收结构化索引和拒绝不合规路径。

## 范围

本次包含：

- 后端 MCP 模板与 skill 文本调整。
- `publish_repository_index` tool schema 描述增强。
- 页面功能粒度模块 payload 的导入测试。
- 后端接口契约和模块 harness 文档更新。

本次不包含：

- 不改前端页面展示和交互。
- 不新增数据库表或字段。
- 不让后端直接 clone 或扫描接入项目仓库。
- 不改变 MCP tool 名称、鉴权方式或 actionToken 传参方式。

## 影响范围

- 接口/API：是，`/requirement/mcp` 的 `tools/list` schema 描述、`get_harness_template` 返回内容和 `publish_repository_index` 语义增强。
- 数据库/SQL：否，不新增表字段，不改 Mapper SQL。
- 权限/菜单：否，不改权限点和菜单。
- 页面/交互：否，不改 reqflow-ui 页面；影响接入项目初始化时的页面分析规则。
- 导出/异步/任务：否。

## 契约与数据口径

- 接口路径和方法：`POST /requirement/mcp`。
- 请求参数：`publish_repository_index` 继续接收 `actionToken`、`remoteUrl`、`commitHash`、`indexVersion`、`modules/pages/apis/tables/permissions/documents`。
- 响应字段：`get_harness_template.repositoryHarnessInstructions[].files` 必须包含一个非模板模块文档，内容强调前端页面功能索引；`tools/list` 中 `modules` 描述应说明业务功能粒度。
- 数据粒度：`modules` 一行代表一个前端菜单、页面、隐藏页签或其对应业务功能；`pages/apis/tables/permissions/documents` 通过 `moduleCode` 归属到对应业务功能。

## 验收标准

- AC-BE-001：`get_harness_template` 返回的仓库模块文档不是仓库概览，而是前端页面功能索引骨架，并明确要求按菜单、页面、接口和权限沉淀业务知识。
- AC-BE-002：`reqflow-mcp` skill 和 `skill://reqflow/project-init` 资源明确要求从前端路由、菜单、页面组件和 API 封装分析模块，且每个页面业务功能形成 `modules`。
- AC-BE-003：`publish_repository_index` tool schema 中 `modules` 描述明确为前端页面业务功能粒度。
- AC-BE-004：服务端导入页面功能粒度的 `modules/pages/apis/permissions` 时保留模块编码、名称、类型、路径和影响面归属。
- AC-BE-005：接口契约和后端模块 harness 同步记录初始化模块知识库的新口径。

## Companion 关联

- companion spec：无，本次不改 reqflow-ui。
- 关联分支：无。

## 客户与分支

- 目标客户：通用。
- 基线分支：main。
- 任务分支：feature/REQ-20260611-001-page-function-harness。

## 约束与假设

- 需求平台仍保持轻量 MVP 边界：平台不直接运行 LLM、不 clone 仓库、不执行目标仓库文件扫描；目标仓库内的 Codex 根据下发 harness 规则完成本地分析并发布结构化索引。
- 本仓库没有配置 Git remote，本次任务基于本地 `main` 创建任务分支，不执行 `git pull --ff-only`。
