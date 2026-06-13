# MCP Key明文持久与交互安装需求说明

## 背景

MCP 管理页已支持 Codex、Claude Code、Trae、Qoder、CodeBuddy、OpenCode 的统一安装指令。用户进一步要求：

- MCP Key 始终保持明文可用于安装指令，不再只在创建时一次性返回。
- 统一执行命令执行后应让用户选择要安装的工具，而不是默认安装全部工具。
- MCP 管理页面不展示明文 Key 和 Key 前缀字段。
- 本地 Harness `--spec` 只能检查 `docs/specs/active/` 中的执行中需求，避免执行过程误写 `docs/specs/done/`。

## 目标

- 后端新增并维护 `req_mcp_user_key.plain_key`，新建 Key 时保存明文，后续列表、详情和使用指令接口均可返回。
- 鉴权继续使用 `key_hash`，不因为保存明文改变认证路径。
- 安装包顶层 `installCommands[]` 只展示一组统一指令；脚本不传 client 时交互选择工具，传 `all` 或单个客户端时保持自动化能力。
- 前端列表和结果弹窗不单独展示明文 Key、Key 前缀或哈希，只用 `plainKey` 渲染可复制命令。
- 同步 API 契约、数据库字典、模块知识库和 companion 前端文档。
- 收紧 `check-harness.sh --spec` 目标路径，拒绝 `docs/specs/done/` 并补充自测。

## 非目标

- 不回填升级前只保存哈希的历史 Key 明文，历史记录无法从哈希反推明文，需要重新生成 Key。
- 不改变 MCP actionToken 的一次性和阶段性规则。
- 不自动调用任何 reqflow MCP tool。

## 验收标准

- AC-001：创建 MCP Key 时，保存 `plain_key`、`key_prefix` 和 `key_hash`，返回 `plainKey`。
- AC-002：打开历史 Key 使用指令时，返回已保存的 `plainKey` 并渲染统一安装命令。
- AC-003：顶层统一命令不再带 `--client all` 或 `-Client "all"`，脚本执行后交互选择工具。
- AC-004：脚本仍支持 `--client all|codex|claude-code|trae|qoder|codebuddy|opencode` 和 PowerShell `-Client` 自动化安装。
- AC-005：MCP 管理页不展示明文 Key 或 Key 前缀字段。
- AC-006：数据库和 harness 文档同步新字段、API 语义和页面展示约束。
- AC-007：`check-harness.sh complete --spec docs/specs/done/...` 必须失败，并提示使用 `docs/specs/active/`。

## 影响范围

- 接口：是，`/requirement/mcp/key/**` 返回 `plainKey` 的长期语义变化。
- 数据库：是，新增 `req_mcp_user_key.plain_key`。
- 权限：否，不改变 `req:mcp:key:*` 权限。
- 页面展示：是，MCP 管理页隐藏明文 Key 和 Key 前缀字段，统一命令执行后选择工具。
- 流程门禁：是，`check-harness.sh --spec` 只允许指向 `docs/specs/active/`。
