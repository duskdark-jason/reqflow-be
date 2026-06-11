# Codex Skill YAML 修复需求说明

## 背景

安装命令会把 MCP 配置和 `reqflow-mcp/SKILL.md` 写入本机 Codex 目录。实际排查发现生成的 `SKILL.md` 文件存在，但 YAML frontmatter 无法解析，原因是 `description` 未加引号且包含 `mcpServer: reqflow`、`mcpTool: reqflow.publish_repository_index` 等 `: ` 片段。Codex skill loader 因 frontmatter 非法而无法加载该 skill。

## 目标

- 平台生成的 `reqflow-mcp` skill 必须是合法 YAML frontmatter。
- 安装脚本和安装包中的 skill 内容都复用同一份修复后的模板。
- 保留足够触发词，让 Codex 能识别项目接入初始化、`actionToken`、`publish_repository_index`、`get_harness_template` 等场景。

## 不做范围

- 不调整 MCP server 配置写入方式。
- 不调整前端页面展示。
- 不新增数据库表或迁移脚本。
- 不自动调用 MCP tool。

## 验收标准

- AC-BE-001：`ReqflowCodexGlobalSkillTemplate.skillContent()` 生成的 frontmatter 可被 YAML 解析。
- AC-BE-002：生成的 frontmatter 中 `name` 为 `reqflow-mcp`，`description` 包含 `actionToken`、`publish_repository_index` 和 `get_harness_template`。
- AC-BE-003：`ReqflowCodexInstallScriptTemplate.shellScript()` 和 `powerShellScript()` 内嵌的 skill 内容同样包含合法且加引号的 frontmatter。
- AC-BE-004：后端定向测试、文档检查、harness 检查和空白检查通过。

## 影响范围

- 接口/API：否，安装脚本返回内容变更但接口路径和字段不变。
- 数据库/SQL：否。
- 权限：否。
- 页面：否。
- 模块知识库：更新 MCP 管理风险说明。
