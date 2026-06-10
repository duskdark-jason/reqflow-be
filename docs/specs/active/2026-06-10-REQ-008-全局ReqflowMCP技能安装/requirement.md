# 全局 Reqflow MCP 技能安装需求说明

## 背景

REQ-007 已让 reqflow MCP 暴露 `skill://reqflow/project-init` 和 `get_harness_template` 中的 `reqflowMcpSkill`。用户进一步明确：希望通过 MCP 管理复制 Codex 配置指令时，不只是配置 MCP server，而是把调用 reqflow MCP 的 skill 安装为全局 Codex skill，效果接近 Context7 的全局触发体验。

当前 `docs/specs/active` 下还残留一个旧的空目录 `2026-06-10-REQ-005-MCP协议握手与工具暴露修复`，会造成“active 有脏目录”的观感；该目录不被 Git 跟踪，本需求执行时清理本地空目录。

## 目标

- MCP 管理配置接口返回全局 Codex skill 包，内容符合 Codex skill 目录规范，由 Codex 按当前运行环境写入全局 skills 目录。
- 人员 MCP Key 创建/重置结果同步返回同一份全局 skill 安装信息，避免复制 Key 配置时漏装 skill。
- 全局 skill 的触发条件覆盖 `actionToken`、`mcpServer: reqflow`、`mcpTool: reqflow.publish_repository_index`、`publish_repository_index`、`get_harness_template`、项目接入初始化和 harness 初始化。
- 全局 skill 的执行规则要求先确认 reqflow MCP server 已加载，再调用 `get_harness_template`，写入本地 harness，运行 init 校验，最后调用 `publish_repository_index` 和 `register_harness_init_result`。
- 全局 skill 安装必须跨平台可用，不能只依赖 macOS/Linux shell；返回数据应让 Codex 能按自身 skill 规范生成目录和文件。
- 清理本地 `docs/specs/active` 下残留的旧空目录。

## 范围

本次包含：

- 后端新增全局 skill 包模板、文件清单、跨平台安装说明和内容字段。
- `/requirement/mcp/key/config` 响应增加全局 skill 安装字段。
- 创建/重置 MCP Key 响应增加全局 skill 安装字段。
- 单元测试覆盖配置接口和创建 Key 返回。
- 同步接口契约与模块 harness 文档。

本次不包含：

- 在调用方机器上远程执行安装命令。
- 修改 Codex 本身的 skill 发现机制。
- 强制覆盖用户已有同名全局 skill。
- 自动 push 或修改生产环境配置。

## 影响范围

- 接口/API：是，MCP 管理配置和 Key 创建/重置响应新增字段。
- 数据库/SQL：否。
- 权限/菜单：否。
- 页面/交互：可能需要前端展示新增字段，先以后端返回为准。
- 本地文件：清理一个未跟踪的 active 空目录，不影响 Git 提交。

## 验收标准

- AC-BE-001：`/requirement/mcp/key/config` 返回 `codexGlobalSkillPackage`，包含 `skillName=reqflow-mcp`、`installScope=global`、`files` 和跨平台安装说明。
- AC-BE-002：创建或重置人员 MCP Key 返回同样的全局 skill 包字段，且 skill 包内容和安装说明不写入人员明文 Key。
- AC-BE-003：全局 skill 包包含符合 Codex skill 规范的 `SKILL.md`，frontmatter 含 `name` 和 `description`，触发条件覆盖 reqflow MCP 项目接入初始化关键字段。
- AC-BE-004：全局 skill 明确要求调用 `mcp__reqflow.get_harness_template`、`mcp__reqflow.publish_repository_index` 和 `mcp__reqflow.register_harness_init_result`。
- AC-BE-005：旧 active 空目录已清理，`docs/specs/active` 只保留 `.gitkeep` 和真实进行中需求目录。
- AC-BE-006：相关单元测试、打包和 harness 检查通过。

## 约束与假设

- 全局 skill 安装必须由 Codex 或用户在本机执行；平台服务端只返回符合规范的 skill 包，不远程写本机文件。
- 安装路径由 Codex 按当前环境决定；默认语义是全局 skills 目录，例如 `$CODEX_HOME/skills` 或 Codex 当前实现的等价位置。
- 如果已有同名 skill，Codex 应按自身规范判断是创建、合并还是覆盖；平台返回内容不得强制绑定单一操作系统命令。
