# MCP多客户端安装支持需求说明

## 背景

当前人员 MCP Key 创建响应主要围绕 Codex 输出安装脚本和全局 skill 包。用户需要安装指令能够覆盖 Codex、Claude Code、Trae、Qoder、CodeBuddy 和 OpenCode，并且全局 `reqflow-mcp` skill 也要用更通用的 `npx skills add` 方式安装。用户确认客户端名称以 CodeBuddy 为准，不增加其他客户端别名。

## 目标

- 扩展 `codexSetupPackage` 为多客户端安装包，支持 Codex、Claude Code、Trae、Qoder、CodeBuddy、OpenCode。
- `installCommands[]` 提供统一安装脚本命令，通过 `--client all` 或 `-Client "all"` 一次覆盖全部目标客户端。
- 每个客户端的独立 MCP 配置片段和单客户端脚本命令只保留在高级 JSON 的 `clientInstructions[]` 中。
- 通用安装脚本内置 `npx skills add` 全局 skill 安装；同时保留单独的 `npx skills add` skill 安装命令作为备用入口。
- 保持明文人员 Key 只在创建结果中出现，安装包模板不写入真实 Key。
- 前端结果弹窗只展示统一安装指令，不再按客户端分组展示。
- 同步后端与前端 harness 文档，记录多客户端安装包结构和维护规则。

## 可行性评估

- 评估结论：可继续设计
- 主要风险：不同客户端 MCP 配置格式存在差异，必须以官方资料为准；历史字段名 `codexSetupPackage` 继续保留兼容前端和接口。
- 需需求人补充或调整：无，用户已确认 CodeBuddy 和 OpenCode 支持范围。
- 是否允许继续生成需求设计：是，需求边界明确。

## 范围

本次包含：

- 后端安装包模板新增 `supportedClients` 和 `clientInstructions`，顶层 `installCommands` 使用统一 `all` 入口。
- 新增 `/requirement/codex/skill/SKILL.md` 匿名只读端点，供 `npx skills add` 下载 skill 内容。
- `/requirement/codex/install.sh` 和 `/requirement/codex/install.ps1` 旧路径继续保留，并升级为支持六类客户端的通用安装脚本。
- 前端 MCP Key 结果弹窗改为统一安装指令展示。
- 后端与前端 harness 文档同步。
- 后端模板、服务和控制器单测覆盖多客户端结构。

本次不包含：

- 不新增数据库字段或 SQL。
- 不新增菜单权限。
- 不改变人员 MCP Key 鉴权逻辑。
- 不执行真实客户端安装或联网登录验证。

## 影响范围

- 接口/API：是，`codexSetupPackage` 响应结构新增字段；新增匿名只读 `GET /requirement/codex/skill/SKILL.md`。
- 数据库/SQL：否。
- 权限/菜单：否。
- 页面/交互：是，MCP Key 结果弹窗展示多客户端安装指令。
- 导出/异步/任务：否。
- Harness 文档：是，更新模块文档、接口契约和前端 UI 契约。

## 契约与数据口径

- `codexSetupPackage.packageName=reqflow-mcp-multi-client-setup`。
- `codexSetupPackage.supportedClients=[codex, claude-code, trae, qoder, codebuddy, opencode]`。
- `installCommands[]` 使用 `install.sh --client all` 或 `install.ps1 -Client "all"` 安装全部目标客户端 MCP 配置和全局 skill。
- `clientInstructions[]` 一行代表一个客户端安装方案，包含单客户端脚本、MCP 配置、全局 skill 单独安装和说明，仅作为高级配置/调试信息。
- `clientInstructions[].commands[]` 使用 `install.sh --client <client>` 或 `install.ps1 -Client <client>` 安装单个目标客户端 MCP 配置和全局 skill。
- `clientInstructions[].skillInstall.commands[]` 使用 `npx skills add` 单独安装到目标 agent。
- `CodeBuddy` 作为唯一对应客户端输出，不增加其他客户端别名。
- 所有 MCP 配置和命令使用 `${REQFLOW_MCP_KEY}` 占位，不包含真实明文 Key。

## 验收标准

- AC-001：后端安装包支持 Codex、Claude Code、Trae、Qoder、CodeBuddy、OpenCode 六个目标客户端。
- AC-002：后端安装包顶层只提供统一安装脚本命令，并包含 OpenCode `opencode.json` 的 `mcp.reqflow.type=remote` 高级配置片段。
- AC-003：通用安装脚本支持 `all`、`codex`、`claude-code`、`trae`、`qoder`、`codebuddy`、`opencode`，并通过 `npx skills add -g -a <client> --copy -y` 安装全局 `reqflow-mcp` skill；单独 skill 安装命令也覆盖六类客户端。
- AC-004：安装包、脚本和 skill 端点不包含人员明文 Key 或一次性 actionToken。
- AC-005：前端结果弹窗只展示统一安装指令，不按客户端分组展示；复制统一安装指令时要求明文 Key。
- AC-006：后端和前端 harness 文档记录多客户端安装包结构、CodeBuddy 和 OpenCode 配置要求。

## Companion 关联

- companion spec：`../reqflow-ui/docs/specs/done/REQ-020-MCP多客户端安装支持`
- 关联分支：`../reqflow-ui` 使用 `feature/req-020-mcp-multi-client-setup`

## 客户与分支

- 目标客户：通用
- 基线分支：main
- 任务分支：feature/req-020-mcp-multi-client-setup

## 约束与假设

- 所有落地文档使用中文说明。
- 继续保留字段名 `codexSetupPackage`，避免破坏既有前端接口绑定。
- 真实客户端安装依赖用户本机工具、登录态和网络，本次验证到模板结构、前端构建和后端单测。
