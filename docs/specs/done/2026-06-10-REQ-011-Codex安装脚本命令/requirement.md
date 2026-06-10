# Codex 安装脚本命令后端需求说明

## 背景

现有创建或重置 MCP Key 响应返回 `codexSetupPackage`，前端直接展示为长 JSON。用户希望接近 Context7 一类 MCP 服务的短命令体验，但内部没有 npm 仓库，因此改为平台提供 shell/PowerShell 安装脚本，并在响应中返回各平台可复制命令。

## 目标

- 后端提供可匿名下载的 Codex 安装脚本：macOS/Linux 使用 shell，Windows 使用 PowerShell。
- 创建和重置 Key 响应中的 `codexSetupPackage` 增加 `installCommands`，描述不同平台的 markdown 代码块展示信息。
- `installCommands` 使用 Key 占位符，后端安装包本身不包含人员明文 `plainKey`；前端只在当前结果弹窗渲染时把一次性明文 Key 填入命令。
- 安装脚本负责写入 reqflow MCP 配置和全局 `reqflow-mcp` skill，并提示用户重启或刷新 Codex；安装后不自动调用 reqflow MCP tool。

## 范围

本次包含：

- 新增安装脚本 HTTP 端点。
- 更新 `codexSetupPackage` 结构，返回脚本 URL 与平台命令模板。
- 更新单元测试、接口契约和模块 harness。

本次不包含：

- npm 包、plugin marketplace 或公开上架。
- 持久化保存人员明文 Key。
- 自动执行 Codex 或自动调用 `publish_repository_index`。
- 改造 MCP 鉴权协议为 Bearer/OAuth。

## 影响范围

- 接口/API：是，新增 `GET /requirement/codex/install.sh`、`GET /requirement/codex/install.ps1`，创建/重置响应的 `codexSetupPackage` 增加字段。
- 数据库/SQL：否。
- 权限/菜单：是，安装脚本端点匿名可读，不新增菜单权限。
- 页面/交互：是，前端 companion 展示多平台命令。
- 导出/异步/任务：否。

## 契约与数据口径

- 安装脚本端点：返回 `text/plain` 脚本内容，不包含任何人员 Key。
- `codexSetupPackage.installCommands[]`：一行代表一种平台安装命令，包含 `platform`、`label`、`language`、`command`。
- `command` 内使用 `${REQFLOW_MCP_KEY}` 占位符；前端在一次性结果弹窗中替换为 `plainKey` 后展示和复制。

## 验收标准

- AC-BE-001：创建或重置 Key 返回的 `codexSetupPackage.installCommands` 至少包含 `macos-linux` 和 `windows-powershell` 两项，且包含 markdown 语言标识和可复制命令模板。
- AC-BE-002：`installCommands` 和安装脚本内容不包含人员明文 `plainKey` 或一次性 `actionToken`。
- AC-BE-003：`GET /requirement/codex/install.sh` 返回 shell 脚本，脚本可写入 Codex MCP 配置和 `reqflow-mcp/SKILL.md`。
- AC-BE-004：`GET /requirement/codex/install.ps1` 返回 PowerShell 脚本，脚本可写入 Codex MCP 配置和 `reqflow-mcp/SKILL.md`。
- AC-BE-005：文档、harness 和空白检查通过。

## Companion 关联

- companion spec：`reqflow-ui docs/specs/active/2026-06-10-REQ-011-Codex安装脚本命令`
- 关联分支：`feature/REQ-20260610-011-codex-install-scripts`

## 客户与分支

- 目标客户：通用
- 基线分支：main
- 任务分支：feature/REQ-20260610-011-codex-install-scripts

## 约束与假设

- 脚本安装会把 MCP Key 写入本机 Codex 配置文件；但不会写入 skill 文件。
- 页面可在当前会话内反复打开最近一次创建/重置结果；刷新后不再提供明文 Key。
