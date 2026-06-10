# MCP下发完整Harness模板需求说明

## 背景

当前 workspace 根目录维护了完整 `harness-template/`，但需求平台 MCP 的 `get_harness_template` 仍由后端代码硬编码生成轻量初始化包。调用 MCP 初始化时只能获得基础 `AGENTS.md`、少量流程文档和检查脚本，无法获得最新的完整模板规范。

## 目标

- 将完整 harness 模板纳入后端可发布资源，随需求平台后端构建和部署。
- `get_harness_template` 基于同一份资源模板生成 workspace 和仓库初始化文件清单。
- 返回包必须覆盖完整流程文档、模板文档、检查脚本和自动 Review 闭环规则。
- 初始化下发的非模板文档不得保留模板占位符，`harness-index.json` 必须是目标仓库已初始化状态。

## 范围

- 接口/API：是，MCP tool `get_harness_template` 返回内容增强，工具名和入参不变。
- 数据库/SQL：否。
- 权限/菜单：否，沿用 `req:project:query`。
- 页面/交互：否。
- 导出/异步/任务：否。

## 验收标准

- AC-001：`get_harness_template` 返回的 `repositoryHarnessInstructions[].files` 包含 `docs/process/agent-workflow.md`、`docs/process/new-requirement-flow.md`、`docs/templates/review-report-template.md` 和 `scripts/test-check-harness.sh`。
- AC-002：返回内容包含“自动 Review、返修和复审循环”等最新 harness 规则。
- AC-003：完整模板文件存储在后端 `src/main/resources/harness-template/`，部署后可通过 classpath 读取。
- AC-004：生成的 `docs/ai-harness/harness-index.json` 设置 `template=false`、`initialized=true`，并包含完整入口、命令和仓库信息。
- AC-005：现有 MCP 权限校验保持不变，无权限调用仍返回 `req:project:query` 相关错误。
