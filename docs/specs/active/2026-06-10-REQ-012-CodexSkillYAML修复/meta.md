# Codex Skill YAML 修复元信息

- 状态：executed
- 当前角色：Execution Agent
- 流程模式：平台自身建设模式
- 需求 Key：无，本地平台建设
- 平台关联远端：未配置
- 平台目标分支：fix/REQ-20260610-012-skill-yaml-frontmatter
- 执行模式：任务分支模式
- 当前分支：fix/REQ-20260610-012-skill-yaml-frontmatter
- 执行授权：已授权
- Review 授权：未授权
- 目标客户：通用
- 基线分支：main
- companion 仓库：无
- 关联 spec：无
- 影响模块：需求管理、MCP 管理、Codex 安装脚本
- 模块知识库动作：更新
- 模块知识库文档：docs/ai-harness/modules/requirement-platform.md
- 无需更新原因：不适用
- 最后更新：2026-06-10 22:38

## 状态说明

本需求修复平台生成的 `reqflow-mcp` 全局 skill 模板。当前安装脚本能写入 `SKILL.md`，但 frontmatter 的 `description` 含 `mcpServer: reqflow` 等未加引号内容，会导致 YAML 解析失败，Codex 重启后仍无法识别 skill。

## 授权说明

- 用户明确要求“优先修平台模板”。
- 本次属于平台自身建设模式，不通过 MCP 伪造需求回写。
- 当前仓库未配置远端，不执行 pull 或 push。
