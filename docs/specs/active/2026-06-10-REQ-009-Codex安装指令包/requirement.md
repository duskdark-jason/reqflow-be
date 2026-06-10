# Codex 安装指令包需求说明

## 背景

REQ-008 已返回 `codexGlobalSkillPackage`，可以让 Codex 按全局 skill 规范安装 `reqflow-mcp`。调研 Context7、GitHub MCP Server 和 Playwright MCP 后，本需求改为更轻量的安装指令包：平台一次性返回 MCP 配置、全局 skill 包、短提示词和 MCP server metadata，而不是强制做 Codex plugin。

## 目标

- MCP 管理配置接口返回 `codexSetupPackage`，用于复制给 Codex 后完成 reqflow MCP 配置和全局 skill 安装。
- 人员 MCP Key 创建/重置结果同步返回同一份 `codexSetupPackage`。
- `codexSetupPackage` 包含 `mcpServer`、`codexConfigTemplate`、`skillPackage`、`installPrompt` 和 `serverMetadata`。
- `serverMetadata` 参考 MCP registry/server.json 风格，描述 reqflow 远程 MCP server、工具组、鉴权 header 和安全提示，为未来接 registry 留出口。
- `codexSetupPackage` 不写入人员明文 Key、`plainKey` 或一次性 `actionToken`；真实 Key 仍只在 `codexConfigTemplate` 和明文展示区一次性出现。
- 保留 `codexGlobalSkillPackage` 兼容字段，避免已有复制流程断裂。

## 范围

本次包含：

- 后端新增 `codexSetupPackage` 结构化返回字段。
- 后端新增 MCP server metadata 模板。
- 单元测试覆盖配置接口和创建 Key 返回。
- 同步接口契约与模块 harness 文档。

本次不包含：

- Codex plugin 包、公共 marketplace 上架或本地 plugin 文件生成。
- 在服务端远程写用户本机 Codex 配置或 skill 目录。
- 移除已有 `codexGlobalSkillPackage`。
- 自动调用 `publish_repository_index` 或其他 reqflow MCP tool。
- 自动 push 或修改生产环境配置。

## 影响范围

- 接口/API：是，MCP 管理配置和 Key 创建/重置响应新增字段。
- 数据库/SQL：否。
- 权限/菜单：否。
- 页面/交互：是，前端 companion 仓库展示并复制安装指令包。

## 验收标准

- AC-BE-001：`/requirement/mcp/key/config` 返回 `codexSetupPackage`，包含 `packageName=reqflow-codex-setup`、`installScope=global`、`mcpServer`、`codexConfigTemplate`、`skillPackage`、`installPrompt` 和 `serverMetadata`。
- AC-BE-002：`mcpServer` 包含 `name=reqflow`、`url`、`transport=streamable-http`、`headerName=X-MCP-Key`，且 `url` 与配置接口展示的 `mcpAddress` 一致。
- AC-BE-003：`installPrompt` 是可复制给 Codex 的短提示词，要求安装 MCP 配置和全局 skill，并明确配置后不要自动调用 `publish_repository_index`。
- AC-BE-004：`serverMetadata` 包含 `name`、`title`、`description`、`remotes[]`、`toolsets[]` 和鉴权 header 说明，至少声明 `project-init`、`index-publish` 和 `package-handoff` 工具组。
- AC-BE-005：创建或重置人员 MCP Key 返回同样的安装指令包字段，且安装指令包内容不包含 `plainKey`。
- AC-BE-006：相关单元测试、打包和 harness 检查通过。

## 约束与假设

- `codexSetupPackage` 是给 Codex/用户复制执行的材料，不代表服务端直接安装。
- 明文 MCP Key 仍只在创建/重置响应中一次性返回；安装指令包使用占位描述，不复制明文 Key。
- 如果后续确认 Codex plugin 是更合适的分发形态，可在本安装包基础上再派生 plugin 包。
